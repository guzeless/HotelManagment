package com.example.hotelmanagement.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.hotelmanagement.data.entity.ClientEntity
import com.example.hotelmanagement.data.entity.RoomEntity
import com.example.hotelmanagement.data.repository.HotelRepository
import com.example.hotelmanagement.ui.components.DatePickerField
import com.example.hotelmanagement.ui.components.DropdownField
import com.example.hotelmanagement.ui.components.InfoRow
import com.example.hotelmanagement.ui.components.ScreenScaffold
import com.example.hotelmanagement.ui.components.SectionTitle
import com.example.hotelmanagement.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * ViewModel экрана "Бронирование" (аналог BookingForm):
 * загрузка свободных типов/номеров на даты, расчёт стоимости,
 * сохранение клиента и бронирования.
 */
class BookingViewModel(private val repository: HotelRepository) : ViewModel() {

    // Даты проживания
    var checkInDate by mutableStateOf(DateUtils.today())
    var checkOutDate by mutableStateOf(DateUtils.today().plusDays(1))

    // Доступные номера и выбор
    var availableRooms by mutableStateOf<List<RoomEntity>>(emptyList())
    var selectedRoomType by mutableStateOf<String?>(null)
    var selectedRoom by mutableStateOf<RoomEntity?>(null)

    // Клиент: новый или из базы
    var useExistingClient by mutableStateOf(false)
    var clients by mutableStateOf<List<ClientEntity>>(emptyList())
    var selectedClientName by mutableStateOf<String?>(null)

    // Поля нового клиента
    var lastName by mutableStateOf("")
    var firstName by mutableStateOf("")
    var middleName by mutableStateOf("")
    var passportSeries by mutableStateOf("")
    var passportNumber by mutableStateOf("")
    var email by mutableStateOf("")
    var phone by mutableStateOf("")

    val message = MutableStateFlow<String?>(null)

    /** Загрузка свободных номеров на выбранные даты (аналог LoadAvailableRooms). */
    fun loadRooms() {
        viewModelScope.launch {
            availableRooms = repository.getAvailableRooms(checkInDate, checkOutDate)
            selectedRoomType = null
            selectedRoom = null
        }
        viewModelScope.launch {
            clients = repository.clients().first()
        }
    }

    /** Свободные типы номеров (аналог LoadRoomTypes). */
    val roomTypes: List<String>
        get() = availableRooms.map { it.roomType }.distinct()

    /** Номера выбранного типа (аналог type_SelectedIndexChanged). */
    fun roomsOfType(type: String): List<RoomEntity> =
        availableRooms.filter { it.roomType == type }

    /** Количество суток. */
    val nights: Int
        get() = DateUtils.nights(checkInDate, checkOutDate)

    /** Расчёт и вывод стоимости (аналог ShowPriceInfo). */
    val priceText: String
        get() = selectedRoom?.let {
            "Итого: $nights сут. × ${"%.0f".format(it.pricePerDay)} руб. = ${"%.0f".format(nights * it.pricePerDay)} руб."
        } ?: "Стоимость: выберите номер"

    /** Проверка корректности введённых данных (аналог ValidateInputs). */
    private fun validateInputs(): String? = when {
        checkOutDate <= checkInDate -> "Дата выезда должна быть позже даты заезда"
        selectedRoom == null -> "Выберите номер"
        !useExistingClient && lastName.isBlank() -> "Укажите фамилию клиента"
        !useExistingClient && firstName.isBlank() -> "Укажите имя клиента"
        !useExistingClient && passportSeries.isBlank() -> "Укажите серию паспорта"
        !useExistingClient && passportNumber.isBlank() -> "Укажите номер паспорта"
        useExistingClient && selectedClientName == null -> "Выберите клиента из базы"
        else -> null
    }

    /** Создание бронирования (аналог button1_Click + SaveClientToDatabase + SaveBookingToDatabase). */
    fun createBooking() {
        val error = validateInputs()
        if (error != null) {
            message.value = error
            return
        }
        viewModelScope.launch {
            val client: ClientEntity = if (useExistingClient) {
                clients.first { "${it.lastName} ${it.firstName}" == selectedClientName }
            } else {
                ClientEntity(
                    lastName = lastName.trim(),
                    firstName = firstName.trim(),
                    middleName = middleName.trim().ifEmpty { null },
                    passportSeries = passportSeries.trim(),
                    passportNumber = passportNumber.trim(),
                    email = email.trim().ifEmpty { null },
                    phone = phone.trim().ifEmpty { null }
                )
            }
            val roomId = selectedRoom!!.roomId
            repository.createBooking(client, roomId, checkInDate, checkOutDate)
            message.value = "Бронирование оформлено: № ${selectedRoom!!.roomNumber}"
            // Очистка формы
            lastName = ""; firstName = ""; middleName = ""
            passportSeries = ""; passportNumber = ""; email = ""; phone = ""
            selectedClientName = null
            loadRooms()
        }
    }
}

