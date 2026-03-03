# Supermarket Chain Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build an in-memory supermarket chain system in Kotlin with sales tracking, revenue reporting, and business hours management.

**Architecture:** Layered design with immutable domain models (`Product`, `Sale`, `BusinessHours`) and aggregate classes (`Supermarket`, `SupermarketChain`) that encapsulate mutable state. TDD throughout. JUnit 5 for tests.

**Tech Stack:** Kotlin 2.0.0, Gradle 8.5 (Kotlin DSL), JUnit 5.10, Java 17

---

### Task 0: Create GitHub Repository and Configure Remote

**Files:**
- No file changes

**Step 1: Create a new GitHub repository**

Run:
```bash
gh repo create nexup-backend-challenge --public --source=. --remote=origin --push
```

If the remote `origin` already exists and points elsewhere, update it:
```bash
git remote set-url origin <new-repo-url>
git push -u origin main
```

Expected: Repository created on GitHub, code pushed.

**Step 2: Verify remote**

Run: `git remote -v`
Expected: `origin` points to the new GitHub repo.

---

### Task 1: Set Up Gradle Build System

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Move: `src/Main.kt` -> `src/main/kotlin/Main.kt`
- Delete: `test/ChallengeTests.kt` (will be replaced by proper tests)

**Step 1: Download Gradle and generate wrapper**

Run:
```bash
wget -q https://services.gradle.org/distributions/gradle-8.5-bin.zip && unzip -q gradle-8.5-bin.zip && ./gradle-8.5/bin/gradle wrapper --gradle-version 8.5 && rm -rf gradle-8.5 gradle-8.5-bin.zip
```

Expected: `gradlew`, `gradlew.bat`, and `gradle/wrapper/` created.

**Step 2: Create `settings.gradle.kts`**

```kotlin
rootProject.name = "nexup-backend-challenge"
```

**Step 3: Create `build.gradle.kts`**

```kotlin
plugins {
    kotlin("jvm") version "2.0.0"
}

group = "com.nexup"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}
```

**Step 4: Restructure source directories**

Run:
```bash
mkdir -p src/main/kotlin src/test/kotlin
mv src/Main.kt src/main/kotlin/Main.kt
rm -rf test
```

**Step 5: Verify build compiles**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 6: Update `.gitignore` for Gradle**

Append to `.gitignore`:
```
# Gradle
.gradle/
build/
!gradle/wrapper/gradle-wrapper.jar
```

**Step 7: Commit**

```bash
git add settings.gradle.kts build.gradle.kts gradlew gradlew.bat gradle/ src/main/kotlin/Main.kt .gitignore
git rm test/ChallengeTests.kt src/Main.kt
git commit -m "build: set up Gradle with Kotlin DSL and JUnit 5"
```

---

### Task 2: Domain Models — Product and Sale (TDD)

**Files:**
- Create: `src/test/kotlin/model/ProductTest.kt`
- Create: `src/test/kotlin/model/SaleTest.kt`
- Create: `src/main/kotlin/model/Product.kt`
- Create: `src/main/kotlin/model/Sale.kt`

**Step 1: Write failing tests for Product**

`src/test/kotlin/model/ProductTest.kt`:
```kotlin
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
```

**Step 2: Run tests to verify they fail**

Run: `./gradlew test --tests "model.ProductTest" 2>&1 | tail -5`
Expected: FAIL — `Product` class does not exist.

**Step 3: Implement Product**

`src/main/kotlin/model/Product.kt`:
```kotlin
package model

data class Product(val id: Int, val name: String, val price: Double) {
    init {
        require(id > 0) { "Product ID must be positive" }
        require(name.isNotBlank()) { "Product name must not be blank" }
        require(price >= 0) { "Product price must not be negative" }
    }
}
```

**Step 4: Run Product tests to verify they pass**

Run: `./gradlew test --tests "model.ProductTest"`
Expected: BUILD SUCCESSFUL, all 6 tests pass.

**Step 5: Write failing tests for Sale**

