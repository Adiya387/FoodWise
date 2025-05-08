package com.example.healthyfoodai

import android.content.Context

data class UserProfile(
    val name: String,
    val gender: String,
    val age: String,
    val height: String,
    val weight: String,
    val goal: String,
    val activity: String,
    val allergies: List<String>
)

object UserManagerr {

    fun getUserProfile(context: Context): UserProfile {
        val prefs = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)

        val allergiesString = prefs.getString("allergies", "") ?: ""
        val allergiesList = if (allergiesString.isNotEmpty()) {
            allergiesString.split(",").map { it.trim() }
        } else {
            emptyList()
        }

        return UserProfile(
            name = prefs.getString("name", "") ?: "",
            gender = prefs.getString("gender", "") ?: "",
            age = prefs.getString("age", "") ?: "",
            height = prefs.getString("height", "") ?: "",
            weight = prefs.getString("weight", "") ?: "",
            goal = prefs.getString("goal", "") ?: "",
            activity = prefs.getString("activity", "") ?: "",
            allergies = allergiesList
        )
    }
}
