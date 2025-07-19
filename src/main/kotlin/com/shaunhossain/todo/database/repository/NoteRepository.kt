package com.shaunhossain.todo.database.repository

import com.shaunhossain.todo.database.model.Note
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface NoteRepository: MongoRepository<Note,ObjectId> {
    fun findByOwnerId(ownerId: ObjectId): List<Note>
}