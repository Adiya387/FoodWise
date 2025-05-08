package com.example.healthyfoodai

data class Meal(
    val name: String = "",
    val calories: Int = 0,
    val proteins: Int = 0,
    val fats: Int = 0,
    val carbs: Int = 0
) {
    fun displayName(): String = name.ifBlank { "Без названия" }
}
