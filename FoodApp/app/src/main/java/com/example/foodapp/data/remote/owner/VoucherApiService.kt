package com.example.foodapp.data.remote.owner

import com.example.foodapp.data.model.owner.voucher.*
import com.example.foodapp.data.remote.owner.response.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service for Owner Voucher Management
 * 
 * Endpoints:
 * - POST /owner/vouchers - Create voucher
 * - GET /owner/vouchers - Get all vouchers
 * - PUT /owner/vouchers/{id} - Update voucher
 * - DELETE /owner/vouchers/{id} - Delete voucher
 * - PUT /owner/vouchers/{id}/status - Update voucher status
 */
interface VoucherApiService {

    /**
     * POST /owner/vouchers
     * Create a new voucher
     */
    @POST("owner/vouchers")
    suspend fun createVoucher(
        @Body request: CreateVoucherRequest
    ): Response<WrappedVoucherResponse>

    /**
     * GET /owner/vouchers
     * Get all vouchers for the shop
     */
    @GET("owner/vouchers")
    suspend fun getVouchers(
        @Query("isActive") isActive: String? = null
    ): Response<WrappedVouchersResponse>

    /**
     * PUT /owner/vouchers/{id}
     * Update voucher details
     */
    @PUT("owner/vouchers/{id}")
    suspend fun updateVoucher(
        @Path("id") voucherId: String,
        @Body request: UpdateVoucherRequest
    ): Response<WrappedVoucherActionResponse>

    /**
     * DELETE /owner/vouchers/{id}
     * Soft delete a voucher
     */
    @DELETE("owner/vouchers/{id}")
    suspend fun deleteVoucher(
        @Path("id") voucherId: String
    ): Response<WrappedVoucherActionResponse>

    /**
     * PUT /owner/vouchers/{id}/status
     * Toggle voucher active status
     */
    @PUT("owner/vouchers/{id}/status")
    suspend fun updateVoucherStatus(
        @Path("id") voucherId: String,
        @Body request: UpdateVoucherStatusRequest
    ): Response<WrappedVoucherActionResponse>
}
