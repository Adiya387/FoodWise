package com.example.healthyfoodai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TodayMealsAdapter(private val meals: List<Meal>) :
    RecyclerView.Adapter<TodayMealsAdapter.MealViewHolder>() {

    class MealViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvMealName)
        val calories: TextView = view.findViewById(R.id.tvMealCalories)
        val bju: TextView = view.findViewById(R.id.tvMealBJU)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meal_today, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = meals[position]
        holder.name.text = meal.displayName()

        holder.calories.text = "${meal.calories} ккал"
        holder.bju.text = "Б: ${meal.proteins}г  Ж: ${meal.fats}г  У: ${meal.carbs}г"
    }

    override fun getItemCount(): Int = meals.size
}
