package com.example.hotelmanagement.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сущность "Сотрудник" — персонал отеля (администратор, горничная и т.д.).
 */
@Entity(tableName = "Employees")
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "EmployeeId")
    val employeeId: Long = 0,

    @ColumnInfo(name = "LastName")
    val lastName: String,

    @ColumnInfo(name = "FirstName")
    val firstName: String,

    @ColumnInfo(name = "MiddleName")
    val middleName: String? = null,

    @ColumnInfo(name = "Position")
    val position: String? = null,

    @ColumnInfo(name = "Phone")
    val phone: String? = null
)
