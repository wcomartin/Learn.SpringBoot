package com.learn.springboot.admin

import com.learn.springboot.auth.Role
import com.learn.springboot.auth.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

data class AdminUserResponse(val id: Long, val email: String, val name: String, val role: Role)

@RestController
@RequestMapping("/api/admin")
class AdminController(private val userRepo: UserRepository) {

    @GetMapping("/users")
    fun listUsers() = userRepo.findAll().map { AdminUserResponse(it.id, it.email, it.name, it.role) }

    @PatchMapping("/users/{id}/role")
    fun updateRole(@PathVariable id: Long, @RequestBody body: Map<String, String>): AdminUserResponse {
        val user = userRepo.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        user.role = Role.valueOf(body["role"] ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST))
        val saved = userRepo.save(user)
        return AdminUserResponse(saved.id, saved.email, saved.name, saved.role)
    }
}
