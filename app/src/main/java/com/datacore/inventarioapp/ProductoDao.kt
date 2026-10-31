package com.datacore.inventarioapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProducto(producto: Producto)

    @Update
    suspend fun actualizarProducto(producto: Producto)

    @Delete
    suspend fun eliminarProducto(producto: Producto)

    @Query("SELECT * FROM productos ORDER BY nombre ASC")
    fun obtenerTodosLosProductos(): Flow<List<Producto>>

    @Query("SELECT * FROM productos WHERE id = :id")
    suspend fun obtenerProductoPorId(id: String): Producto?

    @Query(
        "SELECT * FROM productos " +
                "WHERE nombre LIKE '%' || :texto || '%' " +
                "OR proveedor LIKE '%' || :texto || '%' " +
                "ORDER BY nombre ASC"
    )
    fun buscarProductos(texto: String): Flow<List<Producto>>
}