package domain.model

import java.time.DayOfWeek
import java.time.LocalTime

data class BusinessHours(
    val openTime: LocalTime,
    val closeTime: LocalTime,
    val openDays: Set<DayOfWeek>
) {
    init {
        require(openDays.isNotEmpty()) { "Must have at least one open day" }
        require(openTime != closeTime) { "Open time must differ from close time" }
    }

    /**
     * Checks if the store is open at the given day and time.
     * Supports overnight hours (e.g., 22:00-06:00).
     */
    fun isOpenAt(day: DayOfWeek, time: LocalTime): Boolean {
        return if (openTime < closeTime) {
            day in openDays && time >= openTime && time < closeTime
        } else {
            (day in openDays && time >= openTime) ||
                (day.minus(1) in openDays && time < closeTime)
        }
    }
}
