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
import com.example.foodapp.data.remote.client.response.profile.AddressResponse
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun UserProfileScreen(
    onBackClick: () -> Unit = {},
    onEditAddressClick: (String) -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onOrderButtonClick: () -> Unit = {} // THÊM CALLBACK CHO BUTTON ĐƠN MUA
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
    var showEditAddressDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showSetDefaultConfirmDialog by remember { mutableStateOf(false) }

    var addressToEdit by remember { mutableStateOf<AddressResponse?>(null) }
    var addressToDelete by remember { mutableStateOf<String?>(null) }
    var addressToSetDefault by remember { mutableStateOf<String?>(null) }

    // Observe state từ ViewModel
    val userState by viewModel.userState.observeAsState()
    val currentUser by viewModel.currentUser.observeAsState()
    val addresses by viewModel.addresses.observeAsState(emptyList())
    val updateState by viewModel.updateState.observeAsState()
    val createAddressState by viewModel.createAddressState.observeAsState()
    val deleteAddressState by viewModel.deleteAddressState.observeAsState()
    val updateAddressState by viewModel.updateAddressState.observeAsState()
    val setDefaultAddressState by viewModel.setDefaultAddressState.observeAsState()

    // Xử lý khi update profile thành công
    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is UpdateProfileState.Success -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = state.message,
                        duration = SnackbarDuration.Short
                    )
                    showEditProfileDialog = false
                }
                viewModel.resetUpdateProfileState()
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
                    showAddAddressDialog = false
                }
                viewModel.resetCreateAddressState()
            }
            is CreateAddressState.Error -> {
                // Error được xử lý trong dialog
            }
            else -> {}
        }
    }

    // Xử lý khi cập nhật địa chỉ thành công
    LaunchedEffect(updateAddressState) {
        when (val state = updateAddressState) {
            is UpdateAddressState.Success -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = state.message,
                        duration = SnackbarDuration.Short
                    )
                    showEditAddressDialog = false
                    addressToEdit = null
                }
                viewModel.resetUpdateAddressState()
            }
            is UpdateAddressState.Error -> {
                // Error được xử lý trong dialog
            }
            else -> {}
        }
    }

    // Xử lý khi xóa địa chỉ thành công
    LaunchedEffect(deleteAddressState) {
        when (val state = deleteAddressState) {
            is DeleteAddressState.Success -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = state.message,
                        duration = SnackbarDuration.Short
                    )
                    showDeleteConfirmDialog = false
                    addressToDelete = null
                }
                viewModel.resetDeleteAddressState()
            }
            is DeleteAddressState.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = state.message,
                        duration = SnackbarDuration.Short
                    )
                }
                showDeleteConfirmDialog = false
                addressToDelete = null
                viewModel.resetDeleteAddressState()
            }
            else -> {}
        }
    }

    // Xử lý khi đặt địa chỉ mặc định thành công
    LaunchedEffect(setDefaultAddressState) {
        when (val state = setDefaultAddressState) {
            is SetDefaultAddressState.Success -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = state.message,
                        duration = SnackbarDuration.Short
                    )
                    showSetDefaultConfirmDialog = false
                    addressToSetDefault = null
                }
                viewModel.resetSetDefaultAddressState()
            }
            is SetDefaultAddressState.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = state.message,
                        duration = SnackbarDuration.Short
                    )
                }
                showSetDefaultConfirmDialog = false
                addressToSetDefault = null
                viewModel.resetSetDefaultAddressState()
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
            onDismiss = {
                showEditProfileDialog = false
                viewModel.resetUpdateProfileState()
            }
        )
    }

    // Popup thêm địa chỉ
    if (showAddAddressDialog) {
        AddAddressDialog(
            viewModel = viewModel,
            createAddressState = createAddressState,
            onDismiss = {
                showAddAddressDialog = false
                viewModel.resetCreateAddressState()
            }
        )
    }

    // Popup chỉnh sửa địa chỉ
    if (showEditAddressDialog && addressToEdit != null) {
        EditAddressDialog(
            address = addressToEdit!!,
            viewModel = viewModel,
            updateAddressState = updateAddressState,
            onDismiss = {
                showEditAddressDialog = false
                addressToEdit = null
                viewModel.resetUpdateAddressState()
            }
        )
    }

    // Dialog xác nhận xóa địa chỉ
    if (showDeleteConfirmDialog && addressToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
                addressToDelete = null
            },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa địa chỉ này?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        addressToDelete?.let { viewModel.deleteAddress(it) }
                    },
                    enabled = deleteAddressState !is DeleteAddressState.Loading
                ) {
                    if (deleteAddressState is DeleteAddressState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Xóa")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        addressToDelete = null
                    },
                    enabled = deleteAddressState !is DeleteAddressState.Loading
                ) {
                    Text("Hủy")
                }
            }
        )
    }

    // Dialog xác nhận đặt địa chỉ mặc định
    if (showSetDefaultConfirmDialog && addressToSetDefault != null) {
        AlertDialog(
            onDismissRequest = {
                showSetDefaultConfirmDialog = false
                addressToSetDefault = null
            },
            title = { Text("Đặt địa chỉ mặc định") },
            text = { Text("Bạn có muốn đặt địa chỉ này làm mặc định?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        addressToSetDefault?.let { viewModel.setDefaultAddress(it) }
                    },
                    enabled = setDefaultAddressState !is SetDefaultAddressState.Loading
                ) {
                    if (setDefaultAddressState is SetDefaultAddressState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Đồng ý")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSetDefaultConfirmDialog = false
                        addressToSetDefault = null
                    },
                    enabled = setDefaultAddressState !is SetDefaultAddressState.Loading
                ) {
                    Text("Hủy")
                }
            }
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
                ProfileContent(
                    user = state.user,
                    addresses = state.addresses,
                    modifier = Modifier
                        .background(Color.White)
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState()),
                    onAddAddressClick = { showAddAddressDialog = true },
                    onEditAddressClick = { addressId ->
                        // Tìm địa chỉ theo ID và hiển thị dialog chỉnh sửa
                        val address = state.addresses.find { it.id == addressId }
                        if (address != null) {
                            addressToEdit = address
                            showEditAddressDialog = true
                        }
                    },
                    onChangePasswordClick = onChangePasswordClick,
                    onEditProfileClick = { showEditProfileDialog = true },
                    onDeleteAddressClick = { addressId ->
                        addressToDelete = addressId
                        showDeleteConfirmDialog = true
                    },
                    onSetDefaultAddressClick = { addressId ->
                        addressToSetDefault = addressId
                        showSetDefaultConfirmDialog = true
                    },
                    onOrderButtonClick = onOrderButtonClick // TRUYỀN CALLBACK MỚI
                )
            }
            is ProfileState.Idle, null -> {
                LoadingScreen(modifier = Modifier.padding(padding))
            }
        }
    }
}


