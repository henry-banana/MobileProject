package com.example.foodapp.pages.owner.vouchers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.di.RepositoryProvider
import com.example.foodapp.data.model.owner.voucher.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

class VouchersViewModel : ViewModel() {

    private val repository = RepositoryProvider.getVoucherRepository()

    private val _uiState = MutableStateFlow(VoucherUiState())
    val uiState: StateFlow<VoucherUiState> = _uiState.asStateFlow()

    init {
        loadVouchers()
    }

    /**
     * Load vouchers from API
     */
    fun loadVouchers(refresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoading = !refresh,
                    isRefreshing = refresh,
                    error = null
                ) 
            }

            val result = repository.getVouchers()

            result.fold(
                onSuccess = { vouchers ->
                    _uiState.update { 
                        it.copy(
                            vouchers = vouchers,
                            isLoading = false,
                            isRefreshing = false,
                            error = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = error.message ?: "Không thể tải voucher"
                        )
                    }
                }
            )
        }
    }

    /**
     * Refresh vouchers (pull-to-refresh)
     */
    fun refresh() {
        loadVouchers(refresh = true)
    }

    /**
     * Filter change handler
     */
    fun onFilterSelected(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    /**
     * Search query change handler
     */
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    /**
     * Get filtered vouchers (client-side filtering)
     */
    fun getFilteredVouchers(): List<Voucher> {
        val state = _uiState.value
        var filtered = state.vouchers

        // Apply filter
        filtered = when (state.selectedFilter) {
            VoucherUiState.FILTER_ACTIVE -> filtered.filter { it.isActive && !isExpired(it) }
            VoucherUiState.FILTER_INACTIVE -> filtered.filter { !it.isActive }
            VoucherUiState.FILTER_EXPIRED -> filtered.filter { isExpired(it) }
            else -> filtered
        }

        // Apply search
        val query = state.searchQuery.trim()
        if (query.isNotBlank()) {
            filtered = filtered.filter { voucher ->
                voucher.code.contains(query, ignoreCase = true) ||
                voucher.name?.contains(query, ignoreCase = true) == true ||
                voucher.description?.contains(query, ignoreCase = true) == true
            }
        }

        return filtered
    }

    /**
     * Check if voucher is expired
     */
    private fun isExpired(voucher: Voucher): Boolean {
        return try {
            val expiryTime = Instant.parse(voucher.validTo)
            Instant.now().isAfter(expiryTime)
        } catch (e: Exception) {
            false
        }
    }

    // ==================== Dialog Management ====================

    fun showCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = true) }
    }

    fun dismissCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = false) }
    }

    fun showEditDialog(voucher: Voucher) {
        _uiState.update { 
            it.copy(
                showEditDialog = true,
                selectedVoucher = voucher
            )
        }
    }

    fun dismissEditDialog() {
        _uiState.update { 
            it.copy(
                showEditDialog = false,
                selectedVoucher = null
            )
        }
    }

    fun showDeleteDialog(voucher: Voucher) {
        _uiState.update { 
            it.copy(
                showDeleteDialog = true,
                selectedVoucher = voucher
            )
        }
    }

    fun dismissDeleteDialog() {
        _uiState.update { 
            it.copy(
                showDeleteDialog = false,
                selectedVoucher = null
            )
        }
    }

    // ==================== CRUD Operations ====================

    /**
     * Create a new voucher
     */
    fun createVoucher(request: CreateVoucherRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }

            val result = repository.createVoucher(request)

            result.fold(
                onSuccess = {
                    _uiState.update { 
                        it.copy(
                            isActionLoading = false,
                            showCreateDialog = false,
                            successMessage = "Tạo voucher thành công"
                        )
                    }
                    loadVouchers()
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isActionLoading = false,
                            error = error.message ?: "Không thể tạo voucher"
                        )
                    }
                }
            )
        }
    }

    /**
     * Update a voucher
     */
    fun updateVoucher(voucherId: String, request: UpdateVoucherRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }

            val result = repository.updateVoucher(voucherId, request)

            result.fold(
                onSuccess = {
                    _uiState.update { 
                        it.copy(
                            isActionLoading = false,
                            showEditDialog = false,
                            selectedVoucher = null,
                            successMessage = "Cập nhật voucher thành công"
                        )
                    }
                    loadVouchers()
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isActionLoading = false,
                            error = error.message ?: "Không thể cập nhật voucher"
                        )
                    }
                }
            )
        }
    }

    /**
     * Toggle voucher status
     */
    fun toggleVoucherStatus(voucher: Voucher) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }

            val result = repository.updateVoucherStatus(voucher.id, !voucher.isActive)

            result.fold(
                onSuccess = {
                    _uiState.update { 
                        it.copy(
                            isActionLoading = false,
                            successMessage = if (voucher.isActive) "Đã tắt voucher" else "Đã kích hoạt voucher"
                        )
                    }
                    loadVouchers()
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isActionLoading = false,
                            error = error.message ?: "Không thể cập nhật trạng thái"
                        )
                    }
                }
            )
        }
    }

    /**
     * Delete a voucher
     */
    fun deleteVoucher() {
        val voucher = _uiState.value.selectedVoucher ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }

            val result = repository.deleteVoucher(voucher.id)

            result.fold(
                onSuccess = {
                    _uiState.update { 
                        it.copy(
                            isActionLoading = false,
                            showDeleteDialog = false,
                            selectedVoucher = null,
                            successMessage = "Đã xóa voucher"
                        )
                    }
                    loadVouchers()
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isActionLoading = false,
                            error = error.message ?: "Không thể xóa voucher"
                        )
                    }
                }
            )
        }
    }

    // ==================== Statistics ====================

    fun getTotalVouchers(): Int = _uiState.value.vouchers.size

    fun getActiveVouchers(): Int = _uiState.value.vouchers.count { it.isActive && !isExpired(it) }

    fun getExpiredVouchers(): Int = _uiState.value.vouchers.count { isExpired(it) }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
