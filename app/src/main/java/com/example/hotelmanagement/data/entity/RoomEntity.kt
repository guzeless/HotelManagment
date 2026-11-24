package com.example.hotelmanagement.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сущность "Комната" — номер отеля.
 * Статусы номера: "Свободен", "Забронирован", "Занят".
 */
@Entity(tableName = "Rooms")
data class RoomEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "RoomId")
    val roomId: Long = 0,

    @ColumnInfo(name = "RoomNumber")
    val roomNumber: String,

    @ColumnInfo(name = "RoomType")
    val roomType: String,

    @ColumnInfo(name = "PricePerDay")
    val pricePerDay: Double,

    @ColumnInfo(name = "Status")
    val status: String
) {
    companion object {
        const val STATUS_FREE = "Свободен"
        const val STATUS_BOOKED = "Забронирован"
        const val STATUS_OCCUPIED = "Занят"
    }
}
