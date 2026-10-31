package com.datacore.inventarioapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ProductoAdapter(
    private val alEditar: (Producto) -> Unit,
    private val alEliminar: (Producto) -> Unit
) : ListAdapter<Producto, ProductoAdapter.VH>(Diff) {

    class VH(vista: View) : RecyclerView.ViewHolder(vista) {
        val nombre: TextView = vista.findViewById(R.id.tvNombre)
        val descripcion: TextView = vista.findViewById(R.id.tvDescripcion)
        val info: TextView = vista.findViewById(R.id.tvInfo)
        val proveedor: TextView = vista.findViewById(R.id.tvProveedor)
        val btnEditar: Button = vista.findViewById(R.id.btnEditar)
        val btnEliminar: Button = vista.findViewById(R.id.btnEliminar)
    }

    object Diff : DiffUtil.ItemCallback<Producto>() {
        override fun areItemsTheSame(a: Producto, b: Producto) = a.id == b.id
        override fun areContentsTheSame(a: Producto, b: Producto) = a == b
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto, parent, false)
        return VH(vista)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = getItem(position)
        holder.nombre.text = p.nombre
        holder.descripcion.text = p.descripcion
        holder.info.text = "Cantidad: ${p.cantidad}  •  Precio: \$${p.precio}"
        holder.proveedor.text = "Proveedor: ${p.proveedor}"
        holder.btnEditar.setOnClickListener { alEditar(p) }
        holder.btnEliminar.setOnClickListener { alEliminar(p) }
    }
}