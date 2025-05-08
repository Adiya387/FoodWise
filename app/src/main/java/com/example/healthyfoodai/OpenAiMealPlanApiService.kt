package com.example.healthyfoodai.api

import com.example.healthyfoodai.GPTRequest
import com.example.healthyfoodai.GPTResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface OpenAiMealPlanApiService {
    @Headers(
        "Content-Type: application/json",
        "Authorization: Bearer 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"
    )
    @POST("v1/chat/completions")
    suspend fun generatePlan(@Body request: GPTRequest): GPTResponse
}
