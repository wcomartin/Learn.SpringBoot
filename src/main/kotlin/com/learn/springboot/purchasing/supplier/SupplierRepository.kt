package com.learn.springboot.purchasing.supplier

import org.springframework.data.jpa.repository.JpaRepository

interface SupplierRepository : JpaRepository<Supplier, Long> {
    fun findByStatus(status: SupplierStatus): List<Supplier>
}
