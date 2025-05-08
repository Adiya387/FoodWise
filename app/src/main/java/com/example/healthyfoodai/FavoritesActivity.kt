package com.example.healthyfoodai

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthyfoodai.RecipesAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoritesActivity : AppCompatActivity() {

    private lateinit var rvFavorites: RecyclerView
    private lateinit var adapter: RecipesAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val favoritesList = mutableListOf<Recipe>()

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        loadFavorites()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        rvFavorites = findViewById(R.id.rvFavorites)

        adapter = RecipesAdapter(
            onAddToDiary = { addToDiary(it) },
            onAddToFavorites = {},
            onRemoveFromFavorites = { removeFromFavorites(it) },
            onItemClick = { openRecipeDetail(it) }
        )

        rvFavorites.layoutManager = LinearLayoutManager(this)
        rvFavorites.adapter = adapter

        loadFavorites()
    }

    private fun loadFavorites() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("users").document(uid).collection("favorites")
            .get()
            .addOnSuccessListener { snapshot ->
                favoritesList.clear()
                val likedTitles = mutableSetOf<String>()
                for (doc in snapshot.documents) {
                    val recipe = doc.toObject(Recipe::class.java)
                    if (recipe != null) {
                        favoritesList.add(recipe)
                        likedTitles.add(recipe.title)
                    }
                }
                adapter.setLikedRecipes(likedTitles)
                adapter.submitList(favoritesList.toList())
            }
    }

    private fun removeFromFavorites(recipe: Recipe) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("users").document(uid).collection("favorites")
            .document(recipe.title).delete()
            .addOnSuccessListener {
                favoritesList.remove(recipe)
                adapter.submitList(favoritesList.toList())
                showCustomToast(this, "Удалено из избранного")
            }
    }

    private fun addToDiary(recipe: Recipe) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val entry = hashMapOf(
            "title" to recipe.title,
            "calories" to recipe.calories,
            "proteins" to recipe.proteins,
            "fats" to recipe.fats,
            "carbs" to recipe.carbs,
            "timestamp" to System.currentTimeMillis()
        )
        firestore.collection("users").document(uid).collection("diary").add(entry)
        showCustomToast(this, "Добавлено в дневник")
    }

    private fun openRecipeDetail(recipe: Recipe) {
        val intent = Intent(this, RecipeDetailActivity::class.java)
        intent.putExtra("RECIPE", recipe)
        launcher.launch(intent)
    }
}
