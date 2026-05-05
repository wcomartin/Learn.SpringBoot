package com.learn.springboot.customer.company

import jakarta.validation.Valid
import org.springframework.data.jpa.domain.Specification
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

data class CompanyPatch(
    val name: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val website: String? = null,
    val status: CompanyStatus? = null,
    val primaryContactId: Long? = null
)

@RestController
@RequestMapping("/api/companies")
class CompanyController(
    private val repo: CompanyRepository,
    private val contactRepo: com.learn.springboot.customer.contact.ContactRepository
) {

    @GetMapping
    fun list(
        @RequestParam(required = false) status: CompanyStatus?,
        @RequestParam(required = false) search: String?
    ): List<CompanyResponse> {
        var spec = Specification.where<Company>(null)
        status?.let { spec = spec.and(CompanySpecs.hasStatus(it)) }
        search?.let { spec = spec.and(CompanySpecs.nameContains(it)) }
        return repo.findAll(spec).map { CompanyResponse.from(it) }
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): CompanyResponse {
        val company = repo.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        return CompanyResponse.from(company)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody company: Company): CompanyResponse {
        return CompanyResponse.from(repo.save(company))
    }

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody patch: CompanyPatch): CompanyResponse {
        val company = repo.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        patch.name?.let { company.name = it }
        patch.phone?.let { company.phone = it }
        patch.email?.let { company.email = it }
        patch.website?.let { company.website = it }
        patch.status?.let { company.status = it }
        patch.primaryContactId?.let { cid ->
            company.primaryContact = contactRepo.findById(cid)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found") }
        }
        return CompanyResponse.from(repo.save(company))
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = repo.deleteById(id)
}
