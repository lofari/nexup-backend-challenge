package domain

import domain.model.BusinessHours
import domain.model.Product
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalTime

class SupermarketTest {

    private lateinit var carne: Product
    private lateinit var pescado: Product
    private lateinit var supermarket: Supermarket

    @BeforeEach
    fun setUp() {
        carne = Product(1, "Carne", BigDecimal("10.0"))
        pescado = Product(2, "Pescado", BigDecimal("20.0"))
        supermarket = Supermarket(
            id = 1,
            name = "Supermercado A",
            initialStock = mapOf(carne to 100, pescado to 50)
        )
    }

    // --- constructor validation ---

    @Test
    fun `should reject non-positive id`() {
        assertThrows<IllegalArgumentException> {
            Supermarket(0, "Test", mapOf(carne to 10))
        }
    }

    @Test
    fun `should reject blank name`() {
        assertThrows<IllegalArgumentException> {
            Supermarket(1, "  ", mapOf(carne to 10))
        }
    }

    @Test
    fun `should reject negative initial stock`() {
        assertThrows<IllegalArgumentException> {
            Supermarket(1, "Test", mapOf(carne to -5))
        }
    }

    // --- registerSale ---

    @Test
    fun `registerSale should return total price`() {
        val total = supermarket.registerSale(1, 3)
        assertEquals(BigDecimal("30.0"), total)
    }

    @Test
    fun `registerSale should deduct stock`() {
        supermarket.registerSale(1, 3)
        assertEquals(97, supermarket.getStock(1))
    }

    @Test
    fun `registerSale should allow selling all remaining stock`() {
        supermarket.registerSale(2, 50)
        assertEquals(0, supermarket.getStock(2))
    }

    @Test
    fun `registerSale should throw for unknown product`() {
        assertThrows<IllegalArgumentException> {
            supermarket.registerSale(99, 1)
        }
    }

    @Test
    fun `registerSale should throw for insufficient stock`() {
        assertThrows<IllegalArgumentException> {
            supermarket.registerSale(1, 101)
        }
    }

    @Test
    fun `registerSale should throw for zero quantity`() {
        assertThrows<IllegalArgumentException> {
            supermarket.registerSale(1, 0)
        }
    }

    @Test
    fun `registerSale should throw for negative quantity`() {
        assertThrows<IllegalArgumentException> {
            supermarket.registerSale(1, -1)
        }
    }

    @Test
    fun `registerSale should throw after stock is fully depleted`() {
        supermarket.registerSale(2, 50)
        assertThrows<IllegalArgumentException> {
            supermarket.registerSale(2, 1)
        }
    }

    // --- getQuantitySold ---

    @Test
    fun `getQuantitySold should return total quantity sold`() {
        supermarket.registerSale(1, 3)
        supermarket.registerSale(1, 2)
        assertEquals(5, supermarket.getQuantitySold(1))
    }

    @Test
    fun `getQuantitySold should return zero when no sales`() {
        assertEquals(0, supermarket.getQuantitySold(1))
    }

    @Test
    fun `getQuantitySold should throw for unknown product`() {
        assertThrows<IllegalArgumentException> {
            supermarket.getQuantitySold(99)
        }
    }

    // --- getSalesRevenue ---

    @Test
    fun `getSalesRevenue should return revenue for product`() {
        supermarket.registerSale(1, 3) // 30.0
        supermarket.registerSale(1, 2) // 20.0
        assertEquals(BigDecimal("50.0"), supermarket.getSalesRevenue(1))
    }

    @Test
    fun `getSalesRevenue should return zero when no sales`() {
        assertEquals(BigDecimal.ZERO, supermarket.getSalesRevenue(1))
    }

    @Test
    fun `getSalesRevenue should throw for unknown product`() {
        assertThrows<IllegalArgumentException> {
            supermarket.getSalesRevenue(99)
        }
    }

    @Test
    fun `getSalesRevenue should not include other products`() {
        supermarket.registerSale(1, 3) // 30.0
        supermarket.registerSale(2, 2) // 40.0
        assertEquals(BigDecimal("30.0"), supermarket.getSalesRevenue(1))
    }

    // --- getTotalRevenue ---

    @Test
    fun `getTotalRevenue should sum all sales`() {
        supermarket.registerSale(1, 3) // 30.0
        supermarket.registerSale(2, 2) // 40.0
        assertEquals(BigDecimal("70.0"), supermarket.getTotalRevenue())
    }

    @Test
    fun `getTotalRevenue should return zero when no sales`() {
        assertEquals(BigDecimal.ZERO, supermarket.getTotalRevenue())
    }

    @Test
    fun `getTotalRevenue should be zero for zero-price product`() {
        val free = Product(3, "Gratis", BigDecimal.ZERO)
        val sm = Supermarket(1, "Test", mapOf(free to 10))
        sm.registerSale(3, 5)
        assertEquals(BigDecimal.ZERO, sm.getTotalRevenue())
    }

    // --- isOpenAt ---

    @Test
    fun `isOpenAt should return false when no business hours set`() {
        assertFalse(supermarket.isOpenAt(DayOfWeek.MONDAY, LocalTime.of(12, 0)))
    }

    @Test
    fun `isOpenAt should delegate to business hours`() {
        val hours = BusinessHours(
            LocalTime.of(9, 0), LocalTime.of(21, 0),
            setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY)
        )
        val sm = Supermarket(1, "Test", mapOf(carne to 10), hours)
        assertTrue(sm.isOpenAt(DayOfWeek.MONDAY, LocalTime.of(12, 0)))
        assertFalse(sm.isOpenAt(DayOfWeek.SATURDAY, LocalTime.of(12, 0)))
    }

    // --- getSales ---

    @Test
    fun `getSales should return defensive copy`() {
        supermarket.registerSale(1, 3)
        val salesCopy = supermarket.getSales()
        assertEquals(1, salesCopy.size)
        supermarket.registerSale(2, 1)
        assertEquals(1, salesCopy.size) // original copy unchanged
    }
}
