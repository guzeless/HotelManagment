package com.example.hotelmanagement.data.db

import com.example.hotelmanagement.data.entity.BookingEntity
import com.example.hotelmanagement.data.entity.CheckInEntity
import com.example.hotelmanagement.data.entity.ClientEntity
import com.example.hotelmanagement.data.entity.EmployeeEntity
import com.example.hotelmanagement.data.entity.PaymentEntity
import com.example.hotelmanagement.data.entity.RoomEntity
import com.example.hotelmanagement.data.entity.ServiceEntity
import com.example.hotelmanagement.data.entity.ServiceOrderEntity
import com.example.hotelmanagement.util.DateUtils
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId

/**
 * Демо-данные для наглядной демонстрации приложения на защите.
 * Заполняются один раз — только если таблицы пусты (при первом запуске).
 *
 * Сценарий: 10 клиентов, 10 сотрудников, 10 номеров, 3 активных бронирования
 * (номера «Забронирован»), 2 текущих постояльца (номера «Занят»),
 * 1 выселившийся гость с оплаченным счётом, 4 заказа услуг.
 * Чтобы перегенерировать данные — удалить приложение (сброс БД) и установить заново.
 */
object DemoData {

    private fun millis(date: LocalDate): Long =
        date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun daysFromToday(offset: Long): LocalDate = DateUtils.today().plusDays(offset)

