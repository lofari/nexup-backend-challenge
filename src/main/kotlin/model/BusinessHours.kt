package model

import java.time.DayOfWeek
import java.time.LocalTime

data class BusinessHours(
    val openTime: LocalTime,
    val closeTime: LocalTime,
    val openDays: Set<DayOfWeek>
) {
    init {
        require(openDays.isNotEmpty()) { "Must have at least one open day" }
        require(openTime < closeTime) { "Open time must be before close time" }
    }

    fun isOpenAt(day: DayOfWeek, time: LocalTime): Boolean {
        return day in openDays && time >= openTime && time < closeTime
    }
}
