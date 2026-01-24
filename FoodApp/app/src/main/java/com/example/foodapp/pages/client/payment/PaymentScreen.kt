package com.example.foodapp.pages.client.payment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.foodapp.data.model.client.DeliveryAddress
import com.example.foodapp.data.model.shared.product.Product
import com.example.foodapp.data.remote.client.response.order.ApiResult
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

// Bảng màu
private val PrimaryOrange = Color(0xFFFF6B35)
private val PrimaryOrangeLight = Color(0xFFFF8C5A)
private val AccentGreen = Color(0xFF4CAF50)
private val AccentPurple = Color(0xFF7B68EE)
private val BackgroundGray = Color(0xFFF8F9FA)
private val CardWhite = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF6B7280)
private val BorderLight = Color(0xFFE5E7EB)
private val SelectedBg = Color(0xFFFFF4ED)

@Composable
fun PaymentScreen(
    products: List<Product>,
    quantities: List<Int>,
    onBackPressed: () -> Unit,
    onOrderPlaced: (ApiResult<com.example.foodapp.data.remote.client.response.order.OrderApiModel>) -> Unit
) {
    val viewModel: PaymentViewModel = viewModel(factory = PaymentViewModel.getFactory())
    var showAddressDialog by remember { mutableStateOf(false) }
    var showOrderError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Lấy các state từ ViewModel
    val cartItems by viewModel.cartItems.collectAsState()
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isPlacingOrder by viewModel.isPlacingOrder.collectAsState()
    val totalPrice by viewModel.totalPrice.collectAsState()
    val addresses by viewModel.addresses.collectAsState()
    val selectedAddress by viewModel.selectedAddress.collectAsState()
    val addressError by viewModel.addressError.collectAsState()
    val orderError by viewModel.orderError.collectAsState()

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

    // Hiển thị Snackbar lỗi
    if (!showOrderError.isNullOrBlank()) {
        val errorMessage = showOrderError!!
        LaunchedEffect(errorMessage) {
            // Có thể thêm Snackbar ở đây nếu cần
            println("Order error: $errorMessage")
            // Sau 3 giây tự động ẩn lỗi
            kotlinx.coroutines.delay(3000)
            showOrderError = null
        }
    }

    Scaffold(
        topBar = { PaymentTopBar(onBackPressed = onBackPressed) },
        bottomBar = {
            PaymentBottomBar(
                totalPrice = totalPrice,
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
                        discount = 0.0
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



@Composable
fun AddressSelectionDialog(
    addresses: List<DeliveryAddress>,
    selectedAddress: DeliveryAddress?,
    onAddressSelected: (DeliveryAddress) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Chọn địa chỉ giao hàng",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column {
                if (addresses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOff,
                                contentDescription = null,
                                tint = TextSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Chưa có địa chỉ nào",
                                fontSize = 16.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(addresses) { address ->
                            AddressItem(
                                address = address,
                                isSelected = address.id == selectedAddress?.id,
                                onClick = { onAddressSelected(address) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Đóng",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

@Composable
fun AddressItem(
    address: DeliveryAddress,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) SelectedBg else CardWhite,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) PrimaryOrange else BorderLight
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row - Label và badge mặc định
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = address.label,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    if (address.isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(
                            containerColor = AccentGreen.copy(alpha = 0.1f),
                            contentColor = AccentGreen,
                        ) {
                            Text("Mặc định", fontSize = 10.sp)
                        }
                    }
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Đã chọn",
                        tint = AccentGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Địa chỉ đầy đủ
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = address.fullAddress,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    lineHeight = 20.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Thông tin tòa nhà/phòng nếu có - SỬA Ở ĐÂY
            val buildingInfo = buildString {
                if (!address.building.isNullOrBlank()) {
                    append(address.building)
                }
                if (!address.room.isNullOrBlank()) {
                    if (!address.building.isNullOrBlank()) append(" - ")
                    append("Phòng ${address.room}")
                }
            }

            if (buildingInfo.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Apartment,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = buildingInfo,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Ghi chú nếu có - SỬA Ở ĐÂY
            if (!address.note.isNullOrBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Note,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = address.note!!,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun DeliveryInfoSectionUpdated(
    selectedAddress: DeliveryAddress?,
    hasAddresses: Boolean,
    onChangeAddress: () -> Unit,
    onFetchAddress: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PrimaryOrange.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = PrimaryOrange,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Địa chỉ giao hàng",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (!hasAddresses) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Chưa có địa chỉ nào",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Nút refresh địa chỉ
                    IconButton(
                        onClick = onFetchAddress,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Làm mới",
                            tint = TextSecondary
                        )
                    }

                    TextButton(onClick = onChangeAddress) {
                        Text(
                            text = if (hasAddresses) "Thay đổi" else "Thêm địa chỉ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = BorderLight, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Hiển thị địa chỉ đã chọn hoặc thông báo
            if (selectedAddress != null) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(PrimaryOrange.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = selectedAddress.label,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryOrange
                            )
                        }

                        if (selectedAddress.isDefault) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AccentGreen.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Mặc định",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AccentGreen
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = selectedAddress.fullAddress,
                        fontSize = 15.sp,
                        color = TextPrimary,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))


                    Text(
                        text = selectedAddress.buildingAndRoom,
                        fontSize = 15.sp,
                        color = TextPrimary,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = selectedAddress.building.toString(),
                        fontSize = 15.sp,
                        color = TextPrimary,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium


                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOff,
                        contentDescription = null,
                        tint = TextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Vui lòng chọn địa chỉ giao hàng",
                        fontSize = 16.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorMessage(
    message: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFFEBEE),
        border = BorderStroke(1.dp, Color(0xFFF44336).copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color(0xFFD32F2F),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// Các composable khác giữ nguyên...
@Composable
fun ProductListSection(
    items: List<CartItem>,
    onQuantityIncrease: (String) -> Unit,
    onQuantityDecrease: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AccentGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingBag,
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Thông tin đơn hàng",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            items.forEachIndexed { index, cartItem ->
                ProductInfoRow(
                    product = cartItem.product,
                    quantity = cartItem.quantity,
                    onQuantityIncrease = { onQuantityIncrease(cartItem.product.id) },
                    onQuantityDecrease = { onQuantityDecrease(cartItem.product.id) }
                )
                if (index < items.size - 1) {
                    Divider(
                        color = BorderLight,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ProductInfoRow(
    product: Product,
    quantity: Int,
    onQuantityIncrease: () -> Unit,
    onQuantityDecrease: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        AsyncImage(
            model = product.imageUrl,
            contentDescription = product.name,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))


        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = product.price,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryOrange
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        QuantitySelector(
            quantity = quantity,
            onIncrease = onQuantityIncrease,
            onDecrease = onQuantityDecrease
        )
    }
}

@Composable
fun QuantitySelector(
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = BackgroundGray,
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(2.dp)
        ) {
            IconButton(
                onClick = onDecrease,
                modifier = Modifier.size(36.dp),
                enabled = quantity > 1
            ) {
                Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = "Giảm",
                    tint = if (quantity > 1) PrimaryOrange else TextSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = quantity.toString(),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.widthIn(min = 32.dp),
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = onIncrease,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PrimaryOrange.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Tăng",
                    tint = PrimaryOrange,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentTopBar(onBackPressed: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Thanh toán",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 0.5.sp
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = PrimaryOrange
        )
    )
}

@Composable
fun PaymentMethodSection(
    selectedMethod: Int,
    onMethodSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AccentPurple.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Payment,
                        contentDescription = null,
                        tint = AccentPurple,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Phương thức thanh toán",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            // Lựa chọn phương thức thanh toán
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PaymentMethodItem(
                    title = "Thanh toán khi nhận hàng",
                    description = "Trả tiền mặt cho shipper",
                    icon = Icons.Filled.Money,
                    iconColor = AccentGreen,
                    isSelected = selectedMethod == 0,
                    onClick = { onMethodSelected(0) }
                )

                PaymentMethodItem(
                    title = "Ví điện tử MoMo",
                    description = "Thanh toán an toàn qua MoMo",
                    icon = Icons.Filled.AccountBalanceWallet,
                    iconColor = Color(0xFFAE2070), // Momo color
                    isSelected = selectedMethod == 1,
                    onClick = { onMethodSelected(1) }
                )
            }
        }
    }
}

@Composable
fun PaymentMethodItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) SelectedBg else Color.Transparent,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) PrimaryOrange else BorderLight
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(26.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = PrimaryOrange,
                    unselectedColor = BorderLight
                )
            )
        }
    }
}

@Composable
fun OrderSummarySection(
    productPrice: Double,
    deliveryFee: Double,
    discount: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Receipt,
                    contentDescription = null,
                    tint = PrimaryOrange,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Chi tiết thanh toán",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            PriceDetailRow(label = "Tạm tính", value = productPrice)
            Spacer(modifier = Modifier.height(10.dp))
            PriceDetailRow(label = "Phí giao hàng", value = deliveryFee)

            if (discount > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                PriceDetailRow(
                    label = "Giảm giá",
                    value = -discount,
                    valueColor = AccentGreen
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = BorderLight, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            val total = productPrice + deliveryFee - discount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tổng cộng",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = formatPrice(total),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryOrange
                )
            }
        }
    }
}

@Composable
fun PriceDetailRow(
    label: String,
    value: Double,
    valueColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 15.sp, color = TextSecondary)
        Text(text = formatPrice(value), fontSize = 16.sp, color = valueColor, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun PaymentBottomBar(
    totalPrice: Double,
    isLoading: Boolean,
    onPlaceOrder: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = CardWhite
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Tổng cộng",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Text(
                    text = formatPrice(totalPrice + 15000.0), // Giả sử phí ship cố định
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryOrange
                )
            }

            Button(
                onClick = onPlaceOrder,
                enabled = !isLoading,
                modifier = Modifier
                    .height(52.dp)
                    .fillMaxWidth(0.6f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                } else {
                    Text(
                        text = "Đặt hàng",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatPrice(price: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return format.format(price).replace(" ₫", "đ")
}