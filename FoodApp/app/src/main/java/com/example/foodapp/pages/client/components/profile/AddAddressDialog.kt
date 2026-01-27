package com.example.foodapp.pages.client.components.profile


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.foodapp.pages.client.profile.ProfileViewModel
import com.example.foodapp.pages.client.profile.*

@Composable
fun AddAddressDialog(
    viewModel: ProfileViewModel,
    createAddressState: CreateAddressState?,
    onDismiss: () -> Unit
) {
    // State cho form địa chỉ
    var label by remember { mutableStateOf("") }
    var fullAddress by remember { mutableStateOf("") }
    var building by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isDefault by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isLoading = createAddressState is CreateAddressState.Loading

    AlertDialog(
        onDismissRequest = {
            if (!isLoading) onDismiss()
        },
        title = {
            Text(
                text = "Thêm địa chỉ mới",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Hiển thị lỗi nếu có
                errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }

                // Label field
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Tên địa chỉ") },
                    placeholder = { Text("VD: Nhà riêng, Công ty, ...") },
                    leadingIcon = {
                        Icon(Icons.Filled.Label, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    isError = label.isBlank(),
                    supportingText = {
                        if (label.isBlank()) {
                            Text(
                                text = "Vui lòng nhập tên địa chỉ",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                // Full Address field
                OutlinedTextField(
                    value = fullAddress,
                    onValueChange = { fullAddress = it },
                    label = { Text("Địa chỉ đầy đủ") },
                    placeholder = { Text("Nhập số nhà, tên đường, phường, quận, thành phố") },
                    leadingIcon = {
                        Icon(Icons.Filled.LocationOn, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = false,
                    minLines = 3,
                    maxLines = 4,
                    isError = fullAddress.isBlank(),
                    supportingText = {
                        if (fullAddress.isBlank()) {
                            Text(
                                text = "Vui lòng nhập địa chỉ đầy đủ",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                // Building field
                OutlinedTextField(
                    value = building,
                    onValueChange = { building = it },
                    label = { Text("Tòa nhà/Chung cư") },
                    placeholder = { Text("VD: Tòa nhà A, Chung cư B") },
                    leadingIcon = {
                        Icon(Icons.Filled.Apartment, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true
                )

                // Room field
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Phòng/Số căn hộ") },
                    placeholder = { Text("VD: Phòng 101, Căn hộ 302") },
                    leadingIcon = {
                        Icon(Icons.Filled.DoorFront, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true
                )

                // Note field
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Ghi chú") },
                    placeholder = { Text("VD: Giao hàng ban ngày, gọi trước 30 phút") },
                    leadingIcon = {
                        Icon(Icons.Filled.Note, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = false,
                    minLines = 2,
                    maxLines = 3
                )

                // Checkbox for default address
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isDefault,
                        onCheckedChange = { isDefault = it },
                        enabled = !isLoading
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Đặt làm địa chỉ mặc định",
                        fontSize = 14.sp
                    )
                }

                // Hiển thị lỗi từ ViewModel
                if (createAddressState is CreateAddressState.Error) {
                    Text(
                        text = createAddressState.message,
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
                    if (label.isBlank()) {
                        errorMessage = "Vui lòng nhập tên địa chỉ"
                        return@TextButton
                    }

                    if (fullAddress.isBlank()) {
                        errorMessage = "Vui lòng nhập địa chỉ đầy đủ"
                        return@TextButton
                    }

                    errorMessage = null

                    // Gọi tạo địa chỉ
                    viewModel.createAddress(
                        label = label,
                        fullAddress = fullAddress,
                        building = building.takeIf { it.isNotBlank() },
                        room = room.takeIf { it.isNotBlank() },
                        note = note.takeIf { it.isNotBlank() },
                        isDefault = isDefault
                    )
                },
                enabled = !isLoading && label.isNotBlank() && fullAddress.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đang thêm...")
                } else {
                    Text("Thêm địa chỉ")
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