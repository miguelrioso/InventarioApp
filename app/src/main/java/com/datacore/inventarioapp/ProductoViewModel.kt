package com.datacore.inventarioapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class ProductoViewModel(application: Application) : AndroidViewModel(application) {

    private val _mensajes = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val mensajes: SharedFlow<String> = _mensajes.asSharedFlow()

    private val _importando = MutableStateFlow(false)
    val importando: StateFlow<Boolean> = _importando.asStateFlow()

    private val repositorio = ProductoRepository(
        AppDatabase.getDatabase(application).productoDao()
    ) { mensaje -> _mensajes.tryEmit(mensaje) }

    private val consulta = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val productos: Flow<List<Producto>> = consulta.flatMapLatest { texto ->
        if (texto.isBlank()) repositorio.productos
        else repositorio.buscar(texto.trim())
    }

    fun buscar(texto: String) {
        consulta.value = texto
    }

    fun crear(producto: Producto) {
        viewModelScope.launch {
            try {
                repositorio.crear(producto)
            } catch (e: Exception) {
                _mensajes.emit("Error al guardar en la base local: ${e.message}")
            }
        }
    }

    fun actualizar(producto: Producto) {
        viewModelScope.launch {
            try {
                repositorio.actualizar(producto)
            } catch (e: Exception) {
                _mensajes.emit("Error al guardar en la base local: ${e.message}")
            }
        }
    }

    fun eliminar(producto: Producto) {
        viewModelScope.launch {
            try {
                repositorio.eliminar(producto)
            } catch (e: Exception) {
                _mensajes.emit("Error al eliminar: ${e.message}")
            }
        }
    }

    fun importarCatalogo() {
        viewModelScope.launch {
            _importando.value = true
            _mensajes.emit("Importando catálogo…")
            repositorio.importarCatalogo("phone")
                .onSuccess { cantidad -> _mensajes.emit("Se importaron $cantidad productos") }
                .onFailure { error -> _mensajes.emit("No se pudo importar: ${error.message}") }
            _importando.value = false
        }
    }

    fun iniciarSincronizacion() = repositorio.iniciarSincronizacion()

    fun detenerSincronizacion() = repositorio.detenerSincronizacion()
}