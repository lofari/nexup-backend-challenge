package domain

import domain.model.BusinessHours
import domain.model.Product
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalTime

class SupermarketChainTest {

    private lateinit var carne: Product
    private lateinit var pescado: Product
    private lateinit var pollo: Product
    private lateinit var cerdo: Product
    private lateinit var ternera: Product
    private lateinit var cordero: Product

    private lateinit var supermarketA: Supermarket
    private lateinit var supermarketB: Supermarket
    private lateinit var supermarketC: Supermarket
    private lateinit var chain: SupermarketChain

    @BeforeEach
    fun setUp() {
        carne = Product(1, "Carne", BigDecimal("10.0"))
        pescado = Product(2, "Pescado", BigDecimal("20.0"))
        pollo = Product(3, "Pollo", BigDecimal("30.0"))
        cerdo = Product(4, "Cerdo", BigDecimal("45.0"))
        ternera = Product(5, "Ternera", BigDecimal("50.0"))
        cordero = Product(6, "Cordero", BigDecimal("65.0"))

        val allProducts = mapOf(
            carne to 100, pescado to 100, pollo to 100,
            cerdo to 100, ternera to 100, cordero to 100
        )

        val weekdayHours = BusinessHours(
            LocalTime.of(9, 0), LocalTime.of(21, 0),
            setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
        )
        val weekendHours = BusinessHours(
            LocalTime.of(10, 0), LocalTime.of(14, 0),
            setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        )

        supermarketA = Supermarket(1, "Supermercado A", allProducts, weekdayHours)
        supermarketB = Supermarket(2, "Supermercado B", allProducts, weekendHours)
        supermarketC = Supermarket(3, "Supermercado C", allProducts)

        chain = SupermarketChain(listOf(supermarketA, supermarketB, supermarketC))
    }

    // --- duplicate ID validation ---

    @Test
    fun `should reject duplicate supermarket IDs`() {
        assertThrows<IllegalArgumentException> {
            SupermarketChain(listOf(supermarketA, supermarketA))
        }
    }

    // --- getTopFiveProducts ---

    @Test
    fun `getTopFiveProducts should return top 5 by quantity across all supermarkets`() {
        supermarketA.registerSale(1, 10)  // Carne: 10
        supermarketB.registerSale(1, 5)   // Carne: +5 = 15
        supermarketA.registerSale(2, 12)  // Pescado: 12
        supermarketB.registerSale(3, 8)   // Pollo: 8
        supermarketC.registerSale(4, 7)   // Cerdo: 7
        supermarketC.registerSale(5, 6)   // Ternera: 6
        supermarketA.registerSale(6, 3)   // Cordero: 3

        val result = chain.getTopFiveProducts()
        assertEquals("Carne: 15 - Pescado: 12 - Pollo: 8 - Cerdo: 7 - Ternera: 6", result)
    }

    @Test
    fun `getTopFiveProducts should return exactly 5 when 5 products sold`() {
        supermarketA.registerSale(1, 10)
        supermarketA.registerSale(2, 8)
        supermarketA.registerSale(3, 6)
        supermarketA.registerSale(4, 4)
        supermarketA.registerSale(5, 2)

        val result = chain.getTopFiveProducts()
        assertEquals("Carne: 10 - Pescado: 8 - Pollo: 6 - Cerdo: 4 - Ternera: 2", result)
    }

    @Test
    fun `getTopFiveProducts should return fewer than 5 if not enough products sold`() {
        supermarketA.registerSale(1, 10)
        supermarketA.registerSale(2, 5)

        val result = chain.getTopFiveProducts()
        assertEquals("Carne: 10 - Pescado: 5", result)
    }

    @Test
    fun `getTopFiveProducts should return empty string when no sales`() {
        assertEquals("", chain.getTopFiveProducts())
    }

    // --- getTotalRevenue ---

    @Test
    fun `getTotalRevenue should sum revenue from all supermarkets`() {
        supermarketA.registerSale(1, 3) // 30.0
        supermarketB.registerSale(2, 2) // 40.0
        supermarketC.registerSale(3, 1) // 30.0
        assertEquals(BigDecimal("100.0"), chain.getTotalRevenue())
    }

    @Test
    fun `getTotalRevenue should return zero when no sales`() {
        assertEquals(BigDecimal.ZERO, chain.getTotalRevenue())
    }

    // --- getHighestRevenueSupermarket ---

    @Test
    fun `getHighestRevenueSupermarket should return formatted string`() {
        supermarketA.registerSale(1, 10) // 100.0
        supermarketB.registerSale(2, 2)  // 40.0
        supermarketC.registerSale(3, 1)  // 30.0

        assertEquals(
            "Supermercado A (1). Ingresos totales: 100.0",
            chain.getHighestRevenueSupermarket()
        )
    }

    @Test
    fun `getHighestRevenueSupermarket should return first supermarket when no sales`() {
        val result = chain.getHighestRevenueSupermarket()
        assertEquals("Supermercado A (1). Ingresos totales: 0", result)
    }

    @Test
    fun `getHighestRevenueSupermarket should throw when chain is empty`() {
        val emptyChain = SupermarketChain(emptyList())
        assertThrows<IllegalStateException> {
            emptyChain.getHighestRevenueSupermarket()
        }
    }

    // --- getOpenSupermarketsAt ---

    @Test
    fun `getOpenSupermarketsAt should return weekday supermarket on Monday`() {
        val result = chain.getOpenSupermarketsAt(DayOfWeek.MONDAY, LocalTime.of(12, 0))
        assertEquals("Supermercado A (1)", result)
    }

    @Test
    fun `getOpenSupermarketsAt should return weekend supermarket on Saturday`() {
        val result = chain.getOpenSupermarketsAt(DayOfWeek.SATURDAY, LocalTime.of(11, 0))
        assertEquals("Supermercado B (2)", result)
    }

    @Test
    fun `getOpenSupermarketsAt should return multiple supermarkets when several are open`() {
        val sharedHours = BusinessHours(
            LocalTime.of(9, 0), LocalTime.of(21, 0),
            setOf(DayOfWeek.MONDAY)
        )
        val smA = Supermarket(1, "A", mapOf(carne to 10), sharedHours)
        val smB = Supermarket(2, "B", mapOf(carne to 10), sharedHours)
        val multiChain = SupermarketChain(listOf(smA, smB))

        val result = multiChain.getOpenSupermarketsAt(DayOfWeek.MONDAY, LocalTime.of(12, 0))
        assertEquals("A (1), B (2)", result)
    }

    @Test
    fun `getOpenSupermarketsAt should return empty string when none open`() {
        val result = chain.getOpenSupermarketsAt(DayOfWeek.SUNDAY, LocalTime.of(23, 0))
        assertEquals("", result)
    }

    @Test
    fun `getOpenSupermarketsAt should exclude supermarkets without business hours`() {
        // supermarketC has no business hours, should never appear
        val result = chain.getOpenSupermarketsAt(DayOfWeek.MONDAY, LocalTime.of(12, 0))
        assertFalse(result.contains("Supermercado C"))
    }
}