    suspend fun prefill(db: AppDatabase) {
        val roomDao = db.roomDao()
        val employeeDao = db.employeeDao()
        val serviceDao = db.serviceDao()
        val clientDao = db.clientDao()
        val bookingDao = db.bookingDao()
        val checkInDao = db.checkInDao()
        val serviceOrderDao = db.serviceOrderDao()
        val paymentDao = db.paymentDao()

        // ---------- Комнаты отеля: 10 номеров ----------
        val roomIds = mutableMapOf<String, Long>()
        if (roomDao.getRoomTypes().isEmpty()) {
            val rooms = mutableListOf<RoomEntity>()
            // Стандартные (101-104, цена 3500)
            for (i in 101..104) {
                rooms += RoomEntity(
                    roomNumber = i.toString(),
                    roomType = "Стандарт",
                    pricePerDay = 3500.0,
                    status = RoomEntity.STATUS_FREE
                )
            }
            // Полулюкс (201-203, цена 5500)
            for (i in 201..203) {
                rooms += RoomEntity(
                    roomNumber = i.toString(),
                    roomType = "Полулюкс",
                    pricePerDay = 5500.0,
                    status = RoomEntity.STATUS_FREE
                )
            }
            // Люкс (301-303, цена 9000)
            for (i in 301..303) {
                rooms += RoomEntity(
                    roomNumber = i.toString(),
                    roomType = "Люкс",
                    pricePerDay = 9000.0,
                    status = RoomEntity.STATUS_FREE
                )
            }
            rooms.forEach { roomIds[it.roomNumber] = roomDao.insert(it) }
        }

        // ---------- Сотрудники: 10 записей ----------
        val employeeIds = mutableListOf<Long>()
        if (employeeDao.getFirst() == null) {
            listOf(
                EmployeeEntity(lastName = "Иванова", firstName = "Анна", middleName = "Сергеевна", position = "Администратор", phone = "+7 (900) 111-22-33"),
                EmployeeEntity(lastName = "Петров", firstName = "Дмитрий", middleName = "Игоревич", position = "Портье", phone = "+7 (900) 222-33-44"),
                EmployeeEntity(lastName = "Соколова", firstName = "Елена", middleName = "Викторовна", position = "Администратор", phone = "+7 (900) 333-44-55"),
                EmployeeEntity(lastName = "Козлов", firstName = "Сергей", middleName = "Анатольевич", position = "Горничная", phone = "+7 (900) 444-55-66"),
                EmployeeEntity(lastName = "Морозова", firstName = "Ольга", middleName = "Дмитриевна", position = "Горничная", phone = "+7 (900) 555-66-77"),
                EmployeeEntity(lastName = "Волков", firstName = "Андрей", middleName = "Николаевич", position = "Техник", phone = "+7 (900) 666-77-88"),
                EmployeeEntity(lastName = "Зайцева", firstName = "Татьяна", middleName = "Ивановна", position = "Горничная", phone = "+7 (900) 777-88-99"),
                EmployeeEntity(lastName = "Егоров", firstName = "Максим", middleName = "Олегович", position = "Официант", phone = "+7 (900) 888-99-00"),
                EmployeeEntity(lastName = "Павлова", firstName = "Наталья", middleName = "Львовна", position = "Повар", phone = "+7 (900) 999-00-11"),
                EmployeeEntity(lastName = "Новиков", firstName = "Виктор", middleName = "Степанович", position = "Охранник", phone = "+7 (900) 101-01-01")
            ).forEach { employeeIds += employeeDao.insert(it) }
        }

        // ---------- Услуги: 5 записей ----------
        val serviceIds = mutableListOf<Long>()
        if (serviceDao.getAll().first().isEmpty()) {
            listOf(
                ServiceEntity(name = "Завтрак в номер", description = "Доставка завтрака в номер", price = 500.0),
                ServiceEntity(name = "Уборка номера", description = "Ежедневная влажная уборка", price = 300.0),
                ServiceEntity(name = "Смена белья", description = "Комплексная замена постельного белья", price = 250.0),
                ServiceEntity(name = "Техническое обслуживание", description = "Устранение неисправностей в номере", price = 0.0),
                ServiceEntity(name = "Трансфер", description = "Трансфер до аэропорта / вокзала", price = 1200.0)
            ).forEach { serviceIds += serviceDao.insert(it) }
        }

        // ---------- Клиенты: 10 записей ----------
        val clientIds = mutableListOf<Long>()
        if (clientDao.getAll().first().isEmpty()) {
            listOf(
                ClientEntity(
                    lastName = "Смирнов", firstName = "Алексей", middleName = "Владимирович",
                    passportSeries = "4508", passportNumber = "123456",
                    email = "smirnov@mail.ru", phone = "+7 (910) 555-11-22"
                ),
                ClientEntity(
                    lastName = "Кузнецова", firstName = "Мария", middleName = "Андреевна",
                    passportSeries = "4510", passportNumber = "654321",
                    email = "kuznetsova@mail.ru", phone = "+7 (915) 666-33-44"
                ),
                ClientEntity(
                    lastName = "Орлов", firstName = "Игорь", middleName = "Петрович",
                    passportSeries = "4512", passportNumber = "112233",
                    email = "orlov@mail.ru", phone = "+7 (920) 777-55-66"
                ),
                ClientEntity(
                    lastName = "Васильев", firstName = "Павел", middleName = "Романович",
                    passportSeries = "4514", passportNumber = "223344",
                    email = "vasilyev@mail.ru", phone = "+7 (925) 111-22-44"
                ),
                ClientEntity(
                    lastName = "Никитина", firstName = "Дарья", middleName = "Евгеньевна",
                    passportSeries = "4516", passportNumber = "334455",
                    email = "nikitina@mail.ru", phone = "+7 (930) 222-33-55"
                ),
                ClientEntity(
                    lastName = "Федоров", firstName = "Артем", middleName = "Сергеевич",
                    passportSeries = "4518", passportNumber = "445566",
                    email = "fedorov@mail.ru", phone = "+7 (935) 333-44-66"
                ),
                ClientEntity(
                    lastName = "Беляева", firstName = "Софья", middleName = "Александровна",
                    passportSeries = "4520", passportNumber = "556677",
                    email = "belyaeva@mail.ru", phone = "+7 (940) 444-55-77"
                ),
                ClientEntity(
                    lastName = "Тарасов", firstName = "Николай", middleName = "Владимирович",
                    passportSeries = "4522", passportNumber = "667788",
                    email = "tarasov@mail.ru", phone = "+7 (945) 555-66-88"
                ),
                ClientEntity(
                    lastName = "Романова", firstName = "Ксения", middleName = "Павловна",
                    passportSeries = "4524", passportNumber = "778899",
                    email = "romanova@mail.ru", phone = "+7 (950) 666-77-99"
                ),
                ClientEntity(
                    lastName = "Гусев", firstName = "Даниил", middleName = "Ильич",
                    passportSeries = "4526", passportNumber = "889900",
                    email = "gusev@mail.ru", phone = "+7 (955) 777-88-00"
                )
            ).forEach { clientIds += clientDao.insert(it) }
        }

        // ---------- Демо-сценарий: бронирования, заселения, услуги, оплата ----------
        if (roomIds.isNotEmpty() && clientIds.isNotEmpty() && employeeIds.isNotEmpty()) {
            val admin = employeeIds[0] // Иванова, администратор

            // Активные бронирования (номера «Забронирован», можно «Заселить»)
            val booking101 = bookingDao.insert(
                BookingEntity(
                    clientId = clientIds[0], roomId = roomIds["101"]!!, employeeId = admin,
                    checkInDate = millis(daysFromToday(3)), checkOutDate = millis(daysFromToday(6)),
                    bookingStatus = BookingEntity.STATUS_ACTIVE
                )
            )
            val booking104 = bookingDao.insert(
                BookingEntity(
                    clientId = clientIds[2], roomId = roomIds["104"]!!, employeeId = admin,
                    checkInDate = millis(daysFromToday(10)), checkOutDate = millis(daysFromToday(12)),
                    bookingStatus = BookingEntity.STATUS_ACTIVE
                )
            )
            val booking201 = bookingDao.insert(
                BookingEntity(
                    clientId = clientIds[4], roomId = roomIds["201"]!!, employeeId = admin,
                    checkInDate = millis(daysFromToday(1)), checkOutDate = millis(daysFromToday(4)),
                    bookingStatus = BookingEntity.STATUS_ACTIVE
                )
            )
            roomDao.updateStatus(roomIds["101"]!!, RoomEntity.STATUS_BOOKED)
            roomDao.updateStatus(roomIds["104"]!!, RoomEntity.STATUS_BOOKED)
            roomDao.updateStatus(roomIds["201"]!!, RoomEntity.STATUS_BOOKED)

            // Текущие постояльцы (бронирование -> Завершено, номер -> Занят)
            val booking102 = bookingDao.insert(
                BookingEntity(
                    clientId = clientIds[1], roomId = roomIds["102"]!!, employeeId = admin,
                    checkInDate = millis(daysFromToday(-2)), checkOutDate = millis(daysFromToday(2)),
                    bookingStatus = BookingEntity.STATUS_ACTIVE
                )
            )
            val booking103 = bookingDao.insert(
                BookingEntity(
                    clientId = clientIds[3], roomId = roomIds["103"]!!, employeeId = admin,
                    checkInDate = millis(daysFromToday(-1)), checkOutDate = millis(daysFromToday(3)),
                    bookingStatus = BookingEntity.STATUS_ACTIVE
                )
            )
            checkInDao.insert(
                CheckInEntity(
                    bookingId = booking102, clientId = clientIds[1], roomId = roomIds["102"]!!,
                    employeeId = admin, checkInDate = millis(daysFromToday(-2)),
                    checkOutDate = millis(daysFromToday(2)),
                    checkInStatus = CheckInEntity.STATUS_STAYING,
                    contractNumber = "ДГ-1001"
                )
            )
            checkInDao.insert(
                CheckInEntity(
                    bookingId = booking103, clientId = clientIds[3], roomId = roomIds["103"]!!,
                    employeeId = admin, checkInDate = millis(daysFromToday(-1)),
                    checkOutDate = millis(daysFromToday(3)),
                    checkInStatus = CheckInEntity.STATUS_STAYING,
                    contractNumber = "ДГ-1002"
                )
            )
            bookingDao.updateStatus(booking102, BookingEntity.STATUS_COMPLETED)
            bookingDao.updateStatus(booking103, BookingEntity.STATUS_COMPLETED)
            roomDao.updateStatus(roomIds["102"]!!, RoomEntity.STATUS_OCCUPIED)
            roomDao.updateStatus(roomIds["103"]!!, RoomEntity.STATUS_OCCUPIED)

            // История: выселившийся гость с оплаченным счётом (номер освобождён)
            val booking202 = bookingDao.insert(
                BookingEntity(
                    clientId = clientIds[5], roomId = roomIds["202"]!!, employeeId = admin,
                    checkInDate = millis(daysFromToday(-5)), checkOutDate = millis(daysFromToday(-1)),
                    bookingStatus = BookingEntity.STATUS_ACTIVE
                )
            )
            val checkIn202 = checkInDao.insert(
                CheckInEntity(
                    bookingId = booking202, clientId = clientIds[5], roomId = roomIds["202"]!!,
                    employeeId = admin, checkInDate = millis(daysFromToday(-5)),
                    checkOutDate = millis(daysFromToday(-1)),
                    checkInStatus = CheckInEntity.STATUS_CHECKED_OUT,
                    contractNumber = "ДГ-1003",
                    totalCost = 4 * 5500.0 + 250.0 // 4 суток полулюкс + смена белья
                )
            )
            bookingDao.updateStatus(booking202, BookingEntity.STATUS_COMPLETED)
            paymentDao.insert(
                PaymentEntity(
                    checkInId = checkIn202, amount = 4 * 5500.0 + 250.0,
                    paymentDate = millis(daysFromToday(-1)), paymentMethod = "Карта"
                )
            )

            // Заказы услуг для постояльцев
            if (serviceIds.isNotEmpty()) {
                val checkIn102 = checkInDao.getByBookingId(booking102)
                val checkIn103 = checkInDao.getByBookingId(booking103)
                checkIn102?.let {
                    serviceOrderDao.insert(
                        ServiceOrderEntity(
                            checkInId = it.checkInId, serviceId = serviceIds[0],
                            status = ServiceOrderEntity.STATUS_IN_PROGRESS
                        )
                    )
                    serviceOrderDao.insert(
                        ServiceOrderEntity(
                            checkInId = it.checkInId, serviceId = serviceIds[1],
                            status = ServiceOrderEntity.STATUS_DONE
                        )
                    )
                }
                checkIn103?.let {
                    serviceOrderDao.insert(
                        ServiceOrderEntity(
                            checkInId = it.checkInId, serviceId = serviceIds[4],
                            status = ServiceOrderEntity.STATUS_IN_PROGRESS
                        )
                    )
                }
            }
        }
    }
}
