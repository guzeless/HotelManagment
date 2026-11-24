package com.example.hotelmanagement.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.hotelmanagement.data.entity.ServiceOrderEntity
import kotlinx.coroutines.flow.Flow

/** Строка списка заказанных услуг с названием и ценой. */
data class ServiceOrderDetails(
    val serviceOrderId: Long,
    val checkInId: Long,
    val serviceName: String,
    val price: Double,
    val status: String
)

/**
 * DAO для работы с таблицей ServiceOrders.
 */
@Dao
interface ServiceOrderDao {

    @Query(
        """
        SELECT so.ServiceOrderId AS serviceOrderId, so.CheckInId AS checkInId,
               s.Name AS serviceName, s.Price AS price, so.Status AS status
        FROM ServiceOrders so
        JOIN Services s ON s.ServiceId = so.ServiceId
        ORDER BY so.ServiceOrderId DESC
        """
    )
    fun getAllWithDetails(): Flow<List<ServiceOrderDetails>>

    @Query(
        """
        SELECT so.ServiceOrderId AS serviceOrderId, so.CheckInId AS checkInId,
               s.Name AS serviceName, s.Price AS price, so.Status AS status
        FROM ServiceOrders so
        JOIN Services s ON s.ServiceId = so.ServiceId
        WHERE so.CheckInId = :checkInId
        ORDER BY so.ServiceOrderId DESC
        """
    )
    suspend fun getByCheckInId(checkInId: Long): List<ServiceOrderDetails>

    /** Сумма всех услуг по заселению (независимо от статуса выполнения). */
    @Query(
        """
        SELECT IFNULL(SUM(s.Price), 0.0)
        FROM ServiceOrders so
        JOIN Services s ON s.ServiceId = so.ServiceId
        WHERE so.CheckInId = :checkInId
        """
    )
    suspend fun servicesTotal(checkInId: Long): Double

    @Insert
    suspend fun insert(order: ServiceOrderEntity): Long

    /** Смена статуса: "Выполняется" -> "Выполнен". */
    @Query("UPDATE ServiceOrders SET Status = :status WHERE ServiceOrderId = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Update
    suspend fun update(order: ServiceOrderEntity)
}
