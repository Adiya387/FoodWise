package com.example.healthyfoodai

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object JsonUtils {
    fun loadRecipesFromAssets(context: Context): List<Recipe> {
        return try {
            val json = context.assets.open("recipes1.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<Recipe>>() {}.type
            Gson().fromJson(json, listType)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
