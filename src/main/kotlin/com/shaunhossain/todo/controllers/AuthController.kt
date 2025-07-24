package com.shaunhossain.todo.controllers

import com.shaunhossain.todo.security.AuthService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {

    data class AuthRequest(val email: String, val password: String)

    data class RefreshToken(val refreshToken: String)

    @PostMapping("/register")
    fun register(
        @RequestBody body: AuthRequest,
    ) {
        authService.registerUser(email = body.email, password = body.password)
    }

    @PostMapping("/login")
    fun login(
        @RequestBody body: AuthRequest,
    ): AuthService.TokenPair {
        return  authService.login(email = body.email, password = body.password)
    }

    @PostMapping("/refresh")
    fun refresh(
        @RequestBody body: RefreshToken,
    ): AuthService.TokenPair {
        return  authService.refresh(refreshToken = body.refreshToken)
    }

}