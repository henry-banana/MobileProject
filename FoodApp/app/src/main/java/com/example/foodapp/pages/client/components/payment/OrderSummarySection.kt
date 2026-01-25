// OrderSummarySection.kt (sửa lại)
package com.example.foodapp.pages.client.components.payment

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.foodapp.ui.theme.*

@Composable
fun OrderSummarySection(
    productPrice: Double,
    deliveryFee: Double,
    discount: Double = 0.0,
    modifier: Modifier = Modifier
) {
    val total = productPrice + deliveryFee - discount

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Tổng thanh toán",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Giá sản phẩm
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Tiền hàng",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = formatCurrency(productPrice),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Phí vận chuyển
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Phí vận chuyển",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = formatCurrency(deliveryFee),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }

            // Hiển thị discount nếu có
            if (discount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Giảm giá",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SuccessColor
                    )
                    Text(
                        text = "-${formatCurrency(discount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SuccessColor
                    )
                }
            }

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                color = BorderColor
            )

            // Tổng cộng
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Tổng cộng",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = formatCurrency(total),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryColor
                )
            }
        }
    }
}

private fun formatCurrency(amount: Double): String {
    return String.format("%,.0f", amount) + "đ"
}