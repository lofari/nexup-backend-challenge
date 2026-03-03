package domain.model

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.DayOfWeek
import java.time.LocalTime

class BusinessHoursTest {

    private val weekdayHours = BusinessHours(
        LocalTime.of(9, 0), LocalTime.of(21, 0),
        setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY)
    )

    // --- Normal hours (open < close) ---

    @Test
    fun `should be open during business hours on open day`() {
        assertTrue(weekdayHours.isOpenAt(DayOfWeek.MONDAY, LocalTime.of(12, 0)))
    }

    @Test
    fun `should be open exactly at opening time`() {
        assertTrue(weekdayHours.isOpenAt(DayOfWeek.MONDAY, LocalTime.of(9, 0)))
    }

    @Test
    fun `should be closed at closing time`() {
        assertFalse(weekdayHours.isOpenAt(DayOfWeek.MONDAY, LocalTime.of(21, 0)))
    }

    @Test
    fun `should be closed before opening time`() {
        assertFalse(weekdayHours.isOpenAt(DayOfWeek.MONDAY, LocalTime.of(8, 0)))
    }

    @Test
    fun `should be closed on non-open day`() {
        assertFalse(weekdayHours.isOpenAt(DayOfWeek.SATURDAY, LocalTime.of(12, 0)))
    }

    // --- Overnight hours (open > close, e.g. 22:00-06:00) ---

    @Test
    fun `should support overnight hours before midnight`() {
        val overnight = BusinessHours(
            LocalTime.of(22, 0), LocalTime.of(6, 0),
            setOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)
        )
        assertTrue(overnight.isOpenAt(DayOfWeek.FRIDAY, LocalTime.of(23, 0)))
    }

    @Test
    fun `should support overnight hours after midnight`() {
        val overnight = BusinessHours(
            LocalTime.of(22, 0), LocalTime.of(6, 0),
            setOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)
        )
        // Saturday after midnight counts as Friday's overnight shift
        assertTrue(overnight.isOpenAt(DayOfWeek.SATURDAY, LocalTime.of(3, 0)))
    }

    @Test
    fun `should be closed during daytime for overnight hours`() {
        val overnight = BusinessHours(
            LocalTime.of(22, 0), LocalTime.of(6, 0),
            setOf(DayOfWeek.FRIDAY)
        )
        assertFalse(overnight.isOpenAt(DayOfWeek.FRIDAY, LocalTime.of(12, 0)))
    }

    @Test
    fun `should be closed after overnight closing time`() {
        val overnight = BusinessHours(
            LocalTime.of(22, 0), LocalTime.of(6, 0),
            setOf(DayOfWeek.FRIDAY)
        )
        // Saturday 7:00 — Friday's overnight shift ended at 06:00
        assertFalse(overnight.isOpenAt(DayOfWeek.SATURDAY, LocalTime.of(7, 0)))
    }

    // --- Validation ---

    @Test
    fun `should reject empty open days`() {
        assertThrows<IllegalArgumentException> {
            BusinessHours(LocalTime.of(9, 0), LocalTime.of(21, 0), emptySet())
        }
    }

    @Test
    fun `should reject equal open and close time`() {
        assertThrows<IllegalArgumentException> {
            BusinessHours(LocalTime.of(9, 0), LocalTime.of(9, 0), setOf(DayOfWeek.MONDAY))
        }
    }
}