`src/test/kotlin/model/SaleTest.kt`:
```kotlin
package model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SaleTest {

    private val product = Product(1, "Carne", 10.0)

    @Test
    fun `should calculate total correctly`() {
        val sale = Sale(product, 3)
        assertEquals(30.0, sale.total)
    }

    @Test
    fun `should store product and quantity`() {
        val sale = Sale(product, 5)
        assertEquals(product, sale.product)
        assertEquals(5, sale.quantity)
    }

    @Test
    fun `should reject zero quantity`() {
        assertThrows<IllegalArgumentException> { Sale(product, 0) }
    }

    @Test
    fun `should reject negative quantity`() {
        assertThrows<IllegalArgumentException> { Sale(product, -1) }
    }
}
```

**Step 6: Run Sale tests to verify they fail**

Run: `./gradlew test --tests "model.SaleTest" 2>&1 | tail -5`
Expected: FAIL — `Sale` class does not exist.

**Step 7: Implement Sale**

`src/main/kotlin/model/Sale.kt`:
```kotlin
package model

data class Sale(val product: Product, val quantity: Int) {
    init {
        require(quantity > 0) { "Sale quantity must be positive" }
    }

    val total: Double get() = product.price * quantity
}
```

**Step 8: Run all model tests to verify they pass**

Run: `./gradlew test --tests "model.*"`
Expected: BUILD SUCCESSFUL, all 10 tests pass.

**Step 9: Commit**

```bash
git add src/main/kotlin/model/ src/test/kotlin/model/
git commit -m "feat: add Product and Sale domain models with validation"
```

---

### Task 3: Domain Model — BusinessHours (TDD)

**Files:**
- Create: `src/test/kotlin/model/BusinessHoursTest.kt`
- Create: `src/main/kotlin/model/BusinessHours.kt`

**Step 1: Write failing tests for BusinessHours**

`src/test/kotlin/model/BusinessHoursTest.kt`:
```kotlin
package model

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.DayOfWeek
import java.time.LocalTime

class BusinessHoursTest {

    private val weekdayHours = BusinessHours(
        openTime = LocalTime.of(9, 0),
        closeTime = LocalTime.of(21, 0),
        openDays = setOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        )
    )

    @Test
    fun `should be open during business hours on open day`() {
        assertTrue(weekdayHours.isOpenAt(DayOfWeek.MONDAY, LocalTime.of(12, 0)))
    }

    @Test
    fun `should be open exactly at opening time`() {
        assertTrue(weekdayHours.isOpenAt(DayOfWeek.FRIDAY, LocalTime.of(9, 0)))
    }

    @Test
    fun `should be closed at closing time`() {
        assertFalse(weekdayHours.isOpenAt(DayOfWeek.MONDAY, LocalTime.of(21, 0)))
    }

    @Test
    fun `should be closed before opening time`() {
        assertFalse(weekdayHours.isOpenAt(DayOfWeek.MONDAY, LocalTime.of(8, 59)))
    }

    @Test
    fun `should be closed on non-open day`() {
        assertFalse(weekdayHours.isOpenAt(DayOfWeek.SATURDAY, LocalTime.of(12, 0)))
    }

    @Test
    fun `should reject empty open days`() {
        assertThrows<IllegalArgumentException> {
            BusinessHours(LocalTime.of(9, 0), LocalTime.of(21, 0), emptySet())
        }
    }

    @Test
    fun `should reject open time equal to close time`() {
        assertThrows<IllegalArgumentException> {
            BusinessHours(LocalTime.of(9, 0), LocalTime.of(9, 0), setOf(DayOfWeek.MONDAY))
        }
    }

    @Test
    fun `should reject open time after close time`() {
        assertThrows<IllegalArgumentException> {
            BusinessHours(LocalTime.of(21, 0), LocalTime.of(9, 0), setOf(DayOfWeek.MONDAY))
        }
    }
}
```

**Step 2: Run tests to verify they fail**

Run: `./gradlew test --tests "model.BusinessHoursTest" 2>&1 | tail -5`
Expected: FAIL — `BusinessHours` class does not exist.

**Step 3: Implement BusinessHours**

