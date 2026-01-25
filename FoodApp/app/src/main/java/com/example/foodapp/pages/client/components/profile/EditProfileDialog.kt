package com.example.foodapp.pages.client.components.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.data.model.Client
import com.example.foodapp.pages.client.profile.*

@Composable
fun EditProfileDialog(
    currentUser: Client?,
    viewModel: ProfileViewModel,
    updateState: UpdateProfileState?,
    onDismiss: () -> Unit
) {
    // State cho form
    var displayName by remember { mutableStateOf(currentUser?.fullName ?: "") }
    var phone by remember { mutableStateOf(currentUser?.phone ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isLoading = updateState is UpdateProfileState.Loading

    // Validate phone number
    fun isValidPhoneNumber(phone: String): Boolean {
        val phoneRegex = "^[0-9]{10,11}$".toRegex()
        return phone.matches(phoneRegex)
    }

    AlertDialog(
        onDismissRequest = {
            if (!isLoading) onDismiss()
        },
        title = {
            Text(
                text = "Chỉnh sửa thông tin",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hiển thị lỗi nếu có
                errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Display Name field
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Họ và tên") },
                    placeholder = { Text("Nhập họ và tên") },
                    leadingIcon = {
                        Icon(Icons.Filled.Person, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    isError = displayName.length > 50,
                    supportingText = {
                        if (displayName.length > 50) {
                            Text(
                                text = "Tên không được quá 50 ký tự",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                // Phone field
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Số điện thoại") },
                    placeholder = { Text("Nhập số điện thoại") },
                    leadingIcon = {
                        Icon(Icons.Filled.Phone, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    isError = phone.isNotBlank() && !isValidPhoneNumber(phone),
                    supportingText = {
                        if (phone.isNotBlank() && !isValidPhoneNumber(phone)) {
                            Text(
                                text = "Số điện thoại không hợp lệ (10-11 số)",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                // Hiển thị lỗi từ ViewModel
                if (updateState is UpdateProfileState.Error) {
                    Text(
                        text = updateState.message,
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Validate
                    if (displayName.isBlank() && phone.isBlank()) {
                        errorMessage = "Vui lòng nhập thông tin cần cập nhật"
                        return@TextButton
                    }

                    if (phone.isNotBlank() && !isValidPhoneNumber(phone)) {
                        errorMessage = "Số điện thoại không hợp lệ"
                        return@TextButton
                    }

                    if (displayName.length > 50) {
                        errorMessage = "Tên không được quá 50 ký tự"
                        return@TextButton
                    }

                    errorMessage = null

                    // Gọi update
                    viewModel.updateProfile(
                        displayName = displayName.takeIf { it.isNotBlank() && it != currentUser?.fullName },
                        phone = phone.takeIf { it.isNotBlank() && it != currentUser?.phone }
                    )
                },
                enabled = !isLoading &&
                        ((displayName.isNotBlank() && displayName != currentUser?.fullName) ||
                                (phone.isNotBlank() && phone != currentUser?.phone))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đang lưu...")
                } else {
                    Text("Lưu thay đổi")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Hủy")
            }
        }
    )
}