package com.example.healthyfoodai

data class MealPlan(
    val type: String,
    val recipe: Recipe // <- Вместо title и calories, храним целый объект
)
