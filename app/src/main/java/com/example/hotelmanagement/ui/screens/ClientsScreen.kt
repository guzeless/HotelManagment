package com.example.hotelmanagement.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.hotelmanagement.data.repository.HotelRepository
import com.example.hotelmanagement.ui.components.ScreenScaffold
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** ViewModel экрана "База клиентов". */
class ClientsViewModel(private val repository: HotelRepository) : ViewModel() {
    val clients: StateFlow<List<ClientEntity>> = repository.clients()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Удаление клиента (проверяет отсутствие бронирований/заселений). */
    suspend fun delete(clientId: Long): Boolean = repository.deleteClient(clientId)
}

/**
 * Экран "База клиентов" (аналог ClientForm):
 * все клиенты, когда-либо бронировавшие или проживавшие в отеле.
 * Удаление запрещено при наличии связанных бронирований или заселений.
 */
@Composable
fun ClientsScreen(onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as HotelApp
    val vm: ClientsViewModel = viewModel { ClientsViewModel(app.repository) }
    val clients by vm.clients.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ScreenScaffold(title = "База клиентов", onBack = onBack) { modifier ->
        Column(modifier = modifier) {
            SnackbarHost(hostState = snackbar)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(clients, key = { it.clientId }) { client ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(12.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${client.lastName} ${client.firstName} ${client.middleName.orEmpty()}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Паспорт: ${client.passportSeries.orEmpty()} ${client.passportNumber.orEmpty()}",
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Тел.: ${client.phone.orEmpty()}, email: ${client.email.orEmpty()}",
                                    fontSize = 13.sp
                                )
                            }
                            IconButton(onClick = {
                                scope.launch {
                                    val deleted = vm.delete(client.clientId)
                                    snackbar.showSnackbar(
                                        if (deleted) "Клиент удалён"
                                        else "Нельзя удалить: есть бронирования или заселения"
                                    )
                                }
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Удалить")
                            }
                        }
                    }
                }
            }
        }
    }
}
