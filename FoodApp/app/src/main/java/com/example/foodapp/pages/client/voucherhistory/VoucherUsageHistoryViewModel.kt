package com.example.foodapp.pages.client.voucherhistory

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.foodapp.data.repository.firebase.AuthManager
import com.example.foodapp.data.remote.client.response.voucher.ApiResult
import com.example.foodapp.data.remote.client.response.voucher.VoucherUsageHistoryApiModel
import com.example.foodapp.data.remote.client.response.voucher.VoucherUsageHistoryResponse
import com.example.foodapp.data.repository.client.voucher.VoucherRepository
import kotlinx.coroutines.launch

// ============== VOUCHER HISTORY STATES ==============

sealed class VoucherHistoryState {
    object Idle : VoucherHistoryState()
    object Loading : VoucherHistoryState()
    data class Success(
        val history: List<VoucherUsageHistoryApiModel>,
        val currentPage: Int,
        val totalPages: Int,
        val totalItems: Int,
        val hasMore: Boolean
    ) : VoucherHistoryState()
    data class Error(val message: String) : VoucherHistoryState()
    object Empty : VoucherHistoryState()
}

// ============== VOUCHER HISTORY VIEW MODEL ==============

class VoucherHistoryViewModel(
    private val voucherRepository: VoucherRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _voucherHistoryState = MutableLiveData<VoucherHistoryState>(VoucherHistoryState.Idle)
    val voucherHistoryState: LiveData<VoucherHistoryState> = _voucherHistoryState

    private val _voucherHistory = MutableLiveData<List<VoucherUsageHistoryApiModel>>(emptyList())
    val voucherHistory: LiveData<List<VoucherUsageHistoryApiModel>> = _voucherHistory

    private val _currentPage = MutableLiveData(1)
    val currentPage: LiveData<Int> = _currentPage

    private val _totalPages = MutableLiveData(1)
    val totalPages: LiveData<Int> = _totalPages

    private val _totalItems = MutableLiveData(0)
    val totalItems: LiveData<Int> = _totalItems

    private val _hasMore = MutableLiveData(true)
    val hasMore: LiveData<Boolean> = _hasMore

    private val _isLoadingMore = MutableLiveData(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    // ============== MAIN FUNCTIONS ==============

    fun fetchVoucherHistory(page: Int = 1, forceRefresh: Boolean = false) {
        // Lấy token từ AuthManager
        val token = authManager.getCurrentToken()
        if (token == null) {
            _voucherHistoryState.value = VoucherHistoryState.Error("Chưa đăng nhập")
            return
        }

        if (forceRefresh) {
            _currentPage.value = page
            _hasMore.value = true
        }

        if (page == 1) {
            _voucherHistoryState.value = VoucherHistoryState.Loading
            _isLoadingMore.value = false
        } else {
            _isLoadingMore.value = true
        }

        viewModelScope.launch {
            try {
                val result = voucherRepository.getVoucherUsageHistory(
                    token = token,
                    page = page
                )

                when (result) {
                    is ApiResult.Success -> {
                        val response = result.data

                        // Cập nhật thông tin phân trang
                        _currentPage.value = response.page
                        _totalPages.value = response.totalPages
                        _totalItems.value = response.total
                        _hasMore.value = response.hasMore

                        val historyItems = response.items

                        if (page == 1 || forceRefresh) {
                            // Refresh hoặc trang đầu tiên
                            _voucherHistory.value = historyItems
                        } else {
                            // Load more
                            val currentList = _voucherHistory.value ?: emptyList()
                            _voucherHistory.value = currentList + historyItems
                        }

                        if (historyItems.isNotEmpty()) {
                            _voucherHistoryState.value = VoucherHistoryState.Success(
                                history = _voucherHistory.value ?: emptyList(),
                                currentPage = response.page,
                                totalPages = response.totalPages,
                                totalItems = response.total,
                                hasMore = response.hasMore
                            )
                        } else {
                            if (page == 1) {
                                _voucherHistoryState.value = VoucherHistoryState.Empty
                            } else {
                                // Không có thêm dữ liệu
                                _voucherHistoryState.value = VoucherHistoryState.Success(
                                    history = _voucherHistory.value ?: emptyList(),
                                    currentPage = response.page,
                                    totalPages = response.totalPages,
                                    totalItems = response.total,
                                    hasMore = false
                                )
                                _hasMore.value = false
                            }
                        }

                        println("DEBUG: [VoucherHistoryVM] Page ${response.page}/${response.totalPages}, Items: ${historyItems.size}, Total: ${response.total}, HasMore: ${response.hasMore}")
                    }

                    is ApiResult.Failure -> {
                        if (page == 1) {
                            _voucherHistoryState.value = VoucherHistoryState.Error(
                                result.exception.message ?: "Lỗi không xác định"
                            )
                        } else {
                            // Giữ lại dữ liệu đã load được nếu lỗi ở trang sau
                            val currentHistory = _voucherHistory.value ?: emptyList()
                            _voucherHistoryState.value = VoucherHistoryState.Success(
                                history = currentHistory,
                                currentPage = _currentPage.value ?: 1,
                                totalPages = _totalPages.value ?: 1,
                                totalItems = _totalItems.value ?: currentHistory.size,
                                hasMore = false
                            )
                            _hasMore.value = false
                        }
                        println("DEBUG: [VoucherHistoryVM] Load failed: ${result.exception.message}")
                    }
                }
            } catch (e: Exception) {
                if (page == 1) {
                    _voucherHistoryState.value = VoucherHistoryState.Error(
                        e.message ?: "Lỗi kết nối"
                    )
                } else {
                    val currentHistory = _voucherHistory.value ?: emptyList()
                    _voucherHistoryState.value = VoucherHistoryState.Success(
                        history = currentHistory,
                        currentPage = _currentPage.value ?: 1,
                        totalPages = _totalPages.value ?: 1,
                        totalItems = _totalItems.value ?: currentHistory.size,
                        hasMore = false
                    )
                    _hasMore.value = false
                }
                println("DEBUG: [VoucherHistoryVM] Exception: ${e.message}")
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    fun loadMoreVoucherHistory() {
        if (_isLoadingMore.value == true || _hasMore.value == false) {
            return
        }

        val nextPage = (_currentPage.value ?: 1) + 1
        if (nextPage > (_totalPages.value ?: 1)) {
            _hasMore.value = false
            return
        }

        fetchVoucherHistory(page = nextPage)
    }

    // ============== UTILITY FUNCTIONS ==============

    fun refresh() {
        println("DEBUG: [VoucherHistoryVM] Refresh called")
        fetchVoucherHistory(page = 1, forceRefresh = true)
    }

    fun reset() {
        _voucherHistoryState.value = VoucherHistoryState.Idle
        _voucherHistory.value = emptyList()
        _currentPage.value = 1
        _totalPages.value = 1
        _totalItems.value = 0
        _hasMore.value = true
        _isLoadingMore.value = false
    }

    companion object {
        fun factory(context: Context) = viewModelFactory {
            initializer {
                val voucherRepository = VoucherRepository()
                val authManager = AuthManager(context)
                VoucherHistoryViewModel(voucherRepository, authManager)
            }
        }
    }
}