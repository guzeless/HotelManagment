package com.example.hotelmanagement.data.repository

import com.example.hotelmanagement.data.dao.BookingDetails
import com.example.hotelmanagement.data.dao.CheckInDao
import com.example.hotelmanagement.data.dao.CheckInDetails
import com.example.hotelmanagement.data.dao.ServiceOrderDetails
import com.example.hotelmanagement.data.db.AppDatabase
import com.example.hotelmanagement.data.entity.BookingEntity
import com.example.hotelmanagement.data.entity.CheckInEntity
import com.example.hotelmanagement.data.entity.ClientEntity
import com.example.hotelmanagement.data.entity.PaymentEntity
import com.example.hotelmanagement.data.entity.RoomEntity
import com.example.hotelmanagement.data.entity.ServiceOrderEntity
import com.example.hotelmanagement.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/** Счёт гостя при выезде: стоимость номера + услуги. */
data class Bill(
    val checkInId: Long,
    val clientName: String,
    val roomNumber: String,
    val roomType: String,
    val contractNumber: String,
    val nights: Int,
    val roomCost: Double,
    val services: List<ServiceOrderDetails>,
    val servicesSum: Double,
    val total: Double
)

/**
 * Репозиторий — инкапсулирует всю бизнес-логику работы с БД отеля.
 * Реализует бизнес-правила: смена статусов номеров, контроль 1:1
 * "бронирование -> заселение", расчёт счёта и выселение.
 */
class HotelRepository(db: AppDatabase) {

    private val roomDao = db.roomDao()
    private val clientDao = db.clientDao()
    private val employeeDao = db.employeeDao()
    private val bookingDao = db.bookingDao()
    private val checkInDao: CheckInDao = db.checkInDao()
    private val serviceDao = db.serviceDao()
    private val serviceOrderDao = db.serviceOrderDao()
    private val paymentDao = db.paymentDao()

    // ---------- Номера ----------

    fun rooms(): Flow<List<RoomEntity>> = roomDao.getAll()

    suspend fun updateRoomStatus(roomId: Long, status: String) =
        roomDao.updateStatus(roomId, status)

    // ---------- Клиенты ----------

    fun clients(): Flow<List<ClientEntity>> = clientDao.getAll()

    /** Удаление клиента — запрещено при наличии бронирований/заселений. */
    suspend fun deleteClient(clientId: Long): Boolean {
        if (clientDao.countBookings(clientId) > 0) return false
        if (clientDao.countCheckIns(clientId) > 0) return false
        clientDao.deleteById(clientId)
        return true
    }

    suspend fun findClientByPassport(series: String, number: String): ClientEntity? =
        clientDao.getAll().first().firstOrNull {
            it.passportSeries == series && it.passportNumber == number
        }

    // ---------- Бронирование ----------

    fun bookings(): Flow<List<BookingDetails>> = bookingDao.getAllWithDetails()

    /** Свободные типы номеров на выбранный интервал дат. */
    suspend fun getAvailableRooms(checkIn: LocalDate, checkOut: LocalDate): List<RoomEntity> =
        roomDao.getAvailableRooms(DateUtils.toMillis(checkIn), DateUtils.toMillis(checkOut))

    /**
     * Оформление бронирования: сохранение клиента (поиск по паспорту),
     * вставка бронирования и перевод номера в статус "Забронирован".
     */
    suspend fun createBooking(
        client: ClientEntity,
        roomId: Long,
        checkIn: LocalDate,
        checkOut: LocalDate
    ): Long {
        val existing = findClientByPassport(
            client.passportSeries.orEmpty(),
            client.passportNumber.orEmpty()
        )
        val clientId = existing?.clientId ?: clientDao.insert(client)
        val employee = employeeDao.getFirst()
        val bookingId = bookingDao.insert(
            BookingEntity(
                clientId = clientId,
                roomId = roomId,
                employeeId = employee?.employeeId,
                checkInDate = DateUtils.toMillis(checkIn),
                checkOutDate = DateUtils.toMillis(checkOut),
                bookingStatus = BookingEntity.STATUS_ACTIVE
            )
        )
        roomDao.updateStatus(roomId, RoomEntity.STATUS_BOOKED)
        return bookingId
    }

    /** Отмена бронирования: освобождение номера и удаление записи. */
    suspend fun cancelBooking(bookingId: Long): Boolean {
        val booking = bookingDao.getById(bookingId) ?: return false
        if (booking.bookingStatus != BookingEntity.STATUS_ACTIVE) return false
        roomDao.updateStatus(booking.roomId, RoomEntity.STATUS_FREE)
        bookingDao.deleteById(bookingId)
        return true
    }

