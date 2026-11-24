package com.example.hotelmanagement.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.hotelmanagement.data.dao.ServiceOrderDetails
import com.example.hotelmanagement.data.entity.ServiceEntity
import com.example.hotelmanagement.data.repository.HotelRepository
import com.example.hotelmanagement.ui.components.DropdownField
import com.example.hotelmanagement.ui.components.ScreenScaffold
import com.example.hotelmanagement.ui.components.StatusChip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel экрана "Заказ услуг" (аналог ServiceOrderForm):
 * выбор актуального заселения и услуги, отметка выполнения.
 */
class ServicesViewModel(private val repository: HotelRepository) : ViewModel() {
    val services: StateFlow<List<ServiceEntity>> = repository.services()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val orders: StateFlow<List<ServiceOrderDetails>> = repository.serviceOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val message = MutableStateFlow<String?>(null)
    var guests by mutableStateOf<List<CheckInDetails>>(emptyList())

    /** Загрузка текущих постояльцев для выпадающего списка. */
    fun loadGuests() {
        viewModelScope.launch { guests = repository.currentGuestsList() }
    }

    /** Заказ услуги: статус "Выполняется". */
    fun orderService(guestName: String?, serviceName: String?) {
        val guest = guests.firstOrNull {
            "${it.clientName} (№ ${it.roomNumber})" == guestName
        }
        val service = services.value.firstOrNull { it.name == serviceName }
        if (guest == null || service == null) {
            message.value = "Выберите постояльца и услугу"
            return
        }
        viewModelScope.launch {
            repository.orderService(guest.checkInId, service.serviceId)
            message.value = "Услуга \"${service.name}\" заказана"
        }
    }

    /** Отметка выполнения: статус "Выполнен". */
    fun markDone(orderId: Long) {
        viewModelScope.launch { repository.markOrderDone(orderId) }
    }
}

/**
 * Экран "Заказ услуг": панель заказа услуги и таблица заказанных услуг.
 * Галочка меняет статус заказа "Выполняется" -> "Выполнен".
 */
@Composable
fun ServicesScreen(onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as HotelApp
    val vm: ServicesViewModel = viewModel { ServicesViewModel(app.repository) }
    val services by vm.services.collectAsStateWithLifecycle()
    val orders by vm.orders.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()

    var selectedGuest by remember { mutableStateOf<String?>(null) }
    var selectedService by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { vm.loadGuests() }

    ScreenScaffold(title = "Заказ услуг", onBack = onBack) { modifier ->
        Column(modifier = modifier) {
            DropdownField(
                label = "Постоялец (заселение)",
                options = vm.guests.map { "${it.clientName} (№ ${it.roomNumber})" },
                selected = selectedGuest,
                emptyText = "Нет заселённых клиентов",
                onSelect = { selectedGuest = it }
            )
            DropdownField(
                label = "Услуга",
                options = services.map { "${it.name} — ${"%.0f".format(it.price)} руб." },
                selected = selectedService,
                onSelect = { selectedService = it.substringBefore(" — ") }
            )
            Button(
                onClick = { vm.orderService(selectedGuest, selectedService) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text("Заказать услугу")
            }
            message?.let { Text(it, fontSize = 14.sp) }

            Text("Заказанные услуги", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(orders, key = { it.serviceOrderId }) { order ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(order.serviceName, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    "${"%.0f".format(order.price)} руб., заселение #${order.checkInId}",
                                    fontSize = 13.sp
                                )
                            }
                            StatusChip(order.status)
                            Checkbox(
                                checked = order.status == "Выполнен",
                                onCheckedChange = { if (it) vm.markDone(order.serviceOrderId) }
                            )
                        }
                    }
                }
            }
        }
    }
}
