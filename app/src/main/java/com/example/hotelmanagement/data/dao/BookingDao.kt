package com.example.hotelmanagement.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.hotelmanagement.data.entity.BookingEntity
import kotlinx.coroutines.flow.Flow

/** Строка списка бронирований с данными клиента и номера. */
data class BookingDetails(
    val bookingId: Long,
    val clientId: Long,
    val clientName: String,
    val roomId: Long,
    val roomNumber: String,
    val checkInDate: Long,
    val checkOutDate: Long,
    val bookingStatus: String
)

/**
 * DAO для работы с таблицей Bookings.
 */
@Dao
interface BookingDao {

    @Query(
        """
        SELECT b.BookingId AS bookingId, b.ClientId AS clientId,
               c.LastName || ' ' || c.FirstName AS clientName,
               b.RoomId AS roomId, r.RoomNumber AS roomNumber,
               b.CheckInDate AS checkInDate, b.CheckOutDate AS checkOutDate,
               b.BookingStatus AS bookingStatus
        FROM Bookings b
        JOIN Clients c ON c.ClientId = b.ClientId
        JOIN Rooms r ON r.RoomId = b.RoomId
        ORDER BY b.CheckInDate DESC
        """
    )
    fun getAllWithDetails(): Flow<List<BookingDetails>>

    @Query("SELECT * FROM Bookings WHERE BookingId = :id")
    suspend fun getById(id: Long): BookingEntity?

    @Query(
        """
        SELECT * FROM Bookings
        WHERE BookingStatus = 'Активно'
          AND CheckInDate < :checkOut
          AND CheckOutDate > :checkIn
        """
    )
    suspend fun getActiveOverlapping(checkIn: Long, checkOut: Long): List<BookingEntity>

    @Insert
    suspend fun insert(booking: BookingEntity): Long

    /** Смена статуса бронирования: "Активно" / "Завершено" / "Отменено". */
    @Query("UPDATE Bookings SET BookingStatus = :status WHERE BookingId = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("DELETE FROM Bookings WHERE BookingId = :id")
    suspend fun deleteById(id: Long)
}
