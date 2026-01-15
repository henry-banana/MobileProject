package com.example.foodapp.pages.client.profile

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.foodapp.data.model.Client
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun UserProfileScreen(
    onBackClick: () -> Unit = {},
    onEditAddressClick: (String) -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.Factory(context)
    )
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // State cho các popup
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAddAddressDialog by remember { mutableStateOf(false) }

    // Observe state từ ViewModel
    val userState by viewModel.userState.observeAsState(ProfileState.Idle)
    val currentUser by viewModel.currentUser.observeAsState()
    val updateState by viewModel.updateState.observeAsState()
    val createAddressState by viewModel.createAddressState.observeAsState()

    // Xử lý khi update thành công
    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is UpdateProfileState.Success -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = state.message,
                        duration = SnackbarDuration.Short
                    )
                    showEditProfileDialog = false // Đóng dialog sau khi thành công
                }
            }
            is UpdateProfileState.Error -> {
                // Error được xử lý trong dialog
            }
            else -> {}
        }
    }

    // Xử lý khi thêm địa chỉ thành công
    LaunchedEffect(createAddressState) {
        when (val state = createAddressState) {
            is CreateAddressState.Success -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = state.message,
                        duration = SnackbarDuration.Short
                    )
                    showAddAddressDialog = false // Đóng dialog sau khi thành công
                }
                // Reset state
                viewModel.resetCreateAddressState()
            }
            is CreateAddressState.Error -> {
                // Error được xử lý trong dialog
            }
            else -> {}
        }
    }

    // Popup chỉnh sửa profile
    if (showEditProfileDialog) {
        EditProfileDialog(
            currentUser = currentUser,
            viewModel = viewModel,
            updateState = updateState,
            onDismiss = { showEditProfileDialog = false }
        )
    }

    // Popup thêm địa chỉ
    if (showAddAddressDialog) {
        AddAddressDialog(
            viewModel = viewModel,
            createAddressState = createAddressState,
            onDismiss = { showAddAddressDialog = false }
        )
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            ProfileTopBar(
                onBackClick = onBackClick,
                onRefreshClick = { viewModel.fetchUserData() }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        when (val state = userState) {
            is ProfileState.Loading -> {
                LoadingScreen(modifier = Modifier.padding(padding))
            }
            is ProfileState.Error -> {
                ErrorScreen(
                    errorMessage = state.message,
                    onRetryClick = { viewModel.fetchUserData() },
                    modifier = Modifier.padding(padding)
                )
            }
            is ProfileState.Success -> {
                currentUser?.let { user ->
                    ProfileContent(
                        user = user,
                        modifier = Modifier
                            .background(Color.White)
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState()),
                        onAddAddressClick = { showAddAddressDialog = true },
                        onEditAddressClick = onEditAddressClick,
                        onChangePasswordClick = onChangePasswordClick,
                        onEditProfileClick = { showEditProfileDialog = true }
                    )
                } ?: run {
                    EmptyScreen(
                        onRetryClick = { viewModel.fetchUserData() },
                        modifier = Modifier.padding(padding)
                    )
                }
            }
            is ProfileState.Idle -> {
                LoadingScreen(modifier = Modifier.padding(padding))
            }
        }
    }
}

// Dialog thêm địa chỉ mới
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
                        onCheckedChange = { isDefault = it }
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

// Dialog chỉnh sửa profile
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

                    errorMessage = null

                    // Gọi update
                    viewModel.updateProfile(
                        displayName = displayName.takeIf { it.isNotBlank() },
                        phone = phone.takeIf { it.isNotBlank() }
                    )
                },
                enabled = !isLoading && (displayName.isNotBlank() || phone.isNotBlank())
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

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Đang tải thông tin...")
        }
    }
}

@Composable
fun ErrorScreen(
    errorMessage: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = "Error",
                tint = Color.Red,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                color = Color.Gray,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetryClick
            ) {
                Text("Thử lại")
            }
        }
    }
}

@Composable
fun EmptyScreen(
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.PersonOff,
                contentDescription = "Không có dữ liệu",
                tint = Color.Gray,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Không có dữ liệu người dùng",
                color = Color.Gray,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetryClick
            ) {
                Text("Tải lại")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopBar(
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Hồ sơ cá nhân",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Quay lại"
                )
            }
        },
        actions = {
            IconButton(onClick = onRefreshClick) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Làm mới"
                )
            }
        }
    )
}

