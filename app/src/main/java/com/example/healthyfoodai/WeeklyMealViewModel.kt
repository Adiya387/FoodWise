package com.example.healthyfoodai

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WeeklyMealViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    fun getMealsForDay(day: String): LiveData<List<MealPlan>> {
        val result = MutableLiveData<List<MealPlan>>()

        firestore.collection("weekly_plan")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val rawDayData = doc.get(day)

                    val meals: List<MealPlan> = when (rawDayData) {
                        is List<*> -> {
                            // Старый формат: список рецептов
                            rawDayData.mapNotNull { item ->
                                val map = item as? Map<*, *> ?: return@mapNotNull null
                                mapToMealPlan(map)
                            }
                        }
                        is Map<*, *> -> {
                            // Новый формат: карта из "Завтрак", "Обед", ...
                            rawDayData.values.mapNotNull { item ->
                                val map = item as? Map<*, *> ?: return@mapNotNull null
                                mapToMealPlan(map)
                            }
                        }
                        else -> emptyList()
                    }

                    result.value = meals
                } else {
                    result.value = emptyList()
                }
            }
            .addOnFailureListener {
                result.value = emptyList()
            }

        return result
    }

    // 🔹 ВСПОМОГАТЕЛЬНАЯ ФУНКЦИЯ
    private fun mapToMealPlan(map: Map<*, *>): MealPlan {
        val recipeMap = map

        val recipe = Recipe(
            title = recipeMap["name"] as? String ?: "",
            description = recipeMap["description"] as? String ?: "",
            category = recipeMap["category"] as? String ?: "",
            imageUrl = recipeMap["imageUrl"] as? String ?: "",
            calories = (recipeMap["calories"] as? Number)?.toInt() ?: 0,
            proteins = (recipeMap["proteins"] as? Number)?.toInt() ?: 0,
            fats = (recipeMap["fats"] as? Number)?.toInt() ?: 0,
            carbs = (recipeMap["carbs"] as? Number)?.toInt() ?: 0,
            ingredients = (recipeMap["ingredients"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            steps = (recipeMap["steps"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            unhealthyFactors = (recipeMap["unhealthyFactors"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
        )

        val type = map["type"] as? String
            ?: map["mealType"] as? String
            ?: recipe.category.takeIf { it.isNotBlank() }
            ?: "Без категории"

        return MealPlan(type = type, recipe = recipe)
    }


}
