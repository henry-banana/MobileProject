package com.example.foodapp.data.repository.owner.base

import com.example.foodapp.data.model.owner.voucher.*

/**
 * Interface for Owner Voucher Repository.
 * Defines methods for voucher management through backend API.
 */
interface OwnerVoucherRepository {
    
    /**
     * Get all vouchers for the shop
     * @param isActive Optional filter by active status
     */
    suspend fun getVouchers(isActive: Boolean? = null): Result<List<Voucher>>
    
    /**
     * Create a new voucher
     * @param request Create voucher request
     */
    suspend fun createVoucher(request: CreateVoucherRequest): Result<Voucher>
    
    /**
     * Update voucher details
     * @param voucherId Voucher ID
     * @param request Update voucher request
     */
    suspend fun updateVoucher(voucherId: String, request: UpdateVoucherRequest): Result<Unit>
    
    /**
     * Delete a voucher (soft delete)
     * @param voucherId Voucher ID
     */
    suspend fun deleteVoucher(voucherId: String): Result<Unit>
    
    /**
     * Toggle voucher active status
     * @param voucherId Voucher ID
     * @param isActive New active status
     */
    suspend fun updateVoucherStatus(voucherId: String, isActive: Boolean): Result<Unit>
}