`src/main/kotlin/model/BusinessHours.kt`:
```kotlin
package model

import java.time.DayOfWeek
import java.time.LocalTime

data class BusinessHours(
    val openTime: LocalTime,
    val closeTime: LocalTime,
    val openDays: Set<DayOfWeek>
) {
    init {
        require(openDays.isNotEmpty()) { "Must have at least one open day" }
        require(openTime < closeTime) { "Open time must be before close time" }
    }

    fun isOpenAt(day: DayOfWeek, time: LocalTime): Boolean {
        return day in openDays && time >= openTime && time < closeTime
    }
}
```

**Step 4: Run tests to verify they pass**

Run: `./gradlew test --tests "model.BusinessHoursTest"`
Expected: BUILD SUCCESSFUL, all 8 tests pass.

**Step 5: Commit**

```bash
git add src/main/kotlin/model/BusinessHours.kt src/test/kotlin/model/BusinessHoursTest.kt
git commit -m "feat: add BusinessHours model with open/close time validation"
```

---

### Task 4: Supermarket — Sales and Stock Management (TDD)

**Files:**
- Create: `src/test/kotlin/domain/SupermarketTest.kt`
- Create: `src/main/kotlin/domain/Supermarket.kt`

**Step 1: Write failing tests for Supermarket core functionality**

`src/test/kotlin/domain/SupermarketTest.kt`:
```kotlin
package domain

import model.BusinessHours
import model.Product
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.DayOfWeek
import java.time.LocalTime

class SupermarketTest {

    private lateinit var carne: Product
    private lateinit var pescado: Product
    private lateinit var supermarket: Supermarket

    @BeforeEach
    fun setUp() {
        carne = Product(1, "Carne", 10.0)
        pescado = Product(2, "Pescado", 20.0)
        supermarket = Supermarket(
            id = 1,
            name = "Supermercado A",
            initialStock = mapOf(carne to 100, pescado to 50)
        )
    }

    // --- registerSale ---

    @Test
    fun `registerSale should return total price`() {
        val total = supermarket.registerSale(1, 3)
        assertEquals(30.0, total)
    }

    @Test
    fun `registerSale should deduct stock`() {
        supermarket.registerSale(1, 3)
        assertEquals(97, supermarket.getStock(1))
    }

    @Test
    fun `registerSale should allow selling all remaining stock`() {
        supermarket.registerSale(2, 50)
        assertEquals(0, supermarket.getStock(2))
    }

    @Test
    fun `registerSale should throw for unknown product`() {
        assertThrows<IllegalArgumentException> {
            supermarket.registerSale(99, 1)
        }
    }

    @Test
    fun `registerSale should throw for insufficient stock`() {
        assertThrows<IllegalArgumentException> {
            supermarket.registerSale(1, 101)
        }
    }

    @Test
    fun `registerSale should throw for zero quantity`() {
        assertThrows<IllegalArgumentException> {
            supermarket.registerSale(1, 0)
        }
    }

    @Test
    fun `registerSale should throw for negative quantity`() {
        assertThrows<IllegalArgumentException> {
            supermarket.registerSale(1, -1)
        }
    }

    // --- getQuantitySold ---

    @Test
    fun `getQuantitySold should return total quantity sold`() {
        supermarket.registerSale(1, 3)
        supermarket.registerSale(1, 2)
        assertEquals(5, supermarket.getQuantitySold(1))
    }

    @Test
    fun `getQuantitySold should return zero when no sales`() {
        assertEquals(0, supermarket.getQuantitySold(1))
    }

    @Test
    fun `getQuantitySold should throw for unknown product`() {
        assertThrows<IllegalArgumentException> {
            supermarket.getQuantitySold(99)
        }
    }

    // --- getSalesRevenue ---

    @Test
    fun `getSalesRevenue should return revenue for product`() {
        supermarket.registerSale(1, 3) // 30.0
        supermarket.registerSale(1, 2) // 20.0
        assertEquals(50.0, supermarket.getSalesRevenue(1))
    }

    @Test
    fun `getSalesRevenue should return zero when no sales`() {
        assertEquals(0.0, supermarket.getSalesRevenue(1))
    }

    @Test
    fun `getSalesRevenue should throw for unknown product`() {
        assertThrows<IllegalArgumentException> {
            supermarket.getSalesRevenue(99)
        }
    }

    // --- getTotalRevenue ---

    @Test
    fun `getTotalRevenue should sum all sales`() {
        supermarket.registerSale(1, 3) // 30.0
        supermarket.registerSale(2, 2) // 40.0
        assertEquals(70.0, supermarket.getTotalRevenue())
    }

    @Test
    fun `getTotalRevenue should return zero when no sales`() {
        assertEquals(0.0, supermarket.getTotalRevenue())
    }

    // --- isOpenAt ---

    @Test
    fun `isOpenAt should return false when no business hours set`() {
        assertFalse(supermarket.isOpenAt(DayOfWeek.MONDAY, LocalTime.of(12, 0)))
    }

    @Test
    fun `isOpenAt should delegate to business hours`() {
        val hours = BusinessHours(
            LocalTime.of(9, 0), LocalTime.of(21, 0),
            setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY)
        )
        val sm = Supermarket(1, "Test", mapOf(carne to 10), hours)
        assertTrue(sm.isOpenAt(DayOfWeek.MONDAY, LocalTime.of(12, 0)))
        assertFalse(sm.isOpenAt(DayOfWeek.SATURDAY, LocalTime.of(12, 0)))
    }

    // --- getSales ---

    @Test
    fun `getSales should return defensive copy`() {
        supermarket.registerSale(1, 3)
        val salesCopy = supermarket.getSales()
        assertEquals(1, salesCopy.size)
        supermarket.registerSale(2, 1)
        assertEquals(1, salesCopy.size) // original copy unchanged
    }
}
```

