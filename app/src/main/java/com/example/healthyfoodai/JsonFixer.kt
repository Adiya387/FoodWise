package com.example.healthyfoodai.utils

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser

object JsonFixer {

    private const val TAG = "JsonFixer"

    fun convertDaysToArrays(json: String): String {
        return try {
            val gson = Gson()
            val parser = JsonParser.parseString(json)

            if (!parser.isJsonObject) {
                Log.e(TAG, "❌ Корень JSON не является объектом.")
                return json
            }

            val root = parser.asJsonObject
            val fixed = JsonObject()

            for ((day, element) in root.entrySet()) {
                if (element.isJsonObject) {
                    val meals = element.asJsonObject.entrySet().map { it.value }
                    val array = gson.toJsonTree(meals).asJsonArray
                    fixed.add(day, array)
                    Log.d(TAG, "✅ Преобразован день '$day' в массив.")
                } else {
                    fixed.add(day, element)
                    Log.d(TAG, "✅ День '$day' уже массив или другой формат — оставлен как есть.")
                }
            }

            val fixedJson = gson.toJson(fixed)
            Log.d(TAG, "✅ Итоговый JSON:\n$fixedJson")
            fixedJson

        } catch (e: Exception) {
            Log.e(TAG, "❌ Ошибка в JsonFixer: ${e.message}")
            json
        }
    }
}
