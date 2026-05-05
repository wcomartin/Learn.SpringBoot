package com.learn.springboot.purchasing.supplier

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import java.time.Instant

@Entity
@Table(name = "suppliers")
data class Supplier(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @field:NotBlank
    var name: String = "",

    var contactName: String? = null,
    var email: String? = null,
    var phone: String? = null,
    var leadTimeDays: Int = 0,

    @Enumerated(EnumType.STRING)
    var status: SupplierStatus = SupplierStatus.ACTIVE,

    val createdAt: Instant = Instant.now()
)

enum class SupplierStatus { ACTIVE, INACTIVE }