**Step 2: Run tests to verify they fail**

Run: `./gradlew test --tests "domain.SupermarketTest" 2>&1 | tail -5`
Expected: FAIL — `Supermarket` class does not exist.

**Step 3: Implement Supermarket**

`src/main/kotlin/domain/Supermarket.kt`:
```kotlin
package domain

import model.BusinessHours
import model.Product
import model.Sale
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

    /**
     * Registers a sale for the given product and quantity.
     * Deducts stock and records the sale.
     * @return the total price of the sale
     * @throws IllegalArgumentException if product not found, quantity invalid, or insufficient stock
     */
    fun registerSale(productId: Int, quantity: Int): Double {
        require(quantity > 0) { "Quantity must be positive" }
        val product = findProduct(productId)
        val currentStock = stock.getValue(productId)
        require(currentStock >= quantity) {
            "Insufficient stock for '${product.name}'. Available: $currentStock, requested: $quantity"
        }

        stock[productId] = currentStock - quantity
        val sale = Sale(product, quantity)
        sales.add(sale)
        return sale.total
    }

    /** Returns the total quantity sold for the given product. */
    fun getQuantitySold(productId: Int): Int {
        findProduct(productId)
        return sales.filter { it.product.id == productId }.sumOf { it.quantity }
    }

    /** Returns the total revenue from sales of the given product. */
    fun getSalesRevenue(productId: Int): Double {
        findProduct(productId)
        return sales.filter { it.product.id == productId }.sumOf { it.total }
    }

    /** Returns the total revenue from all sales. */
    fun getTotalRevenue(): Double = sales.sumOf { it.total }

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
```

**Step 4: Run tests to verify they pass**

Run: `./gradlew test --tests "domain.SupermarketTest"`
Expected: BUILD SUCCESSFUL, all 19 tests pass.

**Step 5: Run full test suite**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL, all 29 tests pass.

**Step 6: Commit**

```bash
git add src/main/kotlin/domain/Supermarket.kt src/test/kotlin/domain/SupermarketTest.kt
git commit -m "feat: add Supermarket with sales, stock management, and business hours"
```

---

### Task 5: SupermarketChain — Aggregation Features (TDD)

**Files:**
- Create: `src/test/kotlin/domain/SupermarketChainTest.kt`
- Create: `src/main/kotlin/domain/SupermarketChain.kt`

