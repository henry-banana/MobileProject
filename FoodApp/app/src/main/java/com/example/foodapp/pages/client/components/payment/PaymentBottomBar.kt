package com.example.foodapp.pages.client.components.payment


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PaymentBottomBar(
    totalPrice: Double,
    isLoading: Boolean,
    onPlaceOrder: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = CardWhite
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Tổng cộng",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Text(
                    text = formatPrice(totalPrice + 15000.0), // Giả sử phí ship cố định
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryOrange
                )
            }

            Button(
                onClick = onPlaceOrder,
                enabled = !isLoading,
                modifier = Modifier
                    .height(52.dp)
                    .fillMaxWidth(0.6f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                } else {
                    Text(
                        text = "Đặt hàng",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatPrice(price: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return format.format(price).replace(" ₫", "đ")
}