@Composable
fun ProfileContent(
    user: Client,
    modifier: Modifier = Modifier,
    onAddAddressClick: () -> Unit,
    onEditAddressClick: (String) -> Unit,
    onChangePasswordClick: () -> Unit,
    onEditProfileClick: () -> Unit
) {
    // Format ngày tham gia từ timestamp
    val joinDate = remember(user.createdAt) {
        try {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            formatter.format(user.createdAt)
        } catch (e: Exception) {
            "Không rõ"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        // Thêm tiêu đề và 4 button cho trạng thái đơn mua
        OrderStatusSection()

        // Thông tin cá nhân card
        PersonalInfoCard(
            user = user,
            onEditClick = onEditProfileClick
        )

        // Trạng thái tài khoản
        AccountStatusCard(user = user)

        // Địa chỉ - Chỉ hiển thị nếu có địa chỉ
        if (user.addresses.isNotEmpty()) {
            AddressCard(
                addresses = user.addresses,
                onAddClick = onAddAddressClick,
                onEditClick = onEditAddressClick
            )
        } else {
            EmptyAddressCard(onAddClick = onAddAddressClick)
        }

        // Thông tin tài khoản
        AccountInfoCard(
            role = user.role,
            joinDate = joinDate,
            isVerified = user.isVerify
        )

        // Các chức năng
        SettingsCard(
            onChangePasswordClick = onChangePasswordClick
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun OrderStatusSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Tiêu đề
            Text(
                text = "Trạng thái đơn mua",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // 4 button nằm ngang - 2 hàng 2 cột
            // Hàng 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Button 1: Chờ giao hàng
                OrderStatusButton(
                    icon = Icons.Filled.LocalShipping,
                    text = "Chờ giao hàng",
                    badgeCount = 5, // Số 5 như hình
                    modifier = Modifier.weight(1f)
                )

                // Button 2: Chờ Lấy món
                OrderStatusButton(
                    icon = Icons.Filled.RestaurantMenu,
                    text = "Chờ Lấy món",
                    badgeCount = null, // Không có số
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hàng 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Button 3: Chờ Xác Nhận
                OrderStatusButton(
                    icon = Icons.Filled.HourglassEmpty,
                    text = "Chờ Xác Nhận",
                    badgeCount = 3, // Số 3 như hình
                    modifier = Modifier.weight(1f)
                )

                // Button 4: Đánh giá
                OrderStatusButton(
                    icon = Icons.Filled.StarRate,
                    text = "Đánh giá",
                    badgeCount = null, // Không có số
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun OrderStatusButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    badgeCount: Int? = null,
    modifier: Modifier = Modifier
) {
    val backgroundColor = Color.White
    val iconColor = Color(0xFF4CAF50) // Màu xanh lá như hình

    Box(
        modifier = modifier
            .height(100.dp) // Chiều cao cố định
            .clip(RoundedCornerShape(8.dp)) // Bo góc nhỏ hơn
            .background(backgroundColor)
            .clickable {
                // TODO: Xử lý click cho từng button
            }
            .border(
                width = 1.dp,
                color = Color(0xFFE0E0E0), // Viền xám nhạt
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center // Toàn bộ nội dung nằm giữa
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            // Badge ở góc trái - sử dụng Box với contentAlignment = TopStart
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopStart // Badge ở góc trên bên trái
            ) {
                // Icon container nằm giữa
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = text,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Badge số lượng - nằm góc trên bên trái của button
                if (badgeCount != null) {
                    Box(
                        modifier = Modifier
                            .offset(x = (-4).dp, y = (-4).dp) // Dịch chuyển ra ngoài góc trái
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF5252)), // Màu đỏ như hình
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badgeCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Text nằm dưới icon
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333),
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun PersonalInfoCard(
    user: Client,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header với nút chỉnh sửa
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Thông tin cá nhân",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Chỉnh sửa",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Avatar và tên
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                // Hiển thị avatar từ URL nếu có
                if (user.imageAvatar.isNotEmpty() && user.imageAvatar.startsWith("http")) {
                    Image(
                        painter = rememberAsyncImagePainter(model = user.imageAvatar),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Avatar placeholder nếu không có URL
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Avatar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = user.fullName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user.email,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Divider(modifier = Modifier.fillMaxWidth())

            // Thông tin chi tiết
            ProfileInfoItem(
                icon = Icons.Filled.Email,
                title = "Email",
                value = user.email
            )

            Divider(modifier = Modifier.padding(start = 48.dp))

            ProfileInfoItem(
                icon = Icons.Filled.Person,
                title = "Họ và tên",
                value = user.fullName
            )

            Divider(modifier = Modifier.padding(start = 48.dp))

            ProfileInfoItem(
                icon = Icons.Filled.Phone,
                title = "Số điện thoại",
                value = user.phone.ifEmpty { "Chưa cập nhật" }
            )
        }
    }
}

@Composable
fun AccountStatusCard(user: Client) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Trạng thái tài khoản",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatusChip(
                    icon = Icons.Filled.Verified,
                    label = "Xác thực",
                    value = if (user.isVerify) "Đã xác thực" else "Chưa xác thực",
                    isActive = user.isVerify
                )

                StatusChip(
                    icon = Icons.Filled.CheckCircle,
                    label = "Trạng thái",
                    value = if (user.isVerify) "Hoạt động" else "Không hoạt động",
                    isActive = user.isVerify
                )
            }
        }
    }
}

@Composable
fun AddressCard(
    addresses: List<com.example.foodapp.data.model.client.DeliveryAddress>,
    onAddClick: () -> Unit,
    onEditClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Địa chỉ giao hàng",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            addresses.forEach { address ->
                AddressItem(
                    address = address,
                    onEditClick = { onEditClick(address.id) },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onAddClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Thêm địa chỉ",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thêm địa chỉ mới")
            }
        }
    }
}

