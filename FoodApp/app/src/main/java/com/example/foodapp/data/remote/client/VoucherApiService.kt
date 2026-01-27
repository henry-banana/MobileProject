package com.example.foodapp.data.remote.client

import com.example.foodapp.data.remote.client.response.voucher.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface VoucherApiService {

    /**
     * Lấy danh sách voucher áp dụng được cho đơn hàng
     * GET /vouchers/apply?shopId=xxx&orderAmount=yyy
     */
    @GET("vouchers")
    suspend fun getVouchers(
        @Query("shopId") shopId: String,
    ): Response<VoucherListResponse>

    @POST("vouchers/validate")
    suspend fun validateVoucher(
        @Body request: ValidateVoucherRequest
    ): Response<ValidateVoucherApiResponse>

}