import domain.Supermarket
import domain.SupermarketChain
import domain.model.BusinessHours
import domain.model.Product
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalTime

fun main() {
    // Set up products
    val carne = Product(1, "Carne", BigDecimal("10.0"))
    val pescado = Product(2, "Pescado", BigDecimal("20.0"))
    val pollo = Product(3, "Pollo", BigDecimal("30.0"))
    val cerdo = Product(4, "Cerdo", BigDecimal("45.0"))
    val ternera = Product(5, "Ternera", BigDecimal("50.0"))
    val cordero = Product(6, "Cordero", BigDecimal("65.0"))

    val allProducts = mapOf(
        carne to 100, pescado to 100, pollo to 100,
        cerdo to 100, ternera to 100, cordero to 100
    )

    // Set up supermarkets with business hours
    val supermarketA = Supermarket(
        1, "Supermercado A", allProducts,
        BusinessHours(LocalTime.of(9, 0), LocalTime.of(21, 0),
            setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY))
    )
    val supermarketB = Supermarket(
        2, "Supermercado B", allProducts,
        BusinessHours(LocalTime.of(10, 0), LocalTime.of(14, 0),
            setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY))
    )
    val supermarketC = Supermarket(3, "Supermercado C", allProducts)

    // Register some sales
    supermarketA.registerSale(1, 10)
    supermarketA.registerSale(2, 12)
    supermarketA.registerSale(6, 3)
    supermarketB.registerSale(1, 5)
    supermarketB.registerSale(3, 8)
    supermarketC.registerSale(4, 7)
    supermarketC.registerSale(5, 6)

    // Create chain and query
    val chain = SupermarketChain(listOf(supermarketA, supermarketB, supermarketC))

    println("Top 5 products: ${chain.getTopFiveProducts()}")
    println("Total revenue: ${chain.getTotalRevenue()}")
    println("Highest revenue: ${chain.getHighestRevenueSupermarket()}")
    println("Open on Monday at 12:00: ${chain.getOpenSupermarketsAt(DayOfWeek.MONDAY, LocalTime.of(12, 0))}")
    println("Open on Saturday at 11:00: ${chain.getOpenSupermarketsAt(DayOfWeek.SATURDAY, LocalTime.of(11, 0))}")
}
