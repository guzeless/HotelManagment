package com.example.hotelmanagement.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
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
import com.example.hotelmanagement.data.dao.BookingDetails
import com.example.hotelmanagement.data.repository.HotelRepository
import com.example.hotelmanagement.ui.components.ScreenScaffold
import com.example.hotelmanagement.ui.components.StatusChip
import com.example.hotelmanagement.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel списка бронирований (аналог BookingDataForm):
 * заселение клиента и отмена бронирования.
 */
class BookingsListViewModel(private val repository: HotelRepository) : ViewModel() {
    val bookings: StateFlow<List<BookingDetails>> = repository.bookings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val message = MutableStateFlow<String?>(null)

    /** Заселение клиента из бронирования (аналог CheckInBooking). */
    fun checkIn(bookingId: Long) {
        viewModelScope.launch {
            message.value = if (repository.checkInFromBooking(bookingId))
                "Клиент заселён, бронирование завершено"
            else "Заселение невозможно"
        }
    }

    /** Отмена бронирования и освобождение номера (аналог DeleteBooking). */
    fun cancel(bookingId: Long) {
        viewModelScope.launch {
            message.value = if (repository.cancelBooking(bookingId))
                "Бронирование отменено, номер освобождён"
            else "Отмена невозможна"
        }
    }
}

/**
 * Экран "Список бронирований": таблица бронирований со статусами.
 * Кнопка "Заселить" переводит бронирование в статус "Завершено"
 * (создаётся заселение), кнопка "Отменить" удаляет бронирование
 * и освобождает номер.
 */
@Composable
fun BookingsListScreen(onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as HotelApp
    val vm: BookingsListViewModel = viewModel { BookingsListViewModel(app.repository) }
    val bookings by vm.bookings.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()

    ScreenScaffold(title = "Список бронирований", onBack = onBack) { modifier ->
        Column(modifier = modifier) {
            message?.let {
                Text(it, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(bookings, key = { it.bookingId }) { booking ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = booking.clientName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                StatusChip(booking.bookingStatus)
                            }
                            Text("Номер: ${booking.roomNumber}", fontSize = 14.sp)
                            Text(
                                text = "Заезд: ${DateUtils.format(booking.checkInDate)}, " +
                                    "выезд: ${DateUtils.format(booking.checkOutDate)}",
                                fontSize = 14.sp
                            )
                            if (booking.bookingStatus == com.example.hotelmanagement.data.entity.BookingEntity.STATUS_ACTIVE) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { vm.checkIn(booking.bookingId) }) {
                                        Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                                        Spacer(Modifier.width(6.dp))
                                        Text("Заселить")
                                    }
                                    OutlinedButton(onClick = { vm.cancel(booking.bookingId) }) {
                                        Icon(Icons.Filled.Close, contentDescription = null)
                                        Spacer(Modifier.width(6.dp))
                                        Text("Отменить")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
