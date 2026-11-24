package com.example.hotelmanagement.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.example.hotelmanagement.HotelApp
import com.example.hotelmanagement.data.dao.CheckInDetails
import com.example.hotelmanagement.data.repository.HotelRepository
import com.example.hotelmanagement.ui.components.ScreenScaffold
import com.example.hotelmanagement.ui.components.StatusChip
import com.example.hotelmanagement.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** ViewModel экрана "Текущие постояльцы". */
class CheckedInViewModel(repository: HotelRepository) : ViewModel() {
    val guests: StateFlow<List<CheckInDetails>> = repository.currentGuests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

/**
 * Экран "Текущие постояльцы" (аналог CheckInForm):
 * список заселённых клиентов. Обновляется автоматически при выселении.
 */
@Composable
fun CheckedInScreen(onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as HotelApp
    val vm: CheckedInViewModel = viewModel { CheckedInViewModel(app.repository) }
    val guests by vm.guests.collectAsStateWithLifecycle()

    ScreenScaffold(title = "Текущие постояльцы", onBack = onBack) { modifier ->
        LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(guests, key = { it.checkInId }) { guest ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = guest.clientName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            StatusChip(guest.checkInStatus)
                        }
                        Text("Номер: ${guest.roomNumber}", fontSize = 14.sp)
                        Text("Договор: ${guest.contractNumber}", fontSize = 14.sp)
                        Text(
                            text = "Заселён: ${DateUtils.format(guest.checkInDate)}" +
                                (guest.checkOutDate?.let { ", выезд: ${DateUtils.format(it)}" } ?: ""),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
