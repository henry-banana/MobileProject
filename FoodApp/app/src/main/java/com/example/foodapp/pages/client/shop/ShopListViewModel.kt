package com.example.foodapp.pages.client.shop


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.remote.client.response.shop.*
import com.example.foodapp.data.repository.client.shop.ShopRepository
import kotlinx.coroutines.launch

// ============== SHOP STATES ==============
sealed class ShopListState {
    object Idle : ShopListState()
    object Loading : ShopListState()
    data class Success(val shops: List<ShopApiModel>) : ShopListState()
    data class Error(val message: String) : ShopListState()
    object Empty : ShopListState()
}

// ============== SHOP LIST VIEW MODEL ==============
class ShopListViewModel(
    private val shopRepository: ShopRepository
) : ViewModel() {

    private val _shopListState = MutableLiveData<ShopListState>(ShopListState.Idle)
    val shopListState: LiveData<ShopListState> = _shopListState

    private val _shops = MutableLiveData<List<ShopApiModel>>(emptyList())
    val shops: LiveData<List<ShopApiModel>> = _shops

    private val _currentPage = MutableLiveData(1)
    val currentPage: LiveData<Int> = _currentPage

    private val _totalPages = MutableLiveData(1)
    val totalPages: LiveData<Int> = _totalPages

    private val _totalItems = MutableLiveData(0)
    val totalItems: LiveData<Int> = _totalItems

    private val _hasMore = MutableLiveData(true)
    val hasMore: LiveData<Boolean> = _hasMore

    private val _currentFilters = MutableLiveData<GetAllShopsRequest>(GetAllShopsRequest())
    val currentFilters: LiveData<GetAllShopsRequest> = _currentFilters

    private val _isLoadingMore = MutableLiveData(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> = _searchQuery

    private val _statusFilter = MutableLiveData<String?>(null)
    val statusFilter: LiveData<String?> = _statusFilter

    // ============== SHOP FUNCTIONS ==============

    /**
     * Get all shops with pagination and filters
     */
    fun getShops(
        page: Int = 1,
        limit: Int = 20,
        status: String? = null,
        search: String? = null,
        forceRefresh: Boolean = false
    ) {
        if (forceRefresh) {
            _currentPage.value = page
            _hasMore.value = true
        }

        _shopListState.value = ShopListState.Loading
        _searchQuery.value = search ?: ""
        _statusFilter.value = status

        val filters = GetAllShopsRequest(
            page = page,
            limit = limit,
            status = status,
            search = search
        )
        _currentFilters.value = filters

        viewModelScope.launch {
            try {
                val result = shopRepository.getAllShops(
                    page = page,
                    limit = limit,
                    status = status,
                    search = search
                )

                when (result) {
                    is ApiResult.Success<*> -> {
                        val shopsData = result.data as? ShopsData
                        if (shopsData != null) {
                            val shopsList = shopsData.shops
                            val total = shopsData.total
                            val pageNum = shopsData.page
                            val limitNum = shopsData.limit

                            _totalItems.value = total
                            _currentPage.value = pageNum
                            _totalPages.value = if (limitNum > 0) {
                                kotlin.math.ceil(total.toDouble() / limitNum.toDouble()).toInt()
                            } else {
                                1
                            }
                            _hasMore.value = pageNum < _totalPages.value!!

                            if (page == 1 || forceRefresh) {
                                _shops.value = shopsList
                            } else {
                                val currentList = _shops.value ?: emptyList()
                                _shops.value = currentList + shopsList
                            }

                            if (shopsList.isNotEmpty()) {
                                _shopListState.value = ShopListState.Success(_shops.value ?: emptyList())
                            } else {
                                if (_shops.value?.isEmpty() == true) {
                                    _shopListState.value = ShopListState.Empty
                                }
                            }
                        } else {
                            _shopListState.value = ShopListState.Error("Dữ liệu không hợp lệ")
                            _hasMore.value = false
                        }
                    }
                    is ApiResult.Failure -> {
                        _shopListState.value = ShopListState.Error(
                            result.exception.message ?: "Lỗi không xác định"
                        )
                        _hasMore.value = false
                    }
                }
            } catch (e: Exception) {
                _shopListState.value = ShopListState.Error(
                    e.message ?: "Lỗi kết nối"
                )
                _hasMore.value = false
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    /**
     * Search shops by keyword
     */
    fun searchShops(query: String) {
        _searchQuery.value = query
        getShops(
            page = 1,
            search = if (query.isNotEmpty()) query else null,
            forceRefresh = true
        )
    }

    /**
     * Filter shops by status
     */
    fun filterByStatus(status: String?) {
        _statusFilter.value = status
        getShops(
            page = 1,
            status = status,
            forceRefresh = true
        )
    }

    /**
     * Load more shops for pagination
     */
    fun loadMoreShops() {
        val currentPage = _currentPage.value ?: 1
        val totalPages = _totalPages.value ?: 1

        if (currentPage >= totalPages || _isLoadingMore.value == true) {
            _hasMore.value = false
            return
        }

        _isLoadingMore.value = true
        val currentFilters = _currentFilters.value ?: GetAllShopsRequest()

        getShops(
            page = currentPage + 1,
            limit = currentFilters.limit,
            status = currentFilters.status,
            search = currentFilters.search
        )
    }

    /**
     * Clear search and filters
     */
    fun clearFilters() {
        _searchQuery.value = ""
        _statusFilter.value = null
        getShops(forceRefresh = true)
    }

    /**
     * Refresh shop list
     */
    fun refresh() {
        val currentFilters = _currentFilters.value ?: GetAllShopsRequest()
        getShops(
            page = 1,
            limit = currentFilters.limit,
            status = currentFilters.status,
            search = currentFilters.search,
            forceRefresh = true
        )
    }

    /**
     * Reset to initial state
     */
    fun reset() {
        _shops.value = emptyList()
        _shopListState.value = ShopListState.Idle
        _currentPage.value = 1
        _totalPages.value = 1
        _totalItems.value = 0
        _hasMore.value = true
        _currentFilters.value = GetAllShopsRequest()
        _isLoadingMore.value = false
        _searchQuery.value = ""
        _statusFilter.value = null
    }

    companion object {
        fun factory(shopRepository: ShopRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ShopListViewModel(shopRepository) as T
                }
            }
        }
    }
}