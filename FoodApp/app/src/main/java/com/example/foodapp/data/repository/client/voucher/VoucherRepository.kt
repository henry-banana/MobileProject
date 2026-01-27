package com.example.foodapp.data.repository.client.voucher

import com.example.foodapp.data.remote.api.ApiClient
import com.example.foodapp.data.remote.client.response.voucher.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class VoucherRepository {
    private val voucherService = ApiClient.voucherApiService

    suspend fun getVouchers(shopId: String): ApiResult<List<VoucherApiModel>> {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG: [VoucherRepository] Starting getVouchers for shopId: $shopId")

                val response = voucherService.getVouchers(shopId)

                if (response.isSuccessful) {
                    val body = response.body()
                    println("DEBUG: [VoucherRepository] Response received: success=${body?.success}")

                    if (body != null && body.success) {
                        val vouchers = body.data ?: emptyList()
                        println("DEBUG: [VoucherRepository] Vouchers loaded: ${vouchers.size} vouchers")

                        // Log để debug
                        if (vouchers.isNotEmpty()) {
                            println("DEBUG: [VoucherRepository] First voucher: ${vouchers.first().code} - ${vouchers.first().name}")
                        }

                        ApiResult.Success(vouchers)
                    } else if (body != null && !body.success) {
                        println("DEBUG: [VoucherRepository] API returned success=false")
                        ApiResult.Success(emptyList())
                    } else {
                        println("DEBUG: [VoucherRepository] Response body is null")
                        ApiResult.Success(emptyList())
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("DEBUG: [VoucherRepository] API Error - ${response.code()}: ${errorBody}")
                    ApiResult.Failure(
                        Exception("Lỗi ${response.code()}: ${errorBody ?: response.message()}")
                    )
                }
            } catch (e: IOException) {
                println("DEBUG: [VoucherRepository] IOException: ${e.message}")
                ApiResult.Failure(Exception("Lỗi kết nối: ${e.message}"))
            } catch (e: HttpException) {
                println("DEBUG: [VoucherRepository] HttpException: ${e.code()} - ${e.message()}")
                ApiResult.Failure(Exception("Lỗi server: ${e.code()} - ${e.message()}"))
            } catch (e: Exception) {
                println("DEBUG: [VoucherRepository] Exception: ${e.message}")
                e.printStackTrace()
                ApiResult.Failure(Exception("Lỗi không xác định: ${e.message}"))
            }
        }
    }


    suspend fun validateVoucher(
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

                println("DEBUG: [VoucherRepository] Validating voucher: $voucherCode, shop: $shopId, amount: $subtotal")

                val response = voucherService.validateVoucher(request)

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body != null && body.success) {
                        val validateResult = body.data
                        if (validateResult != null) {
                            println("DEBUG: [VoucherRepository] Voucher validation result: isValid=${validateResult.isValid}, discount=${validateResult.discountAmount}")
                            ApiResult.Success(validateResult)
                        } else {
                            println("DEBUG: [VoucherRepository] Validation response data is null")
                            ApiResult.Failure(Exception("Không nhận được dữ liệu validate"))
                        }
                    } else {
                        val errorMessage = if (body != null && !body.success) {
                            "Voucher không hợp lệ"
                        } else {
                            "Lỗi server khi validate voucher"
                        }
                        println("DEBUG: [VoucherRepository] Validation failed: $errorMessage")
                        ApiResult.Failure(Exception(errorMessage))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = "Lỗi ${response.code()}: ${errorBody ?: response.message()}"
                    println("DEBUG: [VoucherRepository] API Error: $errorMessage")
                    ApiResult.Failure(Exception(errorMessage))
                }
            } catch (e: IOException) {
                println("DEBUG: [VoucherRepository] IOException: ${e.message}")
                ApiResult.Failure(Exception("Lỗi kết nối: ${e.message}"))
            } catch (e: HttpException) {
                println("DEBUG: [VoucherRepository] HttpException: ${e.code()} - ${e.message()}")
                ApiResult.Failure(Exception("Lỗi server: ${e.code()} - ${e.message()}"))
            } catch (e: Exception) {
                println("DEBUG: [VoucherRepository] Exception: ${e.message}")
                e.printStackTrace()
                ApiResult.Failure(Exception("Lỗi không xác định: ${e.message}"))
            }
        }
    }
}