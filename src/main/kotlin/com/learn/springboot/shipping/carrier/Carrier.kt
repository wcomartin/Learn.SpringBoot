package com.learn.springboot.shipping.carrier

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import java.time.Instant

@Entity
@Table(name = "carriers")
data class Carrier(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @field:NotBlank
    var name: String = "",

    var trackingUrlTemplate: String? = null,  // e.g. "https://track.carrier.com/{tracking_number}"
    var phone: String? = null,

    val createdAt: Instant = Instant.now()
)
