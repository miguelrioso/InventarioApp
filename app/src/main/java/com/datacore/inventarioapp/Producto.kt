package com.datacore.inventarioapp

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "nombre") val nombre: String = "",
    @ColumnInfo(name = "descripcion") val descripcion: String = "",
    @ColumnInfo(name = "cantidad") val cantidad: Int = 0,
    @ColumnInfo(name = "precio") val precio: Double = 0.0,
    @ColumnInfo(name = "proveedor") val proveedor: String = "",
    @ColumnInfo(name = "actualizado_en") val actualizadoEn: Long = System.currentTimeMillis()
)