package com.example.foodapp.pages.owner.vouchers

import com.example.foodapp.data.model.owner.voucher.Voucher

/**
 * UI State for Vouchers Screen
 */
data class VoucherUiState(
    // Loading states
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isActionLoading: Boolean = false,
    
    // Data
    val vouchers: List<Voucher> = emptyList(),
    
    // Filters
    val selectedFilter: String = FILTER_ALL,
    val searchQuery: String = "",
    
    // Dialog states
    val showCreateDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val selectedVoucher: Voucher? = null,
    
    // Error
    val error: String? = null,
    val successMessage: String? = null
) {
    companion object {
        const val FILTER_ALL = "Tất cả"
        const val FILTER_ACTIVE = "Đang hoạt động"
        const val FILTER_INACTIVE = "Đã tắt"
        const val FILTER_EXPIRED = "Hết hạn"
        
        val FILTER_OPTIONS = listOf(FILTER_ALL, FILTER_ACTIVE, FILTER_INACTIVE, FILTER_EXPIRED)
    }
}
