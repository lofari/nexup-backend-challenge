package model

data class Product(val id: Int, val name: String, val price: Double) {
    init {
        require(id > 0) { "Product ID must be positive" }
        require(name.isNotBlank()) { "Product name must not be blank" }
        require(price >= 0) { "Product price must not be negative" }
    }
}
