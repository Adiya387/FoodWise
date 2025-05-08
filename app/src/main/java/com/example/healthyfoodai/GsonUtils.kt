package com.example.healthyfoodai

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

object GsonUtils {
    inline fun <reified T> fromJson(json: String): T {
        val type = object : TypeToken<T>() {}.type
        return try {
            Gson().fromJson(json, type)
        } catch (e: JsonSyntaxException) {
            throw IllegalArgumentException("Ошибка при парсинге JSON: ${e.localizedMessage}")
        }
    }
}
