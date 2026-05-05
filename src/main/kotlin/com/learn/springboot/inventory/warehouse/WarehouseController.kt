package com.learn.springboot.inventory.warehouse

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/warehouses")
class WarehouseController(private val repo: WarehouseRepository) {

    @GetMapping
    fun list() = repo.findAll()

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long) =
        repo.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody warehouse: Warehouse) = repo.save(warehouse)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = repo.deleteById(id)
}