@Composable
fun OrderButtonSection(
    onOrderButtonClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFF5722)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Button(
            onClick = onOrderButtonClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF5722),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ShoppingCart,
                contentDescription = "Đơn mua",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "ĐƠN MUA CỦA BẠN",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
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

// Dialog chỉnh sửa địa chỉ
@Composable
fun EditAddressDialog(
    address: AddressResponse,
    viewModel: ProfileViewModel,
    updateAddressState: UpdateAddressState?,
    onDismiss: () -> Unit
) {
    // State cho form địa chỉ
    var label by remember { mutableStateOf(address.label ?: "") }
    var fullAddress by remember { mutableStateOf(address.fullAddress ?: "") }
    var building by remember { mutableStateOf(address.building ?: "") }
    var room by remember { mutableStateOf(address.room ?: "") }
    var note by remember { mutableStateOf(address.note ?: "") }
    var isDefault by remember { mutableStateOf(address.isDefault ?: false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isLoading = updateAddressState is UpdateAddressState.Loading

    AlertDialog(
        onDismissRequest = {
            if (!isLoading) onDismiss()
        },
        title = {
            Text(
                text = "Chỉnh sửa địa chỉ",
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
                if (updateAddressState is UpdateAddressState.Error) {
                    Text(
                        text = updateAddressState.message,
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

                    // Gọi cập nhật địa chỉ
                    address.id?.let { addressId ->
                        viewModel.updateAddress(
                            addressId = addressId,
                            label = label,
                            fullAddress = fullAddress,
                            building = building.takeIf { it.isNotBlank() },
                            room = room.takeIf { it.isNotBlank() },
                            note = note.takeIf { it.isNotBlank() },
                            isDefault = isDefault
                        )
                    }
                },
                enabled = !isLoading && label.isNotBlank() && fullAddress.isNotBlank()
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

                    if (displayName.length > 50) {
                        errorMessage = "Tên không được quá 50 ký tự"
                        return@TextButton
                    }

                    errorMessage = null

                    // Gọi update
                    viewModel.updateProfile(
                        displayName = displayName.takeIf { it.isNotBlank() && it != currentUser?.fullName },
                        phone = phone.takeIf { it.isNotBlank() && it != currentUser?.phone }
                    )
                },
                enabled = !isLoading &&
                        ((displayName.isNotBlank() && displayName != currentUser?.fullName) ||
                                (phone.isNotBlank() && phone != currentUser?.phone))
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
fun ProfileContent(
    user: Client,
    addresses: List<AddressResponse>,
    modifier: Modifier = Modifier,
    onAddAddressClick: () -> Unit,
    onEditAddressClick: (String) -> Unit,
    onChangePasswordClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onDeleteAddressClick: (String) -> Unit,
    onSetDefaultAddressClick: (String) -> Unit,
    onOrderButtonClick: () -> Unit // THÊM THAM SỐ MỚI
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
        // THÊM BUTTON ĐƠN MUA VÀO ĐẦU MÀN HÌNH
        OrderButtonSection(onOrderButtonClick = onOrderButtonClick)


        // Thông tin cá nhân card
        PersonalInfoCard(
            user = user,
            onEditClick = onEditProfileClick
        )

        // Trạng thái tài khoản
        AccountStatusCard(user = user)

        // Địa chỉ - Chỉ hiển thị nếu có địa chỉ
        if (addresses.isNotEmpty()) {
            AddressCard(
                addresses = addresses,
                onAddClick = onAddAddressClick,
                onEditClick = onEditAddressClick,
                onDeleteClick = onDeleteAddressClick,
                onSetDefaultClick = onSetDefaultAddressClick
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

// Previews
@Preview(showBackground = true)
@Composable
fun ProfileMainScreenPreview() {
    MaterialTheme {
        UserProfileScreen(
            onBackClick = {},
            onEditAddressClick = {},
            onChangePasswordClick = {},
            onEditProfileClick = {},
            onOrderButtonClick = {} // THÊM PREVIEW MỚI
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OrderButtonSectionPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            OrderButtonSection(onOrderButtonClick = {})
        }
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
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Button không có badge:", fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
            }
        }
    }
}