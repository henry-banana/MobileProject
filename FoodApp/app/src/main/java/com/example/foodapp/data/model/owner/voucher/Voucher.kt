package com.example.foodapp.data.model.owner.voucher

import com.google.gson.annotations.SerializedName

/**
 * Voucher Entity - represents a discount voucher
 */
data class Voucher(
    val id: String,
    val code: String,
    val shopId: String?,
    val type: VoucherType,
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
    val ownerType: OwnerType,
    val name: String?,
    val description: String?,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Voucher types
 */
enum class VoucherType {
    @SerializedName("PERCENTAGE")
    PERCENTAGE,
    
    @SerializedName("FIXED_AMOUNT")
    FIXED_AMOUNT,
    
    @SerializedName("FREE_SHIP")
    FREE_SHIP;

    fun displayName(): String = when (this) {
        PERCENTAGE -> "Giảm %"
        FIXED_AMOUNT -> "Giảm tiền"
        FREE_SHIP -> "Miễn phí ship"
    }
}

/**
 * Owner types for vouchers
 */
enum class OwnerType {
    @SerializedName("SHOP")
    SHOP,
    
    @SerializedName("ADMIN")
    ADMIN
}

/**
 * Request to create a new voucher
 */
data class CreateVoucherRequest(
    val code: String,
    val type: String,
    val value: Double,
    val maxDiscount: Double? = null,
    val minOrderAmount: Double? = null,
    val usageLimit: Int,
    val usageLimitPerUser: Int,
    val validFrom: String,
    val validTo: String,
    val name: String? = null,
    val description: String? = null
)

/**
 * Request to update a voucher (limited fields)
 */
data class UpdateVoucherRequest(
    val usageLimit: Int? = null,
    val usageLimitPerUser: Int? = null,
    val validTo: String? = null,
    val name: String? = null,
    val description: String? = null
)

/**
 * Request to update voucher status
 */
data class UpdateVoucherStatusRequest(
    val isActive: Boolean
)
