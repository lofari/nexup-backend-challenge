package model

data class Sale(val product: Product, val quantity: Int) {
    init {
        require(quantity > 0) { "Sale quantity must be positive" }
    }

    val total: Double get() = product.price * quantity
}
