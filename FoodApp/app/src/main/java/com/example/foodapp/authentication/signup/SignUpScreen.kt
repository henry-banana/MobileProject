package com.example.foodapp.authentication.signup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.ui.theme.PrimaryOrange
import com.google.android.gms.auth.api.signin.GoogleSignIn

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onBackClicked: () -> Unit,
    onLoginClicked: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: SignUpViewModel = viewModel(
        factory = SignUpViewModel.factory(context)
    )

    // Observe ViewModel states
    val signUpState by viewModel.signUpState.observeAsState(SignUpState.Idle)
    val googleSignInState by viewModel.googleSignInState.observeAsState(GoogleSignInState.Idle)
    val saveUserState by viewModel.saveUserState.observeAsState(null)

    // Google Sign-In Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        viewModel.handleGoogleSignInResult(task)
    }

    // Handle lifecycle
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.handlePendingGoogleSignIn(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Navigate on success
    LaunchedEffect(signUpState) {
        if (signUpState is SignUpState.Success) {
            onSignUpSuccess()
        }
    }

    LaunchedEffect(saveUserState) {
        if (saveUserState == true) {
            onSignUpSuccess()
        }
    }

    // Reset UI khi state thay đổi
    LaunchedEffect(signUpState, googleSignInState) {
        // Reset error khi loading bắt đầu
        if (signUpState is SignUpState.Loading || googleSignInState is GoogleSignInState.Loading) {
            // Có thể reset UI state nếu cần
        }
    }

    // UI Content
    SignUpContent(
        signUpState = signUpState,
        googleSignInState = googleSignInState,
        onRegisterClick = { fullName, email, password, confirmPassword ->
            viewModel.registerWithEmail(fullName, email, password, confirmPassword)
        },
        onGoogleClick = {
            val signInIntent = viewModel.getGoogleSignInClient().signInIntent
            googleSignInLauncher.launch(signInIntent)
        },
        onLoginClicked = onLoginClicked,
        onBackClicked = {
            viewModel.resetStates()
            onBackClicked()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpContent(
    signUpState: SignUpState,
    googleSignInState: GoogleSignInState,
    onRegisterClick: (String, String, String, String) -> Unit,
    onGoogleClick: () -> Unit,
    onLoginClicked: () -> Unit,
    onBackClicked: () -> Unit
) {
    // Local UI State
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    // Derived state từ ViewModel
    val isLoading = signUpState is SignUpState.Loading || googleSignInState is GoogleSignInState.Loading
    val isSuccess = signUpState is SignUpState.Success || googleSignInState is GoogleSignInState.Success
    val serverErrorMessage = (signUpState as? SignUpState.Error)?.message
        ?: (googleSignInState as? GoogleSignInState.Error)?.message

    // Display error
    val displayError = validationError ?: serverErrorMessage

    // Reset validation error khi input thay đổi
    LaunchedEffect(fullName, email, password, confirmPassword) {
        if (validationError != null) {
            validationError = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back Button
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = {
                    onBackClicked()
                },
                enabled = !isLoading
            ) {
                Text(
                    "←",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Title
        Text(
            "Tạo tài khoản",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 20.dp)
        )

        // Error Message
        if (displayError != null) {
            Text(
                text = displayError,
                color = Color.Red,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(Color(0x1AFF0000), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                fontSize = 14.sp
            )
        }

        // TextField Colors
        val textFieldColors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFFAFAFA),
            unfocusedContainerColor = Color(0xFFFAFAFA),
            disabledContainerColor = Color(0xFFF0F0F0),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )

        // Input Fields
        SignUpTextField(
            value = fullName,
            onValueChange = { fullName = it },
            placeholder = "Họ và tên",
            enabled = !isLoading,
            colors = textFieldColors
        )

        SignUpTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = "Email",
            enabled = !isLoading,
            colors = textFieldColors,
            keyboardType = KeyboardType.Email
        )

        SignUpTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = "Mật khẩu (ít nhất 6 ký tự)",
            enabled = !isLoading,
            isPassword = true,
            isVisible = passwordVisible,
            onToggleVisibility = { passwordVisible = !passwordVisible },
            colors = textFieldColors
        )

        SignUpTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = "Xác nhận mật khẩu",
            enabled = !isLoading,
            isPassword = true,
            isVisible = confirmPasswordVisible,
            onToggleVisibility = { confirmPasswordVisible = !confirmPasswordVisible },
            colors = textFieldColors
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Register Button
        Button(
            onClick = {
                // Validate input
                when {
                    fullName.isBlank() -> validationError = "Vui lòng nhập họ và tên"
                    fullName.length < 2 -> validationError = "Tên phải có ít nhất 2 ký tự"
                    email.isBlank() -> validationError = "Vui lòng nhập email"
                    !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                        validationError = "Email không hợp lệ"
                    password.isBlank() -> validationError = "Vui lòng nhập mật khẩu"
                    password.length < 6 -> validationError = "Mật khẩu phải có ít nhất 6 ký tự"
                    password != confirmPassword -> validationError = "Mật khẩu xác nhận không khớp"
                    else -> {
                        validationError = null
                        onRegisterClick(fullName, email, password, confirmPassword)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSuccess) Color(0xFF4CAF50) else PrimaryOrange,
                disabledContainerColor = PrimaryOrange.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(28.dp),
            enabled = !isLoading
        ) {
            if (isLoading && signUpState is SignUpState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đang xử lý...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            } else if (isSuccess) {
                Text("Thành công!", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            } else {
                Text("Đăng ký", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Divider với "Hoặc"
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 1.dp,
                color = Color.LightGray
            )
            Text(
                text = " Hoặc đăng ký với ",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 1.dp,
                color = Color.LightGray
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Google Sign-In Button
        OutlinedButton(
            onClick = onGoogleClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.Black,
                disabledContentColor = Color.Gray
            ),
            border = if (isLoading) {
                null
            } else {
                BorderStroke(1.dp, Color.LightGray)
            }
        ) {
            if (googleSignInState is GoogleSignInState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đang đăng nhập...", color = Color.Gray)
            } else {
                Text("Đăng nhập với Google", color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Login Link
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Bạn đã có tài khoản? ",
                color = Color(0xFF666666),
                fontSize = 14.sp
            )
            Text(
                text = "Đăng nhập",
                color = PrimaryOrange,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable(enabled = !isLoading) {
                        onLoginClicked()
                    }
                    .padding(vertical = 4.dp, horizontal = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean = true,
    isPassword: Boolean = false,
    isVisible: Boolean = false,
    onToggleVisibility: () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text,
    colors: TextFieldColors
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        placeholder = { Text(placeholder, color = Color.Gray) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = colors,
        visualTransformation = if (isPassword && !isVisible)
            PasswordVisualTransformation()
        else
            VisualTransformation.None,
        trailingIcon = if (isPassword) {
            {
                IconButton(
                    onClick = onToggleVisibility,
                    enabled = enabled
                ) {
                    Icon(
                        imageVector = if (isVisible)
                            Icons.Default.VisibilityOff
                        else
                            Icons.Default.Visibility,
                        contentDescription = if (isVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                        tint = if (enabled) Color.Gray else Color.LightGray
                    )
                }
            }
        } else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Next
        )
    )
}