package com.example.healthyfoodai

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import com.example.healthyfoodai.ChatRequest
import com.example.healthyfoodai.ChatResponse

interface OpenAiAdviceApiService {
    @Headers(
        "Authorization: Bearer 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", // заменишь на свой ключ
        "Content-Type: application/json"
    )
    @POST("v1/chat/completions")
    fun sendAdvice(@Body request: ChatRequest): Call<ChatResponse>
}


