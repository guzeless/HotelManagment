package com.example.hotelmanagement.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.hotelmanagement.data.entity.ClientEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с таблицей Clients.
 */
@Dao
interface ClientDao {

    @Query("SELECT * FROM Clients ORDER BY ClientId")
    fun getAll(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM Clients WHERE ClientId = :id")
    suspend fun getById(id: Long): ClientEntity?

    @Insert
    suspend fun insert(client: ClientEntity): Long

    /** Удаление клиента разрешено только при отсутствии связанных данных. */
    @Query("DELETE FROM Clients WHERE ClientId = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM Bookings WHERE ClientId = :clientId")
    suspend fun countBookings(clientId: Long): Int

    @Query("SELECT COUNT(*) FROM CheckIns WHERE ClientId = :clientId")
    suspend fun countCheckIns(clientId: Long): Int
}
