package com.learn.springboot.customer.company

import java.time.Instant

data class CompanyResponse(
    val id: Long,
    val name: String,
    val phone: String?,
    val email: String?,
    val website: String?,
    val status: CompanyStatus,
    val primaryContactId: Long?,
    val createdAt: Instant
) {
    companion object {
        fun from(company: Company) = CompanyResponse(
            id = company.id,
            name = company.name,
            phone = company.phone,
            email = company.email,
            website = company.website,
            status = company.status,
            primaryContactId = company.primaryContact?.id,
            createdAt = company.createdAt
        )
    }
}
