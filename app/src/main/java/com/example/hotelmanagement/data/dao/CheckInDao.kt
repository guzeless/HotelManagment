package com.example.hotelmanagement.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.hotelmanagement.data.entity.CheckInEntity
import kotlinx.coroutines.flow.Flow

/** Строка списка текущих постояльцев с данными клиента и номера. */
data class CheckInDetails(
    val checkInId: Long,
    val clientName: String,
    val roomNumber: String,
    val checkInDate: Long,
    val checkOutDate: Long?,
    val checkInStatus: String,
    val contractNumber: String,
    val totalCost: Double
)

/**
 * DAO для работы с таблицей CheckIns.
 */
@Dao
interface CheckInDao {

    @Query(
        """
        SELECT ci.CheckInId AS checkInId,
               c.LastName || ' ' || c.FirstName || ' ' || IFNULL(c.MiddleName, '') AS clientName,
               r.RoomNumber AS roomNumber,
               ci.CheckInDate AS checkInDate, ci.CheckOutDate AS checkOutDate,
               ci.CheckInStatus AS checkInStatus,
               ci.ContractNumber AS contractNumber, ci.TotalCost AS totalCost
        FROM CheckIns ci
        JOIN Clients c ON c.ClientId = ci.ClientId
        JOIN Rooms r ON r.RoomId = ci.RoomId
        ORDER BY ci.CheckInDate DESC
        """
    )
    fun getAllWithDetails(): Flow<List<CheckInDetails>>

    @Query(
        """
        SELECT ci.CheckInId AS checkInId,
               c.LastName || ' ' || c.FirstName || ' ' || IFNULL(c.MiddleName, '') AS clientName,
               r.RoomNumber AS roomNumber,
               ci.CheckInDate AS checkInDate, ci.CheckOutDate AS checkOutDate,
               ci.CheckInStatus AS checkInStatus,
               ci.ContractNumber AS contractNumber, ci.TotalCost AS totalCost
        FROM CheckIns ci
        JOIN Clients c ON c.ClientId = ci.ClientId
        JOIN Rooms r ON r.RoomId = ci.RoomId
        WHERE ci.CheckInStatus = 'Проживает'
        ORDER BY ci.CheckInDate DESC
        """
    )
    fun getCurrentGuests(): Flow<List<CheckInDetails>>

    @Query("SELECT * FROM CheckIns WHERE CheckInId = :id")
    suspend fun getById(id: Long): CheckInEntity?

    @Query("SELECT * FROM CheckIns WHERE BookingId = :bookingId LIMIT 1")
    suspend fun getByBookingId(bookingId: Long): CheckInEntity?

    @Insert
    suspend fun insert(checkIn: CheckInEntity): Long

    /** Обновление при выселении: статус, дата выселения, итоговая стоимость. */
    @Query(
        """
        UPDATE CheckIns
        SET CheckInStatus = :status, CheckOutDate = :checkOutDate, TotalCost = :totalCost
        WHERE CheckInId = :id
        """
    )
    suspend fun updateOnCheckOut(id: Long, status: String, checkOutDate: Long, totalCost: Double)
}
