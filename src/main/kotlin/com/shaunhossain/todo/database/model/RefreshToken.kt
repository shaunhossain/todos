package com.shaunhossain.todo.database.model

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant


@Document("refresh_token")
data class RefreshToken(
    val userId: ObjectId,
    val hashedToken: String,
    @Indexed(expireAfter = "0s")
    val expiresAt: Instant,
    val createdAt: Instant = Instant.now()
)
