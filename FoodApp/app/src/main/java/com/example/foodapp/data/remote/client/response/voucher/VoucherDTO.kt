package com.example.foodapp.data.remote.client.response.voucher

import com.google.gson.annotations.SerializedName

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Failure(val exception: Exception) : ApiResult<Nothing>()
}

/**
 * Model voucher/khuyến mãi
 * Format API response:
 * {
 *   "success": true,
 *   "data": [
 *     {
 *       "id": "3LGvY4T77cUBpjdNu6KK",
 *       "code": "GIAM10K",
 *       "shopId": "nzIfau9GtqIPyWkmLyku",
 *       "type": "FIXED_AMOUNT",
 *       "value": 10000,
 *       "maxDiscount": 20000,
 *       "minOrderAmount": 30000,
 *       "usageLimit": 100,
 *       "usageLimitPerUser": 1,
 *       "validFrom": "2026-01-21T00:00:00Z",
 *       "validTo": "2026-12-31T23:59:59Z",
 *       "isActive": true,
 *       "ownerType": "SHOP",
 *       "name": "Giam 10k",
 *       "description": "Giảm 10k phí vận chuyển cho đơn từ 30k",
 *       "isDeleted": false  // (có thể không có trong response)
 *     }
 *   ]
 * }
 */
data class VoucherApiModel @JvmOverloads constructor(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("code")
    val code: String = "",

    @SerializedName("shopId")
    val shopId: String = "",

    @SerializedName("type")
    val type: String = "",

    @SerializedName("value")
    val value: Double = 0.0,

    @SerializedName("maxDiscount")
    val maxDiscount: Double? = null,

    @SerializedName("minOrderAmount")
    val minOrderAmount: Double = 0.0,

    @SerializedName("usageLimit")
    val usageLimit: Int = 0,

    @SerializedName("usageLimitPerUser")
    val usageLimitPerUser: Int = 0,

    // Trường này có thể không có trong response, để giá trị mặc định
    @SerializedName("currentUsage")
    val currentUsage: Int = 0,

    @SerializedName("validFrom")
    val validFrom: String = "",

    @SerializedName("validTo")
    val validTo: String = "",

    @SerializedName("isActive")
    val isActive: Boolean = false,

    // Trường này có thể không có trong response, để giá trị mặc định
    @SerializedName("isDeleted")
    val isDeleted: Boolean = false,

    @SerializedName("ownerType")
    val ownerType: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("description")
    val description: String = "",

    @SerializedName("createdAt")
    val createdAt: String = "",

    @SerializedName("updatedAt")
    val updatedAt: String = "",

    @SerializedName("myUsageCount")
    val myUsageCount: Int = 0,

    @SerializedName("myRemainingUses")
    val myRemainingUses: Int = 0
)

/**
 * Model wrapper cho response API
 */
data class VoucherListResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: List<VoucherApiModel> = emptyList()
)


data class ValidateVoucherRequest(
    @SerializedName("code")
    val voucherCode: String,

    @SerializedName("shopId")
    val shopId: String,

    @SerializedName("subtotal")
    val subtotal: Double,

    @SerializedName("shipFee")
    val shipFee: Double
)

/**
 * Response model cho API validate voucher
 * Format:
 * {
 *   "success": true,
 *   "data": {
 *     "valid": true,
 *     "voucherId": "voucher_abc",
 *     "discountAmount": 7500
 *   }
 * }
 */
data class ValidateVoucherResponse(
    @SerializedName("valid")
    val isValid: Boolean = false,

    @SerializedName("voucherId")
    val voucherId: String = "",

    @SerializedName("discountAmount")
    val discountAmount: Double = 0.0
)

/**
 * Model wrapper cho validate voucher response
 */
data class ValidateVoucherApiResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: ValidateVoucherResponse?
)

/**
 * Model cho lịch sử sử dụng voucher
 * Format từ API usage history:
 * {
 *   "id": "voucher_summer_2024_user_123_order_456",
 *   "voucherId": "voucher_summer_2024",
 *   "code": "SUMMER20",
 *   "shopId": "shop_123",
 *   "orderId": "order_abc123",
 *   "discountAmount": 7500,
 *   "createdAt": "2026-01-20T15:30:45Z"
 * }
 */
data class VoucherUsageHistoryApiModel(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("voucherId")
    val voucherId: String = "",

    @SerializedName("code")
    val code: String = "",

    @SerializedName("shopId")
    val shopId: String = "",

    @SerializedName("orderId")
    val orderId: String = "",

    @SerializedName("discountAmount")
    val discountAmount: Double = 0.0,

    @SerializedName("createdAt")
    val createdAt: String = ""
)

/**
 * Model wrapper cho paginated response của usage history
 * Format:
 * {
 *   "items": [ ... ],
 *   "page": 1,
 *   "limit": 20,
 *   "total": 45,
 *   "pages": 3,
 *   "hasMore": true
 * }
 */
data class VoucherUsageHistoryResponse(
    @SerializedName("items")
    val items: List<VoucherUsageHistoryApiModel> = emptyList(),

    @SerializedName("page")
    val page: Int = 1,

    @SerializedName("limit")
    val limit: Int = 20,

    @SerializedName("total")
    val total: Int = 0,

    @SerializedName("pages")
    val totalPages: Int = 0,

    @SerializedName("hasMore")
    val hasMore: Boolean = false
)

/**
 * Wrapper response cho API usage history (theo pattern success/data)
 * Format:
 * {
 *   "success": true,
 *   "data": {
 *     "items": [...],
 *     "page": 1,
 *     "limit": 20,
 *     "total": 45,
 *     "pages": 3,
 *     "hasMore": true
 *   }
 * }
 */
data class VoucherUsageHistoryApiResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: VoucherUsageHistoryResponse?
)
