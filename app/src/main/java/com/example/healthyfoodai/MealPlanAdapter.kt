package com.example.healthyfoodai

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

class MealPlanAdapter(
    private val viewModel: DiaryViewModel
) : ListAdapter<MealPlan, MealPlanAdapter.MealViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_meal_plan, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mealType: TextView = itemView.findViewById(R.id.mealType)
        private val mealTitle: TextView = itemView.findViewById(R.id.mealTitle)
        private val mealCalories: TextView = itemView.findViewById(R.id.mealCalories)
        private val btnChange: Button = itemView.findViewById(R.id.btnChange)
        private val btnViewRecipe: Button = itemView.findViewById(R.id.btnViewRecipe)
        private val btnAddToDiary: Button = itemView.findViewById(R.id.btnAddToDiary)

        fun bind(meal: MealPlan) {
            mealType.text = meal.type
            mealTitle.text = if (meal.recipe.title.isNotBlank()) meal.recipe.title else "Без названия"
            mealCalories.text = "Калории: ${meal.recipe.calories}"

            btnViewRecipe.setOnClickListener {
                val context = it.context
                val intent = Intent(context, FullRecipeActivity::class.java)
                val json = Gson().toJson(meal.recipe)
                intent.putExtra("recipe_json", json)
                context.startActivity(intent)
            }

            btnAddToDiary.setOnClickListener {
                val context = it.context
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
                val date = getTodayDate()
                val firestore = FirebaseFirestore.getInstance()

                val entry = mapOf(
                    "title" to meal.recipe.title,
                    "calories" to meal.recipe.calories,
                    "proteins" to meal.recipe.proteins,
                    "fats" to meal.recipe.fats,
                    "carbs" to meal.recipe.carbs,
                    "timestamp" to System.currentTimeMillis()
                )

                val diaryRef = firestore.collection("users").document(uid)
                    .collection("diary").document(date)

                val mealRef = diaryRef.collection("meals").document()

                diaryRef.get().addOnSuccessListener { snapshot ->
                    val currentCalories = snapshot.getLong("calories") ?: 0
                    val currentProteins = snapshot.getLong("proteins") ?: 0
                    val currentFats = snapshot.getLong("fats") ?: 0
                    val currentCarbs = snapshot.getLong("carbs") ?: 0

                    val updated = mapOf(
                        "calories" to (currentCalories + meal.recipe.calories),
                        "proteins" to (currentProteins + meal.recipe.proteins),
                        "fats" to (currentFats + meal.recipe.fats),
                        "carbs" to (currentCarbs + meal.recipe.carbs)
                    )

                    diaryRef.set(updated)
                        .addOnSuccessListener {
                            mealRef.set(entry).addOnSuccessListener {
                                CustomToast.showCustomToast(context, "Добавлено в дневник")
                                viewModel.notifyMealAdded()
                            }
                        }
                }
            }

            btnChange.setOnClickListener {
                val context = it.context
                val firestore = FirebaseFirestore.getInstance()
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
                val currentType = meal.type
                val currentTitle = meal.recipe.title

                firestore.collection("recipes")
                    .whereEqualTo("category", currentType)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val alternatives = snapshot.documents
                            .mapNotNull { it.toObject(Recipe::class.java) }
                            .filter { it.title != currentTitle }

                        if (alternatives.isNotEmpty()) {
                            val newRecipe = alternatives.random()
                            val newMeal = MealPlan(type = currentType, recipe = newRecipe)

                            // Обновление UI
                            val position = bindingAdapterPosition
                            if (position != RecyclerView.NO_POSITION) {
                                val updatedList = currentList.toMutableList()
                                updatedList[position] = newMeal
                                submitList(updatedList)
                            }

                            // Обновление в Firestore
                            val calendar = Calendar.getInstance()
                            val dayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 2
                            val days = listOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье")
                            val today = days.getOrElse(dayIndex) { "Понедельник" }

                            firestore.collection("weekly_plan").document(uid).get()
                                .addOnSuccessListener { doc ->
                                    val dayData = doc.get(today) as? List<Map<String, Any>> ?: return@addOnSuccessListener
                                    val updatedDay = dayData.toMutableList()

                                    val indexToReplace = updatedDay.indexOfFirst {
                                        val type = it["type"] as? String
                                        val name = it["name"] as? String
                                        type == currentType && name == currentTitle
                                    }

                                    if (indexToReplace != -1) {
                                        val newEntry = mapOf(
                                            "type" to currentType,
                                            "name" to newRecipe.title,
                                            "description" to newRecipe.description,
                                            "category" to newRecipe.category,
                                            "imageUrl" to newRecipe.imageUrl,
                                            "calories" to newRecipe.calories,
                                            "proteins" to newRecipe.proteins,
                                            "fats" to newRecipe.fats,
                                            "carbs" to newRecipe.carbs,
                                            "ingredients" to newRecipe.ingredients,
                                            "steps" to newRecipe.steps,
                                            "unhealthyFactors" to newRecipe.unhealthyFactors
                                        )
                                        updatedDay[indexToReplace] = newEntry

                                        firestore.collection("weekly_plan").document(uid)
                                            .update(today, updatedDay)
                                            .addOnSuccessListener {
                                                CustomToast.showCustomToast(context, "✅ Рецепт заменён")
                                            }
                                            .addOnFailureListener {
                                                CustomToast.showCustomToast(context, "Ошибка при сохранении рецепта")
                                            }
                                    }
                                }
                        } else {
                            CustomToast.showCustomToast(context, "Нет альтернатив для $currentType")
                        }
                    }
                    .addOnFailureListener {
                        CustomToast.showCustomToast(context, "Ошибка загрузки рецептов")
                    }
            }
        }

        private fun getTodayDate(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MealPlan>() {
        override fun areItemsTheSame(oldItem: MealPlan, newItem: MealPlan): Boolean {
            return oldItem.recipe.title == newItem.recipe.title && oldItem.type == newItem.type
        }

        override fun areContentsTheSame(oldItem: MealPlan, newItem: MealPlan): Boolean {
            return oldItem == newItem
        }
    }
}
