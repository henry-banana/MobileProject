package com.example.foodapp.data.model.admin

import com.google.gson.annotations.SerializedName

/**
 * Model cho Category API Response
 * API: GET /categories (Public API)
 * 
 * Backend trả về: {"success": true, "data": [...], "timestamp": "..."}
 */
data class Category(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("status") val status: String = "active",
    @SerializedName("displayOrder") val displayOrder: Int = 0,
    @SerializedName("productCount") val productCount: Int = 0
)

/**
 * Response wrapper cho GET /categories
 * data trực tiếp là List<Category>
 */
data class CategoriesResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<Category>,
    @SerializedName("timestamp") val timestamp: String? = null
)
