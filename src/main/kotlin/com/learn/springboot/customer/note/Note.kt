package com.learn.springboot.customer.note

import com.learn.springboot.customer.contact.Contact
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import java.time.Instant

@Entity
@Table(name = "notes")
data class Note(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @field:NotBlank
    var body: String = "",

    @Enumerated(EnumType.STRING)
    var kind: NoteKind = NoteKind.GENERAL,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    var contact: Contact? = null,

    val createdAt: Instant = Instant.now()
)

enum class NoteKind { GENERAL, CALL, EMAIL, MEETING }
