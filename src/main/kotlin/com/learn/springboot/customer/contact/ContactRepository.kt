package com.learn.springboot.customer.contact

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface ContactRepository : JpaRepository<Contact, Long>, JpaSpecificationExecutor<Contact> {
    fun findByCompanyId(companyId: Long): List<Contact>
    fun findByLastNameContainingIgnoreCase(name: String): List<Contact>
}
