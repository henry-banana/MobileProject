package com.example.foodapp.pages.client.components.profile


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.data.remote.client.response.profile.AddressResponse

@Composable
fun AddressCard(
    addresses: List<AddressResponse>,
    onAddClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onSetDefaultClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header với icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Color(0xFFFFF3E0),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Địa chỉ giao hàng",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
            }

            // Danh sách địa chỉ
            if (addresses.isNotEmpty()) {
                addresses.forEachIndexed { index, address ->
                    AddressItem(
                        address = address,
                        onEditClick = { address.id?.let { onEditClick(it) } },
                        onDeleteClick = { address.id?.let { onDeleteClick(it) } },
                        onSetDefaultClick = { address.id?.let { onSetDefaultClick(it) } },
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    if (index < addresses.size - 1) {
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color(0xFFEEEEEE),
                            thickness = 1.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            } else {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFFBDBDBD),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Chưa có địa chỉ giao hàng",
                        fontSize = 14.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Button thêm địa chỉ
            Button(
                onClick = onAddClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Thêm địa chỉ",
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Thêm địa chỉ mới",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun AddressItem(
    address: AddressResponse,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSetDefaultClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = address.label ?: "Địa chỉ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )

                        if (address.isDefault == true) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge(
                                containerColor = Color.Green.copy(alpha = 0.1f),
                                contentColor = Color.Green,
                            ) {
                                Text("Mặc định", fontSize = 10.sp)
                            }
                        }
                    }

                    Text(
                        text = address.fullAddress ?: "",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    // Hiển thị thông tin tòa nhà và phòng nếu có
                    if (!address.building.isNullOrBlank() || !address.room.isNullOrBlank()) {
                        Text(
                            text = "${address.building ?: ""} ${if (!address.room.isNullOrBlank()) "Phòng ${address.room}" else ""}".trim(),
                            fontSize = 13.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // Hiển thị ghi chú nếu có
                    if (!address.note.isNullOrBlank()) {
                        Text(
                            text = "📝 ${address.note}",
                            fontSize = 12.sp,
                            color = Color(0xFF888888),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Menu options
                Box {
                    var showMenu by remember { mutableStateOf(false) }

                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Tùy chọn",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        // Nút đặt mặc định
                        if (address.isDefault != true) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFFF9800),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Đặt làm mặc định")
                                    }
                                },
                                onClick = {
                                    onSetDefaultClick()
                                    showMenu = false
                                }
                            )
                        }

                        // Nút chỉnh sửa
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = null,
                                        tint = Color.Blue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Chỉnh sửa")
                                }
                            },
                            onClick = {
                                onEditClick()
                                showMenu = false
                            }
                        )

                        // Nút xóa
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = null,
                                        tint = Color.Red,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Xóa")
                                }
                            },
                            onClick = {
                                onDeleteClick()
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}