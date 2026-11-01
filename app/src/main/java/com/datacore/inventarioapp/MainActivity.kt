package com.datacore.inventarioapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.activity.viewModels
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: ProductoViewModel by viewModels()
    private lateinit var adapter: ProductoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val rv = findViewById<RecyclerView>(R.id.rvProductos)
        val tvVacio = findViewById<TextView>(R.id.tvVacio)
        val etBuscar = findViewById<EditText>(R.id.etBuscar)
        val fab = findViewById<FloatingActionButton>(R.id.fabAgregar)
        val btnImportar = findViewById<Button>(R.id.btnImportar)
        btnImportar.setOnClickListener { viewModel.importarCatalogo() }

        adapter = ProductoAdapter(
            alEditar = { mostrarDialogo(it) },
            alEliminar = { confirmarEliminar(it) }
        )
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        etBuscar.doOnTextChanged { texto, _, _, _ ->
            viewModel.buscar(texto?.toString() ?: "")
        }
        fab.setOnClickListener { mostrarDialogo(null) }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.productos.collect { lista ->
                        adapter.submitList(lista)
                        tvVacio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.mensajes.collect { mensaje ->
                        Toast.makeText(this@MainActivity, mensaje, Toast.LENGTH_LONG).show()
                    }
                }
                launch {
                    viewModel.importando.collect { enCurso ->
                        btnImportar.isEnabled = !enCurso
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.iniciarSincronizacion()
    }

    override fun onStop() {
        super.onStop()
        viewModel.detenerSincronizacion()
    }

    // CREAR y ACTUALIZAR (mismo formulario)
    private fun mostrarDialogo(producto: Producto?) {
        val vista = layoutInflater.inflate(R.layout.dialog_producto, null)
        val etNombre = vista.findViewById<EditText>(R.id.etNombre)
        val etDescripcion = vista.findViewById<EditText>(R.id.etDescripcion)
        val etCantidad = vista.findViewById<EditText>(R.id.etCantidad)
        val etPrecio = vista.findViewById<EditText>(R.id.etPrecio)
        val etProveedor = vista.findViewById<EditText>(R.id.etProveedor)

        producto?.let {
            etNombre.setText(it.nombre)
            etDescripcion.setText(it.descripcion)
            etCantidad.setText(it.cantidad.toString())
            etPrecio.setText(it.precio.toString())
            etProveedor.setText(it.proveedor)
        }

        val dialogo = MaterialAlertDialogBuilder(this)
            .setTitle(if (producto == null) "Nuevo producto" else "Editar producto")
            .setView(vista)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar", null)
            .create()

        dialogo.setOnShowListener {
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val nombre = etNombre.text.toString().trim()
                val cantidad = etCantidad.text.toString().toIntOrNull()
                val precio = etPrecio.text.toString().toDoubleOrNull()

                if (nombre.isEmpty()) {
                    etNombre.error = "Ingresa un nombre"
                    return@setOnClickListener
                }
                if (cantidad == null || cantidad < 0) {
                    etCantidad.error = "Cantidad inválida"
                    return@setOnClickListener
                }
                if (precio == null || precio < 0) {
                    etPrecio.error = "Precio inválido"
                    return@setOnClickListener
                }

                val guardado = (producto ?: Producto()).copy(
                    nombre = nombre,
                    descripcion = etDescripcion.text.toString().trim(),
                    cantidad = cantidad,
                    precio = precio,
                    proveedor = etProveedor.text.toString().trim()
                )

                if (producto == null) viewModel.crear(guardado)
                else viewModel.actualizar(guardado)
                dialogo.dismiss()
            }
        }
        dialogo.show()
    }

    // ELIMINAR
    private fun confirmarEliminar(producto: Producto) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar producto")
            .setMessage("¿Eliminar \"${producto.nombre}\" del inventario?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ -> viewModel.eliminar(producto) }
            .show()
    }
}