package com.learn.springboot.purchasing.purchaseorder

import com.learn.springboot.purchasing.supplier.Supplier
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "purchase_orders")
data class PurchaseOrder(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true)
    var poNumber: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    var supplier: Supplier? = null,

    @Enumerated(EnumType.STRING)
    var status: PurchaseOrderStatus = PurchaseOrderStatus.DRAFT,

    var orderDate: LocalDate = LocalDate.now(),
    var expectedDelivery: LocalDate? = null,
    var warehouseId: Long = 0,  // reference to inventory context

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    val lineItems: MutableList<PurchaseOrderLine> = mutableListOf(),

    val createdAt: Instant = Instant.now()
) {
    val total: BigDecimal get() = lineItems.sumOf { it.lineTotal }
}

enum class PurchaseOrderStatus { DRAFT, SUBMITTED, ACKNOWLEDGED, RECEIVED, CANCELLED }

@Entity
@Table(name = "purchase_order_lines")
data class PurchaseOrderLine(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: PurchaseOrder? = null,

    var productId: Long = 0,
    var productSku: String = "",
    var productName: String = "",

    var quantity: Int = 0,
    var receivedQuantity: Int = 0,

    @Column(precision = 10, scale = 2)
    var unitCost: BigDecimal = BigDecimal.ZERO
) {
    val lineTotal: BigDecimal get() = unitCost * quantity.toBigDecimal()
    val fullyReceived: Boolean get() = receivedQuantity >= quantity
}
