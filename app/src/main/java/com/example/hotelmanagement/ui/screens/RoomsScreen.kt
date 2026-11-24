package com.example.hotelmanagement.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.example.hotelmanagement.HotelApp
import androidx.compose.ui.platform.LocalContext
import com.example.hotelmanagement.data.entity.RoomEntity
import com.example.hotelmanagement.data.repository.HotelRepository
import com.example.hotelmanagement.ui.components.ScreenScaffold
import com.example.hotelmanagement.ui.components.StatusChip
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** ViewModel экрана "Список номеров". */
class RoomsViewModel(repository: HotelRepository) : ViewModel() {
    val rooms: StateFlow<List<RoomEntity>> = repository.rooms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

/**
 * Экран "Список номеров" (аналог RoomForm):
 * номера отеля и их статусы "Свободен"/"Забронирован"/"Занят".
 * Статусы меняются автоматически при бронировании, заселении и выселении.
 */
@Composable
fun RoomsScreen(onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as HotelApp
    val vm: RoomsViewModel = viewModel { RoomsViewModel(app.repository) }
    val rooms by vm.rooms.collectAsStateWithLifecycle()

    ScreenScaffold(title = "Список номеров", onBack = onBack) { modifier ->
        LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(rooms, key = { it.roomId }) { room ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "№ ${room.roomNumber}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            StatusChip(room.status)
                        }
                        Text("Тип: ${room.roomType}", fontSize = 14.sp)
                        Text("Цена за сутки: ${"%.0f".format(room.pricePerDay)} руб.", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
