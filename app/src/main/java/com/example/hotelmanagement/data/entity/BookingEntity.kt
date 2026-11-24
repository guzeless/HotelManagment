package com.example.hotelmanagement.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Сущность "Бронирование".
 * Связи (1:М): Клиент -> Бронирование, Комната -> Бронирование, Сотрудник -> Бронирование.
 * Даты хранятся как Long (epoch millis).
 * Статусы: "Активно", "Завершено", "Отменено".
 */
@Entity(
    tableName = "Bookings",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["ClientId"],
            childColumns = ["ClientId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = RoomEntity::class,
            parentColumns = ["RoomId"],
            childColumns = ["RoomId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = EmployeeEntity::class,
            parentColumns = ["EmployeeId"],
            childColumns = ["EmployeeId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("ClientId"),
        Index("RoomId"),
        Index("EmployeeId")
    ]
)
data class BookingEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "BookingId")
    val bookingId: Long = 0,

    @ColumnInfo(name = "ClientId")
    val clientId: Long,

    @ColumnInfo(name = "RoomId")
    val roomId: Long,

    @ColumnInfo(name = "EmployeeId")
    val employeeId: Long? = null,

    @ColumnInfo(name = "CheckInDate")
    val checkInDate: Long,

    @ColumnInfo(name = "CheckOutDate")
    val checkOutDate: Long,

    @ColumnInfo(name = "BookingStatus")
    val bookingStatus: String
) {
    companion object {
        const val STATUS_ACTIVE = "Активно"
        const val STATUS_COMPLETED = "Завершено"
        const val STATUS_CANCELLED = "Отменено"
    }
}
