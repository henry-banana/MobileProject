package com.example.foodapp.data.model.admin

import com.google.gson.annotations.SerializedName

/**
 * Model cho Category API Response
 * API: GET /categories (Public API)
 */
data class Category(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("status") val status: String = "active",
    @SerializedName("displayOrder") val displayOrder: Int = 0,
    @SerializedName("productCount") val productCount: Int = 0
)

data class CategoriesData(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<Category>
)

data class CategoriesResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: CategoriesData
)
