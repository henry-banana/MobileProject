package com.example.foodapp.data.repository.client.voucher

import com.example.foodapp.data.remote.api.ApiClient
import com.example.foodapp.data.remote.client.response.voucher.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class VoucherRepository {
    private val voucherService = ApiClient.voucherApiService

    // 1. Lấy danh sách voucher
    suspend fun getVouchers(
        token: String,
        shopId: String
    ): ApiResult<List<VoucherApiModel>> {
        return withContext(Dispatchers.IO) {
            try {
                val authToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val response = voucherService.getVouchers(authToken, shopId)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        ApiResult.Success(body.data ?: emptyList())
                    } else {
                        ApiResult.Success(emptyList())
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    ApiResult.Failure(Exception("Lỗi ${response.code()}: ${errorBody ?: response.message()}"))
                }
            } catch (e: IOException) {
                ApiResult.Failure(Exception("Lỗi kết nối"))
            } catch (e: HttpException) {
                ApiResult.Failure(Exception("Lỗi server"))
            } catch (e: Exception) {
                ApiResult.Failure(Exception("Lỗi không xác định"))
            }
        }
    }

    // 2. Validate voucher
    suspend fun validateVoucher(
        token: String,
        voucherCode: String,
        shopId: String,
        subtotal: Double,
        shipFee: Double
    ): ApiResult<ValidateVoucherResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = ValidateVoucherRequest(
                    voucherCode = voucherCode,
                    shopId = shopId,
                    subtotal = subtotal,
                    shipFee = shipFee
                )

                val authToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val response = voucherService.validateVoucher(authToken, request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success && body.data != null) {
                        ApiResult.Success(body.data)
                    } else {
                        ApiResult.Failure(Exception("Voucher không hợp lệ"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    ApiResult.Failure(Exception("Lỗi ${response.code()}: ${errorBody ?: response.message()}"))
                }
            } catch (e: IOException) {
                ApiResult.Failure(Exception("Lỗi kết nối"))
            } catch (e: HttpException) {
                ApiResult.Failure(Exception("Lỗi server"))
            } catch (e: Exception) {
                ApiResult.Failure(Exception("Lỗi không xác định"))
            }
        }
    }

    // 3. Lấy lịch sử sử dụng voucher (phân trang)
    suspend fun getVoucherUsageHistory(
        token: String,
        page: Int = 1,
        limit: Int = 20,
        shopId: String? = null,
        from: String? = null,
        to: String? = null
    ): ApiResult<VoucherUsageHistoryResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val authToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val response = voucherService.getVoucherUsageHistory(
                    authToken,
                    page,
                    limit,
                    shopId,
                    from,
                    to
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success && body.data != null) {
                        ApiResult.Success(body.data)
                    } else {
                        ApiResult.Failure(Exception("Không có dữ liệu"))
                    }
                } else {
                    when (response.code()) {
                        401 -> ApiResult.Failure(Exception("Phiên đăng nhập hết hạn"))
                        403 -> ApiResult.Failure(Exception("Không có quyền truy cập"))
                        else -> {
                            val errorBody = response.errorBody()?.string()
                            ApiResult.Failure(Exception("Lỗi ${response.code()}: ${errorBody ?: response.message()}"))
                        }
                    }
                }
            } catch (e: IOException) {
                ApiResult.Failure(Exception("Lỗi kết nối"))
            } catch (e: HttpException) {
                ApiResult.Failure(Exception("Lỗi server"))
            } catch (e: Exception) {
                ApiResult.Failure(Exception("Lỗi không xác định"))
            }
        }
    }
}