package com.example.healthyfoodai

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var recipe: Recipe
    private lateinit var btnFav: ImageButton
    private lateinit var btnAddToDiary: Button
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        firestore = FirebaseFirestore.getInstance()

        val receivedRecipe = intent.getSerializableExtra("RECIPE") as? Recipe
        if (receivedRecipe == null) {
            showCustomToast(this, "Ошибка загрузки рецепта")
            finish()
            return
        }
        recipe = receivedRecipe

        val img = findViewById<ImageView>(R.id.imgRecipe)
        val title = findViewById<TextView>(R.id.tvTitle)
        val description = findViewById<TextView>(R.id.tvDescription)
        val caloriesText = findViewById<TextView>(R.id.tvCalories)
        val proteinBar = findViewById<View>(R.id.proteinBar)
        val fatBar = findViewById<View>(R.id.fatBar)
        val carbBar = findViewById<View>(R.id.carbBar)
        val tvProteinPercent = findViewById<TextView>(R.id.tvProteinPercent)
        val tvFatPercent = findViewById<TextView>(R.id.tvFatPercent)
        val tvCarbPercent = findViewById<TextView>(R.id.tvCarbPercent)
        val ingredientsList = findViewById<LinearLayout>(R.id.ingredientsList)
        val stepsContainer = findViewById<LinearLayout>(R.id.stepsContainer)
        val unhealthyList = findViewById<LinearLayout>(R.id.unhealthyList)
        val infoButton = findViewById<ImageView>(R.id.btnHealthInfo)
        val bowlImage = findViewById<ImageView>(R.id.bowlImage)
        val tvIndexScore = findViewById<TextView>(R.id.tvHealthIndexScore)
        val tvProtein = findViewById<TextView>(R.id.tvProteinValue)
        val tvFat = findViewById<TextView>(R.id.tvFatValue)
        val tvCarb = findViewById<TextView>(R.id.tvCarbValue)
        val tvEnergy = findViewById<TextView>(R.id.tvEnergyValue)

        btnAddToDiary = findViewById(R.id.btnAddToDiary)
        btnFav = findViewById(R.id.btnFavorite)

        Glide.with(this).load(recipe.imageUrl).into(img)
        title.text = recipe.title
        description.text = recipe.description
        caloriesText.text = "\uD83D\uDD25 ${recipe.calories} ккал"

        val proteinCalories = recipe.proteins * 4
        val fatCalories = recipe.fats * 9
        val carbCalories = recipe.carbs * 4
        val totalCalories = proteinCalories + fatCalories + carbCalories

        val pPercent = if (totalCalories > 0) (proteinCalories.toFloat() / totalCalories * 100).toInt() else 0
        val fPercent = if (totalCalories > 0) (fatCalories.toFloat() / totalCalories * 100).toInt() else 0
        val cPercent = 100 - pPercent - fPercent

        proteinBar.layoutParams = (proteinBar.layoutParams as LinearLayout.LayoutParams).apply { weight = pPercent.toFloat() }
        fatBar.layoutParams = (fatBar.layoutParams as LinearLayout.LayoutParams).apply { weight = fPercent.toFloat() }
        carbBar.layoutParams = (carbBar.layoutParams as LinearLayout.LayoutParams).apply { weight = cPercent.toFloat() }

        tvProteinPercent.text = "\uD83D\uDFE1 Белки: ${recipe.proteins} г (${pPercent}%)"
        tvFatPercent.text = "\uD83D\uDD35 Жиры: ${recipe.fats} г (${fPercent}%)"
        tvCarbPercent.text = "\uD83D\uDFE3 Углеводы: ${recipe.carbs} г (${cPercent}%)"

        // Заголовок "Ингредиенты"
        val ingredientsHeader = TextView(this)
        ingredientsHeader.text = "🍅 Ингредиенты"
        ingredientsHeader.textSize = 18f
        ingredientsHeader.setTextColor(ContextCompat.getColor(this, R.color.black))
        ingredientsHeader.setPadding(0, 24, 0, 16)
        ingredientsList.addView(ingredientsHeader)

        recipe.ingredients.forEach { ingredient ->
            val itemView = TextView(this)
            itemView.text = "\uD83D\uDFE2 $ingredient"
            itemView.textSize = 16f
            itemView.setPadding(24, 16, 24, 16)
            itemView.setBackgroundResource(R.drawable.ingredient_item_background)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 16)
            itemView.layoutParams = params
            ingredientsList.addView(itemView)
        }

        // Заголовок "Шаги приготовления"
        val stepsHeader = TextView(this)
        stepsHeader.text = "🍳 Шаги приготовления"
        stepsHeader.textSize = 18f
        stepsHeader.setTextColor(ContextCompat.getColor(this, R.color.black))
        stepsHeader.setPadding(0, 24, 0, 16)
        stepsContainer.addView(stepsHeader)

        recipe.steps.forEachIndexed { index, step ->
            val view = layoutInflater.inflate(R.layout.item_step_circle, stepsContainer, false)
            view.findViewById<TextView>(R.id.tvStepNumber).text = (index + 1).toString()
            view.findViewById<TextView>(R.id.tvStepDescription).text = step
            stepsContainer.addView(view)
        }

        // Заголовок "Вредные пищевые факторы"
        val harmfulHeader = TextView(this)
        harmfulHeader.text = "⚠️ Вредные пищевые факторы"
        harmfulHeader.textSize = 18f
        harmfulHeader.setTextColor(ContextCompat.getColor(this, R.color.black))
        harmfulHeader.setPadding(0, 24, 0, 16)
        unhealthyList.addView(harmfulHeader)

        recipe.unhealthyFactors.forEach { factor ->
            val textView = TextView(this)
            textView.text = "\u26A0\uFE0F $factor"
            textView.textSize = 16f
            textView.setTextColor(resources.getColor(R.color.red))
            textView.setPadding(24, 16, 24, 16)
            textView.setBackgroundResource(R.drawable.unhealthy_background)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 16)
            textView.layoutParams = params
            unhealthyList.addView(textView)
        }

        // Индекс здоровья
        val score = calculateHealthIndex(recipe.proteins, recipe.fats, recipe.carbs, recipe.calories, recipe.unhealthyFactors)
        displayHealthIndex(score)

        infoButton.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_health_index_info, null)
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Понятно", null)
                .create()
            dialog.show()
        }

        checkFavoriteState()

        btnAddToDiary.setOnClickListener {
            animateClick(btnAddToDiary)
            addToDiary()
            showCustomToast(this, "Добавлено в дневник")
        }

        btnFav.setOnClickListener {
            toggleFavorite()
        }
    }

    private fun calculateHealthIndex(
        proteins: Int,
        fats: Int,
        carbs: Int,
        calories: Int,
        unhealthyFactors: List<String>
    ): Float {
        var score = 5.0

        // ❗️Снижать за вредные факторы сильнее
        score -= unhealthyFactors.size * 0.5

        // ❗️Снижать за калорийность — строгая проверка
        if (calories > 500) score -= 0.7
        if (calories < 100) score -= 0.5

        // ✅ Повышать, если БЖУ сбалансированы
        if (proteins in 10..30) score += 0.2
        if (fats in 5..20) score += 0.1
        if (carbs in 10..40) score += 0.1

        // ❗️Снижать, если белков меньше 5 или жиров больше 30
        if (proteins < 5) score -= 0.3
        if (fats > 30) score -= 0.3

        return score.coerceIn(0.5, 5.0).toFloat()
    }


    private fun displayHealthIndex(score: Float) {
        val scoreText = findViewById<TextView>(R.id.tvHealthIndexScore)
        val bowlImage = findViewById<ImageView>(R.id.bowlImage)  // ImageView для миски

        // Отображаем индекс с двумя знаками после запятой
        scoreText.text = String.format("%.2f", score)

        // Устанавливаем картинку миски в зависимости от диапазона
        when {
            score <= 2.5f -> bowlImage.setImageResource(R.drawable.m1) // Красная
            score <= 4.4f -> bowlImage.setImageResource(R.drawable.m2) // Жёлтая
            else -> bowlImage.setImageResource(R.drawable.m3)         // Зелёная
        }

        // Обновление нутриентов
        findViewById<TextView>(R.id.tvProteinValue).text = "Белки: ${recipe.proteins} г"
        findViewById<TextView>(R.id.tvFatValue).text = "Жиры: ${recipe.fats} г"
        findViewById<TextView>(R.id.tvCarbValue).text = "Углеводы: ${recipe.carbs} г"
        findViewById<TextView>(R.id.tvEnergyValue).text = "Калории: ${recipe.calories} ккал"
    }




    private fun checkFavoriteState() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("users").document(uid).collection("favorites")
            .document(recipe.title).get()
            .addOnSuccessListener {
                isFavorite = it.exists()
                updateFavoriteIcon()
            }
    }

    private fun toggleFavorite() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val favRef = firestore.collection("users").document(uid).collection("favorites").document(recipe.title)
        if (isFavorite) {
            favRef.delete()
            isFavorite = false
            showCustomToast(this, "Удалено из избранного")
        } else {
            favRef.set(recipe)
            isFavorite = true
            showCustomToast(this, "Добавлено в избранное")
        }
        updateFavoriteIcon()
    }

    private fun addToDiary() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val entry = hashMapOf(
            "title" to recipe.title,
            "calories" to recipe.calories,
            "proteins" to recipe.proteins,
            "fats" to recipe.fats,
            "carbs" to recipe.carbs,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("users").document(uid)
            .collection("diary").document(date)
            .collection("meals")
            .add(entry)
            .addOnSuccessListener {
                recalculateDailyNutrition(uid, date)
                setResult(RESULT_OK) // 🔥 это добавь
            }
    }



    private fun recalculateDailyNutrition(uid: String, date: String) {
        val diaryRef = firestore.collection("users").document(uid)
            .collection("diary").document(date)

        diaryRef.collection("meals").get()
            .addOnSuccessListener { snapshot ->
                var totalCalories = 0
                var totalProteins = 0
                var totalFats = 0
                var totalCarbs = 0

                for (doc in snapshot.documents) {
                    totalCalories += doc.getLong("calories")?.toInt() ?: 0
                    totalProteins += doc.getLong("proteins")?.toInt() ?: 0
                    totalFats += doc.getLong("fats")?.toInt() ?: 0
                    totalCarbs += doc.getLong("carbs")?.toInt() ?: 0
                }

                val totals = hashMapOf(
                    "calories" to totalCalories,
                    "proteins" to totalProteins,
                    "fats" to totalFats,
                    "carbs" to totalCarbs
                )

                diaryRef.set(totals, com.google.firebase.firestore.SetOptions.merge())
            }
    }


    private fun updateFavoriteIcon() {
        btnFav.setImageResource(
            if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
        )
    }

    private fun animateClick(view: View) {
        view.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction {
            view.animate().scaleX(1f).scaleY(1f).duration = 100
        }
    }
}
