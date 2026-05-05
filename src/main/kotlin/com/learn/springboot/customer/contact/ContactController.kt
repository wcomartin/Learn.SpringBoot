package com.learn.springboot.customer.contact

import jakarta.validation.Valid
import org.springframework.data.jpa.domain.Specification
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

data class ContactPatch(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val jobTitle: String? = null,
    val companyId: Long? = null
)

@RestController
@RequestMapping("/api/contacts")
class ContactController(
    private val repo: ContactRepository,
    private val companyRepo: com.learn.springboot.customer.company.CompanyRepository
) {

    @GetMapping
    fun list(
        @RequestParam(required = false) companyId: Long?,
        @RequestParam(required = false) search: String?
    ): List<ContactResponse> {
        var spec = Specification.where<Contact>(null)
        companyId?.let { spec = spec.and(ContactSpecs.belongsToCompany(it)) }
        search?.let { spec = spec.and(ContactSpecs.nameContains(it)) }
        return repo.findAll(spec).map { contact ->
            val primaryId = contact.company?.primaryContact?.id
            ContactResponse.from(contact, primaryId)
        }
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ContactResponse {
        val contact = repo.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        val primaryId = contact.company?.primaryContact?.id
        return ContactResponse.from(contact, primaryId)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody contact: Contact): ContactResponse {
        val saved = repo.save(contact)
        return ContactResponse.from(saved)
    }

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody patch: ContactPatch): ContactResponse {
        val contact = repo.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        patch.firstName?.let { contact.firstName = it }
        patch.lastName?.let { contact.lastName = it }
        patch.email?.let { contact.email = it }
        patch.phone?.let { contact.phone = it }
        patch.jobTitle?.let { contact.jobTitle = it }
        patch.companyId?.let { cid ->
            contact.company = companyRepo.findById(cid)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found") }
        }
        val saved = repo.save(contact)
        val primaryId = saved.company?.primaryContact?.id
        return ContactResponse.from(saved, primaryId)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = repo.deleteById(id)
}
