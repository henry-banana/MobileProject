package com.example.foodapp.data.model.shipper.application

import com.google.gson.annotations.SerializedName

// Shop model for shipper to browse
data class ShopForApplication(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("address")
    val address: String? = null,
    
    @SerializedName("rating")
    val rating: Double? = null,
    
    @SerializedName("totalRatings")
    val totalRatings: Int? = null,
    
    @SerializedName("isOpen")
    val isOpen: Boolean = false,
    
    @SerializedName("openTime")
    val openTime: String? = null,
    
    @SerializedName("closeTime")
    val closeTime: String? = null,
    
    @SerializedName("logoUrl")
    val logoUrl: String? = null,
    
    @SerializedName("coverImageUrl")
    val coverImageUrl: String? = null
)

// Inner paginated data
data class PaginatedShopsData(
    @SerializedName("shops")
    val shops: List<ShopForApplication> = emptyList(),
    
    @SerializedName("total")
    val total: Int = 0,
    
    @SerializedName("page")
    val page: Int = 1,
    
    @SerializedName("limit")
    val limit: Int = 20
)

// API Response wrapper: { success: true, data: {...} }
data class ShopsApiResponse(
    @SerializedName("success")
    val success: Boolean = false,
    
    @SerializedName("data")
    val data: PaginatedShopsData? = null
)

// API Response wrapper for applications: { success: true, data: [...] }
data class ApplicationsApiResponse(
    val success: Boolean = false,
    val data: List<ShipperApplication>? = null
)

// API Response wrapper for single application
data class ApplicationApiResponse(
    val success: Boolean = false,
    val data: ShipperApplication? = null
)

// Shipper Application model
data class ShipperApplication(
    val id: String,
    val userId: String? = null,
    val userName: String? = null,
    val userPhone: String? = null,
    val shopId: String,
    val shopName: String? = null,
    val vehicleType: String? = null,
    val vehicleNumber: String? = null,
    val idCardNumber: String? = null,
    val message: String? = null,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val createdAt: String? = null,
    val rejectedReason: String? = null
)

// Apply request DTO
data class ApplyShipperRequest(
    val shopId: String,
    val vehicleType: String, // MOTORBIKE, BICYCLE
    val vehicleNumber: String,
    val idCardNumber: String,
    val message: String? = null
)

// Status enum for display
enum class ApplicationStatus(val displayName: String, val color: Long) {
    PENDING("Đang chờ duyệt", 0xFFFF9800),
    APPROVED("Đã duyệt", 0xFF4CAF50),
    REJECTED("Bị từ chối", 0xFFF44336)
}
