package com.example.hotelmanagement.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.hotelmanagement.data.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с таблицей Payments.
 */
@Dao
interface PaymentDao {

    @Query("SELECT * FROM Payments ORDER BY PaymentDate DESC")
    fun getAll(): Flow<List<PaymentEntity>>

    @Insert
    suspend fun insert(payment: PaymentEntity): Long
}
