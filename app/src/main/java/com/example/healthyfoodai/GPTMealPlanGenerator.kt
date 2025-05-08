package com.example.healthyfoodai

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.healthyfoodai.api.RetrofitMealPlanInstance
import com.example.healthyfoodai.utils.JsonFixer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// 🔹 Модели
data class GPTMessage(val role: String, val content: String)
data class GPTRequest(val model: String, val messages: List<GPTMessage>)
data class GPTChoice(val message: GPTMessage)
data class GPTResponse(val choices: List<GPTChoice>)

fun generateWeeklyPlanFromGPT(context: Context) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val firestore = FirebaseFirestore.getInstance()

    firestore.collection("weekly_plan").document(userId).get()
        .addOnSuccessListener { existing ->
            val createdAt = existing.get("createdAt") as? Long
            val sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000
            val now = System.currentTimeMillis()

            if (createdAt != null && now - createdAt < sevenDaysInMillis) {
                Toast.makeText(context, "План уже создан", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { doc ->
                    if (doc == null || !doc.exists()) {
                        Toast.makeText(context, "Профиль не найден", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val gender = doc.getString("gender") ?: "мужчина"
                    val age = (doc.get("age") as? Long)?.toInt() ?: 25
                    val height = doc.getString("height")?.toIntOrNull() ?: 170
                    val weight = doc.getString("weight")?.toIntOrNull() ?: 70
                    val goal = doc.getString("goal") ?: "поддержание"
                    val allergyRaw = doc.get("allergy")
                    val allergyList = when (allergyRaw) {
                        is String -> listOf(allergyRaw)
                        is List<*> -> allergyRaw.filterIsInstance<String>()
                        else -> emptyList()
                    }

                    // Расширение продуктовых групп
                    val productGroups = mapOf(
                        "молочные продукты" to listOf("молоко", "сыр", "творог", "йогурт", "кефир", "сливки", "сметана", "масло"),
                        "овощи" to listOf("капуста", "морковь", "помидор", "огурец", "лук", "чеснок", "баклажан", "перец"),
                        "фрукты" to listOf("яблоко", "банан", "апельсин", "груша", "виноград", "киви", "ананас"),
                        "мясо" to listOf("курица", "говядина", "свинина", "баранина", "индейка", "печень", "колбаса"),
                        "рыба" to listOf("лосось", "тунец", "треска", "сёмга", "хек", "креветки", "мидии"),
                        "мука" to listOf("пшеничная мука", "ржаная мука", "овсяная мука", "хлеб", "булочки", "лепёшки", "блины", "лаваш", "пицца", "макароны", "панировка")
                    )

                    val forbiddenExpanded = mutableSetOf<String>()
                    for (allergy in allergyList) {
                        val key = allergy.lowercase()
                        if (productGroups.containsKey(key)) {
                            forbiddenExpanded.addAll(productGroups[key]!!)
                        } else {
                            forbiddenExpanded.add(key)
                        }
                    }

                    val forbiddenText = if (forbiddenExpanded.isNotEmpty())
                        "Не используй следующие продукты (аллергии и ограничения): ${forbiddenExpanded.joinToString(", ")}"
                    else
                        "Нет ограничений по продуктам"

                    val prompt = """
                        Составь недельный план питания для пользователя:
                        Пол: $gender
                        Возраст: $age
                        Рост: $height см
                        Вес: $weight кг
                        Цель: $goal
                        $forbiddenText

                        На каждый день недели (Понедельник – Воскресенье) добавь 4 приёма пищи: Завтрак, Обед, Ужин, Перекус.

                        Каждый рецепт должен быть объектом с такими полями:
                        - name (название)
                        - calories (в ккал)
                        - proteins, fats, carbs (в граммах)
                        - ingredients (массив строк)
                        - steps (массив шагов приготовления)
                        - unhealthyFactors (вредные компоненты, если есть: "сахар", "жареное", "соль")

                        Формат ответа строго в виде JSON:
                        {
                          "Понедельник": [ { ... }, { ... }, ... ],
                          ...
                        }
                    """.trimIndent()

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val messages = listOf(
                                GPTMessage("system", "Ты — профессиональный диетолог, отвечай только JSON."),
                                GPTMessage("user", prompt)
                            )

                            val request = GPTRequest("gpt-3.5-turbo", messages)
                            val response = RetrofitMealPlanInstance.api.generatePlan(request)

                            val rawJson = response.choices.firstOrNull()?.message?.content ?: ""
                            Log.d("GPT_PLAN", "📩 GPT ответ: $rawJson")

                            val fixedJson = JsonFixer.convertDaysToArrays(rawJson)
                            Log.d("GPT_PLAN", "🛠 Исправленный JSON: $fixedJson")

                            try {
                                val planMap: Map<String, List<Map<String, Any>>> =
                                    Gson().fromJson(fixedJson, object : TypeToken<Map<String, List<Map<String, Any>>>>() {}.type)

                                if (planMap.size < 7) {
                                    Log.e("GPT_PLAN", "❌ GPT сгенерировал только ${planMap.size} дней. Повтор.")
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "GPT сгенерировал неполный план. Повторите позже.", Toast.LENGTH_SHORT).show()
                                    }
                                    return@launch
                                }

                                val dataToSave = hashMapOf<String, Any>(
                                    "createdAt" to System.currentTimeMillis()
                                )

                                for ((day, meals) in planMap) {
                                    val enrichedMeals = meals.mapIndexed { index, meal ->
                                        val mealWithType = HashMap(meal)
                                        mealWithType["type"] = when (index) {
                                            0 -> "Завтрак"
                                            1 -> "Обед"
                                            2 -> "Ужин"
                                            3 -> "Перекус"
                                            else -> "Без категории"
                                        }
                                        mealWithType
                                    }
                                    dataToSave[day] = enrichedMeals
                                }

                                firestore.collection("weekly_plan").document(userId)
                                    .set(dataToSave)
                                    .addOnSuccessListener {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            Toast.makeText(context, "AI-план успешно сохранён", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .addOnFailureListener {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            Toast.makeText(context, "Ошибка при сохранении плана", Toast.LENGTH_SHORT).show()
                                        }
                                    }

                            } catch (e: Exception) {
                                Log.e("GPT_PLAN", "❌ Ошибка парсинга: ${e.message}")
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Ошибка обработки AI-плана", Toast.LENGTH_SHORT).show()
                                }
                            }

                        } catch (e: Exception) {
                            Log.e("GPT_PLAN", "❌ Ошибка генерации: ${e.message}")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Ошибка генерации AI-плана", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
        }
}
