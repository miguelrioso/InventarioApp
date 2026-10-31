package com.datacore.inventarioapp

import com.google.gson.annotations.SerializedName

data class RespuestaProductos(
    @SerializedName("products") val productos: List<ProductoRemoto> = emptyList(),
    @SerializedName("total") val total: Int = 0
)

data class ProductoRemoto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("title") val titulo: String? = null,
    @SerializedName("description") val descripcion: String? = null,
    @SerializedName("price") val precio: Double? = null,
    @SerializedName("stock") val stock: Int? = null,
    @SerializedName("brand") val marca: String? = null
)