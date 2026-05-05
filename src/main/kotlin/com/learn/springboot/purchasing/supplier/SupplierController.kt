package com.learn.springboot.purchasing.supplier

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/suppliers")
class SupplierController(private val repo: SupplierRepository) {

    @GetMapping
    fun list(@RequestParam(required = false) status: SupplierStatus?) =
        status?.let { repo.findByStatus(it) } ?: repo.findAll()

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long) =
        repo.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody supplier: Supplier) = repo.save(supplier)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = repo.deleteById(id)
}
