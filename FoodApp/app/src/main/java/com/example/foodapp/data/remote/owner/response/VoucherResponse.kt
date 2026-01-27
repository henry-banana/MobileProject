package com.example.foodapp.data.remote.owner.response

import com.example.foodapp.data.model.owner.voucher.*
import com.google.gson.annotations.SerializedName

/**
 * Response wrapper for voucher list
 */
data class WrappedVouchersResponse(
    val success: Boolean,
    val data: List<VoucherDto>?,
    val message: String? = null,
    val timestamp: String? = null
)

/**
 * Response wrapper for single voucher
 */
data class WrappedVoucherResponse(
    val success: Boolean,
    val data: VoucherDto?,
    val message: String? = null,
    val timestamp: String? = null
)

/**
 * Response wrapper for voucher action (update/delete)
 */
data class WrappedVoucherActionResponse(
    val success: Boolean,
    val message: String? = null,
    val timestamp: String? = null
)

/**
 * DTO for Voucher from API
 */
data class VoucherDto(
    val id: String,
    val code: String,
    val shopId: String?,
    val type: String,
    val value: Double,
    val maxDiscount: Double?,
    val minOrderAmount: Double?,
    val usageLimit: Int,
    val usageLimitPerUser: Int,
    val currentUsage: Int,
    val validFrom: String,
    val validTo: String,
    val isActive: Boolean,
    val isDeleted: Boolean,
    val ownerType: String,
    val name: String?,
    val description: String?,
    val createdAt: String,
    val updatedAt: String
) {
    fun toVoucher(): Voucher = Voucher(
        id = id,
        code = code,
        shopId = shopId,
        type = when (type) {
            "PERCENTAGE" -> VoucherType.PERCENTAGE
            "FIXED_AMOUNT" -> VoucherType.FIXED_AMOUNT
            "FREE_SHIP" -> VoucherType.FREE_SHIP
            else -> VoucherType.PERCENTAGE
        },
        value = value,
        maxDiscount = maxDiscount,
        minOrderAmount = minOrderAmount,
        usageLimit = usageLimit,
        usageLimitPerUser = usageLimitPerUser,
        currentUsage = currentUsage,
        validFrom = validFrom,
        validTo = validTo,
        isActive = isActive,
        isDeleted = isDeleted,
        ownerType = when (ownerType) {
            "SHOP" -> OwnerType.SHOP
            "ADMIN" -> OwnerType.ADMIN
            else -> OwnerType.SHOP
        },
        name = name,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
