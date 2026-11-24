package com.example.hotelmanagement.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.hotelmanagement.data.entity.ServiceEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с таблицей Services.
 */
@Dao
interface ServiceDao {

    @Query("SELECT * FROM Services ORDER BY Name")
    fun getAll(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM Services WHERE ServiceId = :id")
    suspend fun getById(id: Long): ServiceEntity?

    @Insert
    suspend fun insert(service: ServiceEntity): Long
}
