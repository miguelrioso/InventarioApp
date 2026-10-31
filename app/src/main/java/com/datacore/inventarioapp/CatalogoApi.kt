package com.datacore.inventarioapp

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface CatalogoApi {

    @GET("products/search")
    suspend fun buscarProductos(
        @Query("q") texto: String,
        @Query("limit") limite: Int = 10
    ): Response<RespuestaProductos>
}

object ClienteRest {
    private const val URL_BASE = "https://dummyjson.com/"

    val api: CatalogoApi by lazy {
        Retrofit.Builder()
            .baseUrl(URL_BASE)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CatalogoApi::class.java)
    }
}