package com.example.foodapp.pages.shipper.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.pages.shipper.theme.ShipperColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun NotificationsScreen() {
    val viewModel: NotificationsViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShipperColors.Background)
    ) {
        if (uiState.unreadCount > 0) {
            Text(
                text = "${uiState.unreadCount} thông báo chưa đọc",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = ShipperColors.Primary,
                modifier = Modifier.padding(16.dp).padding(bottom = 0.dp)
            )
        }

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ShipperColors.Primary)
                }
            }
            uiState.error != null && uiState.notifications.isEmpty() -> {
                Text(
                    text = uiState.error ?: "Không thể tải thông báo",
                    fontSize = 14.sp,
                    color = ShipperColors.Error,
                    modifier = Modifier.padding(16.dp)
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .padding(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    uiState.notifications.forEach { notification ->
                        val titleText = mapTitle(notification)
                        val bodyText = mapBody(notification)
                        NotificationCard(
                            notification = notification,
                            timeText = formatNotificationTime(notification.createdAt),
                            titleText = titleText,
                            bodyText = bodyText,
                            onClick = {
                                if (!notification.read) {
                                    viewModel.markAsRead(notification.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun formatNotificationTime(timestamp: String): String {
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = dateFormat.parse(timestamp)

        val now = Date()
        val diff = now.time - (date?.time ?: 0)

        when {
            diff < 60000 -> "Vừa xong"
            diff < 3600000 -> "${diff / 60000} phút trước"
            diff < 86400000 -> "${diff / 3600000} giờ trước"
            diff < 604800000 -> "${diff / 86400000} ngày trước"
            else -> {
                val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                displayFormat.format(date ?: Date())
            }
        }
    } catch (e: Exception) {
        "Không xác định"
    }
}

private fun mapTitle(notification: Notification): String {
    return when (notification.type) {
        NotificationType.NEW_ORDER -> "Đơn hàng mới"
        NotificationType.ORDER_CONFIRMED -> "Đơn hàng đã xác nhận"
        NotificationType.ORDER_PREPARING -> "Đơn hàng đang chuẩn bị"
        NotificationType.ORDER_READY -> "Đơn hàng sẵn sàng"
        NotificationType.ORDER_SHIPPING -> "Đang giao hàng"
        NotificationType.ORDER_DELIVERED -> "Đã giao hàng"
        NotificationType.ORDER_CANCELLED -> "Đơn hàng đã hủy"

        NotificationType.PAYMENT_SUCCESS -> "Thanh toán thành công"
        NotificationType.PAYMENT_FAILED -> "Thanh toán thất bại"
        NotificationType.PAYMENT_REFOUNDED -> "Đã hoàn tiền"

        NotificationType.SHIPPER_ASSIGNED -> "Bạn được phân đơn"
        NotificationType.SHIPPER_APPLIED -> "Có shipper ứng tuyển"
        NotificationType.SHIPPER_APPLICATION_APPROVED -> "Đơn ứng tuyển đã duyệt"
        NotificationType.SHIPPER_APPLICATION_REJECTED -> "Đơn ứng tuyển bị từ chối"

        NotificationType.DAILY_SUMMARY -> "Tổng kết hôm nay"
        NotificationType.SUBSCRIPTION_EXPIRING -> "Gói sắp hết hạn"

        NotificationType.PROMOTION -> "Khuyến mãi"
        NotificationType.VOUCHER_AVAILABLE -> "Voucher mới"

        NotificationType.UNKNOWN -> notification.title
    }
}

private fun mapBody(notification: Notification): String {
    val orderId = notification.orderId
    return when (notification.type) {
        NotificationType.NEW_ORDER ->
            if (orderId != null) "Có đơn hàng mới #$orderId" else "Có đơn hàng mới"
        NotificationType.ORDER_CONFIRMED ->
            if (orderId != null) "Đơn #$orderId đã được xác nhận" else "Đơn hàng đã được xác nhận"
        NotificationType.ORDER_PREPARING ->
            if (orderId != null) "Đơn #$orderId đang được chuẩn bị" else "Đơn hàng đang được chuẩn bị"
        NotificationType.ORDER_READY ->
            if (orderId != null) "Đơn #$orderId đã sẵn sàng để giao" else "Đơn hàng đã sẵn sàng để giao"
        NotificationType.ORDER_SHIPPING ->
            if (orderId != null) "Đơn #$orderId đang được giao" else "Đơn hàng đang được giao"
        NotificationType.ORDER_DELIVERED ->
            if (orderId != null) "Đơn #$orderId đã giao thành công" else "Đơn hàng đã giao thành công"
        NotificationType.ORDER_CANCELLED ->
            if (orderId != null) "Đơn #$orderId đã bị hủy" else "Đơn hàng đã bị hủy"

        NotificationType.PAYMENT_SUCCESS -> "Thanh toán đã được xử lý thành công"
        NotificationType.PAYMENT_FAILED -> "Thanh toán không thành công"
        NotificationType.PAYMENT_REFOUNDED -> "Đã hoàn tiền về phương thức thanh toán"

        NotificationType.SHIPPER_ASSIGNED -> "Bạn đã được phân cho một đơn mới"
        NotificationType.SHIPPER_APPLIED -> "Có shipper mới ứng tuyển"
        NotificationType.SHIPPER_APPLICATION_APPROVED -> "Đơn ứng tuyển của bạn đã được duyệt"
        NotificationType.SHIPPER_APPLICATION_REJECTED -> "Đơn ứng tuyển của bạn bị từ chối"

        NotificationType.DAILY_SUMMARY -> "Tổng kết hoạt động trong ngày"
        NotificationType.SUBSCRIPTION_EXPIRING -> "Gói dịch vụ sắp hết hạn"

        NotificationType.PROMOTION -> "Có chương trình khuyến mãi mới"
        NotificationType.VOUCHER_AVAILABLE -> "Có voucher mới dành cho bạn"

        NotificationType.UNKNOWN -> notification.body
    }
}
