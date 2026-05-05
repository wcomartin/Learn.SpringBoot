package com.learn.springboot.auth

import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

data class RegisterRequest(val email: String, val password: String, val name: String)
data class LoginRequest(val email: String, val password: String)
data class AuthResponse(val token: String, val user: UserResponse)
data class UserResponse(val id: Long, val email: String, val name: String, val role: Role)

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userRepo: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@RequestBody body: RegisterRequest): AuthResponse {
        if (userRepo.findByEmail(body.email) != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Email already taken")
        }
        val user = userRepo.save(User(
            email = body.email,
            password = passwordEncoder.encode(body.password),
            name = body.name
        ))
        return AuthResponse(jwtService.generateToken(user), user.toResponse())
    }

    @PostMapping("/login")
    fun login(@RequestBody body: LoginRequest): AuthResponse {
        val user = userRepo.findByEmail(body.email)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")
        if (!passwordEncoder.matches(body.password, user.password)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")
        }
        return AuthResponse(jwtService.generateToken(user), user.toResponse())
    }

    @GetMapping("/me")
    fun me(): UserResponse {
        val user = SecurityContextHolder.getContext().authentication.principal as User
        return user.toResponse()
    }

    private fun User.toResponse() = UserResponse(id, email, name, role)
}
