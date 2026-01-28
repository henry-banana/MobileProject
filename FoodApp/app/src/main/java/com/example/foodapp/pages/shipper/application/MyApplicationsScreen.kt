package com.example.foodapp.pages.shipper.application

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.foodapp.data.di.RepositoryProvider
import com.example.foodapp.data.model.shipper.application.ApplicationStatus
import com.example.foodapp.data.model.shipper.application.ShipperApplication
import com.example.foodapp.data.model.shipper.application.VehicleType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ViewModel
data class MyApplicationsUiState(
    val applications: List<ShipperApplication> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showCancelDialog: Boolean = false,
    val selectedApplication: ShipperApplication? = null,
    val isCancelling: Boolean = false,
    val cancelSuccess: Boolean = false
)

class MyApplicationsViewModel : ViewModel() {
    private val repository = RepositoryProvider.getShipperApplicationRepository()
    
    private val _uiState = MutableStateFlow(MyApplicationsUiState())
    val uiState: StateFlow<MyApplicationsUiState> = _uiState.asStateFlow()
    
    init {
        loadApplications()
    }
    
    fun loadApplications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getMyApplications()
                .onSuccess { applications ->
                    _uiState.update { 
                        it.copy(
                            applications = applications.sortedByDescending { app -> app.createdAt },
                            isLoading = false
                        ) 
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }
    
    fun showCancelDialog(application: ShipperApplication) {
        _uiState.update { it.copy(showCancelDialog = true, selectedApplication = application) }
    }
    
    fun dismissCancelDialog() {
        _uiState.update { it.copy(showCancelDialog = false, selectedApplication = null) }
    }
    
    fun cancelApplication() {
        val application = _uiState.value.selectedApplication ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isCancelling = true) }
            repository.cancelApplication(application.id)
                .onSuccess {
                    _uiState.update { 
                        it.copy(
                            isCancelling = false,
                            showCancelDialog = false,
                            selectedApplication = null,
                            cancelSuccess = true
                        ) 
                    }
                    loadApplications()
                }
                .onFailure { e ->
                    _uiState.update { 
                        it.copy(isCancelling = false, error = e.message) 
                    }
                }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun clearSuccess() {
        _uiState.update { it.copy(cancelSuccess = false) }
    }
}

// Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApplicationsScreen(
    onBack: () -> Unit = {},
    showTopBar: Boolean = true,
    onRefresh: (() -> Unit)? = null,
    viewModel: MyApplicationsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Error handling
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }
    
    // Success handling
    LaunchedEffect(uiState.cancelSuccess) {
        if (uiState.cancelSuccess) {
            Toast.makeText(context, "Đã hủy đơn ứng tuyển", Toast.LENGTH_SHORT).show()
            viewModel.clearSuccess()
        }
    }
    
    val content: @Composable (PaddingValues) -> Unit = { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.applications.isEmpty() -> {
                    EmptyApplicationsView()
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.applications) { application ->
                            ApplicationCard(
                                application = application,
                                onCancel = { viewModel.showCancelDialog(application) }
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showTopBar) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Đơn ứng tuyển của tôi") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Quay lại",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White
                    ),
                    actions = {
                        IconButton(onClick = { viewModel.loadApplications() }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Làm mới",
                                tint = Color.White
                            )
                        }
                    }
                )
            }
        ) { padding ->
            content(padding)
        }
    } else {
        // No TopAppBar version for use inside dashboard
        content(PaddingValues(0.dp))
    }
    
    // Cancel confirmation dialog
    if (uiState.showCancelDialog && uiState.selectedApplication != null) {
        AlertDialog(
            onDismissRequest = { if (!uiState.isCancelling) viewModel.dismissCancelDialog() },
            title = { Text("Hủy đơn ứng tuyển") },
            text = { 
                Text("Bạn có chắc muốn hủy đơn ứng tuyển tại ${uiState.selectedApplication?.shopName ?: "cửa hàng này"}?") 
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.cancelApplication() },
                    enabled = !uiState.isCancelling,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) {
                    if (uiState.isCancelling) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Hủy đơn")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissCancelDialog() },
                    enabled = !uiState.isCancelling
                ) {
                    Text("Đóng")
                }
            }
        )
    }
}

@Composable
private fun EmptyApplicationsView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Assignment,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.LightGray
            )
            Text(
                "Chưa có đơn ứng tuyển nào",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )
            Text(
                "Hãy chọn một cửa hàng để gửi đơn",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun ApplicationCard(
    application: ShipperApplication,
    onCancel: () -> Unit
) {
    val status = try {
        ApplicationStatus.valueOf(application.status)
    } catch (e: Exception) {
        ApplicationStatus.PENDING
    }
    
    val vehicleInfo = try {
        VehicleType.valueOf(application.vehicleType ?: "MOTORBIKE")
    } catch (e: Exception) {
        VehicleType.MOTORBIKE
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shop name
                Text(
                    text = application.shopName ?: "Cửa hàng",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                // Status chip
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(status.color).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = status.displayName,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(status.color),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Vehicle info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = vehicleInfo.icon,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = vehicleInfo.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = application.vehicleNumber ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // ID Card info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Badge,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CCCD: ${maskIdNumber(application.idCardNumber)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            // Message if present
            if (!application.message.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"${application.message}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Reject reason if rejected
            if (status == ApplicationStatus.REJECTED && !application.rejectReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFEBEE)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFF44336)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lý do: ${application.rejectReason}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFC62828)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Footer: Date and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Created date
                Text(
                    text = formatDate(application.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                
                // Cancel button (only for pending)
                if (status == ApplicationStatus.PENDING) {
                    TextButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFFF44336)
                        )
                    ) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hủy đơn")
                    }
                }
            }
        }
    }
}

// Helper functions
private fun maskIdNumber(idNumber: String?): String {
    if (idNumber.isNullOrBlank() || idNumber.length < 4) return "***"
    return "*".repeat(idNumber.length - 4) + idNumber.takeLast(4)
}

private fun formatDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: return dateString)
    } catch (e: Exception) {
        dateString
    }
}
