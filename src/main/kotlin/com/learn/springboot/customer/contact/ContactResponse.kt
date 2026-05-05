package com.learn.springboot.customer.contact

import com.learn.springboot.customer.company.CompanyResponse
import java.time.Instant

data class ContactResponse(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val email: String?,
    val phone: String?,
    val jobTitle: String?,
    val isPrimary: Boolean,
    val company: CompanyResponse?,
    val createdAt: Instant
) {
    companion object {
        fun from(contact: Contact, primaryContactId: Long? = null) = ContactResponse(
            id = contact.id,
            firstName = contact.firstName,
            lastName = contact.lastName,
            fullName = contact.fullName,
            email = contact.email,
            phone = contact.phone,
            jobTitle = contact.jobTitle,
            isPrimary = contact.id == primaryContactId,
            company = contact.company?.let { CompanyResponse.from(it) },
            createdAt = contact.createdAt
        )
    }
}
