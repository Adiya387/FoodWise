package com.example.healthyfoodai

object NutritionUtils {

    fun calculateBMR(
        gender: String,
        weightKg: Int,
        heightCm: Int,
        age: Int
    ): Double {
        return if (gender.equals("мужчина", true)) {
            10 * weightKg + 6.25 * heightCm - 5 * age + 5
        } else {
            10 * weightKg + 6.25 * heightCm - 5 * age - 161
        }
    }

    fun getActivityMultiplier(activity: String): Double {
        return when (activity.lowercase()) {
            "низкая" -> 1.2
            "умеренная" -> 1.375
            "высокая" -> 1.55
            "очень высокая" -> 1.725
            else -> 1.2
        }
    }

    fun calculateDailyCalories(
        gender: String,
        weight: Int,
        height: Int,
        age: Int,
        activity: String,
        goal: String
    ): Int {
        val bmr = calculateBMR(gender, weight, height, age)
        val activityMultiplier = getActivityMultiplier(activity)
        val baseCalories = bmr * activityMultiplier

        return when (goal.lowercase()) {
            "похудение" -> (baseCalories - 300).toInt()
            "набор массы" -> (baseCalories + 300).toInt()
            else -> baseCalories.toInt()
        }
    }

    fun calculateBMI(weight: Int, height: Int): Double {
        val heightMeters = height / 100.0
        return weight / (heightMeters * heightMeters)
    }

    fun getBMICategory(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Недостаточная масса"
            bmi < 25 -> "Норма"
            bmi < 30 -> "Избыточная масса"
            else -> "Ожирение"
        }
    }

    fun getSmartAdvice(goal: String, bmi: Double): String {
        return when {
            bmi < 18.5 && goal == "похудение" ->
                "У вас недостаточный вес. Лучше сосредоточиться на наборе массы."
            bmi >= 25 && goal == "набор массы" ->
                "У вас избыточный вес. Лучше сначала нормализовать вес, прежде чем набирать массу."
            else -> "Цель соответствует вашему ИМТ. Продолжайте!"
        }
    }
}
