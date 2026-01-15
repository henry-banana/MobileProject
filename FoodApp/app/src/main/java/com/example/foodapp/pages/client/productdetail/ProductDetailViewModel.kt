// ProductDetailViewModel.kt
package com.example.foodapp.pages.client.productdetail

import android.content.Context
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.foodapp.data.remote.api.ApiClient
import com.example.foodapp.data.model.shared.product.Product
import com.example.foodapp.data.model.client.product.ApiResult
import com.example.foodapp.data.repository.client.products.*
import kotlinx.coroutines.launch

// State cho màn hình chi tiết sản phẩm
sealed class ProductDetailState {
    object Idle : ProductDetailState()
    object Loading : ProductDetailState()
    data class Success(val product: Product) : ProductDetailState()
    data class Error(val message: String) : ProductDetailState()
}

// State cho thêm vào yêu thích
sealed class FavoriteState {
    object Idle : FavoriteState()
    object Loading : FavoriteState()
    data class Success(val message: String) : FavoriteState()
    data class Error(val message: String) : FavoriteState()
}

class ProductDetailViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _productDetailState = MutableLiveData<ProductDetailState>(ProductDetailState.Idle)
    val productDetailState: LiveData<ProductDetailState> = _productDetailState

    private val _favoriteState = MutableLiveData<FavoriteState>(FavoriteState.Idle)
    val favoriteState: LiveData<FavoriteState> = _favoriteState

    private val _product = MutableLiveData<Product?>(null)
    val product: LiveData<Product?> = _product

    // Lấy chi tiết sản phẩm
    fun getProductDetail(productId: String) {
        if (productId.isBlank()) {
            _productDetailState.value = ProductDetailState.Error("ID sản phẩm không hợp lệ")
            return
        }

        _productDetailState.value = ProductDetailState.Loading

        viewModelScope.launch {
            try {
                val result = productRepository.getProductDetail(productId)

                when (result) {
                    is ApiResult.Success<*> -> {
                        val product = (result.data as? Product)
                        if (product != null) {
                            _product.value = product
                            _productDetailState.value = ProductDetailState.Success(product)
                        } else {
                            _productDetailState.value = ProductDetailState.Error("Dữ liệu sản phẩm không hợp lệ")
                        }
                    }
                    is ApiResult.Failure -> {
                        _productDetailState.value = ProductDetailState.Error(
                            result.exception.message ?: "Lỗi không xác định"
                        )
                    }
                }
            } catch (e: Exception) {
                _productDetailState.value = ProductDetailState.Error(
                    e.message ?: "Lỗi kết nối"
                )
            }
        }
    }

    // Thêm sản phẩm vào danh sách yêu thích
    fun addToFavorites(productId: String) {
        if (productId.isBlank()) {
            _favoriteState.value = FavoriteState.Error("ID sản phẩm không hợp lệ")
            return
        }

        // Kiểm tra nếu đang loading thì không thực hiện
        if (_favoriteState.value == FavoriteState.Loading) {
            return
        }

        _favoriteState.value = FavoriteState.Loading

        viewModelScope.launch {
            try {
                val result = productRepository.addToFavorites(productId)

                when (result) {
                    is ApiResult.Success<*> -> {
                        val response = result.data
                        // Kiểm tra response có phải là AddToFavoriteResponse không
                        if (response is com.example.foodapp.data.model.client.product.AddToFavoriteResponse) {
                            if (response.success) {
                                val successMessage = response.message.ifBlank { "Đã thêm vào yêu thích" }
                                _favoriteState.value = FavoriteState.Success(successMessage)

                                // Cập nhật sản phẩm hiện tại nếu productId khớp
                                _product.value?.let { currentProduct ->
                                    if (currentProduct.id == productId) {
                                        // Cập nhật sản phẩm với trạng thái yêu thích
                                        val updatedProduct = currentProduct.copy(isFavorite = true)
                                        _product.value = updatedProduct
                                    }
                                }
                            } else {
                                _favoriteState.value = FavoriteState.Error(
                                    response.message.ifBlank { "Không thể thêm vào yêu thích" }
                                )
                            }
                        } else {
                            _favoriteState.value = FavoriteState.Error("Định dạng response không hợp lệ")
                        }
                    }
                    is ApiResult.Failure -> {
                        val errorMessage = result.exception.message ?: "Lỗi không xác định"

                        // Xử lý các lỗi đặc biệt
                        when {
                            errorMessage.contains("đã có trong danh sách yêu thích", ignoreCase = true) ||
                                    errorMessage.contains("already in favorites", ignoreCase = true) -> {
                                // Nếu đã có trong favorites, cập nhật UI
                                _product.value?.let { currentProduct ->
                                    if (currentProduct.id == productId) {
                                        val updatedProduct = currentProduct.copy(isFavorite = true)
                                        _product.value = updatedProduct
                                    }
                                }
                                _favoriteState.value = FavoriteState.Success("Sản phẩm đã có trong yêu thích")
                            }
                            errorMessage.contains("đăng nhập", ignoreCase = true) ||
                                    errorMessage.contains("login", ignoreCase = true) ||
                                    errorMessage.contains("401", ignoreCase = true) -> {
                                _favoriteState.value = FavoriteState.Error("Vui lòng đăng nhập để thêm vào yêu thích")
                            }
                            else -> {
                                _favoriteState.value = FavoriteState.Error(errorMessage)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _favoriteState.value = FavoriteState.Error(
                    e.message ?: "Lỗi kết nối"
                )
            }
        }
    }

    // Reset state favorite
    fun resetFavoriteState() {
        _favoriteState.value = FavoriteState.Idle
    }

    // Reset state
    fun reset() {
        _productDetailState.value = ProductDetailState.Idle
        _favoriteState.value = FavoriteState.Idle
        _product.value = null
    }

    companion object {
        /**
         * Factory để tạo ProductDetailViewModel
         */
        fun factory(context: Context) = viewModelFactory {
            initializer {
                val apiService = ApiClient.productApiService
                val productRepository = ProductRepository(apiService)
                ProductDetailViewModel(productRepository)
            }
        }
    }
}