    /**
     * Заселение из бронирования: создание заселения (связь 1:1),
     * статус бронирования -> "Завершено", номер -> "Занят".
     */
    suspend fun checkInFromBooking(bookingId: Long): Boolean {
        val booking = bookingDao.getById(bookingId) ?: return false
        if (booking.bookingStatus != BookingEntity.STATUS_ACTIVE) return false
        if (checkInDao.getByBookingId(bookingId) != null) return false
        val employee = employeeDao.getFirst() ?: return false

        checkInDao.insert(
            CheckInEntity(
                bookingId = booking.bookingId,
                clientId = booking.clientId,
                roomId = booking.roomId,
                employeeId = employee.employeeId,
                checkInDate = DateUtils.toMillis(DateUtils.today()),
                checkOutDate = booking.checkOutDate,
                checkInStatus = CheckInEntity.STATUS_STAYING,
                contractNumber = "ДГ-${booking.bookingId}"
            )
        )
        bookingDao.updateStatus(bookingId, BookingEntity.STATUS_COMPLETED)
        roomDao.updateStatus(booking.roomId, RoomEntity.STATUS_OCCUPIED)
        return true
    }

    // ---------- Заселение / постояльцы ----------

    fun currentGuests(): Flow<List<CheckInDetails>> = checkInDao.getCurrentGuests()

    fun allCheckIns(): Flow<List<CheckInDetails>> = checkInDao.getAllWithDetails()

    /** Список текущих постояльцев (для выпадающих списков). */
    suspend fun currentGuestsList(): List<CheckInDetails> =
        checkInDao.getCurrentGuests().first()

    // ---------- Услуги ----------

    fun services() = serviceDao.getAll()

    fun serviceOrders(): Flow<List<ServiceOrderDetails>> = serviceOrderDao.getAllWithDetails()

    /** Заказ услуги постояльцем: статус по умолчанию "Выполняется". */
    suspend fun orderService(checkInId: Long, serviceId: Long) =
        serviceOrderDao.insert(
            ServiceOrderEntity(
                checkInId = checkInId,
                serviceId = serviceId,
                status = ServiceOrderEntity.STATUS_IN_PROGRESS
            )
        )

    /** Отметка выполнения заказа услуги: "Выполняется" -> "Выполнен". */
    suspend fun markOrderDone(orderId: Long) =
        serviceOrderDao.updateStatus(orderId, ServiceOrderEntity.STATUS_DONE)

    // ---------- Счёт, оплата, выселение ----------

    /** Формирование счёта: номер за период + все заказанные услуги. */
    suspend fun getBill(checkInId: Long): Bill? {
        val checkIn = checkInDao.getById(checkInId) ?: return null
        val room = roomDao.getById(checkIn.roomId) ?: return null
        val client = clientDao.getById(checkIn.clientId) ?: return null
        val orders = serviceOrderDao.getByCheckInId(checkInId)

        val nights = DateUtils.nights(
            DateUtils.fromMillis(checkIn.checkInDate),
            checkIn.checkOutDate?.let { DateUtils.fromMillis(it) } ?: DateUtils.today()
        )
        val roomCost = nights * room.pricePerDay
        val servicesSum = orders.sumOf { it.price }

        return Bill(
            checkInId = checkInId,
            clientName = listOfNotNull(
                client.lastName, client.firstName, client.middleName
            ).joinToString(" ").trim(),
            roomNumber = room.roomNumber,
            roomType = room.roomType,
            contractNumber = checkIn.contractNumber,
            nights = nights,
            roomCost = roomCost,
            services = orders,
            servicesSum = servicesSum,
            total = roomCost + servicesSum
        )
    }

    /**
     * Оплата и выселение: сохранение платежа, статус заселения -> "Выселён",
     * итоговая стоимость фиксируется, номер освобождается.
     * Гость остаётся в базе клиентов.
     */
    suspend fun processPayment(checkInId: Long, method: String): Boolean {
        val checkIn = checkInDao.getById(checkInId) ?: return false
        val bill = getBill(checkInId) ?: return false

        paymentDao.insert(
            PaymentEntity(
                checkInId = checkInId,
                amount = bill.total,
                paymentDate = DateUtils.toMillis(DateUtils.today()),
                paymentMethod = method
            )
        )
        checkInDao.updateOnCheckOut(
            id = checkInId,
            status = CheckInEntity.STATUS_CHECKED_OUT,
            checkOutDate = DateUtils.toMillis(DateUtils.today()),
            totalCost = bill.total
        )
        roomDao.updateStatus(checkIn.roomId, RoomEntity.STATUS_FREE)
        return true
    }
}
