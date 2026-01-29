package com.example.foodapp.pages.shipper.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.pages.shipper.theme.ShipperColors

@Composable
fun NotificationCard(
    notification: Notification,
    onClick: () -> Unit = {},
    timeText: String,
    titleText: String = notification.title,
    bodyText: String = notification.body
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.read) ShipperColors.Surface else ShipperColors.PrimaryLight
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        getTypeColor(notification.type).copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getTypeIcon(notification.type),
                    contentDescription = null,
                    tint = getTypeColor(notification.type),
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                Text(
                    text = titleText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ShipperColors.TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                    if (!notification.read) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(ShipperColors.Primary, CircleShape)
                        )
                    }
                }

                Text(
                    text = bodyText,
                    fontSize = 14.sp,
                    color = ShipperColors.TextSecondary,
                    modifier = Modifier.padding(top = 4.dp),
                    lineHeight = 20.sp
                )

                Text(
                    text = timeText,
                    fontSize = 12.sp,
                    color = ShipperColors.TextTertiary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

private fun getTypeIcon(type: NotificationType) = when (type) {
    NotificationType.NEW_ORDER,
    NotificationType.ORDER_CONFIRMED,
    NotificationType.ORDER_PREPARING,
    NotificationType.ORDER_READY,
    NotificationType.ORDER_SHIPPING,
    NotificationType.ORDER_DELIVERED -> Icons.Outlined.ShoppingBag

    NotificationType.ORDER_CANCELLED -> Icons.Outlined.Cancel

    NotificationType.PAYMENT_SUCCESS,
    NotificationType.PAYMENT_FAILED,
    NotificationType.PAYMENT_REFOUNDED -> Icons.Outlined.AccountBalanceWallet

    NotificationType.SHIPPER_ASSIGNED,
    NotificationType.SHIPPER_APPLIED,
    NotificationType.SHIPPER_APPLICATION_APPROVED,
    NotificationType.SHIPPER_APPLICATION_REJECTED -> Icons.Outlined.LocalShipping

    NotificationType.DAILY_SUMMARY,
    NotificationType.SUBSCRIPTION_EXPIRING -> Icons.Outlined.Info

    NotificationType.PROMOTION,
    NotificationType.VOUCHER_AVAILABLE -> Icons.Outlined.LocalOffer

    NotificationType.UNKNOWN -> Icons.Outlined.Notifications
}

private fun getTypeColor(type: NotificationType) = when (type) {
    NotificationType.NEW_ORDER,
    NotificationType.ORDER_CONFIRMED,
    NotificationType.ORDER_PREPARING,
    NotificationType.ORDER_READY,
    NotificationType.ORDER_SHIPPING,
    NotificationType.ORDER_DELIVERED,
    NotificationType.PAYMENT_SUCCESS,
    NotificationType.SHIPPER_APPLICATION_APPROVED -> ShipperColors.Success

    NotificationType.ORDER_CANCELLED,
    NotificationType.PAYMENT_FAILED,
    NotificationType.SHIPPER_APPLICATION_REJECTED -> ShipperColors.Error

    NotificationType.PAYMENT_REFOUNDED,
    NotificationType.SUBSCRIPTION_EXPIRING -> ShipperColors.Warning

    NotificationType.SHIPPER_ASSIGNED,
    NotificationType.SHIPPER_APPLIED -> ShipperColors.Primary

    NotificationType.DAILY_SUMMARY -> ShipperColors.Info

    NotificationType.PROMOTION,
    NotificationType.VOUCHER_AVAILABLE -> ShipperColors.Info

    NotificationType.UNKNOWN -> ShipperColors.TextSecondary
}
