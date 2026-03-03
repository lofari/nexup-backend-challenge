package domain

import domain.model.BusinessHours
import domain.model.Product
import domain.model.Sale
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalTime

class Supermarket(
    val id: Int,
    val name: String,
    initialStock: Map<Product, Int>,
    val businessHours: BusinessHours? = null
) {
    private val products: Map<Int, Product> = initialStock.keys.associateBy { it.id }
    private val stock: MutableMap<Int, Int> = initialStock.entries.associate { it.key.id to it.value }.toMutableMap()
    private val sales: MutableList<Sale> = mutableListOf()

    init {
        require(id > 0) { "Supermarket ID must be positive" }
        require(name.isNotBlank()) { "Supermarket name must not be blank" }
        initialStock.forEach { (product, qty) ->
            require(qty >= 0) { "Stock for '${product.name}' must not be negative" }
        }
    }

    /**
     * Registers a sale for the given product and quantity.
     * Deducts stock and records the sale.
     * @return the total price of the sale
     * @throws IllegalArgumentException if product not found, quantity invalid, or insufficient stock
     */
    fun registerSale(productId: Int, quantity: Int): BigDecimal {
        val product = findProduct(productId)
        val sale = Sale(product, quantity)
        val currentStock = stock.getValue(productId)
        require(currentStock >= quantity) {
            "Insufficient stock for '${product.name}'. Available: $currentStock, requested: $quantity"
        }

        stock[productId] = currentStock - quantity
        sales.add(sale)
        return sale.total
    }

    /** Returns the total quantity sold for the given product. */
    fun getQuantitySold(productId: Int): Int {
        findProduct(productId)
        return sales.filter { it.product.id == productId }.sumOf { it.quantity }
    }

    /** Returns the total revenue from sales of the given product. */
    fun getSalesRevenue(productId: Int): BigDecimal {
        findProduct(productId)
        return sales.filter { it.product.id == productId }.sumOf { it.total }
    }

    /** Returns the total revenue from all sales. */
    fun getTotalRevenue(): BigDecimal = sales.sumOf { it.total }

    /** Returns the current stock level for the given product. */
    fun getStock(productId: Int): Int {
        findProduct(productId)
        return stock.getValue(productId)
    }

    /** Returns a defensive copy of all sales. */
    fun getSales(): List<Sale> = sales.toList()

    /** Checks whether the supermarket is open at the given day and time. */
    fun isOpenAt(day: DayOfWeek, time: LocalTime): Boolean {
        return businessHours?.isOpenAt(day, time) ?: false
    }

    private fun findProduct(productId: Int): Product {
        return products[productId]
            ?: throw IllegalArgumentException("Product with ID $productId not found in this supermarket")
    }
}
