package com.example.healthyfoodai

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthyfoodai.RecipesAdapter
import com.example.healthyfoodai.showCustomToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class RecipeListActivity : AppCompatActivity() {

    private lateinit var tvCategoryName: TextView
    private lateinit var etSearch: EditText
    private lateinit var rvRecipes: RecyclerView
    private lateinit var adapter: RecipesAdapter
    private lateinit var allRecipes: List<Recipe>
    private val firestore = FirebaseFirestore.getInstance()

    // ✅ ActivityResultLauncher для возврата из RecipeDetail
    private val recipeDetailLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            loadFavoritesAndSetLiked() // ⬅ перезагрузить состояние лайков
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_list)

        val category = intent.getStringExtra("CATEGORY") ?: "Категория"

        tvCategoryName = findViewById(R.id.tvCategoryName)
        etSearch = findViewById(R.id.etSearch)
        rvRecipes = findViewById(R.id.rvCategoryRecipes)

        tvCategoryName.text = category

        adapter = RecipesAdapter(
            onAddToDiary = { addToDiary(it) },
            onAddToFavorites = { addToFavorites(it) },
            onRemoveFromFavorites = { removeFromFavorites(it) },
            onItemClick = { openRecipeDetail(it) }
        )

        rvRecipes.layoutManager = LinearLayoutManager(this)
        rvRecipes.adapter = adapter

        loadRecipesByCategory(category)

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim().lowercase()
                val filtered = allRecipes.filter {
                    it.title.lowercase().contains(query)
                }
                adapter.submitList(filtered)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadRecipesByCategory(category: String) {
        firestore.collection("recipes")
            .whereEqualTo("category", category)
            .get()
            .addOnSuccessListener { result ->
                allRecipes = result.documents.mapNotNull { it.toObject(Recipe::class.java) }
                adapter.submitList(allRecipes)
                loadFavoritesAndSetLiked()
            }
            .addOnFailureListener {
                showCustomToast(this, "Ошибка загрузки рецептов")
            }
    }

    private fun loadFavoritesAndSetLiked() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("users").document(uid).collection("favorites")
            .get()
            .addOnSuccessListener { snapshot ->
                val likedTitles = snapshot.documents.map { it.id }.toSet()
                adapter.setLikedRecipes(likedTitles)
            }
            .addOnFailureListener {
                showCustomToast(this, "Ошибка загрузки избранного")
            }
    }

    private fun addToDiary(recipe: Recipe) {
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
                showCustomToast(this, "Добавлено в дневник")
            }
            .addOnFailureListener {
                showCustomToast(this, "Ошибка при добавлении")
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

    private fun addToFavorites(recipe: Recipe) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("users").document(uid)
            .collection("favorites").document(recipe.title).set(recipe)
        showCustomToast(this, "Добавлено в избранное")
    }

    private fun removeFromFavorites(recipe: Recipe) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("users").document(uid)
            .collection("favorites").document(recipe.title).delete()
        showCustomToast(this, "Удалено из избранного")
    }

    private fun openRecipeDetail(recipe: Recipe) {
        val intent = Intent(this, RecipeDetailActivity::class.java)
        intent.putExtra("RECIPE", recipe)
        recipeDetailLauncher.launch(intent) // ✅ запуск с отслеживанием возврата
    }
}
