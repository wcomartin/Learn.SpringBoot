package com.learn.springboot.customer.contact

import com.learn.springboot.customer.company.Company
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import java.time.Instant

@Entity
@Table(name = "contacts")
data class Contact(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @field:NotBlank
    var firstName: String = "",

    @field:NotBlank
    var lastName: String = "",

    var email: String? = null,
    var phone: String? = null,
    var jobTitle: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    var company: Company? = null,

    val createdAt: Instant = Instant.now()
) {
    val fullName: String get() = "$firstName $lastName"
}
