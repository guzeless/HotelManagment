package com.example.hotelmanagement.util

import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Утилиты работы с датами: преобразование LocalDate <-> Long (epoch millis),
 * форматирование для отображения и расчёт количества суток.
 */
object DateUtils {

    private val zone: ZoneId = ZoneId.systemDefault()
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    fun today(): LocalDate = LocalDate.now()

    fun toMillis(date: LocalDate): Long =
        date.atStartOfDay(zone).toInstant().toEpochMilli()

    fun fromMillis(millis: Long): LocalDate =
        java.time.Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    fun format(millis: Long): String = fromMillis(millis).format(formatter)

    /** Количество суток проживания (минимум 1). */
    fun nights(from: LocalDate, to: LocalDate): Int =
        maxOf(1, ChronoUnit.DAYS.between(from, to).toInt())
}
