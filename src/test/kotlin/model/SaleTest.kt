package model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SaleTest {

    private val product = Product(1, "Carne", 10.0)

    @Test
    fun `should calculate total correctly`() {
        val sale = Sale(product, 3)
        assertEquals(30.0, sale.total)
    }

    @Test
    fun `should store product and quantity`() {
        val sale = Sale(product, 5)
        assertEquals(product, sale.product)
        assertEquals(5, sale.quantity)
    }

    @Test
    fun `should reject zero quantity`() {
        assertThrows<IllegalArgumentException> { Sale(product, 0) }
    }

    @Test
    fun `should reject negative quantity`() {
        assertThrows<IllegalArgumentException> { Sale(product, -1) }
    }
}
