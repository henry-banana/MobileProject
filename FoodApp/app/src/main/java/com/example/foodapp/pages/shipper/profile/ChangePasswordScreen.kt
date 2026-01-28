package com.example.foodapp.pages.shipper.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.pages.shipper.theme.ShipperColors
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(onCancel: () -> Unit = {}) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Đổi mật khẩu", 
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack, 
                            contentDescription = "Quay lại",
                            tint = ShipperColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ShipperColors.Surface,
                    titleContentColor = ShipperColors.TextPrimary
                )
            )
        },
        containerColor = ShipperColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ShipperColors.InfoLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = ShipperColors.Info,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Yêu cầu mật khẩu mới:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = ShipperColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Tối thiểu 8 ký tự\n• Chứa chữ hoa và chữ thường\n• Chứa ít nhất 1 số",
                            fontSize = 13.sp,
                            color = ShipperColors.TextSecondary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ShipperColors.Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    PasswordField(
                        label = "Mật khẩu hiện tại",
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        passwordVisible = currentPasswordVisible,
                        onVisibilityChange = { currentPasswordVisible = it },
                        icon = Icons.Outlined.Lock
                    )

                    HorizontalDivider(color = ShipperColors.Divider)

                    PasswordField(
                        label = "Mật khẩu mới",
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        passwordVisible = newPasswordVisible,
                        onVisibilityChange = { newPasswordVisible = it },
                        icon = Icons.Outlined.Key
                    )

                    HorizontalDivider(color = ShipperColors.Divider)

                    PasswordField(
                        label = "Xác nhận mật khẩu mới",
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        passwordVisible = confirmPasswordVisible,
                        onVisibilityChange = { confirmPasswordVisible = it },
                        icon = Icons.Outlined.CheckCircle
                    )
                }
            }

            if (errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = ShipperColors.ErrorLight)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Error,
                            contentDescription = null,
                            tint = ShipperColors.Error,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = errorMessage,
                            color = ShipperColors.Error,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            if (successMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = ShipperColors.SuccessLight)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = ShipperColors.Success,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = successMessage,
                            color = ShipperColors.Success,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Button(
                onClick = {
                    errorMessage = ""
                    successMessage = ""

                    when {
                        currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty() -> {
                            errorMessage = "Vui lòng điền đầy đủ thông tin"
                        }
                        newPassword.length < 8 -> {
                            errorMessage = "Mật khẩu mới phải có ít nhất 8 ký tự"
                        }
                        newPassword != confirmPassword -> {
                            errorMessage = "Mật khẩu xác nhận không khớp"
                        }
                        else -> {
                            isLoading = true
                            val user = FirebaseAuth.getInstance().currentUser
                            val credential = EmailAuthProvider.getCredential(user?.email ?: "", currentPassword)
                            
                            user?.reauthenticate(credential)?.addOnCompleteListener { reauth ->
                                if (reauth.isSuccessful) {
                                    user.updatePassword(newPassword).addOnCompleteListener { task ->
                                        isLoading = false
                                        if (task.isSuccessful) {
                                            successMessage = "Đổi mật khẩu thành công!"
                                            currentPassword = ""
                                            newPassword = ""
                                            confirmPassword = ""
                                        } else {
                                            errorMessage = "Đổi mật khẩu thất bại: ${task.exception?.message}"
                                        }
                                    }
                                } else {
                                    isLoading = false
                                    errorMessage = "Mật khẩu hiện tại không đúng"
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ShipperColors.Primary),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = ShipperColors.Surface,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Đổi mật khẩu", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    passwordVisible: Boolean,
    onVisibilityChange: (Boolean) -> Unit,
    icon: ImageVector
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ShipperColors.TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = ShipperColors.TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { onVisibilityChange(!passwordVisible) }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = if (passwordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                        tint = ShipperColors.TextSecondary
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ShipperColors.Primary,
                unfocusedBorderColor = ShipperColors.Divider
            ),
            singleLine = true
        )
    }
}
