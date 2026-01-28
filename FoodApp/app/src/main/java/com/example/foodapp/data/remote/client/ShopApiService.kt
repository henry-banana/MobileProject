package com.example.foodapp.data.remote.client


import com.example.foodapp.data.remote.client.response.shop.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ShopApiService {

    @GET("shops")
    suspend fun getAllShops(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("status") status: String? = null,
        @Query("search") search: String? = null
    ): Response<GetAllShopsResponse>


    @GET("shops/{id}")
    suspend fun getShopDetail(
        @Path("id") id: String
    ): Response<GetShopDetailResponse>
}

