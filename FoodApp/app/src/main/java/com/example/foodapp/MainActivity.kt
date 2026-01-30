package com.example.foodapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.foodapp.navigation.FoodAppNavHost
import com.example.foodapp.ui.theme.FoodAppTheme
import com.example.foodapp.data.remote.api.ApiClient
import com.example.foodapp.utils.LanguageManager

class MainActivity : ComponentActivity() {
    
    override fun attachBaseContext(newBase: Context) {
        // Áp dụng ngôn ngữ đã lưu trước khi attach context
        super.attachBaseContext(LanguageManager.wrapContext(newBase))
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ApiClient.init(this)

        setContent {
            FoodAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    FoodAppNavHost(navController = navController)
                }
            }
        }
    }
}