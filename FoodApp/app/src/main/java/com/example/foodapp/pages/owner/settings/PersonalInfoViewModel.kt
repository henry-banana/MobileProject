package com.example.foodapp.pages.owner.settings

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.di.RepositoryProvider
import com.example.foodapp.data.model.user.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

/**
 * UI State cho Personal Info Screen
 */
data class PersonalInfoUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEditing: Boolean = false,
    
    // Profile data
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val phone: String = "",
    val avatarUrl: String = "",
    val role: String = "",
    val addresses: List<Address> = emptyList(),
    
    // Messages
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel cho Personal Info Screen
 * Sử dụng RealUserProfileRepository để gọi API
 */
class PersonalInfoViewModel : ViewModel() {

    companion object {
        private const val TAG = "PersonalInfoViewModel"
    }

    private val repository = RepositoryProvider.getUserProfileRepository()

    private val _uiState = MutableStateFlow(PersonalInfoUiState())
    val uiState: StateFlow<PersonalInfoUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    /**
     * Load profile từ API
     */
    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = repository.getProfile()

            result.onSuccess { profile ->
                Log.d(TAG, "✅ Loaded profile: ${profile.displayName}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        id = profile.id,
                        displayName = profile.displayName,
                        email = profile.email,
                        phone = profile.phone ?: "",
                        avatarUrl = profile.avatarUrl ?: "",
                        role = profile.role,
                        addresses = profile.addresses ?: emptyList()
                    )
                }
            }.onFailure { error ->
                Log.e(TAG, "❌ Failed to load profile", error)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Không thể tải thông tin: ${error.message}"
                    )
                }
            }
        }
    }

    /**
     * Toggle edit mode
     */
    fun toggleEditMode() {
        _uiState.update { it.copy(isEditing = !it.isEditing) }
    }

    /**
     * Cancel editing
     */
    fun cancelEdit() {
        _uiState.update { it.copy(isEditing = false) }
        loadProfile() // Reload to discard changes
    }

    /**
     * Update display name locally
     */
    fun updateDisplayName(name: String) {
        _uiState.update { it.copy(displayName = name) }
    }

    /**
     * Update phone locally
     */
    fun updatePhone(phone: String) {
        _uiState.update { it.copy(phone = phone) }
    }

    /**
     * Save profile to API
     */
    fun saveProfile() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            val result = repository.updateProfile(
                displayName = state.displayName,
                phone = state.phone.ifBlank { null }
            )

            result.onSuccess { profile ->
                Log.d(TAG, "✅ Profile saved")
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isEditing = false,
                        displayName = profile.displayName,
                        phone = profile.phone ?: "",
                        successMessage = "Đã lưu thông tin thành công"
                    )
                }
            }.onFailure { error ->
                Log.e(TAG, "❌ Failed to save profile", error)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Không thể lưu: ${error.message}"
                    )
                }
            }
        }
    }

    /**
     * Upload avatar from Uri
     */
    fun uploadAvatar(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            try {
                // Convert Uri to File
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val tempFile = File.createTempFile("avatar", ".jpg", context.cacheDir)
                inputStream?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                val result = repository.uploadAvatar(tempFile)

                result.onSuccess { avatarUrl ->
                    Log.d(TAG, "✅ Avatar uploaded: $avatarUrl")
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            avatarUrl = avatarUrl,
                            successMessage = "Đã cập nhật ảnh đại diện"
                        )
                    }
                }.onFailure { error ->
                    Log.e(TAG, "❌ Failed to upload avatar", error)
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = "Không thể tải ảnh: ${error.message}"
                        )
                    }
                }

                // Cleanup temp file
                tempFile.delete()
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception uploading avatar", e)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Lỗi: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Clear messages
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
