package com.shaunhossain.todo.controllers

import com.shaunhossain.todo.controllers.NoteController.NoteResponse
import com.shaunhossain.todo.database.model.Note
import com.shaunhossain.todo.database.repository.NoteRepository
import org.bson.types.ObjectId
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant


@RestController
@RequestMapping("/notes")
class NoteController(
    private val repository: NoteRepository,
    private val noteRepository: NoteRepository
) {

    data class NoteRequest(
        val title: String,
        val content: String,
        val color: Long,
        val id: String?
    )

    data class NoteResponse(
        val id: String,
        val title: String,
        val content: String,
        val color: Long,
        val createdAt: Instant
    )

    @PostMapping
    fun save(
        @RequestBody body: NoteRequest
    ): NoteResponse {
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        val note = repository.save(Note(
            id = body.id?.let { ObjectId(it) } ?: ObjectId.get(),
            title = body.title,
            content = body.content,
            color = body.color,
            createdAt = Instant.now(),
            ownerId = ObjectId(ownerId)
        ))
        return note.toResponse()
    }


    @GetMapping
    fun findByOwnerId(): List<NoteResponse> {
       val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        return repository.findByOwnerId((ObjectId(ownerId))).map {
            it.toResponse()
        }
    }

    @DeleteMapping(path = ["/{id}"])
    fun deleteById(
        @PathVariable id: String
    ) {
        val note = noteRepository.findById(ObjectId(id)).orElseThrow{
            IllegalArgumentException("Not found note")
        }
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String

        if(note.ownerId.toHexString() == ownerId) {
            repository.deleteById(ObjectId(id))
        }

    }
}

private fun Note.toResponse(): NoteController.NoteResponse {
    return NoteResponse(
        id = id.toHexString(),
        title = title,
        content = content,
        color = color,
        createdAt = createdAt
    )
}