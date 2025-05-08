package com.example.healthyfoodai

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthyfoodai.RecipesAdapter
import com.example.healthyfoodai.databinding.FragmentRecipesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class RecipesFragment : Fragment() {

    private var _binding: FragmentRecipesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RecipesAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var allRecipes: List<Recipe>
    private lateinit var mealViewModel: MealViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipesBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        mealViewModel = ViewModelProvider(requireActivity())[MealViewModel::class.java]

        adapter = RecipesAdapter(
            onAddToDiary = { addToDiary(it) },
            onAddToFavorites = { addToFavorites(it) },
            onRemoveFromFavorites = { removeFromFavorites(it) },
            onItemClick = {
                val intent = Intent(requireContext(), RecipeDetailActivity::class.java)
                intent.putExtra("RECIPE", it)
                startActivity(intent)
            }
        )

        binding.rvRecipes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecipes.adapter = adapter

        loadAllRecipes()

        binding.etSearch.addTextChangedListener(object : TextWatcher {
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

        return binding.root
    }

    private fun loadAllRecipes() {
        firestore.collection("recipes")
            .get()
            .addOnSuccessListener { snapshot ->
                allRecipes = snapshot.documents.mapNotNull { it.toObject(Recipe::class.java) }
                adapter.submitList(allRecipes)
                loadFavoritesAndSetLiked()
            }
            .addOnFailureListener {
                showCustomToast(requireContext(), "Ошибка загрузки рецептов")
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
                showCustomToast(requireContext(), "Ошибка загрузки избранного")
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
                mealViewModel.notifyMealAdded()
                showCustomToast(requireContext(), "Добавлено в дневник")
            }
            .addOnFailureListener {
                showCustomToast(requireContext(), "Ошибка при добавлении")
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
        showCustomToast(requireContext(), "Добавлено в избранное")
    }

    private fun removeFromFavorites(recipe: Recipe) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("users").document(uid)
            .collection("favorites").document(recipe.title).delete()
        showCustomToast(requireContext(), "Удалено из избранного")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
