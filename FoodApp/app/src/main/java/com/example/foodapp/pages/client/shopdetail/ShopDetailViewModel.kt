package com.example.foodapp.pages.client.shopdetail


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.foodapp.data.remote.client.response.shop.ShopDetailApiModel
import com.example.foodapp.data.repository.client.shop.ShopRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed class ShopDetailState {
    object Idle : ShopDetailState()
    object Loading : ShopDetailState()
    data class Success(val shop: ShopDetailApiModel) : ShopDetailState()
    data class Error(val message: String) : ShopDetailState()
}

class ShopDetailViewModel(
    private val shopRepository: ShopRepository
) : ViewModel() {

    private val _shopDetailState = MutableLiveData<ShopDetailState>(ShopDetailState.Idle)
    val shopDetailState: LiveData<ShopDetailState> = _shopDetailState

    private val _shop = MutableLiveData<ShopDetailApiModel?>()
    val shop: LiveData<ShopDetailApiModel?> = _shop

    fun getShopDetail(id: String) {
        _shopDetailState.value = ShopDetailState.Loading

        CoroutineScope(Dispatchers.IO).launch {
            val result = shopRepository.getShopDetail(id)

            launch(Dispatchers.Main) {
                when (result) {
                    is com.example.foodapp.data.remote.client.response.shop.ApiResult.Success -> {
                        val shopData = result.data
                        _shop.value = shopData
                        _shopDetailState.value = ShopDetailState.Success(shopData)
                    }
                    is com.example.foodapp.data.remote.client.response.shop.ApiResult.Failure -> {
                        val errorMessage = result.exception.message ?: "Có lỗi xảy ra"
                        _shopDetailState.value = ShopDetailState.Error(errorMessage)
                    }
                }
            }
        }
    }

    fun clear() {
        _shop.value = null
        _shopDetailState.value = ShopDetailState.Idle
    }

    companion object {
        fun factory(shopRepository: ShopRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ShopDetailViewModel(shopRepository) as T
                }
            }
        }
    }
}