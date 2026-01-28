package com.example.foodapp.pages.client.payment

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.foodapp.data.model.shared.product.Product
import com.example.foodapp.data.remote.client.response.order.OrderApiModel
import com.example.foodapp.pages.client.components.payment.*
import com.example.foodapp.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    products: List<Product>,
    quantities: List<Int>,
    onBackPressed: () -> Unit,
    onPaymentSuccess: (OrderApiModel) -> Unit
) {
    val context = LocalContext.current
    val viewModel: PaymentViewModel = viewModel(factory = PaymentViewModel.factory(context = context))

    var showAddressDialog by remember { mutableStateOf(false) }
    var showPaymentError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Sử dụng observeAsState() để observe LiveData
    val cartItems by viewModel.cartItems.observeAsState(initial = emptyList())
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.observeAsState(initial = 0)
    val isProcessingPayment by viewModel.isProcessingPayment.observeAsState(initial = false)
    val totalPrice by viewModel.totalPrice.observeAsState(initial = 0.0)
    val discountAmount by viewModel.discountAmount.observeAsState(initial = 0.0)
    val finalPrice by viewModel.finalPrice.observeAsState(initial = 0.0)
    val addresses by viewModel.addresses.observeAsState(initial = emptyList())
    val selectedAddress by viewModel.selectedAddress.observeAsState()
    val addressError by viewModel.addressError.observeAsState()
    val paymentError by viewModel.paymentError.observeAsState()
    val vouchers by viewModel.vouchers.observeAsState(initial = emptyList())
    val selectedVoucher by viewModel.selectedVoucher.observeAsState()
    val isLoadingVouchers by viewModel.isLoadingVouchers.observeAsState(initial = false)
    val voucherError by viewModel.voucherError.observeAsState()
    val createdOrder by viewModel.createdOrder.observeAsState()

    // State cho QR code/bank transfer dialog
    val showBankTransferInfo by viewModel.showBankTransferInfo.observeAsState()

    // SỬA: Polling result bây giờ là OrderApiModel
    val pollingResult by viewModel.pollingResult.observeAsState()
    val pollingError by viewModel.pollingError.observeAsState()

    // State cho navigation với order
    val shouldNavigateWithOrder by viewModel.shouldNavigateWithOrder.observeAsState()

    // Kiểm tra xem có địa chỉ được chọn không
    val isAddressSelected = selectedAddress != null
    val hasAddresses = !addresses.isNullOrEmpty()

    // Khởi tạo ViewModel
    LaunchedEffect(products, quantities) {
        viewModel.initializeWithProductsAndQuantities(products, quantities)
    }

    // Theo dõi payment error
    LaunchedEffect(paymentError) {
        if (!paymentError.isNullOrBlank()) {
            showPaymentError = paymentError
        }
    }

    // Theo dõi polling error
    LaunchedEffect(pollingError) {
        if (!pollingError.isNullOrBlank()) {
            showPaymentError = pollingError
        }
    }

    // SỬA: Theo dõi polling result (cho SEPAY)
    LaunchedEffect(pollingResult) {
        pollingResult?.let { order ->
            // Chuyển trang với order
            onPaymentSuccess(order)
            viewModel.resetNavigation()
        }
    }

    // SỬA: Theo dõi shouldNavigateWithOrder (cho COD)
    LaunchedEffect(shouldNavigateWithOrder) {
        shouldNavigateWithOrder?.let { order ->
            // Chuyển trang với order
            onPaymentSuccess(order)
            viewModel.resetNavigation()
        }
    }

    // Hiển thị dialog chọn địa chỉ
    if (showAddressDialog) {
        AddressSelectionDialog(
            addresses = addresses,
            selectedAddress = selectedAddress,
            onAddressSelected = { address ->
                viewModel.selectAddress(address)
                showAddressDialog = false
            },
            onDismiss = { showAddressDialog = false }
        )
    }

    Scaffold(
        topBar = { PaymentTopBar(onBackPressed = onBackPressed) },
        bottomBar = {
            // SỬA: Thêm isAddressValid để kiểm tra địa chỉ
            PaymentBottomBar(
                totalPrice = finalPrice,
                isLoading = isProcessingPayment,
                isAddressValid = isAddressSelected,
                onPlaceOrder = {
                    coroutineScope.launch {
                        // GỌI API TẠO ORDER VÀ PAYMENT
                        viewModel.createOrderAndPayment()
                    }
                }
            )
        },
        containerColor = BackgroundGray
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item { Spacer(modifier = Modifier.height(12.dp)) }

                // Hiển thị address error
                item {
                    addressError?.let { error ->
                        if (error?.isNotBlank() == true) {
                            ErrorMessage(message = error)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                // Hiển thị voucher error
                item {
                    voucherError?.let { error ->
                        if (error?.isNotBlank() == true) {
                            ErrorMessage(message = error)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                item {
                    DeliveryInfoSectionUpdated(
                        selectedAddress = selectedAddress,
                        hasAddresses = viewModel.hasAddresses(),
                        onChangeAddress = { showAddressDialog = true },
                        onFetchAddress = { viewModel.fetchAddress() }
                    )
                }

                // Hiển thị cảnh báo nếu chưa chọn địa chỉ
                item {
                    if (!isAddressSelected && hasAddresses) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFF3CD)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Warning",
                                        tint = Color(0xFF856404),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Vui lòng chọn địa chỉ giao hàng để tiếp tục",
                                        color = Color(0xFF856404),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }

                item {
                    ProductListSection(
                        items = cartItems,
                        onQuantityIncrease = viewModel::increaseQuantity,
                        onQuantityDecrease = viewModel::decreaseQuantity
                    )
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }

                item {
                    VoucherDropdown(
                        vouchers = vouchers,
                        selectedVoucher = selectedVoucher,
                        isLoading = isLoadingVouchers,
                        onVoucherSelected = viewModel::selectVoucher
                    )
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }

                item {
                    PaymentMethodSection(
                        selectedMethod = selectedPaymentMethod,
                        onMethodSelected = viewModel::selectPaymentMethod
                    )
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }

                item {
                    OrderSummarySection(
                        productPrice = totalPrice,
                        discount = discountAmount
                    )
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }

            // Hiển thị payment error dialog
            if (!showPaymentError.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    AlertDialog(
                        onDismissRequest = {
                            showPaymentError = null
                            viewModel.clearPaymentError()
                            viewModel.clearPollingError()
                        },
                        title = { Text(text = "Thông báo") },
                        text = { Text(text = showPaymentError!!) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showPaymentError = null
                                    viewModel.clearPaymentError()
                                    viewModel.clearPollingError()
                                }
                            ) {
                                Text("OK")
                            }
                        }
                    )
                }
            }

            // Hiển thị QR Code/Bank Transfer Dialog khi thanh toán SEPAY
            if (showBankTransferInfo != null) {
                SimpleBankTransferDialog(
                    bankTransferInfo = showBankTransferInfo!!,
                    onDismiss = { viewModel.closeBankTransferDialog() }
                )
            }

            // Loading overlay khi đang xử lý
            if (isProcessingPayment) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = Color.White)
                        Text(
                            text = "Đang tạo đơn hàng...",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * PaymentBottomBar đã sửa để disable khi không có địa chỉ
 */
@Composable
fun PaymentBottomBar(
    totalPrice: Double,
    isLoading: Boolean,
    isAddressValid: Boolean, // Thêm tham số mới để kiểm tra địa chỉ
    onPlaceOrder: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Tổng thanh toán",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "${totalPrice.formatVND()}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor
                )
            }

            // SỬA: Thêm điều kiện disable dựa trên isAddressValid
            Button(
                onClick = onPlaceOrder,
                modifier = Modifier
                    .height(48.dp)
                    .width(140.dp),
                enabled = !isLoading && isAddressValid, // Sửa: thêm điều kiện isAddressValid
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isLoading && isAddressValid) PrimaryColor else Color.Gray,
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "ĐẶT HÀNG",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

/**
 * Dialog đơn giản chỉ hiển thị QR code và thông tin thanh toán
 */
@Composable
fun SimpleBankTransferDialog(
    bankTransferInfo: PaymentViewModel.BankTransferInfo,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            // Không cho phép đóng dialog khi chưa thanh toán
        },
        title = {
            Text(
                text = "Quét QR Code để thanh toán",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hiển thị QR Code
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = bankTransferInfo.qrCodeUrl,
                        contentDescription = "QR Code thanh toán",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Hiển thị thông tin tài khoản
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Hoặc chuyển khoản thủ công:",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )

                    InfoRow(label = "Số tài khoản:", value = bankTransferInfo.accountNumber)
                    InfoRow(label = "Tên tài khoản:", value = bankTransferInfo.accountName)
                    InfoRow(label = "Ngân hàng:", value = "MB (${bankTransferInfo.bankCode})")
                    InfoRow(label = "Số tiền:", value = "${bankTransferInfo.amount.formatVND()}")
                    InfoRow(label = "Nội dung:", value = bankTransferInfo.sepayContent)
                }

                // Lưu ý
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryColor.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = "⚠️ Lưu ý: Vui lòng chuyển khoản đúng nội dung và số tiền trên để hệ thống tự động xác nhận thanh toán.",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }

                // Thông báo nhỏ về polling ngầm
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.LightGray.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = "💡 Hệ thống đang tự động kiểm tra thanh toán. Bạn có thể đóng cửa sổ này sau khi chuyển khoản.",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColor
                )
            ) {
                Text(text = "Tôi đã chuyển khoản")
            }
        },
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}

fun Double.formatVND(): String {
    return String.format("%,.0fđ", this).replace(",", ".")
}

fun Int.formatVND(): String {
    return String.format("%,dđ", this).replace(",", ".")
}