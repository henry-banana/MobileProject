package com.example.foodapp.pages.client.orderdetail


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.foodapp.data.remote.client.response.order.OrderApiModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    onBack: () -> Unit,
    orderId: String,
) {

    val context = LocalContext.current
    val viewModel: OrderDetailViewModel = viewModel(factory = OrderDetailViewModel.factory(context))
    val orderDetailState by viewModel.orderDetailState.observeAsState(OrderDetailState.Idle)
    val cancelOrderState by viewModel.cancelOrderState.observeAsState(CancelOrderState.Idle)
    val currentOrder by viewModel.currentOrder.observeAsState()

    var showCancelDialog by remember { mutableStateOf(false) }

    // Load order detail when screen opens
    LaunchedEffect(orderId) {
        viewModel.fetchOrderDetail(orderId)
    }

    // Handle cancel order state
    LaunchedEffect(cancelOrderState) {
        when (cancelOrderState) {
            is CancelOrderState.Success -> {
                viewModel.resetCancelState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chi tiết đơn hàng",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { // <- Đang sử dụng callback onBack
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF212121),
                    navigationIconContentColor = Color(0xFF212121)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (orderDetailState) {
                is OrderDetailState.Loading -> {
                    LoadingView()
                }
                is OrderDetailState.Success -> {
                    currentOrder?.let { order ->
                        OrderDetailContent(
                            order = order,
                            onCancelOrder = { showCancelDialog = true }
                        )
                    }
                }
                is OrderDetailState.Error -> {
                    ErrorView(
                        message = (orderDetailState as OrderDetailState.Error).message,
                        onRetry = { viewModel.fetchOrderDetail(orderId) }
                    )
                }
                is OrderDetailState.Empty -> {
                    EmptyView()
                }
                else -> {}
            }

            // Cancel order loading overlay
            if (cancelOrderState is CancelOrderState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }

        // Cancel Dialog
        if (showCancelDialog) {
            AlertDialog(
                onDismissRequest = { showCancelDialog = false },
                title = {
                    Text(
                        text = "Hủy đơn hàng",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text("Bạn có chắc chắn muốn hủy đơn hàng này không?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.cancelOrder(orderId)
                            showCancelDialog = false
                        }
                    ) {
                        Text("Hủy đơn", color = Color(0xFFF44336))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCancelDialog = false }) {
                        Text("Đóng")
                    }
                }
            )
        }

        // Cancel error dialog
        if (cancelOrderState is CancelOrderState.Error) {
            AlertDialog(
                onDismissRequest = { viewModel.resetCancelState() },
                title = {
                    Text(
                        text = "Lỗi",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text((cancelOrderState as CancelOrderState.Error).message)
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.resetCancelState() }) {
                        Text("Đóng")
                    }
                }
            )
        }
    }
}

@Composable
fun OrderDetailContent(
    order: OrderApiModel,
    onCancelOrder: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
    ) {
        // Order Status Timeline
        OrderStatusTimeline(order)

        Spacer(modifier = Modifier.height(12.dp))

        // Shop Info
        ShopInfoSection(order)

        Spacer(modifier = Modifier.height(12.dp))

        // Order Items
        OrderItemsSection(order)

        Spacer(modifier = Modifier.height(12.dp))

        // Delivery Info
        DeliveryInfoSection(order)

        Spacer(modifier = Modifier.height(12.dp))

        // Payment Info
        PaymentInfoSection(order)

        Spacer(modifier = Modifier.height(12.dp))

        // Order Summary
        OrderSummarySection(order)

        Spacer(modifier = Modifier.height(12.dp))

        // Cancel Button (only show if order can be cancelled)
        if (order.status == "PENDING" || order.status == "CONFIRMED") {
            Button(
                onClick = onCancelOrder,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF44336)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Hủy đơn hàng",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun OrderStatusTimeline(order: OrderApiModel) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Trạng thái đơn hàng",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )

            Spacer(modifier = Modifier.height(16.dp))

            val steps = listOf(
                TimelineStep("PENDING", "Đơn hàng đã đặt", Icons.Default.ShoppingCart, order.createdAt),
                TimelineStep("CONFIRMED", "Đã xác nhận", Icons.Default.CheckCircle, order.confirmedAt),
                TimelineStep("PREPARING", "Đang chuẩn bị", Icons.Default.Restaurant, order.preparingAt),
                TimelineStep("SHIPPING", "Đang giao hàng", Icons.Default.LocalShipping, order.shippingAt),
                TimelineStep("DELIVERED", "Đã giao hàng", Icons.Default.Done, order.deliveredAt)
            )

            val currentStatusIndex = steps.indexOfFirst { it.status == order.status }

            steps.forEachIndexed { index, step ->
                val isCompleted = index <= currentStatusIndex
                val isActive = index == currentStatusIndex

                TimelineItem(
                    step = step,
                    isCompleted = isCompleted,
                    isActive = isActive,
                    isLast = index == steps.size - 1
                )
            }
        }
    }
}

