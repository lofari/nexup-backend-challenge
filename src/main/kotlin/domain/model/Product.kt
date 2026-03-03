package domain.model

import java.math.BigDecimal

data class Product(val id: Int, val name: String, val price: BigDecimal) {
    init {
        require(id > 0) { "Product ID must be positive" }
        require(name.isNotBlank()) { "Product name must not be blank" }
        require(price >= BigDecimal.ZERO) { "Product price must not be negative" }
    }
}
