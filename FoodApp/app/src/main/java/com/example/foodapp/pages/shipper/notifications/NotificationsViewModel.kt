package com.example.foodapp.pages.shipper.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.di.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShipperNotificationsUiState(
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

class NotificationsViewModel : ViewModel() {
    private val repository = RepositoryProvider.getNotificationRepository()

    private val _uiState = MutableStateFlow(ShipperNotificationsUiState())
    val uiState: StateFlow<ShipperNotificationsUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.getNotifications(read = null, page = 1, limit = 50)
            result.fold(
                onSuccess = { data ->
                    _uiState.update {
                        it.copy(
                            notifications = data.items,
                            unreadCount = data.unreadCount,
                            isLoading = false,
                            error = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Không thể tải thông báo"
                        )
                    }
                }
            )
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            val result = repository.markAsRead(notificationId)
            result.fold(
                onSuccess = { updated ->
                    _uiState.update { state ->
                        val updatedList = state.notifications.map {
                            if (it.id == notificationId) updated else it
                        }
                        state.copy(
                            notifications = updatedList,
                            unreadCount = updatedList.count { !it.read }
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message ?: "Không thể đánh dấu đã đọc") }
                }
            )
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            val result = repository.markAllAsRead()
            result.fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            notifications = state.notifications.map { it.copy(read = true) },
                            unreadCount = 0
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message ?: "Không thể đánh dấu tất cả đã đọc") }
                }
            )
        }
    }
}
