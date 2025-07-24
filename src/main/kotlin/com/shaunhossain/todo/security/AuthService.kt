package com.shaunhossain.todo.security

import com.shaunhossain.todo.database.model.RefreshToken
import com.shaunhossain.todo.database.model.User
import com.shaunhossain.todo.database.repository.RefreshTokenRepository
import com.shaunhossain.todo.database.repository.UserRepository
import org.bson.types.ObjectId
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import java.util.*

@Service
class AuthService(
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
    private val hashEncoder: HashEncoder,
    private val refreshTokenRepository: RefreshTokenRepository
) {

    data class TokenPair(
        val accessToken: String,
        val refreshToken: String
    )

    fun registerUser(email: String, password: String): User {
        return userRepository.save(
            User(
                email = email,
                hashPassword = hashEncoder.encode(password)
            )
        )
    }

    fun login(email: String, password: String): TokenPair {
        val user = userRepository.findByEmail(email) ?: throw BadCredentialsException("This user does not exist")

        if (!hashEncoder.matches(password, user.hashPassword)) {
            throw BadCredentialsException("Invalid credentials")
        }

        val newAccessToken = jwtService.generateAccessToken(user.id.toHexString())
        val newRefreshToken = jwtService.generateRefreshToken(user.id.toHexString())  // fixed: use user id

        storeRefreshToken(userId = user.id, rawRefreshToken = newRefreshToken)

        return TokenPair(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }

    @Transactional
    fun refresh(refreshToken: String): TokenPair {
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw BadCredentialsException("Invalid refresh token")
        }
        val userId = jwtService.getUserIdFromToken(refreshToken)

        val user = userRepository.findById(ObjectId(userId))
            .orElseThrow { IllegalArgumentException("Invalid refresh token") }
        val hashed = hashToken(refreshToken)

        refreshTokenRepository.findByUserIdAndHashedToken(user.id, hashed)
            ?: throw IllegalArgumentException("Refresh token not found")

        refreshTokenRepository.deleteByUserIdAndHashedToken(user.id, hashed)

        val newAccessToken = jwtService.generateAccessToken(userId)
        val newRefreshToken = jwtService.generateRefreshToken(userId)
        storeRefreshToken(userId = user.id, rawRefreshToken = newRefreshToken)
        return TokenPair(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }

    private fun storeRefreshToken(userId: ObjectId, rawRefreshToken: String) {
        val hashed = hashToken(rawRefreshToken)
        val expiryMs = jwtService.refreshTokenValidityMs
        val expiresAt = Instant.now().plusMillis(expiryMs)

        refreshTokenRepository.save(
            RefreshToken(
                userId = userId,
                expiresAt = expiresAt,
                hashedToken = hashed
            )
        )
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.encodeToByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}
