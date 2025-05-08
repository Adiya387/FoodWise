package com.example.healthyfoodai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.gson.Gson

class FullRecipeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_recipe)

        val recipeJson = intent.getStringExtra("recipe_json")
        val recipe = Gson().fromJson(recipeJson, Recipe::class.java)

        val titleView = findViewById<TextView>(R.id.recipeTitle)
        val caloriesView = findViewById<TextView>(R.id.recipeCalories)

        val ingredientsLayout = findViewById<LinearLayout>(R.id.recipeIngredients)
        val stepsLayout = findViewById<LinearLayout>(R.id.recipeSteps)
        val unhealthyLayout = findViewById<LinearLayout>(R.id.recipeUnhealthy)

        val proteinBar = findViewById<View>(R.id.proteinBar)
        val fatBar = findViewById<View>(R.id.fatBar)
        val carbBar = findViewById<View>(R.id.carbBar)

        val proteinText = findViewById<TextView>(R.id.proteinText)
        val fatText = findViewById<TextView>(R.id.fatText)
        val carbText = findViewById<TextView>(R.id.carbText)

        // Название
        titleView.text = recipe.title

        // Калории
        caloriesView.text = "🔥 ${recipe.calories} ккал"

        val proteinCal = recipe.proteins * 4
        val fatCal = recipe.fats * 9
        val carbCal = recipe.carbs * 4
        val total = proteinCal + fatCal + carbCal

        val pPercent = if (total > 0) (proteinCal * 100f / total).toInt() else 0
        val fPercent = if (total > 0) (fatCal * 100f / total).toInt() else 0
        val cPercent = 100 - pPercent - fPercent

        // Обновление полосок
        (proteinBar.layoutParams as LinearLayout.LayoutParams).weight = pPercent.toFloat()
        (fatBar.layoutParams as LinearLayout.LayoutParams).weight = fPercent.toFloat()
        (carbBar.layoutParams as LinearLayout.LayoutParams).weight = cPercent.toFloat()

        proteinBar.requestLayout()
        fatBar.requestLayout()
        carbBar.requestLayout()

        proteinText.text = "\uD83D\uDFE1 Белки: ${recipe.proteins} г (${pPercent}%)"
        fatText.text = "\uD83D\uDD35 Жиры: ${recipe.fats} г (${fPercent}%)"
        carbText.text = "\uD83D\uDFE3 Углеводы: ${recipe.carbs} г (${cPercent}%)"

        // Ингредиенты
        recipe.ingredients.forEach { ingredient ->
            val ingredientView = TextView(this)
            ingredientView.text = "🟢 $ingredient"
            ingredientView.textSize = 16f
            ingredientView.setTextColor(ContextCompat.getColor(this, R.color.black))
            ingredientView.setBackgroundResource(R.drawable.ingredient_item_background)
            ingredientView.setPadding(24, 16, 24, 16)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 16)
            ingredientView.layoutParams = params

            ingredientsLayout.addView(ingredientView)
        }

        // Шаги
        recipe.steps.forEachIndexed { index, step ->
            val stepView = LayoutInflater.from(this).inflate(R.layout.item_step_circle, stepsLayout, false)
            val stepNumber = stepView.findViewById<TextView>(R.id.tvStepNumber)
            val stepDescription = stepView.findViewById<TextView>(R.id.tvStepDescription)

            stepNumber.text = (index + 1).toString()
            stepDescription.text = "Шаг ${index + 1}: $step"

            stepsLayout.addView(stepView)
        }

        // Вредные компоненты
        if (recipe.unhealthyFactors.isNotEmpty()) {
            recipe.unhealthyFactors.forEach { factor ->
                val factorView = TextView(this)
                factorView.text = "⚠️ $factor"
                factorView.textSize = 16f
                factorView.setTextColor(ContextCompat.getColor(this, R.color.red))
                factorView.setBackgroundResource(R.drawable.unhealthy_background)
                factorView.setPadding(24, 16, 24, 16)

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 16)
                factorView.layoutParams = params

                unhealthyLayout.addView(factorView)
            }
        } else {
            val okView = TextView(this)
            okView.text = "✅ Нет вредных компонентов"
            okView.textSize = 16f
            okView.setTextColor(ContextCompat.getColor(this, R.color.green))
            unhealthyLayout.addView(okView)
        }
    }
}
