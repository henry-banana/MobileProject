package com.example.foodapp.data.repository.owner.categories

import com.example.foodapp.data.model.admin.Category
import com.example.foodapp.data.remote.admin.CategoriesApiService
import com.example.foodapp.data.repository.owner.base.OwnerCategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Real implementation của Category Repository
 * Gọi public API để lấy danh sách categories
 */
class RealCategoryRepository(private val apiService: CategoriesApiService) : OwnerCategoryRepository {

    override suspend fun getCategories(): Result<List<Category>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCategories()
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        // data là List<Category> trực tiếp
                        Result.success(body.data)
                    } else {
                        Result.failure(Exception("Failed to get categories"))
                    }
                } else {
                    Result.failure(Exception(response.errorBody()?.string() ?: "Error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