/**
 * Экран "Бронирование": данные клиента, даты заезда/выезда, тип и номер,
 * расчёт стоимости и кнопка создания бронирования.
 */
@Composable
fun BookingScreen(onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as HotelApp
    val vm: BookingViewModel = viewModel { BookingViewModel(app.repository) }
    val message by vm.message.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.loadRooms() }

    ScreenScaffold(title = "Новое бронирование", onBack = onBack) { modifier ->
        Column(
            modifier = modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionTitle("Даты проживания")
            DatePickerField("Дата заезда", DateUtils.toMillis(vm.checkInDate)) { millis ->
                vm.checkInDate = DateUtils.fromMillis(millis)
                if (vm.checkOutDate <= vm.checkInDate) vm.checkOutDate = vm.checkInDate.plusDays(1)
                vm.loadRooms()
            }
            DatePickerField("Дата выезда", DateUtils.toMillis(vm.checkOutDate)) { millis ->
                vm.checkOutDate = DateUtils.fromMillis(millis)
                vm.loadRooms()
            }

            SectionTitle("Клиент")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Клиент из базы", fontSize = 14.sp)
                Switch(
                    checked = vm.useExistingClient,
                    onCheckedChange = { vm.useExistingClient = it }
                )
            }
            if (vm.useExistingClient) {
                DropdownField(
                    label = "Выберите клиента",
                    options = vm.clients.map { "${it.lastName} ${it.firstName}" },
                    selected = vm.selectedClientName,
                    onSelect = { vm.selectedClientName = it }
                )
            } else {
                androidx.compose.material3.OutlinedTextField(
                    value = vm.lastName, onValueChange = { vm.lastName = it },
                    label = { Text("Фамилия *") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                androidx.compose.material3.OutlinedTextField(
                    value = vm.firstName, onValueChange = { vm.firstName = it },
                    label = { Text("Имя *") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                androidx.compose.material3.OutlinedTextField(
                    value = vm.middleName, onValueChange = { vm.middleName = it },
                    label = { Text("Отчество") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    androidx.compose.material3.OutlinedTextField(
                        value = vm.passportSeries, onValueChange = { vm.passportSeries = it },
                        label = { Text("Серия паспорта *") },
                        modifier = Modifier.weight(1f), singleLine = true
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = vm.passportNumber, onValueChange = { vm.passportNumber = it },
                        label = { Text("Номер паспорта *") },
                        modifier = Modifier.weight(1f), singleLine = true
                    )
                }
                androidx.compose.material3.OutlinedTextField(
                    value = vm.email, onValueChange = { vm.email = it },
                    label = { Text("Электронная почта") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                androidx.compose.material3.OutlinedTextField(
                    value = vm.phone, onValueChange = { vm.phone = it },
                    label = { Text("Телефон") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                )
            }

            SectionTitle("Номер")
            DropdownField(
                label = "Тип номера",
                options = vm.roomTypes,
                selected = vm.selectedRoomType,
                emptyText = "Нет свободных номеров на даты",
                onSelect = { vm.selectedRoomType = it; vm.selectedRoom = null }
            )
            DropdownField(
                label = "Номер",
                options = vm.selectedRoomType?.let { vm.roomsOfType(it).map { r -> "№ ${r.roomNumber}" } } ?: emptyList(),
                selected = vm.selectedRoom?.let { "№ ${it.roomNumber}" },
                enabled = vm.selectedRoomType != null,
                emptyText = "Сначала выберите тип",
                onSelect = { num ->
                    vm.selectedRoom = vm.roomsOfType(vm.selectedRoomType!!)
                        .firstOrNull { "№ ${it.roomNumber}" == num }
                }
            )

            InfoRow("Суток", vm.nights.toString())
            Text(vm.priceText, fontSize = 15.sp, fontWeight = FontWeight.Bold)

            message?.let {
                Text(it, fontSize = 14.sp, color = androidx.compose.ui.graphics.Color(0xFF2E7D32))
            }

            Button(
                onClick = { vm.createBooking() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Event, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Забронировать", fontSize = 16.sp)
            }
        }
    }
}
