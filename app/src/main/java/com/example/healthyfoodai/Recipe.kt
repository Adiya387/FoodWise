package com.example.healthyfoodai

import java.io.Serializable

data class Recipe(
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val imageUrl: String = "",
    val calories: Int = 0,
    val proteins: Int = 0,
    val fats: Int = 0,
    val carbs: Int = 0,
    val ingredients: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val unhealthyFactors: List<String> = emptyList()
) : Serializable
