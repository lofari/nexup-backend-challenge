package model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ProductTest {

    @Test
    fun `should create product with valid data`() {
        val product = Product(1, "Carne", 10.0)
        assertEquals(1, product.id)
        assertEquals("Carne", product.name)
        assertEquals(10.0, product.price)
    }

    @Test
    fun `should support value equality`() {
        val p1 = Product(1, "Carne", 10.0)
        val p2 = Product(1, "Carne", 10.0)
        assertEquals(p1, p2)
    }

    @Test
    fun `should reject non-positive id`() {
        assertThrows<IllegalArgumentException> { Product(0, "Carne", 10.0) }
    }

    @Test
    fun `should reject blank name`() {
        assertThrows<IllegalArgumentException> { Product(1, "", 10.0) }
    }

    @Test
    fun `should reject negative price`() {
        assertThrows<IllegalArgumentException> { Product(1, "Carne", -5.0) }
    }

    @Test
    fun `should allow zero price`() {
        val product = Product(1, "Free Sample", 0.0)
        assertEquals(0.0, product.price)
    }
}
