package com.example.hotelmanagement.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RoomService
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hotelmanagement.ui.navigation.Routes

/**
 * Главная панель управления (аналог MainForm): семь разделов с иконками и выход.
 */
@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Hotel,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Панель управления отелем",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        MenuButton("Новое бронирование", Icons.Filled.Event, Routes.BOOKING, onNavigate)
        MenuButton("Список бронирований", Icons.Filled.FactCheck, Routes.BOOKINGS_LIST, onNavigate)
        MenuButton("Список номеров", Icons.Filled.Bed, Routes.ROOMS, onNavigate)
        MenuButton("База клиентов", Icons.Filled.People, Routes.CLIENTS, onNavigate)
        MenuButton("Текущие постояльцы", Icons.Filled.HowToReg, Routes.GUESTS, onNavigate)
        MenuButton("Заказ услуг", Icons.Filled.RoomService, Routes.SERVICES, onNavigate)
        MenuButton("Выставить счёт", Icons.Filled.Receipt, Routes.PAYMENT, onNavigate)

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = onExit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Выйти", fontSize = 16.sp)
        }
    }
}

@Composable
private fun MenuButton(
    label: String,
    icon: ImageVector,
    route: String,
    onNavigate: (String) -> Unit
) {
    Button(
        onClick = { onNavigate(route) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(10.dp))
        Text(label, fontSize = 16.sp)
    }
}