**Step 1: Write failing tests for SupermarketChain**

`src/test/kotlin/domain/SupermarketChainTest.kt`:
```kotlin
package domain

import model.BusinessHours
import model.Product
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.DayOfWeek
import java.time.LocalTime

class SupermarketChainTest {

    private lateinit var carne: Product
    private lateinit var pescado: Product
    private lateinit var pollo: Product
    private lateinit var cerdo: Product
    private lateinit var ternera: Product
    private lateinit var cordero: Product

    private lateinit var supermarketA: Supermarket
    private lateinit var supermarketB: Supermarket
    private lateinit var supermarketC: Supermarket
    private lateinit var chain: SupermarketChain

    @BeforeEach
    fun setUp() {
        carne = Product(1, "Carne", 10.0)
        pescado = Product(2, "Pescado", 20.0)
        pollo = Product(3, "Pollo", 30.0)
        cerdo = Product(4, "Cerdo", 45.0)
        ternera = Product(5, "Ternera", 50.0)
        cordero = Product(6, "Cordero", 65.0)

        val allProducts = mapOf(
            carne to 100, pescado to 100, pollo to 100,
            cerdo to 100, ternera to 100, cordero to 100
        )

        val weekdayHours = BusinessHours(
            LocalTime.of(9, 0), LocalTime.of(21, 0),
            setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
        )
        val weekendHours = BusinessHours(
            LocalTime.of(10, 0), LocalTime.of(14, 0),
            setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        )

        supermarketA = Supermarket(1, "Supermercado A", allProducts, weekdayHours)
        supermarketB = Supermarket(2, "Supermercado B", allProducts, weekendHours)
        supermarketC = Supermarket(3, "Supermercado C", allProducts)

        chain = SupermarketChain(listOf(supermarketA, supermarketB, supermarketC))
    }

    // --- getTopFiveProducts ---

    @Test
    fun `getTopFiveProducts should return top 5 by quantity across all supermarkets`() {
        supermarketA.registerSale(1, 10)  // Carne: 10
        supermarketB.registerSale(1, 5)   // Carne: +5 = 15
        supermarketA.registerSale(2, 12)  // Pescado: 12
        supermarketB.registerSale(3, 8)   // Pollo: 8
        supermarketC.registerSale(4, 7)   // Cerdo: 7
        supermarketC.registerSale(5, 6)   // Ternera: 6
        supermarketA.registerSale(6, 3)   // Cordero: 3

        val result = chain.getTopFiveProducts()
        assertEquals("Carne: 15 - Pescado: 12 - Pollo: 8 - Cerdo: 7 - Ternera: 6", result)
    }

    @Test
    fun `getTopFiveProducts should return fewer than 5 if not enough products sold`() {
        supermarketA.registerSale(1, 10)
        supermarketA.registerSale(2, 5)

        val result = chain.getTopFiveProducts()
        assertEquals("Carne: 10 - Pescado: 5", result)
    }

    @Test
    fun `getTopFiveProducts should return empty string when no sales`() {
        assertEquals("", chain.getTopFiveProducts())
    }

    // --- getTotalRevenue ---

    @Test
    fun `getTotalRevenue should sum revenue from all supermarkets`() {
        supermarketA.registerSale(1, 3) // 30.0
        supermarketB.registerSale(2, 2) // 40.0
        supermarketC.registerSale(3, 1) // 30.0
        assertEquals(100.0, chain.getTotalRevenue())
    }

    @Test
    fun `getTotalRevenue should return zero when no sales`() {
        assertEquals(0.0, chain.getTotalRevenue())
    }

    // --- getHighestRevenueSupermarket ---

    @Test
    fun `getHighestRevenueSupermarket should return formatted string`() {
        supermarketA.registerSale(1, 10) // 100.0
        supermarketB.registerSale(2, 2)  // 40.0
        supermarketC.registerSale(3, 1)  // 30.0

        assertEquals(
            "Supermercado A (1). Ingresos totales: 100.0",
            chain.getHighestRevenueSupermarket()
        )
    }

    @Test
    fun `getHighestRevenueSupermarket should throw when chain is empty`() {
        val emptyChain = SupermarketChain(emptyList())
        assertThrows<IllegalStateException> {
            emptyChain.getHighestRevenueSupermarket()
        }
    }

    // --- getOpenSupermarketsAt ---

    @Test
    fun `getOpenSupermarketsAt should return weekday supermarket on Monday`() {
        val result = chain.getOpenSupermarketsAt(DayOfWeek.MONDAY, LocalTime.of(12, 0))
        assertEquals("Supermercado A (1)", result)
    }

    @Test
    fun `getOpenSupermarketsAt should return weekend supermarket on Saturday`() {
        val result = chain.getOpenSupermarketsAt(DayOfWeek.SATURDAY, LocalTime.of(11, 0))
        assertEquals("Supermercado B (2)", result)
    }

    @Test
    fun `getOpenSupermarketsAt should return empty string when none open`() {
        val result = chain.getOpenSupermarketsAt(DayOfWeek.SUNDAY, LocalTime.of(23, 0))
        assertEquals("", result)
    }

    @Test
    fun `getOpenSupermarketsAt should exclude supermarkets without business hours`() {
        // supermarketC has no business hours, should never appear
        val result = chain.getOpenSupermarketsAt(DayOfWeek.MONDAY, LocalTime.of(12, 0))
        assertFalse(result.contains("Supermercado C"))
    }

    private fun assertFalse(condition: Boolean) {
        org.junit.jupiter.api.Assertions.assertFalse(condition)
    }
}
```

