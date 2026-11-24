package com.example.hotelmanagement.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сущность "Услуга" — дополнительная услуга отеля
 * (подача еды, уборка номеров, техническое обслуживание и т.п.).
 */
@Entity(tableName = "Services")
data class ServiceEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ServiceId")
    val serviceId: Long = 0,

    @ColumnInfo(name = "Name")
    val name: String,

    @ColumnInfo(name = "Description")
    val description: String? = null,

    @ColumnInfo(name = "Price")
    val price: Double
)
