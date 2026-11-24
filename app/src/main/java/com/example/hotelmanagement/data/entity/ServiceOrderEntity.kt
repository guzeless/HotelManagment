package com.example.hotelmanagement.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Сущность "Заказ услуг" — услуга, заказанная постояльцем в рамках заселения.
 * Связи (1:М): Заселение -> Заказ услуг, Услуга -> Заказ услуг.
 * Статусы: "Выполняется", "Выполнен".
 */
@Entity(
    tableName = "ServiceOrders",
    foreignKeys = [
        ForeignKey(
            entity = CheckInEntity::class,
            parentColumns = ["CheckInId"],
            childColumns = ["CheckInId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = ServiceEntity::class,
            parentColumns = ["ServiceId"],
            childColumns = ["ServiceId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("CheckInId"),
        Index("ServiceId")
    ]
)
data class ServiceOrderEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ServiceOrderId")
    val serviceOrderId: Long = 0,

    @ColumnInfo(name = "CheckInId")
    val checkInId: Long,

    @ColumnInfo(name = "ServiceId")
    val serviceId: Long,

    @ColumnInfo(name = "Status")
    val status: String
) {
    companion object {
        const val STATUS_IN_PROGRESS = "Выполняется"
        const val STATUS_DONE = "Выполнен"
    }
}