**Step 2: Run tests to verify they fail**

Run: `./gradlew test --tests "domain.SupermarketChainTest" 2>&1 | tail -5`
Expected: FAIL — `SupermarketChain` class does not exist.

**Step 3: Implement SupermarketChain**

`src/main/kotlin/domain/SupermarketChain.kt`:
```kotlin
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
```

**Step 4: Run tests to verify they pass**

Run: `./gradlew test --tests "domain.SupermarketChainTest"`
Expected: BUILD SUCCESSFUL, all 11 tests pass.

**Step 5: Run full test suite**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL, all 40 tests pass.

**Step 6: Commit**

```bash
git add src/main/kotlin/domain/SupermarketChain.kt src/test/kotlin/domain/SupermarketChainTest.kt
git commit -m "feat: add SupermarketChain with aggregation and business hours query"
```

---

### Task 6: Final Verification and Cleanup

**Files:**
- Modify: `src/main/kotlin/Main.kt`

**Step 1: Update Main.kt with a demo**

`src/main/kotlin/Main.kt`:
```kotlin
import domain.Supermarket
import domain.SupermarketChain
import model.BusinessHours
import model.Product
import java.time.DayOfWeek
import java.time.LocalTime

fun main() {
    // Set up products
    val carne = Product(1, "Carne", 10.0)
    val pescado = Product(2, "Pescado", 20.0)
    val pollo = Product(3, "Pollo", 30.0)
    val cerdo = Product(4, "Cerdo", 45.0)
    val ternera = Product(5, "Ternera", 50.0)
    val cordero = Product(6, "Cordero", 65.0)

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
```

**Step 2: Run the demo**

Run: `./gradlew run` (requires adding `application` plugin) or `./gradlew build && kotlin -cp build/classes/kotlin/main MainKt`

If using application plugin, add to `build.gradle.kts`:
```kotlin
plugins {
    kotlin("jvm") version "2.0.0"
    application
}

application {
    mainClass.set("MainKt")
}
```

Expected output:
```
Top 5 products: Carne: 15 - Pescado: 12 - Pollo: 8 - Cerdo: 7 - Ternera: 6
Total revenue: 1005.0
Highest revenue: Supermercado A (1). Ingresos totales: 535.0
Open on Monday at 12:00: Supermercado A (1)
Open on Saturday at 11:00: Supermercado B (2)
```

**Step 3: Run full test suite one final time**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL, all 40 tests pass.

**Step 4: Commit and push**

```bash
git add src/main/kotlin/Main.kt build.gradle.kts
git commit -m "feat: add demo in Main and application plugin"
git push origin main
```
