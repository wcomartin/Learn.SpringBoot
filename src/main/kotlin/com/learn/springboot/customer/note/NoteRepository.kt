package com.learn.springboot.customer.note

import org.springframework.data.jpa.repository.JpaRepository

interface NoteRepository : JpaRepository<Note, Long> {
    fun findByContactIdOrderByCreatedAtDesc(contactId: Long): List<Note>
    fun findByKindAndContactId(kind: NoteKind, contactId: Long): List<Note>
}
