package com.example.hotelmanagement.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.hotelmanagement.data.dao.BookingDao
import com.example.hotelmanagement.data.dao.CheckInDao
import com.example.hotelmanagement.data.dao.ClientDao
import com.example.hotelmanagement.data.dao.EmployeeDao
import com.example.hotelmanagement.data.dao.PaymentDao
import com.example.hotelmanagement.data.dao.RoomDao
import com.example.hotelmanagement.data.dao.ServiceDao
import com.example.hotelmanagement.data.dao.ServiceOrderDao
import com.example.hotelmanagement.data.entity.BookingEntity
import com.example.hotelmanagement.data.entity.CheckInEntity
import com.example.hotelmanagement.data.entity.ClientEntity
import com.example.hotelmanagement.data.entity.EmployeeEntity
import com.example.hotelmanagement.data.entity.PaymentEntity
import com.example.hotelmanagement.data.entity.RoomEntity
import com.example.hotelmanagement.data.entity.ServiceEntity
import com.example.hotelmanagement.data.entity.ServiceOrderEntity

/**
 * Главная база данных Room — содержит все 8 таблиц предметной области отеля.
 */
@Database(
    entities = [
        ClientEntity::class,
        RoomEntity::class,
        EmployeeEntity::class,
        BookingEntity::class,
        CheckInEntity::class,
        ServiceEntity::class,
        ServiceOrderEntity::class,
        PaymentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun clientDao(): ClientDao
    abstract fun roomDao(): RoomDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun bookingDao(): BookingDao
    abstract fun checkInDao(): CheckInDao
    abstract fun serviceDao(): ServiceDao
    abstract fun serviceOrderDao(): ServiceOrderDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hotel.db"
                ).build().also { INSTANCE = it }
            }
    }
}
