package com.example.hotelmanagement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.hotelmanagement.ui.navigation.AppNavGraph
import com.example.hotelmanagement.ui.theme.HotelManagementTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HotelManagementTheme {
                AppNavGraph()
            }
        }
    }
}
