package com.learn.springboot.customer.company

import com.learn.springboot.customer.contact.Contact
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import java.time.Instant

@Entity
@Table(name = "companies")
data class Company(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @field:NotBlank
    var name: String = "",

    var phone: String? = null,
    var email: String? = null,
    var website: String? = null,

    @Enumerated(EnumType.STRING)
    var status: CompanyStatus = CompanyStatus.ACTIVE,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_contact_id")
    var primaryContact: Contact? = null,

    val createdAt: Instant = Instant.now()
)

enum class CompanyStatus { LEAD, ACTIVE, INACTIVE }
