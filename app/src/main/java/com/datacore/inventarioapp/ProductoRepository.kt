package com.datacore.inventarioapp

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ProductoRepository(
    private val dao: ProductoDao,
    private val alError: (String) -> Unit = {}
) {
    private val coleccion = FirebaseFirestore.getInstance().collection("productos")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var listener: ListenerRegistration? = null

    // ---------- LEER (siempre desde Room) ----------
    val productos: Flow<List<Producto>> = dao.obtenerTodosLosProductos()

    fun buscar(texto: String): Flow<List<Producto>> = dao.buscarProductos(texto)

    suspend fun obtenerPorId(id: String): Producto? = dao.obtenerProductoPorId(id)

    // ---------- CREAR ----------
    suspend fun crear(producto: Producto) {
        val nuevo = producto.copy(actualizadoEn = System.currentTimeMillis())
        dao.insertarProducto(nuevo)
        subirANube(nuevo)
    }

    // ---------- ACTUALIZAR ----------
    suspend fun actualizar(producto: Producto) {
        val modificado = producto.copy(actualizadoEn = System.currentTimeMillis())
        dao.actualizarProducto(modificado)
        subirANube(modificado)
    }


    suspend fun eliminar(producto: Producto) {
        dao.eliminarProducto(producto)
        coleccion.document(producto.id).delete()
            .addOnFailureListener { alError("No se pudo eliminar en la nube: ${it.message}") }
    }


    suspend fun importarCatalogo(texto: String): Result<Int> {
        return try {
            val respuesta = ClienteRest.api.buscarProductos(texto)
            if (!respuesta.isSuccessful) {
                return Result.failure(Exception("El servicio respondió con error ${respuesta.code()}"))
            }
            val remotos = respuesta.body()?.productos ?: emptyList()
            var importados = 0
            for (remoto in remotos) {
                val producto = remoto.aProducto() ?: continue
                crear(producto)
                importados++
            }
            Result.success(importados)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    fun iniciarSincronizacion() {
        if (listener != null) return
        listener = coleccion.addSnapshotListener { snapshot, error ->
            if (error != null) {
                alError("Error de sincronización: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener

            scope.launch {
                for (cambio in snapshot.documentChanges) {
                    // Ignora los cambios que hizo este mismo dispositivo
                    if (cambio.document.metadata.hasPendingWrites()) continue

                    when (cambio.type) {
                        DocumentChange.Type.ADDED,
                        DocumentChange.Type.MODIFIED -> {
                            val remoto = cambio.document.aProducto()
                            val local = dao.obtenerProductoPorId(remoto.id)
                            if (local == null || remoto.actualizadoEn >= local.actualizadoEn) {
                                dao.insertarProducto(remoto)
                            }
                        }
                        DocumentChange.Type.REMOVED -> {
                            dao.obtenerProductoPorId(cambio.document.id)
                                ?.let { dao.eliminarProducto(it) }
                        }
                    }
                }
            }
        }
    }

    fun detenerSincronizacion() {
        listener?.remove()
        listener = null
    }

    // ---------- AUXILIARES ----------
    private fun subirANube(producto: Producto) {
        coleccion.document(producto.id).set(producto.aMapa())
            .addOnFailureListener { alError("No se pudo sincronizar con la nube: ${it.message}") }
    }

    private fun Producto.aMapa(): HashMap<String, Any> = hashMapOf(
        "id" to id,
        "nombre" to nombre,
        "descripcion" to descripcion,
        "cantidad" to cantidad,
        "precio" to precio,
        "proveedor" to proveedor,
        "actualizadoEn" to actualizadoEn
    )

    private fun DocumentSnapshot.aProducto(): Producto = Producto(
        id = getString("id") ?: this.id,
        nombre = getString("nombre") ?: "",
        descripcion = getString("descripcion") ?: "",
        cantidad = (getLong("cantidad") ?: 0L).toInt(),
        precio = getDouble("precio") ?: 0.0,
        proveedor = getString("proveedor") ?: "",
        actualizadoEn = getLong("actualizadoEn") ?: 0L
    )

    private fun ProductoRemoto.aProducto(): Producto? {
        val nombre = titulo?.trim()
        if (nombre.isNullOrEmpty()) return null
        return Producto(
            id = "dummy-$id",
            nombre = nombre,
            descripcion = descripcion ?: "",
            cantidad = stock ?: 0,
            precio = precio ?: 0.0,
            proveedor = marca ?: "Sin proveedor"
        )
    }
}