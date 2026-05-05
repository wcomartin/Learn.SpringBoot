package com.learn.springboot.customer.note

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/contacts/{contactId}/notes")
class NoteController(private val repo: NoteRepository) {

    @GetMapping
    fun list(@PathVariable contactId: Long, @RequestParam(required = false) kind: NoteKind?) =
        kind?.let { repo.findByKindAndContactId(it, contactId) }
            ?: repo.findByContactIdOrderByCreatedAtDesc(contactId)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@PathVariable contactId: Long, @Valid @RequestBody note: Note): Note {
        // contactId is set via the JSON body's contact reference
        return repo.save(note)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = repo.deleteById(id)
}
