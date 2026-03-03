package domain.model

import java.math.BigDecimal

data class Sale(val product: Product, val quantity: Int) {
    init {
        require(quantity > 0) { "Sale quantity must be positive" }
    }

    val total: BigDecimal get() = product.price * quantity.toBigDecimal()
}
