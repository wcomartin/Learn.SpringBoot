package com.learn.springboot.inventory.product

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface ProductRepository : JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    fun findBySku(sku: String): Product?
    fun findByStatus(status: ProductStatus): List<Product>
}
