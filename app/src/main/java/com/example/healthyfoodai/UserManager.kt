package com.example.healthyfoodai

import android.content.Context
import android.content.SharedPreferences

class UserManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun saveUserData(name: String, gender: String, age: String, height: String, weight: String, goal: String, activityLevel: String) {
        prefs.edit().apply {
            putString("name", name)
            putString("gender", gender)
            putString("age", age)
            putString("height", height)
            putString("weight", weight)
            putString("goal", goal)
            putString("activity", activityLevel)
            apply()
        }
    }

    fun getUserData(): Map<String, String> {
        return mapOf(
            "name" to prefs.getString("name", "")!!,
            "gender" to prefs.getString("gender", "")!!,
            "age" to prefs.getString("age", "")!!,
            "height" to prefs.getString("height", "")!!,
            "weight" to prefs.getString("weight", "")!!,
            "goal" to prefs.getString("goal", "")!!,
            "activity" to prefs.getString("activity", "")!!
        )
    }
}
