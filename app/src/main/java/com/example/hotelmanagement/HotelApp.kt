package com.example.hotelmanagement

import android.app.Application
import com.example.hotelmanagement.data.db.AppDatabase
import com.example.hotelmanagement.data.db.DemoData
import com.example.hotelmanagement.data.repository.HotelRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Класс приложения — точка доступа к базе данных и репозиторию (синглтоны).
 */
class HotelApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.get(this) }
    val repository: HotelRepository by lazy { HotelRepository(database) }

    override fun onCreate() {
        super.onCreate()
        // Предзаполнение демо-данными при первом запуске
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            DemoData.prefill(database)
        }
    }
}
