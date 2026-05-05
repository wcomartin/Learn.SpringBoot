package com.learn.springboot.sales.order

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "sales_orders")
data class SalesOrder(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true)
    var orderNumber: String = "",

    var customerId: Long = 0,  // reference to customer context (no JPA association across contexts)

    @Enumerated(EnumType.STRING)
    var status: SalesOrderStatus = SalesOrderStatus.DRAFT,

    var orderDate: LocalDate = LocalDate.now(),
    var dueDate: LocalDate? = null,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    val lineItems: MutableList<SalesOrderLine> = mutableListOf(),

    val createdAt: Instant = Instant.now()
) {
    val total: BigDecimal get() = lineItems.sumOf { it.lineTotal }
}

enum class SalesOrderStatus { DRAFT, CONFIRMED, SHIPPED, DELIVERED, CANCELLED }

@Entity
@Table(name = "sales_order_lines")
data class SalesOrderLine(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: SalesOrder? = null,

    var productId: Long = 0,  // reference to inventory context
    var productSku: String = "",
    var productName: String = "",

    var quantity: Int = 0,

    @Column(precision = 10, scale = 2)
    var unitPrice: BigDecimal = BigDecimal.ZERO
) {
    val lineTotal: BigDecimal get() = unitPrice * quantity.toBigDecimal()
}
