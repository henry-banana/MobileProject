package com.example.foodapp.pages.client.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.data.model.shared.product.Product
import com.example.foodapp.data.remote.client.response.order.ApiResult
import com.example.foodapp.data.remote.client.response.order.OrderApiModel
import kotlinx.coroutines.launch
import com.example.foodapp.pages.client.components.payment.*
import com.example.foodapp.ui.theme.*

@Composable
fun PaymentScreen(
    products: List<Product>,
    quantities: List<Int>,
    onBackPressed: () -> Unit,
    onOrderPlaced: (ApiResult<OrderApiModel>) -> Unit
) {
    val viewModel: PaymentViewModel = viewModel(factory = PaymentViewModel.getFactory())
    var showAddressDialog by remember { mutableStateOf(false) }
    var showOrderError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Lấy các state từ ViewModel
    val cartItems by viewModel.cartItems.collectAsState()
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsState()
    val isPlacingOrder by viewModel.isPlacingOrder.collectAsState()
    val totalPrice by viewModel.totalPrice.collectAsState()
    val discountAmount by viewModel.discountAmount.collectAsState()
    val finalPrice by viewModel.finalPrice.collectAsState()
    val addresses by viewModel.addresses.collectAsState()
    val selectedAddress by viewModel.selectedAddress.collectAsState()
    val addressError by viewModel.addressError.collectAsState()
    val orderError by viewModel.orderError.collectAsState()
    val vouchers by viewModel.vouchers.collectAsState()
    val selectedVoucher by viewModel.selectedVoucher.collectAsState()
    val isLoadingVouchers by viewModel.isLoadingVouchers.collectAsState()

    // Khởi tạo ViewModel
    LaunchedEffect(products, quantities) {
        viewModel.initializeWithProductsAndQuantities(products, quantities)
        viewModel.fetchAddress()
    }

    // Theo dõi lỗi order
    LaunchedEffect(orderError) {
        if (!orderError.isNullOrBlank()) {
            showOrderError = orderError
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
            PaymentBottomBar(
                totalPrice = finalPrice,
                isLoading = isPlacingOrder,
                onPlaceOrder = {
                    coroutineScope.launch {
                        // GỌI API ĐẶT ORDER TRỰC TIẾP
                        val result = viewModel.placeOrder()
                        // Trả kết quả về cho màn hình cha
                        onOrderPlaced(result)
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

                item {
                    // Hiển thị thông báo lỗi nếu có
                    addressError?.let { error ->
                        ErrorMessage(message = error)
                        Spacer(modifier = Modifier.height(8.dp))
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
                        deliveryFee = 15000.0,
                        discount = discountAmount
                    )
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }

            // Hiển thị lỗi order nếu có
            if (!showOrderError.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    ErrorMessage(message = showOrderError!!)
                }
            }
        }
    }
}