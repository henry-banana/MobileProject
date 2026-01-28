package com.example.foodapp.data.remote.client

import com.example.foodapp.data.remote.client.response.voucher.*
import retrofit2.Response
import retrofit2.http.*

interface VoucherApiService {

    /**
     * Lấy danh sách voucher áp dụng được cho đơn hàng
     * GET /vouchers?shopId=xxx
     */
    @GET("vouchers")
    suspend fun getVouchers(
        @Header("Authorization") token: String,  // Thêm token header
        @Query("shopId") shopId: String,
    ): Response<VoucherListResponse>

    /**
     * Validate voucher
     * POST /vouchers/validate
     */
    @POST("vouchers/validate")
    suspend fun validateVoucher(
        @Header("Authorization") token: String,  // Thêm token header
        @Body request: ValidateVoucherRequest
    ): Response<ValidateVoucherApiResponse>

    @GET("vouchers/me/usage")
    suspend fun getVoucherUsageHistory(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("shopId") shopId: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<VoucherUsageHistoryApiResponse>

}