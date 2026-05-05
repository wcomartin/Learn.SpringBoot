package com.learn.springboot.inventory.warehouse

import org.springframework.data.jpa.repository.JpaRepository

interface WarehouseRepository : JpaRepository<Warehouse, Long>
