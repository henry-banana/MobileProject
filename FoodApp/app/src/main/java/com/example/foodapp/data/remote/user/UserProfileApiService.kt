package com.example.foodapp.data.remote.user

import com.example.foodapp.data.model.user.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service cho User Profile Management
 * Base URL: /api/me
 */
interface UserProfileApiService {

    /**
     * GET /me - Get current user profile
     */
    @GET("me")
    suspend fun getProfile(): Response<ProfileResponse>

    /**
     * PUT /me - Update profile
     */
    @PUT("me")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<ProfileResponse>

    /**
     * POST /me/avatar - Upload avatar
     */
    @Multipart
    @POST("me/avatar")
    suspend fun uploadAvatar(
        @Part avatar: MultipartBody.Part
    ): Response<AvatarUploadResponse>

    /**
     * GET /me/settings - Get settings
     */
    @GET("me/settings")
    suspend fun getSettings(): Response<SettingsResponse>

    /**
     * PUT /me/settings - Update settings
     */
    @PUT("me/settings")
    suspend fun updateSettings(
        @Body request: UpdateSettingsRequest
    ): Response<SettingsResponse>

    /**
     * GET /me/addresses - List addresses
     */
    @GET("me/addresses")
    suspend fun getAddresses(): Response<AddressesResponse>

    /**
     * POST /me/addresses - Create address
     */
    @POST("me/addresses")
    suspend fun createAddress(
        @Body request: CreateAddressRequest
    ): Response<AddressResponse>

    /**
     * PUT /me/addresses/{id} - Update address
     */
    @PUT("me/addresses/{id}")
    suspend fun updateAddress(
        @Path("id") id: String,
        @Body request: UpdateAddressRequest
    ): Response<AddressResponse>

    /**
     * DELETE /me/addresses/{id} - Delete address
     */
    @DELETE("me/addresses/{id}")
    suspend fun deleteAddress(
        @Path("id") id: String
    ): Response<MessageResponse>

    /**
     * PUT /me/addresses/{id}/default - Set default address
     */
    @PUT("me/addresses/{id}/default")
    suspend fun setDefaultAddress(
        @Path("id") id: String
    ): Response<MessageResponse>

    /**
     * DELETE /me - Delete account
     */
    @DELETE("me")
    suspend fun deleteAccount(): Response<MessageResponse>
}
