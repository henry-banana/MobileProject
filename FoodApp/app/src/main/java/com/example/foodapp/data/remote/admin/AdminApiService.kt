package com.example.foodapp.data.remote.admin

import com.example.foodapp.data.model.admin.CategoriesResponse
import retrofit2.Response
import retrofit2.http.GET

/**
 * API Service cho Categories (Public API)
 * Base URL: /categories
 */
interface CategoriesApiService {
    
    /**
     * GET /categories
     * Lấy danh sách categories active (Public API, không cần auth)
     */
    @GET("categories")
    suspend fun getCategories(): Response<CategoriesResponse>
}
