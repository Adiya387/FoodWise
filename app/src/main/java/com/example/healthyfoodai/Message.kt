package com.example.healthyfoodai

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("role")
    val role: String,

    @SerializedName("content")
    val content: String,

    // Добавлено: флаг, чтобы можно было вручную указать, что это рецепт
    val isRecipe: Boolean = false
)