@Composable
fun EmptyAddressCard(onAddClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOff,
                contentDescription = "Không có địa chỉ",
                tint = Color.Gray,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Chưa có địa chỉ nào",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onAddClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Thêm địa chỉ",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thêm địa chỉ đầu tiên")
            }
        }
    }
}

@Composable
fun AccountInfoCard(
    role: String,
    joinDate: String,
    isVerified: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Thông tin tài khoản",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            ProfileInfoItem(
                icon = Icons.Filled.Person,
                title = "Vai trò",
                value = when (role.lowercase()) {
                    "customer", "user" -> "Khách hàng"
                    "admin" -> "Quản trị viên"
                    else -> role
                }
            )

            Divider(modifier = Modifier.padding(start = 48.dp))

            ProfileInfoItem(
                icon = Icons.Filled.CalendarToday,
                title = "Ngày tham gia",
                value = joinDate
            )

            Divider(modifier = Modifier.padding(start = 48.dp))

            ProfileInfoItem(
                icon = Icons.Filled.Verified,
                title = "Xác thực",
                value = if (isVerified) "Đã xác thực" else "Chưa xác thực",
                valueColor = if (isVerified) Color.Green else Color.Blue
            )
        }
    }
}

@Composable
fun SettingsCard(
    onChangePasswordClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            // Cài đặt
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clickable { onChangePasswordClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Cài đặt",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Cài đặt",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }
        }
    }
}

@Composable
fun ProfileInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = valueColor,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun StatusChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    isActive: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) Color.Green.copy(alpha = 0.1f)
                    else Color.Gray.copy(alpha = 0.1f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.Green else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isActive) Color.Green else Color.Gray
        )
    }
}

@Composable
fun AddressItem(
    address: com.example.foodapp.data.model.client.DeliveryAddress,
    onEditClick: () -> Unit,
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = address.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                if (address.isDefault) {
                    Badge(
                        containerColor = Color.Green.copy(alpha = 0.1f),
                        contentColor = Color.Green,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text("Mặc định", fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Chỉnh sửa",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = address.address,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// Previews
@Preview(showBackground = true)
@Composable
fun ProfileMainScreenPreview() {
    MaterialTheme {
        UserProfileScreen(
            onBackClick = {},
            onEditAddressClick = {},
            onChangePasswordClick = {},
            onEditProfileClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingScreenPreview() {
    MaterialTheme {
        LoadingScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorScreenPreview() {
    MaterialTheme {
        ErrorScreen(
            errorMessage = "Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng.",
            onRetryClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OrderStatusSectionPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            OrderStatusSection()
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 200)
@Composable
fun OrderStatusButtonPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Button với badge:", fontWeight = FontWeight.Bold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OrderStatusButton(
                    icon = Icons.Filled.LocalShipping,
                    text = "Chờ giao hàng",
                    badgeCount = 5,
                    modifier = Modifier.weight(1f)
                )

                OrderStatusButton(
                    icon = Icons.Filled.RestaurantMenu,
                    text = "Chờ Lấy món",
                    badgeCount = null,
                    modifier = Modifier.weight(1f)
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Button không có badge:", fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OrderStatusButton(
                    icon = Icons.Filled.HourglassEmpty,
                    text = "Chờ Xác Nhận",
                    badgeCount = null,
                    modifier = Modifier.weight(1f)
                )

                OrderStatusButton(
                    icon = Icons.Filled.StarRate,
                    text = "Đánh giá",
                    badgeCount = null,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}