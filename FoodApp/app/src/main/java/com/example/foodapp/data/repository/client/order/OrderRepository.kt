package com.example.foodapp.data.repository.client.order

import com.example.foodapp.data.remote.api.ApiClient
import com.example.foodapp.data.remote.client.OrderApiService
import com.example.foodapp.data.remote.client.response.order.*

class OrderRepository() {

    private val orderApiService: OrderApiService = ApiClient.orderApiService

    /**
     * Tạo đơn hàng mới
     */
    suspend fun createOrder(request: CreateOrderRequest): ApiResult<OrderApiModel> {
        return try {
            val response = orderApiService.createOrder(request)
            if (response.success && response.data != null) {
                ApiResult.Success(response.data)
            } else {
                ApiResult.Failure(Exception("Create order failed"))
            }
        } catch (e: Exception) {
            ApiResult.Failure(e)
        }
    }

    /**
     * Lấy danh sách đơn hàng
     * @param status: Lọc theo trạng thái (PENDING, DELIVERED,...)
     * @param page: Số trang hiện tại
     * @param limit: Số lượng đơn hàng mỗi trang
     */
    suspend fun getOrders(
        status: String? = null,
        page: Int = 1,
        limit: Int = 10
    ): ApiResult<GetOrdersDataResponse> {
        return try {
            // Sử dụng Named Arguments để khớp chính xác với OrderApiService
            val response = orderApiService.getOrders(
                status = status,
                page = page,
                limit = limit
            )

            if (response.success && response.data != null) {
                ApiResult.Success(response.data)
            } else {
                ApiResult.Failure(Exception("Không thể lấy danh sách đơn hàng"))
            }
        } catch (e: Exception) {
            ApiResult.Failure(e)
        }
    }


    /**
     * Xóa một đơn hàng theo ID
     * @param orderId: ID của đơn hàng cần xóa
     */
    suspend fun deleteOrder(orderId: String): ApiResult<Boolean> {
        return try {
            val response = orderApiService.deleteOrder(orderId)
            if (response.success) {
                ApiResult.Success(true)
            } else {
                ApiResult.Failure(Exception("Xóa đơn hàng thất bại"))
            }
        } catch (e: Exception) {
            ApiResult.Failure(e)
        }
    }


    suspend fun getOrderById(orderId: String): ApiResult<OrderApiModel> {
        return try {
            val response = orderApiService.getOrderById(orderId)
            if (response.success && response.data != null) {
                ApiResult.Success(response.data)
            } else {
                ApiResult.Failure(Exception("Không thể lấy chi tiết đơn hàng"))
            }
        } catch (e: Exception) {
            ApiResult.Failure(e)
        }
    }




} // Chỉ để lại 1 dấu đóng class duy nhất ở đây