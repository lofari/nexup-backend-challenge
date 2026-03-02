package model

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.DayOfWeek
import java.time.LocalTime

class BusinessHoursTest {

    private val weekdayHours = BusinessHours(
        openTime = LocalTime.of(9, 0),
        closeTime = LocalTime.of(21, 0),
        openDays = setOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        )
    )

    @Test
    fun `should be open during business hours on open day`() {
        assertTrue(weekdayHours.isOpenAt(DayOfWeek.MONDAY, LocalTime.of(12, 0)))
    }

    @Test
    fun `should be open exactly at opening time`() {
        assertTrue(weekdayHours.isOpenAt(DayOfWeek.FRIDAY, LocalTime.of(9, 0)))
    }

    @Test
    fun `should be closed at closing time`() {
        assertFalse(weekdayHours.isOpenAt(DayOfWeek.MONDAY, LocalTime.of(21, 0)))
    }

    @Test
    fun `should be closed before opening time`() {
        assertFalse(weekdayHours.isOpenAt(DayOfWeek.MONDAY, LocalTime.of(8, 59)))
    }

    @Test
    fun `should be closed on non-open day`() {
        assertFalse(weekdayHours.isOpenAt(DayOfWeek.SATURDAY, LocalTime.of(12, 0)))
    }

    @Test
    fun `should reject empty open days`() {
        assertThrows<IllegalArgumentException> {
            BusinessHours(LocalTime.of(9, 0), LocalTime.of(21, 0), emptySet())
        }
    }

    @Test
    fun `should reject open time equal to close time`() {
        assertThrows<IllegalArgumentException> {
            BusinessHours(LocalTime.of(9, 0), LocalTime.of(9, 0), setOf(DayOfWeek.MONDAY))
        }
    }

    @Test
    fun `should reject open time after close time`() {
        assertThrows<IllegalArgumentException> {
            BusinessHours(LocalTime.of(21, 0), LocalTime.of(9, 0), setOf(DayOfWeek.MONDAY))
        }
    }
}
