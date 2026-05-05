package com.learn.springboot.customer.contact

import com.learn.springboot.customer.company.Company
import org.springframework.data.jpa.domain.Specification

object ContactSpecs {
    fun belongsToCompany(companyId: Long) = Specification<Contact> { root, _, cb ->
        cb.equal(root.get<Company>("company").get<Long>("id"), companyId)
    }

    fun nameContains(term: String) = Specification<Contact> { root, _, cb ->
        val lower = term.lowercase()
        cb.or(
            cb.like(cb.lower(root.get("firstName")), "%$lower%"),
            cb.like(cb.lower(root.get("lastName")), "%$lower%")
        )
    }

    fun hasEmail() = Specification<Contact> { root, _, cb ->
        cb.isNotNull(root.get<String>("email"))
    }
}
