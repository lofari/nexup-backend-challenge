package domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class ProductTest {

    @Test
    fun `should create product with valid data`() {
        val product = Product(1, "Carne", BigDecimal("10.0"))
        assertEquals(1, product.id)
        assertEquals("Carne", product.name)
        assertEquals(BigDecimal("10.0"), product.price)
    }

    @Test
    fun `should support value equality`() {
        val a = Product(1, "Carne", BigDecimal("10.0"))
        val b = Product(1, "Carne", BigDecimal("10.0"))
        assertEquals(a, b)
    }

    @Test
    fun `should reject non-positive id`() {
        assertThrows<IllegalArgumentException> {
            Product(0, "Carne", BigDecimal("10.0"))
        }
    }

    @Test
    fun `should reject blank name`() {
        assertThrows<IllegalArgumentException> {
            Product(1, "  ", BigDecimal("10.0"))
        }
    }

    @Test
    fun `should reject negative price`() {
        assertThrows<IllegalArgumentException> {
            Product(1, "Carne", BigDecimal("-1.0"))
        }
    }

    @Test
    fun `should allow zero price`() {
        val product = Product(1, "Carne", BigDecimal.ZERO)
        assertEquals(BigDecimal.ZERO, product.price)
    }
}
