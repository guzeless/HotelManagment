package com.example.hotelmanagement.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.hotelmanagement.data.entity.EmployeeEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с таблицей Employees.
 */
@Dao
interface EmployeeDao {

    @Query("SELECT * FROM Employees ORDER BY EmployeeId")
    fun getAll(): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM Employees WHERE EmployeeId = :id")
    suspend fun getById(id: Long): EmployeeEntity?

    /** Первый сотрудник (администратор по умолчанию) для оформления операций. */
    @Query("SELECT * FROM Employees ORDER BY EmployeeId LIMIT 1")
    suspend fun getFirst(): EmployeeEntity?

    @Insert
    suspend fun insert(employee: EmployeeEntity): Long
}
