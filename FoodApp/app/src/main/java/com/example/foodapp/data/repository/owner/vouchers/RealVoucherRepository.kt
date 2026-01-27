package com.example.foodapp.data.repository.owner.vouchers

import com.example.foodapp.data.model.owner.voucher.*
import com.example.foodapp.data.remote.owner.VoucherApiService
import com.example.foodapp.data.repository.owner.base.OwnerVoucherRepository
import retrofit2.Response

/**
 * Real implementation of OwnerVoucherRepository using backend API
 */
class RealVoucherRepository(
    private val apiService: VoucherApiService
) : OwnerVoucherRepository {

    override suspend fun getVouchers(isActive: Boolean?): Result<List<Voucher>> {
        return try {
            val isActiveParam = isActive?.toString()
            val response = apiService.getVouchers(isActiveParam)
            if (response.isSuccessful && response.body() != null) {
                val wrapper = response.body()!!
                if (wrapper.success && wrapper.data != null) {
                    Result.success(wrapper.data.map { it.toVoucher() })
                } else {
                    Result.failure(Exception(wrapper.message ?: "Failed to load vouchers"))
                }
            } else {
                Result.failure(Exception(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createVoucher(request: CreateVoucherRequest): Result<Voucher> {
        return try {
            val response = apiService.createVoucher(request)
            if (response.isSuccessful && response.body() != null) {
                val wrapper = response.body()!!
                if (wrapper.success && wrapper.data != null) {
                    Result.success(wrapper.data.toVoucher())
                } else {
                    Result.failure(Exception(wrapper.message ?: "Failed to create voucher"))
                }
            } else {
                Result.failure(Exception(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateVoucher(voucherId: String, request: UpdateVoucherRequest): Result<Unit> {
        return try {
            val response = apiService.updateVoucher(voucherId, request)
            if (response.isSuccessful && response.body() != null) {
                val wrapper = response.body()!!
                if (wrapper.success) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(wrapper.message ?: "Failed to update voucher"))
                }
            } else {
                Result.failure(Exception(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteVoucher(voucherId: String): Result<Unit> {
        return try {
            val response = apiService.deleteVoucher(voucherId)
            if (response.isSuccessful && response.body() != null) {
                val wrapper = response.body()!!
                if (wrapper.success) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(wrapper.message ?: "Failed to delete voucher"))
                }
            } else {
                Result.failure(Exception(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateVoucherStatus(voucherId: String, isActive: Boolean): Result<Unit> {
        return try {
            val response = apiService.updateVoucherStatus(voucherId, UpdateVoucherStatusRequest(isActive))
            if (response.isSuccessful && response.body() != null) {
                val wrapper = response.body()!!
                if (wrapper.success) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(wrapper.message ?: "Failed to update voucher status"))
                }
            } else {
                Result.failure(Exception(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun <T> getErrorMessage(response: Response<T>): String {
        return try {
            response.errorBody()?.string() ?: "Unknown error occurred"
        } catch (e: Exception) {
            "Error: ${response.code()}"
        }
    }
}
