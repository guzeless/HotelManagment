package com.example.hotelmanagement.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Сущность "Оплата" — платеж по заселению при выставлении счёта.
 * Связь (1:М): Заселение -> Оплата.
 * Способы оплаты: "Наличные", "Карта", "Перевод".
 */
@Entity(
    tableName = "Payments",
    foreignKeys = [
        ForeignKey(
            entity = CheckInEntity::class,
            parentColumns = ["CheckInId"],
            childColumns = ["CheckInId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("CheckInId")]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "PaymentId")
    val paymentId: Long = 0,

    @ColumnInfo(name = "CheckInId")
    val checkInId: Long,

    @ColumnInfo(name = "Amount")
    val amount: Double,

    @ColumnInfo(name = "PaymentDate")
    val paymentDate: Long,

    @ColumnInfo(name = "PaymentMethod")
    val paymentMethod: String
) {
    companion object {
        val METHODS = listOf("Наличные", "Карта", "Перевод")
    }
}
