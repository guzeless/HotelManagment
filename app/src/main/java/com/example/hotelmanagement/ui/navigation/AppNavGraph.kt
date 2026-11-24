package com.example.hotelmanagement.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hotelmanagement.ui.screens.BookingScreen
import com.example.hotelmanagement.ui.screens.BookingsListScreen
import com.example.hotelmanagement.ui.screens.CheckedInScreen
import com.example.hotelmanagement.ui.screens.ClientsScreen
import com.example.hotelmanagement.ui.screens.HomeScreen
import com.example.hotelmanagement.ui.screens.PaymentScreen
import com.example.hotelmanagement.ui.screens.RoomsScreen
import com.example.hotelmanagement.ui.screens.ServicesScreen

/** Маршруты экранов приложения (аналог форм WinForms). */
object Routes {
    const val HOME = "home"
    const val BOOKING = "booking"          // Бронирование
    const val BOOKINGS_LIST = "bookings"   // Список бронирований
    const val ROOMS = "rooms"              // Список номеров
    const val CLIENTS = "clients"          // База клиентов
    const val GUESTS = "guests"            // Текущие постояльцы
    const val SERVICES = "services"        // Заказ услуг
    const val PAYMENT = "payment"          // Выставить счёт
}

/**
 * Защищённый возврат назад: всплывание выполняется только один раз —
 * когда текущая запись стека в состоянии RESUMED. Это исключает
 * двойное всплывание (срыв главного экрана) при быстром двойном нажатии.
 */
private fun NavHostController.safePopBack() {
    val entry = currentBackStackEntry ?: return
    if (entry.lifecycle.currentState == Lifecycle.State.RESUMED &&
        previousBackStackEntry != null
    ) {
        popBackStack()
    }
}

/** Граф навигации приложения. */
@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigate = { route -> navController.navigate(route) },
                onExit = { (navController.context as? android.app.Activity)?.finish() }
            )
        }
        composable(Routes.BOOKING) { BookingScreen(onBack = { navController.safePopBack() }) }
        composable(Routes.BOOKINGS_LIST) { BookingsListScreen(onBack = { navController.safePopBack() }) }
        composable(Routes.ROOMS) { RoomsScreen(onBack = { navController.safePopBack() }) }
        composable(Routes.CLIENTS) { ClientsScreen(onBack = { navController.safePopBack() }) }
        composable(Routes.GUESTS) { CheckedInScreen(onBack = { navController.safePopBack() }) }
        composable(Routes.SERVICES) { ServicesScreen(onBack = { navController.safePopBack() }) }
        composable(Routes.PAYMENT) { PaymentScreen(onBack = { navController.safePopBack() }) }
    }
}
