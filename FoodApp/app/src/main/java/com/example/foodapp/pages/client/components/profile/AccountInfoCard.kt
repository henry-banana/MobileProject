package com.example.foodapp.pages.client.components.profile


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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