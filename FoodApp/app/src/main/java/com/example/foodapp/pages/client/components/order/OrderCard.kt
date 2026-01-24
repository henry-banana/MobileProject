package com.example.foodapp.pages.client.components.order

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.data.remote.client.response.order.OrderPreviewApiModel

@Composable
fun OrderCard(
    order: OrderPreviewApiModel,
    onOrderClick: (String) -> Unit = {},
    onDeleteClick: (String) -> Unit = {},
    isDeleting: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header với màu nền gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = "Cửa hàng",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(24.dp)
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = order.shopName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color(0xFF212121)
                            )
                            Text(
                                text = "#${order.orderNumber}",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )
                        }
                    }

                    // Nút xóa (chỉ hiển thị cho đơn hàng có thể xóa)
                    if (order.status == "PENDING" || order.status == "CANCELLED") {
                        IconButton(
                            onClick = { onDeleteClick(order.id) },
                            enabled = !isDeleting,
                            modifier = Modifier.size(40.dp)
                        ) {
                            if (isDeleting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Xóa đơn hàng",
                                    tint = Color(0xFFF44336)
                                )
                            }
                        }
                    }
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOrderClick(order.id) }
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Status chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusChip(
                        text = when (order.status) {
                            "PENDING" -> "Đang chờ"
                            "CONFIRMED" -> "Đã xác nhận"
                            "PREPARING" -> "Đang chuẩn bị"
                            "SHIPPING" -> "Đang giao"
                            "DELIVERED" -> "Đã giao"
                            "CANCELLED" -> "Đã hủy"
                            else -> order.status
                        },
                        color = when (order.status) {
                            "PENDING" -> Color(0xFFFF9800)
                            "CONFIRMED" -> Color(0xFF2196F3)
                            "PREPARING" -> Color(0xFF9C27B0)
                            "SHIPPING" -> Color(0xFF4CAF50)
                            "DELIVERED" -> Color(0xFF4CAF50)
                            "CANCELLED" -> Color(0xFFF44336)
                            else -> Color.Gray
                        }
                    )

                    StatusChip(
                        text = when (order.paymentStatus) {
                            "UNPAID" -> "Chưa thanh toán"
                            "PAID" -> "Đã thanh toán"
                            "FAILED" -> "Thất bại"
                            "REFUNDED" -> "Đã hoàn tiền"
                            else -> order.paymentStatus
                        },
                        color = when (order.paymentStatus) {
                            "UNPAID" -> Color(0xFFFF5722)
                            "PAID" -> Color(0xFF4CAF50)
                            "FAILED" -> Color(0xFFF44336)
                            "REFUNDED" -> Color(0xFF9E9E9E)
                            else -> Color.Gray
                        }
                    )
                }

                // Items preview với background
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFAFAFA),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        order.itemsPreview.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                                        modifier = Modifier.wrapContentSize()
                                    ) {
                                        Text(
                                            text = "${item.quantity}x",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4CAF50),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = item.productName,
                                        fontSize = 14.sp,
                                        color = Color(0xFF424242),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = formatPrice(item.subtotal),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF212121)
                                )
                            }
                        }

                        if (order.itemsPreviewCount > order.itemsPreview.size) {
                            Text(
                                text = "+ ${order.itemsPreviewCount - order.itemsPreview.size} món khác",
                                fontSize = 12.sp,
                                color = Color(0xFF757575),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                HorizontalDivider(
                    color = Color(0xFFE0E0E0),
                    thickness = 1.dp
                )

                // Footer info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Tổng cộng",
                            fontSize = 13.sp,
                            color = Color(0xFF757575),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatPrice(order.total),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = "Thời gian",
                            tint = Color(0xFF757575),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = formatDate(order.createdAt),
                            fontSize = 13.sp,
                            color = Color(0xFF757575),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Delivery address
                order.deliveryAddress?.let { address ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF5F5F5),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Địa chỉ",
                                tint = Color(0xFFFF5722),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = address.label,
                                fontSize = 13.sp,
                                color = Color(0xFF424242),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(
    text: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.12f),
        modifier = Modifier.wrapContentSize()
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

fun formatDate(dateString: String): String {
    return try {
        if (dateString.contains("T")) {
            val parts = dateString.split("T")
            if (parts.isNotEmpty()) {
                parts[0]
            } else {
                dateString
            }
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}

fun formatPrice(price: Double): String {
    return try {
        String.format("%,.0f", price) + "đ"
    } catch (e: Exception) {
        "0đ"
    }
}