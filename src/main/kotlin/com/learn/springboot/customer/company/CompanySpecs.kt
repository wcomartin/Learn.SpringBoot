package com.learn.springboot.customer.company

import org.springframework.data.jpa.domain.Specification

object CompanySpecs {
    fun hasStatus(status: CompanyStatus) = Specification<Company> { root, _, cb ->
        cb.equal(root.get<CompanyStatus>("status"), status)
    }

    fun nameContains(term: String) = Specification<Company> { root, _, cb ->
        cb.like(cb.lower(root.get("name")), "%${term.lowercase()}%")
    }

    fun hasEmail() = Specification<Company> { root, _, cb ->
        cb.isNotNull(root.get<String>("email"))
    }
}