data class TimelineStep(
    val status: String,
    val label: String,
    val icon: ImageVector,
    val timestamp: String?
)

@Composable
fun TimelineItem(
    step: TimelineStep,
    isCompleted: Boolean,
    isActive: Boolean,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Surface(
                shape = RoundedCornerShape(50),
                color = when {
                    isActive -> Color(0xFF4CAF50)
                    isCompleted -> Color(0xFF4CAF50)
                    else -> Color(0xFFE0E0E0)
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = step.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(8.dp)
                )
            }

            // Line
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(
                            if (isCompleted) Color(0xFF4CAF50)
                            else Color(0xFFE0E0E0)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = step.label,
                fontSize = 14.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isCompleted) Color(0xFF212121) else Color(0xFF9E9E9E)
            )

            step.timestamp?.let { timestamp ->
                Text(
                    text = formatDateTime(timestamp),
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
            }

            if (!isLast) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ShopInfoSection(order: OrderApiModel) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                modifier = Modifier.size(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = order.shopName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = "Mã đơn: #${order.orderNumber}",
                    fontSize = 13.sp,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

@Composable
fun OrderItemsSection(order: OrderApiModel) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Món đã đặt",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )

            Spacer(modifier = Modifier.height(12.dp))

            order.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "${item.quantity}x",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = item.productName,
                            fontSize = 14.sp,
                            color = Color(0xFF424242)
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
        }
    }
}

@Composable
fun DeliveryInfoSection(order: OrderApiModel) {
    order.deliveryAddress?.let { address ->
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Thông tin giao hàng",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFFFF5722),
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = address.label,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF212121)
                        )
                        Text(
                            text = address.fullAddress,
                            fontSize = 13.sp,
                            color = Color(0xFF757575)
                        )
                        address.building?.let {
                            Text(
                                text = "Tòa: $it",
                                fontSize = 13.sp,
                                color = Color(0xFF757575)
                            )
                        }
                        address.room?.let {
                            Text(
                                text = "Phòng: $it",
                                fontSize = 13.sp,
                                color = Color(0xFF757575)
                            )
                        }
                        address.note?.let {
                            if (it.isNotEmpty()) {
                                Text(
                                    text = "Ghi chú: $it",
                                    fontSize = 13.sp,
                                    color = Color(0xFF757575),
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentInfoSection(order: OrderApiModel) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Phương thức thanh toán",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (order.paymentMethod == "COD")
                            Icons.Default.AccountBalanceWallet
                        else
                            Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50)
                    )
                    Text(
                        text = when (order.paymentMethod) {
                            "COD" -> "Thanh toán khi nhận hàng"
                            "MOMO" -> "Ví MoMo"
                            "BANKING" -> "Chuyển khoản ngân hàng"
                            else -> order.paymentMethod
                        },
                        fontSize = 14.sp,
                        color = Color(0xFF424242)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = when (order.paymentStatus) {
                        "PAID" -> Color(0xFF4CAF50).copy(alpha = 0.12f)
                        "UNPAID" -> Color(0xFFFF5722).copy(alpha = 0.12f)
                        else -> Color.Gray.copy(alpha = 0.12f)
                    }
                ) {
                    Text(
                        text = when (order.paymentStatus) {
                            "PAID" -> "Đã thanh toán"
                            "UNPAID" -> "Chưa thanh toán"
                            "FAILED" -> "Thất bại"
                            "REFUNDED" -> "Đã hoàn tiền"
                            else -> order.paymentStatus
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when (order.paymentStatus) {
                            "PAID" -> Color(0xFF4CAF50)
                            "UNPAID" -> Color(0xFFFF5722)
                            else -> Color.Gray
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OrderSummarySection(order: OrderApiModel) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Tổng kết đơn hàng",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )

            Spacer(modifier = Modifier.height(12.dp))

            SummaryRow("Tạm tính", formatPrice(order.subtotal))
            SummaryRow("Phí vận chuyển", formatPrice(order.shipFee))
            if (order.discount > 0) {
                SummaryRow(
                    "Giảm giá",
                    "-${formatPrice(order.discount)}",
                    valueColor = Color(0xFFF44336)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFE0E0E0))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tổng cộng",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = formatPrice(order.total),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF424242)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF757575)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFF4CAF50)
        )
    }
}

@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = Color(0xFFF44336),
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = message,
                fontSize = 16.sp,
                color = Color(0xFF757575),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("Thử lại")
            }
        }
    }
}

@Composable
fun EmptyView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = null,
                tint = Color(0xFF9E9E9E),
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "Không tìm thấy đơn hàng",
                fontSize = 16.sp,
                color = Color(0xFF757575),
                textAlign = TextAlign.Center
            )
        }
    }
}

// Helper functions
fun formatPrice(price: Double): String {
    return try {
        String.format("%,.0f", price) + "đ"
    } catch (e: Exception) {
        "0đ"
    }
}

fun formatDateTime(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString)
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}