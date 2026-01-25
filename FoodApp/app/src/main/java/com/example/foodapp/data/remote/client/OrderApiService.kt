package com.example.foodapp.data.remote.client

import com.example.foodapp.data.remote.client.response.order.*
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface OrderApiService {

    /**
     * Tạo đơn hàng mới
     * POST /orders
     */
    @POST("orders")
    suspend fun createOrder(
        @Body request: CreateOrderRequest
    ): CreateOrderResponse

    @GET("orders")
    suspend fun getOrders(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): GetOrdersResponse

    @PUT("orders/{id}/cancel")
    suspend fun deleteOrder(
        @Path("id") orderId: String
    ): DeleteOrdersResponse


    @GET("orders/{id}")
    suspend fun getOrderById(
        @Path("id") orderId: String
    ): GetOrderByIdResponse

}