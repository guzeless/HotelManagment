package com.example.hotelmanagement.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сущность "Клиент" — гость отеля.
 * Аналог таблицы Clients из реляционной схемы БД.
 */
@Entity(tableName = "Clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ClientId")
    val clientId: Long = 0,

    @ColumnInfo(name = "LastName")
    val lastName: String,

    @ColumnInfo(name = "FirstName")
    val firstName: String,

    @ColumnInfo(name = "MiddleName")
    val middleName: String? = null,

    @ColumnInfo(name = "PassportSeries")
    val passportSeries: String? = null,

    @ColumnInfo(name = "PassportNumber")
    val passportNumber: String? = null,

    @ColumnInfo(name = "Email")
    val email: String? = null,

    @ColumnInfo(name = "Phone")
    val phone: String? = null
)
