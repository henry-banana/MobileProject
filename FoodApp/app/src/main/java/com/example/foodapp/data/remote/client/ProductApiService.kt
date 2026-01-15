// data/api/client/ProductApiService.kt
package com.example.foodapp.data.remote.client

import com.example.foodapp.data.model.client.product.*
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface ProductApiService {

    @GET("products")
    suspend fun getProducts(
        @QueryMap filters: Map<String, String>
    ): ProductApiResponse
    @GET("products/{id}")
    suspend fun getProductDetail(
        @Path("id") productId: String
    ): ProductDetailApiResponse


    @POST("me/favorites/products/{productId}")
    suspend fun addToFavorites(
        @Path("productId") productId: String
    ): AddToFavoriteResponse
}