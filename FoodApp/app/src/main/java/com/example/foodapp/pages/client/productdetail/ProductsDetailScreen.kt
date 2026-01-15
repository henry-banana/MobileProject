package com.example.foodapp.pages.client.productdetail

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.foodapp.data.model.shared.product.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProductDetailScreen(
    productId: String,
    onBackPressed: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: ProductDetailViewModel = viewModel(
        factory = ProductDetailViewModel.factory(context)
    )

    val productDetailState by viewModel.productDetailState.observeAsState(ProductDetailState.Idle)
    val product by viewModel.product.observeAsState(null)
    val favoriteState by viewModel.favoriteState.observeAsState(FavoriteState.Idle)

    // State cho số lượng
    var quantity by remember { mutableStateOf(1) }

    // State cho trạng thái yêu thích
    val isFavorite = remember(product) {
        mutableStateOf(product?.isFavorite ?: false)
    }

    // Load product detail khi vào màn hình
    LaunchedEffect(productId) {
        if (productId.isNotBlank()) {
            viewModel.getProductDetail(productId)
        }
    }

    // Cập nhật isFavorite khi product thay đổi
    LaunchedEffect(product) {
        product?.let {
            isFavorite.value = it.isFavorite
        }
    }

    // Xử lý khi có thay đổi từ favoriteState
    LaunchedEffect(favoriteState) {
        when (favoriteState) {
            is FavoriteState.Success -> {
                isFavorite.value = true
            }
            is FavoriteState.Error -> {
                // Xử lý lỗi, không thay đổi trạng thái
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            // SỬA: Custom TopAppBar với button quay lại và tiêu đề cùng hàng
            ProductDetailTopBar(
                onBackPressed = onBackPressed,
                isFavorite = isFavorite.value,
                isLoading = favoriteState == FavoriteState.Loading,
                onFavoriteClick = {
                    if (favoriteState != FavoriteState.Loading) {
                        productId.takeIf { it.isNotBlank() }?.let { id ->
                            viewModel.addToFavorites(id)
                        }
                    }
                }
            )
        },
        bottomBar = {
            // Bottom bar chứa số lượng và 2 button
            if (product != null) {
                ProductBottomBar(
                    product = product!!,
                    quantity = quantity,
                    onQuantityChange = { newQuantity -> quantity = newQuantity },
                    onAddToCart = {
                        // TODO: Xử lý thêm vào giỏ hàng
                        println("Thêm vào giỏ hàng: ${product!!.name} x $quantity")
                    },
                    onBuyNow = {
                        // TODO: Xử lý mua ngay
                        println("Mua ngay: ${product!!.name} x $quantity")
                    }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            when (val state = productDetailState) {
                is ProductDetailState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is ProductDetailState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Đã xảy ra lỗi")
                            Text(text = state.message)
                            Button(
                                onClick = { viewModel.getProductDetail(productId) },
                                modifier = Modifier.padding(top = 16.dp)
                            ) {
                                Text("Thử lại")
                            }

                            // Button quay lại trong trường hợp lỗi
                            Button(
                                onClick = onBackPressed,
                                modifier = Modifier.padding(top = 16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.LightGray,
                                    contentColor = Color.Black
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowBack,
                                        contentDescription = "Quay lại",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Quay lại")
                                }
                            }
                        }
                    }
                }
                is ProductDetailState.Success -> {
                    ProductDetailContent(
                        product = state.product,
                        isFavorite = isFavorite.value,
                        onFavoriteClick = {
                            productId.takeIf { it.isNotBlank() }?.let { id ->
                                viewModel.addToFavorites(id)
                            }
                        },
                        isLoading = favoriteState == FavoriteState.Loading,
                        onBackPressed = onBackPressed
                    )
                }
                else -> {
                    // ProductDetailState.Idle - không hiển thị gì
                }
            }
        }
    }
}

// SỬA: Custom TopAppBar với button quay lại kế bên tiêu đề
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailTopBar(
    onBackPressed: () -> Unit,
    isFavorite: Boolean,
    isLoading: Boolean,
    onFavoriteClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Button quay lại kế bên tiêu đề
                IconButton(
                    onClick = onBackPressed,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Chi tiết sản phẩm",
                    color = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFFFF9800)  // Màu cam giống bottom bar
        ),
        actions = {
            // Nút yêu thích bên phải
            FavoriteIconButton(
                isFavorite = isFavorite,
                isLoading = isLoading,
                onClick = onFavoriteClick
            )
        }
    )
}

