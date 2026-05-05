package com.learn.springboot.auth

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true)
    var email: String = "",

    var password: String = "",
    var name: String = "",

    @Enumerated(EnumType.STRING)
    var role: Role = Role.USER,

    val createdAt: Instant = Instant.now()
)

enum class Role { USER, ADMIN }
