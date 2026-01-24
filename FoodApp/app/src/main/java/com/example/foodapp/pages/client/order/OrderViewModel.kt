package com.example.foodapp.pages.client.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.foodapp.data.remote.api.ApiClient
import com.example.foodapp.data.remote.client.response.order.ApiResult
import com.example.foodapp.data.remote.client.response.order.GetOrdersDataResponse
import com.example.foodapp.data.remote.client.response.order.OrderPreviewApiModel
import com.example.foodapp.data.repository.client.order.OrderRepository
import kotlinx.coroutines.launch

// ============== ORDER STATES ==============

sealed class OrderState {
    object Idle : OrderState()
    object Loading : OrderState()
    data class Success(val orders: List<OrderPreviewApiModel>) : OrderState()
    data class Error(val message: String) : OrderState()
    object Empty : OrderState()
}

// ============== DELETE STATES ==============

sealed class DeleteOrderState {
    object Idle : DeleteOrderState()
    object Loading : DeleteOrderState()
    data class Success(val deletedOrderId: String) : DeleteOrderState()
    data class Error(val message: String) : DeleteOrderState()
}

// ============== ORDER VIEW MODEL ==============

class OrderViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _orderState = MutableLiveData<OrderState>(OrderState.Idle)
    val orderState: LiveData<OrderState> = _orderState

    private val _orders = MutableLiveData<List<OrderPreviewApiModel>>(emptyList())
    val orders: LiveData<List<OrderPreviewApiModel>> = _orders

    private val _currentPage = MutableLiveData(1)
    val currentPage: LiveData<Int> = _currentPage

    private val _hasMore = MutableLiveData(true)
    val hasMore: LiveData<Boolean> = _hasMore

    private val _isLoadingMore = MutableLiveData(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    private val _selectedStatus = MutableLiveData<String?>(null)
    val selectedStatus: LiveData<String?> = _selectedStatus

    // ============== DELETE STATES ==============
    private val _deleteOrderState = MutableLiveData<DeleteOrderState>(DeleteOrderState.Idle)
    val deleteOrderState: LiveData<DeleteOrderState> = _deleteOrderState

    // ============== ORDER FUNCTIONS ==============

    /**
     * Lấy danh sách đơn hàng
     */
    fun getOrders(
        forceRefresh: Boolean = false,
        status: String? = null
    ) {
        if (forceRefresh) {
            _currentPage.value = 1
            _hasMore.value = true
            _orders.value = emptyList()
        }

        _orderState.value = OrderState.Loading
        _selectedStatus.value = status

        viewModelScope.launch {
            try {
                val result = orderRepository.getOrders(
                    page = if (forceRefresh) 1 else (_currentPage.value ?: 1),
                    limit = 10,
                    status = status
                )

                when (result) {
                    is ApiResult.Success -> {
                        handleOrderSuccess(result.data, forceRefresh)
                    }
                    is ApiResult.Failure -> {
                        handleOrderFailure(result.exception.message ?: "Lỗi không xác định")
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: [OrderViewModel] Exception in getOrders: ${e.message}")
                e.printStackTrace()
                _orderState.value = OrderState.Error(
                    e.message ?: "Lỗi kết nối"
                )
                _hasMore.value = false
            }
        }
    }

    /**
     * Xử lý thành công khi lấy đơn hàng
     */
    private fun handleOrderSuccess(data: GetOrdersDataResponse, forceRefresh: Boolean) {
        val orderList = data.orders

        if (forceRefresh) {
            _orders.value = orderList
        } else {
            val currentOrders = _orders.value ?: emptyList()
            _orders.value = currentOrders + orderList
        }

        _currentPage.value = data.page
        _hasMore.value = data.page < data.totalPages

        if (orderList.isNotEmpty()) {
            _orderState.value = OrderState.Success(orderList)
        } else {
            _orderState.value = OrderState.Empty
        }

        println("DEBUG: [OrderViewModel] Orders loaded: ${orderList.size}, page: ${data.page}, total: ${data.total}, hasMore: ${_hasMore.value}")
    }

    /**
     * Xử lý thất bại khi lấy đơn hàng
     */
    private fun handleOrderFailure(message: String) {
        _orderState.value = OrderState.Error(message)
        _hasMore.value = false
        println("DEBUG: [OrderViewModel] Failed to load orders: $message")
    }

    /**
     * Tải thêm đơn hàng
     */
    fun loadMoreOrders() {
        val currentPage = _currentPage.value ?: 1
        val status = _selectedStatus.value

        if (_isLoadingMore.value == true || _hasMore.value == false) return

        _isLoadingMore.value = true

        viewModelScope.launch {
            try {
                val result = orderRepository.getOrders(
                    page = currentPage + 1,
                    limit = 10,
                    status = status
                )

                when (result) {
                    is ApiResult.Success -> {
                        handleOrderSuccess(result.data, false)
                    }
                    is ApiResult.Failure -> {
                        handleOrderFailure(result.exception.message ?: "Lỗi không xác định")
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: [OrderViewModel] Exception in loadMoreOrders: ${e.message}")
                _orderState.value = OrderState.Error(
                    e.message ?: "Lỗi kết nối"
                )
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    /**
     * Lọc đơn hàng theo trạng thái
     */
    fun filterByStatus(status: String?) {
        println("DEBUG: [OrderViewModel] Filter by status: $status")
        getOrders(forceRefresh = true, status = status)
    }

    /**
     * Refresh danh sách đơn hàng
     */
    fun refresh() {
        println("DEBUG: [OrderViewModel] Refreshing orders")
        getOrders(forceRefresh = true, status = _selectedStatus.value)
    }

    /**
     * Reset view model
     */
    fun reset() {
        _orders.value = emptyList()
        _orderState.value = OrderState.Idle
        _currentPage.value = 1
        _hasMore.value = true
        _isLoadingMore.value = false
        _selectedStatus.value = null
    }

    // ============== DELETE FUNCTION ==============

    /**
     * Xóa một đơn hàng
     * @param orderId: ID của đơn hàng cần xóa
     */
    fun deleteOrder(orderId: String) {
        _deleteOrderState.value = DeleteOrderState.Loading

        viewModelScope.launch {
            try {
                val result = orderRepository.deleteOrder(orderId)

                when (result) {
                    is ApiResult.Success -> {
                        // Xóa đơn hàng khỏi danh sách hiện tại
                        val currentOrders = _orders.value ?: emptyList()
                        val updatedOrders = currentOrders.filter { it.id != orderId }
                        _orders.value = updatedOrders

                        // Cập nhật state thành công
                        _deleteOrderState.value = DeleteOrderState.Success(orderId)

                        // Kiểm tra nếu danh sách rỗng sau khi xóa
                        if (updatedOrders.isEmpty()) {
                            _orderState.value = OrderState.Empty
                        }

                        println("DEBUG: [OrderViewModel] Order deleted: $orderId")
                    }
                    is ApiResult.Failure -> {
                        _deleteOrderState.value = DeleteOrderState.Error(
                            result.exception.message ?: "Xóa đơn hàng thất bại"
                        )
                        println("DEBUG: [OrderViewModel] Delete order failed: ${result.exception.message}")
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: [OrderViewModel] Exception in deleteOrder: ${e.message}")
                _deleteOrderState.value = DeleteOrderState.Error(
                    e.message ?: "Lỗi kết nối"
                )
            }
        }
    }

    /**
     * Reset delete state về Idle
     */
    fun resetDeleteState() {
        _deleteOrderState.value = DeleteOrderState.Idle
    }

    companion object {
        fun factory() = viewModelFactory {
            initializer {
                val orderRepository = OrderRepository()
                OrderViewModel(orderRepository)
            }
        }
    }
}