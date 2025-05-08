package com.example.healthyfoodai

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitAdviceInstance {
    private const val BASE_URL = "https://api.openai.com/"

    val api: OpenAiAdviceApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())  // ✅ важно!
            .build()
            .create(OpenAiAdviceApiService::class.java)
    }
}
