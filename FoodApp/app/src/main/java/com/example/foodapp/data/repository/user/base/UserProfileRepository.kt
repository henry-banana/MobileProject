package com.example.foodapp.data.repository.user.base

import com.example.foodapp.data.model.user.*
import java.io.File

/**
 * Interface cho User Profile Repository
 */
interface UserProfileRepository {

    /**
     * Get current user profile
     */
    suspend fun getProfile(): Result<UserProfile>

    /**
     * Update profile (name, phone)
     */
    suspend fun updateProfile(displayName: String?, phone: String?): Result<UserProfile>

    /**
     * Upload avatar image
     */
    suspend fun uploadAvatar(imageFile: File): Result<String>

    /**
     * Get user settings
     */
    suspend fun getSettings(): Result<UserSettings>

    /**
     * Update user settings
     */
    suspend fun updateSettings(settings: UpdateSettingsRequest): Result<UserSettings>

    /**
     * Get all addresses
     */
    suspend fun getAddresses(): Result<List<Address>>

    /**
     * Create new address
     */
    suspend fun createAddress(label: String, fullAddress: String, isDefault: Boolean): Result<Address>

    /**
     * Update address
     */
    suspend fun updateAddress(id: String, label: String?, fullAddress: String?, isDefault: Boolean?): Result<Address>

    /**
     * Delete address
     */
    suspend fun deleteAddress(id: String): Result<String>

    /**
     * Set default address
     */
    suspend fun setDefaultAddress(id: String): Result<String>

    /**
     * Delete account permanently
     */
    suspend fun deleteAccount(): Result<String>
}
