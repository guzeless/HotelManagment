package com.example.hotelmanagement.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Сущность "Заселение".
 * Связь 1:1 с Бронированием (BookingId UNIQUE).
 * Связи (1:М): Клиент/Комната/Сотрудник -> Заселение.
 * Статусы: "Проживает", "Выселён".
 */
@Entity(
    tableName = "CheckIns",
    foreignKeys = [
        ForeignKey(
            entity = BookingEntity::class,
            parentColumns = ["BookingId"],
            childColumns = ["BookingId"],
            onDelete = ForeignKey.RESTRICT
        ),
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
        Index(value = ["BookingId"], unique = true),
        Index("ClientId"),
        Index("RoomId"),
        Index("EmployeeId")
    ]
)
data class CheckInEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "CheckInId")
    val checkInId: Long = 0,

    @ColumnInfo(name = "BookingId")
    val bookingId: Long,

    @ColumnInfo(name = "ClientId")
    val clientId: Long,

    @ColumnInfo(name = "RoomId")
    val roomId: Long,

    @ColumnInfo(name = "EmployeeId")
    val employeeId: Long,

    @ColumnInfo(name = "CheckInDate")
    val checkInDate: Long,

    @ColumnInfo(name = "CheckOutDate")
    val checkOutDate: Long? = null,

    @ColumnInfo(name = "CheckInStatus")
    val checkInStatus: String,

    @ColumnInfo(name = "ContractNumber")
    val contractNumber: String,

    @ColumnInfo(name = "TotalCost")
    val totalCost: Double = 0.0
) {
    companion object {
        const val STATUS_STAYING = "Проживает"
        const val STATUS_CHECKED_OUT = "Выселён"
    }
}
