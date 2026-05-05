package com.learn.springboot.inventory.warehouse

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import java.time.Instant

@Entity
@Table(name = "warehouses")
data class Warehouse(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @field:NotBlank
    var name: String = "",

    var address: String? = null,
    var city: String? = null,
    var province: String? = null,
    var postalCode: String? = null,

    val createdAt: Instant = Instant.now()
)
