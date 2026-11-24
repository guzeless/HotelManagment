package com.example.hotelmanagement.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Card
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
import com.example.hotelmanagement.data.entity.PaymentEntity
import com.example.hotelmanagement.data.repository.Bill
import com.example.hotelmanagement.data.repository.HotelRepository
import com.example.hotelmanagement.ui.components.DropdownField
import com.example.hotelmanagement.ui.components.InfoRow
import com.example.hotelmanagement.ui.components.ScreenScaffold
import com.example.hotelmanagement.ui.components.SectionTitle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel экрана "Выставить счёт" (аналог PaymentForm):
 * расчёт суммы к оплате, сохранение платежа и выселение клиента.
 */
class PaymentViewModel(private val repository: HotelRepository) : ViewModel() {
    var guests by mutableStateOf<List<CheckInDetails>>(emptyList())
    var bill by mutableStateOf<Bill?>(null)
    val message = MutableStateFlow<String?>(null)

    /** Загрузка заселённых клиентов (аналог LoadCheckedInClients). */
    fun loadGuests() {
        viewModelScope.launch { guests = repository.currentGuestsList() }
    }

    /** Загрузка данных о номере и услугах выбранного клиента. */
    fun selectGuest(name: String?) {
        val guest = guests.firstOrNull { "${it.clientName} (№ ${it.roomNumber})" == name }
        viewModelScope.launch {
            bill = guest?.let { repository.getBill(it.checkInId) }
        }
    }

    /** Оплата и выселение (аналог ProcessPayment + CheckOutClient). */
    fun processPayment(method: String) {
        val current = bill ?: run {
            message.value = "Выберите клиента"
            return
        }
        viewModelScope.launch {
            val ok = repository.processPayment(current.checkInId, method)
            message.value = if (ok) "Оплата ${"%.0f".format(current.total)} руб. принята, клиент выселён"
            else "Ошибка оплаты"
            bill = null
            loadGuests()
        }
    }
}

/**
 * Экран "Выставить счёт": клиент выбирается из списка текущих постояльцев,
 * остальные данные загружаются автоматически. Кнопка "Оплатить и Выселить"
 * сохраняет платеж, освобождает комнату, гость остаётся в базе клиентов.
 */
@Composable
fun PaymentScreen(onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as HotelApp
    val vm: PaymentViewModel = viewModel { PaymentViewModel(app.repository) }
    val message by vm.message.collectAsStateWithLifecycle()

    var selectedGuest by remember { mutableStateOf<String?>(null) }
    var selectedMethod by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { vm.loadGuests() }

    ScreenScaffold(title = "Выставить счёт", onBack = onBack) { modifier ->
        Column(
            modifier = modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DropdownField(
                label = "Текущий постоялец",
                options = vm.guests.map { "${it.clientName} (№ ${it.roomNumber})" },
                selected = selectedGuest,
                emptyText = "Нет заселённых клиентов",
                onSelect = { name ->
                    selectedGuest = name
                    vm.selectGuest(name)
                }
            )

            vm.bill?.let { bill ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        SectionTitle("Счёт (договор ${bill.contractNumber})")
                        InfoRow("Клиент", bill.clientName)
                        InfoRow("Номер", "№ ${bill.roomNumber} (${bill.roomType})")
                        InfoRow("Проживание", "${bill.nights} сут. × ${"%.0f".format(bill.roomCost / bill.nights)} руб.")
                        InfoRow("Стоимость номера", "${"%.0f".format(bill.roomCost)} руб.")

                        if (bill.services.isNotEmpty()) {
                            SectionTitle("Дополнительные услуги")
                            bill.services.forEach { service ->
                                InfoRow(service.serviceName, "${"%.0f".format(service.price)} руб. (${service.status})")
                            }
                        }
                        InfoRow("Услуги всего", "${"%.0f".format(bill.servicesSum)} руб.")
                        InfoRow("ИТОГО К ОПЛАТЕ", "${"%.0f".format(bill.total)} руб.")
                    }
                }

                DropdownField(
                    label = "Способ оплаты",
                    options = PaymentEntity.METHODS,
                    selected = selectedMethod,
                    onSelect = { selectedMethod = it }
                )

                message?.let { Text(it, fontSize = 14.sp) }

                Button(
                    onClick = { vm.processPayment(selectedMethod ?: "Наличные") },
                    enabled = selectedMethod != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Paid, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Оплатить и Выселить", fontSize = 16.sp)
                }
            } ?: Text(
                "Выберите постояльца для формирования счёта",
                fontSize = 14.sp
            )
        }
    }
}
