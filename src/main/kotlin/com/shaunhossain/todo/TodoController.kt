package com.shaunhossain.todo

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("todos")
class TodoController {
    @GetMapping
    fun getTodos(): List<String> {
        return listOf("todo1","todo2","todo3")
    }
}