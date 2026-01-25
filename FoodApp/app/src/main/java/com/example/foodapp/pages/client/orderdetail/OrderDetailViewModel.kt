package com.example.foodapp.pages.client.orderdetail


import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.foodapp.data.model.shared.product.Product
import com.example.foodapp.data.remote.api.ApiClient
import com.example.foodapp.data.remote.client.OrderApiService
import com.example.foodapp.data.remote.client.response.order.*
import com.example.foodapp.data.repository.client.order.OrderRepository
import kotlinx.coroutines.launch

// ============== ORDER DETAIL STATES ==============

sealed class OrderDetailState {
    object Idle : OrderDetailState()
    object Loading : OrderDetailState()
    data class Success(val order: OrderApiModel) : OrderDetailState()
    data class Error(val message: String) : OrderDetailState()
    object Empty : OrderDetailState()
}

sealed class CancelOrderState {
    object Idle : CancelOrderState()
    object Loading : CancelOrderState()
    data class Success(val orderId: String) : CancelOrderState()
    data class Error(val message: String) : CancelOrderState()
}

// ============== ORDER DETAIL VIEW MODEL ==============

class OrderDetailViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _orderDetailState = MutableLiveData<OrderDetailState>(OrderDetailState.Idle)
    val orderDetailState: LiveData<OrderDetailState> = _orderDetailState

    private val _cancelOrderState = MutableLiveData<CancelOrderState>(CancelOrderState.Idle)
    val cancelOrderState: LiveData<CancelOrderState> = _cancelOrderState

    private val _currentOrder = MutableLiveData<OrderApiModel?>(null)
    val currentOrder: LiveData<OrderApiModel?> = _currentOrder

    // ============== ORDER DETAIL FUNCTIONS ==============

    fun fetchOrderDetail(orderId: String) {
        println("DEBUG: [OrderDetailViewModel] Fetching order detail for: $orderId")

        _orderDetailState.value = OrderDetailState.Loading

        viewModelScope.launch {
            try {
                val result = orderRepository.getOrderById(orderId)
                println("DEBUG: [OrderDetailViewModel] Result type: ${result::class.simpleName}")

                when (result) {
                    is ApiResult.Success<*> -> {
                        println("DEBUG: [OrderDetailViewModel] Entered Success branch")
                        val order = (result.data as? OrderApiModel) ?: run {
                            _orderDetailState.value = OrderDetailState.Empty
                            return@launch
                        }

                        println("DEBUG: [OrderDetailViewModel] Order loaded: ${order.orderNumber}")
                        _currentOrder.value = order
                        _orderDetailState.value = OrderDetailState.Success(order)
                        println("DEBUG: [OrderDetailViewModel] State set to Success")
                    }
                    is ApiResult.Failure -> {
                        println("DEBUG: [OrderDetailViewModel] Entered Failure branch: ${result.exception.message}")
                        _orderDetailState.value = OrderDetailState.Error(
                            result.exception.message ?: "Không thể tải chi tiết đơn hàng"
                        )
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: [OrderDetailViewModel] Exception in fetchOrderDetail: ${e.message}")
                e.printStackTrace()
                _orderDetailState.value = OrderDetailState.Error(
                    e.message ?: "Lỗi kết nối"
                )
            }
        }
    }

    // ============== ORDER ACTIONS FUNCTIONS ==============

    fun cancelOrder(orderId: String) {
        println("DEBUG: [OrderDetailViewModel] Canceling order: $orderId")

        _cancelOrderState.value = CancelOrderState.Loading

        viewModelScope.launch {
            try {
                val result = orderRepository.deleteOrder(orderId)
                println("DEBUG: [OrderDetailViewModel] Cancel result: ${result::class.simpleName}")

                when (result) {
                    is ApiResult.Success<*> -> {
                        println("DEBUG: [OrderDetailViewModel] Cancel successful")
                        _cancelOrderState.value = CancelOrderState.Success(orderId)
                        // Refresh order detail after cancellation
                        fetchOrderDetail(orderId)
                    }
                    is ApiResult.Failure -> {
                        println("DEBUG: [OrderDetailViewModel] Cancel failed: ${result.exception.message}")
                        _cancelOrderState.value = CancelOrderState.Error(
                            result.exception.message ?: "Hủy đơn hàng thất bại"
                        )
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: [OrderDetailViewModel] Exception in cancelOrder: ${e.message}")
                e.printStackTrace()
                _cancelOrderState.value = CancelOrderState.Error(
                    e.message ?: "Lỗi kết nối"
                )
            }
        }
    }

    fun resetCancelState() {
        _cancelOrderState.value = CancelOrderState.Idle
    }

    fun refresh(orderId: String) {
        println("DEBUG: [OrderDetailViewModel] Refresh order detail")
        fetchOrderDetail(orderId)
    }

    fun reset() {
        _orderDetailState.value = OrderDetailState.Idle
        _cancelOrderState.value = CancelOrderState.Idle
        _currentOrder.value = null
    }

    companion object {
        fun factory(context: Context) = viewModelFactory {
            initializer {
                val orderApiService: OrderApiService = ApiClient.orderApiService
                val orderRepository = OrderRepository()
                OrderDetailViewModel(orderRepository)
            }
        }
    }
}