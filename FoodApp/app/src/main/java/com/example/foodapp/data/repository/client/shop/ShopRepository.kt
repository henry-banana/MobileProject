package com.example.foodapp.data.repository.client.shop

import com.example.foodapp.data.remote.api.ApiClient
import com.example.foodapp.data.remote.client.response.shop.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class ShopRepository {
    private val shopApiService = ApiClient.shopApiService

    /**
     * Get all shops with pagination and filters
     * @param page Page number (default: 1)
     * @param limit Items per page (default: 20)
     * @param status Shop status filter (OPEN/CLOSED)
     * @param search Search keyword for shop name/description
     * @return ApiResult containing ShopsData or Exception
     */
    suspend fun getAllShops(
        page: Int = 1,
        limit: Int = 20,
        status: String? = null,
        search: String? = null
    ): ApiResult<ShopsData> {
        return withContext(Dispatchers.IO) {
            try {
                val response = shopApiService.getAllShops(
                    page = page,
                    limit = limit,
                    status = status,
                    search = search
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        ApiResult.Success(body.data)
                    } else {
                        ApiResult.Failure(
                            Exception("Failed to get shops: ${body?.success ?: "No response"}")
                        )
                    }
                } else {
                    ApiResult.Failure(
                        HttpException(response)
                    )
                }
            } catch (e: IOException) {
                ApiResult.Failure(
                    Exception("Network error: ${e.message}")
                )
            } catch (e: HttpException) {
                ApiResult.Failure(e)
            } catch (e: Exception) {
                ApiResult.Failure(e)
            }
        }
    }

    /**
     * Get shop details by ID
     * @param id Shop ID
     * @return ApiResult containing ShopDetailApiModel or Exception
     */
    suspend fun getShopDetail(id: String): ApiResult<ShopDetailApiModel> {
        return withContext(Dispatchers.IO) {
            try {
                val response = shopApiService.getShopDetail(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    when {
                        body?.success == true && body.data != null -> {
                            ApiResult.Success(body.data)
                        }
                        response.code() == 404 -> {
                            ApiResult.Failure(
                                Exception("Shop not found (404)")
                            )
                        }
                        else -> {
                            ApiResult.Failure(
                                Exception("Failed to get shop details. Success: ${body?.success}, Data: ${body?.data}")
                            )
                        }
                    }
                } else {
                    // Log để debug
                    val errorCode = response.code()
                    val errorMessage = response.message()
                    ApiResult.Failure(
                        Exception("HTTP $errorCode: $errorMessage")
                    )
                }
            } catch (e: IOException) {
                ApiResult.Failure(
                    Exception("Network error: ${e.message}")
                )
            } catch (e: HttpException) {
                // Log thêm thông tin cho HttpException
                ApiResult.Failure(
                    Exception("HTTP Exception: ${e.code()} - ${e.message()}")
                )
            } catch (e: Exception) {
                ApiResult.Failure(
                    Exception("Unknown error: ${e.message}")
                )
            }
        }
    }
}