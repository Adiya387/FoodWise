package com.example.healthyfoodai

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthyfoodai.RecipesAdapter
import com.example.healthyfoodai.databinding.FragmentMealSectionsBinding
import com.example.healthyfoodai.JsonUtils
import com.example.healthyfoodai.showCustomToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MealsFragment : Fragment() {

    private var _binding: FragmentMealSectionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var allRecipes: List<Recipe>
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMealSectionsBinding.inflate(inflater, container, false)

        setupRecyclerViews()
        loadRecipes()

        return binding.root
    }

    private fun setupRecyclerViews() {
        binding.rvBreakfast.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvLunch.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvDinner.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvSnack.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun loadRecipes() {
        allRecipes = JsonUtils.loadRecipesFromAssets(requireContext())

        val breakfasts = allRecipes.filter { it.category.equals("Завтраки", ignoreCase = true) }
        val lunches = allRecipes.filter { it.category.equals("Обед", ignoreCase = true) }
        val dinners = allRecipes.filter { it.category.equals("Ужин", ignoreCase = true) }
        val snacks = allRecipes.filter { it.category.equals("Перекус", ignoreCase = true) }

        binding.rvBreakfast.adapter = createAdapter(breakfasts)
        binding.rvLunch.adapter = createAdapter(lunches)
        binding.rvDinner.adapter = createAdapter(dinners)
        binding.rvSnack.adapter = createAdapter(snacks)
    }

    private fun createAdapter(recipes: List<Recipe>): RecipesAdapter {
        return RecipesAdapter(
            onAddToDiary = { addToDiary(it) },
            onAddToFavorites = { addToFavorites(it) },
            onRemoveFromFavorites = { removeFromFavorites(it) },
            onItemClick = { openRecipeDetail(it) }
        ).apply {
            submitList(recipes)
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
        showCustomToast(requireContext(), "Добавлено в дневник")
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

    private fun openRecipeDetail(recipe: Recipe) {
        val intent = Intent(requireContext(), RecipeDetailActivity::class.java)
        intent.putExtra("RECIPE", recipe)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
