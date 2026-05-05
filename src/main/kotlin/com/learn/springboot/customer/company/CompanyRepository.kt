package com.learn.springboot.customer.company

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface CompanyRepository : JpaRepository<Company, Long>, JpaSpecificationExecutor<Company> {
    fun findByStatus(status: CompanyStatus): List<Company>
    fun findByNameContainingIgnoreCase(name: String): List<Company>
}
