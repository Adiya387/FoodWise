package com.example.healthyfoodai

data class ChatMessage(
    val role: String,         // "user" или "assistant"
    val content: String       // Текст запроса или ответа
)
