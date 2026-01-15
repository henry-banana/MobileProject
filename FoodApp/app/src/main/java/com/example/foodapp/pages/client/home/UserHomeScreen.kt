package com.example.foodapp.presentation.view.user.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.foodapp.data.model.shared.product.Product
import com.example.foodapp.pages.client.home.HomeViewModel
import com.example.foodapp.pages.client.home.ProductState
import com.example.foodapp.pages.client.home.UserNameState
import com.example.foodapp.pages.client.components.UserBottomNav
import com.example.foodapp.pages.client.components.UserCategoryList
import com.example.foodapp.pages.client.components.UserProductCard

@Composable
fun UserHomeScreen(
    navController: NavHostController,
    onProductClick: (String) -> Unit, // Đã sửa thành String (ID)
    onProfileClick: () -> Unit
) {
    val context = LocalContext.current

    // Khởi tạo ViewModel
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.factory(context)
    )

    // Quan sát các state từ ViewModel
    val nameState by viewModel.userNameState.observeAsState(UserNameState.Idle)
    val productState by viewModel.productState.observeAsState(ProductState.Idle)
    val products by viewModel.products.observeAsState(emptyList())
    val isLoadingMore by viewModel.isLoadingMore.observeAsState(false)
    val hasMore by viewModel.hasMore.observeAsState(true)

    // Gọi API khi vào màn hình
    LaunchedEffect(Unit) {
        viewModel.fetchUserName()
        viewModel.getProducts()
    }

    // Giao diện chính
    UserHomeContent(
        navController = navController,
        nameState = nameState,
        productState = productState,
        products = products,
        isLoadingMore = isLoadingMore,
        hasMore = hasMore,
        onProductClick = onProductClick,
        onProfileClick = onProfileClick,
        onSearch = { query -> viewModel.searchProducts(query) },
        onRefresh = { viewModel.refresh() },
        onLoadMore = { viewModel.loadMoreProducts() }
    )
}

@Composable
fun UserHomeContent(
    navController: NavHostController,
    nameState: UserNameState,
    productState: ProductState,
    products: List<Product>,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onProductClick: (String) -> Unit, // Đã sửa thành String
    onProfileClick: () -> Unit,
    onSearch: (String) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit
) {
    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            UserBottomNav(
                navController = navController,
                onProfileClick = onProfileClick
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(8.dp)
        ) {
            // Header và Search
            item(span = { GridItemSpan(2) }) {
                Column {
                    SimpleUserHeader(nameState = nameState)
                    UserSearchBarWithCallback(onSearch = onSearch)
                    UserCategoryList()
                    Text(
                        text = "Món ăn phổ biến",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Trạng thái hiển thị danh sách sản phẩm
            when (productState) {
                is ProductState.Loading -> {
                    item(span = { GridItemSpan(2) }) {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is ProductState.Error -> {
                    item(span = { GridItemSpan(2) }) {
                        ErrorView(message = productState.message, onRetry = onRefresh)
                    }
                }
                is ProductState.Empty -> {
                    item(span = { GridItemSpan(2) }) {
                        EmptyView(onRefresh = onRefresh)
                    }
                }
                is ProductState.Success -> {
                    // Danh sách sản phẩm chính
                    items(products) { product ->
                        UserProductCard(
                            product = product,
                            onClick = { onProductClick(product.id) } // Chỉ truyền ID khi click
                        )
                    }

                    // Nút "Xem thêm" hoặc Loading khi load phân trang
                    item(span = { GridItemSpan(2) }) {
                        LoadMoreSection(
                            isLoadingMore = isLoadingMore,
                            hasMore = hasMore,
                            onLoadMore = onLoadMore
                        )
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun SimpleUserHeader(nameState: UserNameState) {
    val userName = when (nameState) {
        is UserNameState.Success -> nameState.userName
        is UserNameState.Loading -> "Đang tải..."
        else -> "Khách"
    }
    Text(
        text = "Xin chào, $userName!",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun UserSearchBarWithCallback(onSearch: (String) -> Unit) {
    var searchText by remember { mutableStateOf("") }
    OutlinedTextField(
        value = searchText,
        onValueChange = { searchText = it },
        placeholder = { Text("Tìm kiếm món ăn...") },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        trailingIcon = {
            IconButton(onClick = { onSearch(searchText) }) {
                Icon(Icons.Default.Search, contentDescription = "Tìm kiếm")
            }
        }
    )
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Lỗi: $message")
        Button(onClick = onRetry) { Text("Thử lại") }
    }
}

@Composable
fun EmptyView(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Không có sản phẩm nào")
        Button(onClick = onRefresh) { Text("Làm mới") }
    }
}

@Composable
fun LoadMoreSection(isLoadingMore: Boolean, hasMore: Boolean, onLoadMore: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
        if (isLoadingMore) {
            CircularProgressIndicator(modifier = Modifier.size(30.dp))
        } else if (hasMore) {
            TextButton(onClick = onLoadMore) {
                Text("Xem thêm sản phẩm")
            }
        }
    }
}