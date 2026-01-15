package com.example.foodapp.data.repository.owner.base

import com.example.foodapp.data.model.admin.Category

/**
 * Base interface cho Category Repository
 * Định nghĩa contract cho việc lấy danh sách categories
 */
interface OwnerCategoryRepository {
    /**
     * Lấy danh sách tất cả categories từ public API
     * @return Result chứa List<Category> nếu thành công, Exception nếu thất bại
     */
    suspend fun getCategories(): Result<List<Category>>
}
