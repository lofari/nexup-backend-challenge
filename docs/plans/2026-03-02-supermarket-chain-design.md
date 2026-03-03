# Supermarket Chain Challenge — Design Document

## Context

Nexup backend challenge: build an in-memory supermarket chain system in Kotlin.
Includes required features (sales, revenue, top products) and the optional feature (business hours).

## Decisions

- **Build system:** Gradle with Kotlin DSL, JUnit 5
- **Architecture:** Layered — immutable domain models, encapsulated mutable state in aggregate classes
- **Error handling:** Exceptions (`IllegalArgumentException`) for invalid operations
- **Language:** All English (code, comments, docs)
- **Scope:** All required features + optional business hours feature

## Domain Models (Immutable)

```kotlin
data class Product(val id: Int, val name: String, val price: Double)
data class Sale(val product: Product, val quantity: Int) {
    val total: Double get() = product.price * quantity
}
data class BusinessHours(
    val openTime: LocalTime,
    val closeTime: LocalTime,
    val openDays: Set<DayOfWeek>
)
```

- `Product` — value object, shared across supermarkets
- `Sale` — immutable record of a completed sale, metrics derived from it
- `BusinessHours` — encapsulates schedule for a supermarket

## Supermarket (Aggregate Root)

```kotlin
class Supermarket(
    val id: Int,
    val name: String,
    initialStock: Map<Product, Int>,
    val businessHours: BusinessHours? = null
)
```

**Internal state:** mutable maps for stock and sales list, never exposed directly.

**Operations:**
- `registerSale(productId, quantity) -> Double` — validates stock, deducts, records sale, returns total
- `getQuantitySold(productId) -> Int` — sums quantities from sale records
- `getSalesRevenue(productId) -> Double` — sums totals from sale records
- `getTotalRevenue() -> Double` — sums all sale totals
- `isOpenAt(day, time) -> Boolean` — checks against business hours
- `getStock(productId) -> Int` — current stock level
- `getSales() -> List<Sale>` — defensive copy

**Errors:** `IllegalArgumentException` for unknown products, insufficient stock, invalid quantities.

## SupermarketChain (Aggregator)

```kotlin
class SupermarketChain(private val supermarkets: List<Supermarket>)
```

**Operations:**
- `getTopFiveProducts() -> String` — aggregates sales across all supermarkets, format: `<name>: <qty> - <name>: <qty> - ...`
- `getTotalRevenue() -> Double` — sums revenue from all supermarkets
- `getHighestRevenueSupermarket() -> String` — format: `<name> (<id>). Ingresos totales: <revenue>`
- `getOpenSupermarketsAt(day, time) -> String` — format: `<name> (<id>), <name> (<id>), ...`

## Project Structure

```
build.gradle.kts
settings.gradle.kts
src/
  Main.kt
  model/
    Product.kt
    Sale.kt
    BusinessHours.kt
  domain/
    Supermarket.kt
    SupermarketChain.kt
test/
  model/
    ProductTest.kt
    BusinessHoursTest.kt
  domain/
    SupermarketTest.kt
    SupermarketChainTest.kt
```

## Test Strategy

- **ProductTest:** data class equality, computed properties
- **SupermarketTest:** register sales, stock deduction, revenue, error cases (unknown product, insufficient stock, zero/negative quantity)
- **SupermarketChainTest:** top 5 aggregation, total revenue, highest revenue, open supermarkets
- **BusinessHoursTest:** open/closed at various day/time combos, edge cases

Test data: 6 products and 3 supermarkets as specified in the challenge.
