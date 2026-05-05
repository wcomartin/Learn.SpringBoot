package com.learn.springboot.shipping.carrier

import org.springframework.data.jpa.repository.JpaRepository

interface CarrierRepository : JpaRepository<Carrier, Long>