// Các Composable khác giữ nguyên...
@Composable
fun FavoriteIconButton(
    isFavorite: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
        } else {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                contentDescription = if (isFavorite) "Bỏ yêu thích" else "Thêm vào yêu thích",
                tint = Color.White
            )
        }
    }
}

// ... Các hàm khác giữ nguyên ...

@Composable
fun ProductDetailContent(
    product: Product,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    isLoading: Boolean = false,
    onBackPressed: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Product Image với nút yêu thích
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            if (product.imageUrl != null && product.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(product.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Không có ảnh", color = Color.Gray)
                }
            }

            // Badge trạng thái (phía trên bên phải)
            if (!product.isAvailable) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Red.copy(alpha = 0.8f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "HẾT HÀNG",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Nút yêu thích trên ảnh (phía dưới bên phải)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 16.dp, end = 16.dp)
            ) {
                FavoriteIconOnImage(
                    isFavorite = isFavorite,
                    isLoading = isLoading,
                    onClick = onFavoriteClick
                )
            }
        }

        // Product Info
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = product.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Rating and sold count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${product.rating} (${product.totalRatings} đánh giá)",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Đã bán: ${product.soldCount}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Price
            Text(
                text = if (product.price is String) product.price
                else product.price.toString(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9800),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Description
            Text(
                text = "Mô tả",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = product.description ?: "Không có mô tả",
                fontSize = 16.sp,
                lineHeight = 24.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Shop Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Thông tin cửa hàng",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = product.shopName ?: "Không có thông tin",
                        fontSize = 16.sp
                    )
                }
            }

            // Preparation Time và trạng thái
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Preparation Time
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Timer,
                        contentDescription = "Thời gian chuẩn bị",
                        tint = Color(0xFF666666),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Chuẩn bị: ${product.preparationTime ?: 0} phút",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }

                // Availability status
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (product.isAvailable) Color(0xFF4CAF50)
                                else Color.Red
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (product.isAvailable) "Còn hàng" else "Hết hàng",
                        fontSize = 14.sp,
                        color = if (product.isAvailable) Color(0xFF4CAF50) else Color.Red
                    )
                }
            }

            // Khoảng trống để không bị bottom bar che
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// Nút yêu thích trên ảnh
@Composable
fun FavoriteIconOnImage(
    isFavorite: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape),
        color = Color.White.copy(alpha = 0.9f),
        shadowElevation = 4.dp
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                IconButton(
                    onClick = onClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                        contentDescription = if (isFavorite) "Bỏ yêu thích" else "Thêm vào yêu thích",
                        tint = if (isFavorite) Color.Red else Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

// Các hàm khác giữ nguyên...
@Composable
fun ProductBottomBar(
    product: Product,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    onAddToCart: () -> Unit,
    onBuyNow: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Phần chọn số lượng
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Số lượng:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(end = 12.dp)
                )

                // Container cho nút - + và số lượng
                Row(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clip(RoundedCornerShape(8.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Nút giảm (-)
                    IconButton(
                        onClick = {
                            if (quantity > 1) {
                                onQuantityChange(quantity - 1)
                            }
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = "Giảm số lượng",
                            tint = if (quantity > 1) Color(0xFF333333) else Color(0xFFCCCCCC)
                        )
                    }

                    // Số lượng
                    Box(
                        modifier = Modifier.width(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = quantity.toString(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Nút tăng (+)
                    IconButton(
                        onClick = {
                            // Có thể thêm giới hạn tối đa nếu cần
                            onQuantityChange(quantity + 1)
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Tăng số lượng",
                            tint = Color(0xFF333333)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Hai button bên phải
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Button Thêm vào giỏ hàng
                Button(
                    onClick = onAddToCart,
                    enabled = product.isAvailable,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFFFF9800)
                    ),
                    border = ButtonDefaults.outlinedButtonBorder,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = "Thêm vào giỏ",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Giỏ hàng",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Button Mua ngay
                Button(
                    onClick = onBuyNow,
                    enabled = product.isAvailable,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Mua ngay",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}