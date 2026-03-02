package domain

import java.time.DayOfWeek
import java.time.LocalTime

class SupermarketChain(
    private val supermarkets: List<Supermarket>
) {

    /**
     * Returns the top 5 best-selling products across all supermarkets.
     * Format: "<name>: <quantity> - <name>: <quantity> - ..."
     */
    fun getTopFiveProducts(): String {
        return supermarkets
            .flatMap { it.getSales() }
            .groupBy { it.product }
            .mapValues { (_, sales) -> sales.sumOf { it.quantity } }
            .entries
            .sortedByDescending { it.value }
            .take(5)
            .joinToString(" - ") { "${it.key.name}: ${it.value}" }
    }

    /** Returns the total revenue across all supermarkets. */
    fun getTotalRevenue(): Double {
        return supermarkets.sumOf { it.getTotalRevenue() }
    }

    /**
     * Returns the supermarket with the highest total revenue.
     * Format: "<name> (<id>). Ingresos totales: <revenue>"
     * @throws IllegalStateException if the chain has no supermarkets
     */
    fun getHighestRevenueSupermarket(): String {
        val top = supermarkets.maxByOrNull { it.getTotalRevenue() }
            ?: throw IllegalStateException("No supermarkets in chain")
        return "${top.name} (${top.id}). Ingresos totales: ${top.getTotalRevenue()}"
    }

    /**
     * Returns supermarkets open at the given day and time.
     * Format: "<name> (<id>), <name> (<id>), ..."
     */
    fun getOpenSupermarketsAt(day: DayOfWeek, time: LocalTime): String {
        return supermarkets
            .filter { it.isOpenAt(day, time) }
            .joinToString(", ") { "${it.name} (${it.id})" }
    }
}
