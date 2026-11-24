package com.example.hotelmanagement.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.hotelmanagement.data.entity.RoomEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с таблицей Rooms.
 */
@Dao
interface RoomDao {

    @Query("SELECT * FROM Rooms ORDER BY RoomNumber")
    fun getAll(): Flow<List<RoomEntity>>

    @Query("SELECT * FROM Rooms WHERE RoomId = :id")
    suspend fun getById(id: Long): RoomEntity?

    @Query("SELECT * FROM Rooms WHERE Status = :status ORDER BY RoomNumber")
    suspend fun getByStatus(status: String): List<RoomEntity>

    @Query("SELECT DISTINCT RoomType FROM Rooms")
    suspend fun getRoomTypes(): List<String>

    @Insert
    suspend fun insert(room: RoomEntity): Long

    @Update
    suspend fun update(room: RoomEntity)

    /** Обновление статуса номера: "Свободен" / "Забронирован" / "Занят". */
    @Query("UPDATE Rooms SET Status = :status WHERE RoomId = :id")
    suspend fun updateStatus(id: Long, status: String)

    /**
     * Номера, доступные для бронирования на интервал дат:
     * свободны сейчас и не имеют активных бронирований с пересечением интервала.
     */
    @Query(
        """
        SELECT * FROM Rooms
        WHERE Status = 'Свободен'
          AND RoomId NOT IN (
              SELECT RoomId FROM Bookings
              WHERE BookingStatus = 'Активно'
                AND CheckInDate < :checkOut
                AND CheckOutDate > :checkIn
          )
        ORDER BY RoomNumber
        """
    )
    suspend fun getAvailableRooms(checkIn: Long, checkOut: Long): List<RoomEntity>
}
