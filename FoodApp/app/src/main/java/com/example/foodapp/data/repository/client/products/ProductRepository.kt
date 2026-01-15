package com.example.foodapp.data.repository.client.products

import com.example.foodapp.data.model.client.product.AddToFavoriteResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import com.example.foodapp.data.remote.client.ProductApiService
import com.example.foodapp.data.model.client.product.ProductFilterDto
import com.example.foodapp.data.model.shared.product.Product
import com.example.foodapp.data.model.client.product.ApiResult
import javax.inject.Inject

class ProductRepository @Inject constructor(
    private val productService: ProductApiService
) {

    suspend fun getProducts(filters: ProductFilterDto = ProductFilterDto()): ApiResult<List<Product>> {
        return withContext(Dispatchers.IO) {
            try {
                val queryMap = filters.toQueryMap()
                val apiResponse = productService.getProducts(queryMap)

                if (apiResponse.success) {
                    val productListData = apiResponse.data
                    if (productListData != null) {
                        val products = productListData.products.map { it.toProduct() }
                        ApiResult.Success(products)
                    } else {
                        ApiResult.Success(emptyList())
                    }
                } else {
                    ApiResult.Failure(
                        Exception(apiResponse.message ?: "API request failed")
                    )
                }
            } catch (e: IOException) {
                e.printStackTrace()
                ApiResult.Failure(
                    Exception("Lỗi kết nối mạng: ${e.message}")
                )
            } catch (e: HttpException) {
                println("DEBUG: [ProductRepository] HttpException: ${e.code()}: ${e.message}")
                ApiResult.Failure(
                    Exception("Lỗi server ${e.code()}: ${e.message}")
                )
            } catch (e: Exception) {
                e.printStackTrace()
                ApiResult.Failure(
                    Exception("Lỗi không xác định: ${e.message}")
                )
            }
        }
    }

    // THÊM HÀM LẤY CHI TIẾT SẢN PHẨM
    suspend fun getProductDetail(productId: String): ApiResult<Product> {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG: [ProductRepository] Getting product detail for ID: $productId")

                val apiResponse = productService.getProductDetail(productId)
                println("DEBUG: [ProductRepository] API Response success: ${apiResponse.success}")

                if (apiResponse.success && apiResponse.data != null) {
                    val product = apiResponse.data.toProduct()
                    println("DEBUG: [ProductRepository] Successfully got product: ${product.name}")
                    ApiResult.Success(product)
                } else {
                    val errorMessage = apiResponse.message ?: "Không tìm thấy sản phẩm"
                    println("DEBUG: [ProductRepository] Error: $errorMessage")
                    ApiResult.Failure(Exception(errorMessage))
                }
            } catch (e: IOException) {
                println("DEBUG: [ProductRepository] IOException: ${e.message}")
                e.printStackTrace()
                ApiResult.Failure(
                    Exception("Lỗi kết nối mạng: ${e.message}")
                )
            } catch (e: HttpException) {
                println("DEBUG: [ProductRepository] HttpException ${e.code()}: ${e.message}")
                val errorMsg = when (e.code()) {
                    404 -> "Không tìm thấy sản phẩm"
                    500 -> "Lỗi server"
                    else -> "Lỗi ${e.code()}: ${e.message}"
                }
                ApiResult.Failure(Exception(errorMsg))
            } catch (e: Exception) {
                println("DEBUG: [ProductRepository] Exception: ${e.message}")
                e.printStackTrace()
                ApiResult.Failure(
                    Exception("Lỗi không xác định: ${e.message}")
                )
            }
        }
    }

    suspend fun addToFavorites(productId: String): ApiResult<AddToFavoriteResponse> {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG: [ProductRepository] Adding product to favorites: $productId")

                // Gọi API thêm vào favorites
                val apiResponse = productService.addToFavorites(productId)
                println("DEBUG: [ProductRepository] Add favorite API Response success: ${apiResponse.success}")

                if (apiResponse.success) {
                    println("DEBUG: [ProductRepository] Successfully added to favorites: ${apiResponse.message}")
                    ApiResult.Success(apiResponse)
                } else {
                    val errorMessage = apiResponse.message ?: "Không thể thêm vào yêu thích"
                    println("DEBUG: [ProductRepository] Error: $errorMessage")
                    ApiResult.Failure(Exception(errorMessage))
                }
            } catch (e: IOException) {
                println("DEBUG: [ProductRepository] IOException: ${e.message}")
                e.printStackTrace()
                ApiResult.Failure(
                    Exception("Lỗi kết nối mạng: ${e.message}")
                )
            } catch (e: HttpException) {
                println("DEBUG: [ProductRepository] HttpException ${e.code()}: ${e.message}")
                val errorMsg = when (e.code()) {
                    409 -> "Sản phẩm đã có trong danh sách yêu thích"
                    401 -> "Vui lòng đăng nhập để thêm vào yêu thích"
                    404 -> "Không tìm thấy sản phẩm"
                    500 -> "Lỗi server"
                    else -> "Lỗi ${e.code()}: ${e.message}"
                }
                ApiResult.Failure(Exception(errorMsg))
            } catch (e: Exception) {
                println("DEBUG: [ProductRepository] Exception: ${e.message}")
                e.printStackTrace()
                ApiResult.Failure(
                    Exception("Lỗi không xác định: ${e.message}")
                )
            }
        }
    }

}