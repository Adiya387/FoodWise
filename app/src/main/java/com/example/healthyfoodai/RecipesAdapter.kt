package com.example.healthyfoodai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.healthyfoodai.R
import com.example.healthyfoodai.Recipe
import com.example.healthyfoodai.showCustomToast

class RecipesAdapter(
    private val onAddToDiary: (Recipe) -> Unit,
    private val onAddToFavorites: (Recipe) -> Unit,
    private val onRemoveFromFavorites: (Recipe) -> Unit,
    private val onItemClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipesAdapter.RecipeViewHolder>() {

    private val recipes = mutableListOf<Recipe>()
    private val likedRecipes = mutableSetOf<String>()

    fun submitList(newList: List<Recipe>) {
        recipes.clear()
        recipes.addAll(newList)
        notifyDataSetChanged()
    }

    fun setLikedRecipes(favoriteTitles: Set<String>) {
        likedRecipes.clear()
        likedRecipes.addAll(favoriteTitles)
        notifyDataSetChanged()
    }

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val img = itemView.findViewById<ImageView>(R.id.imgRecipe)
        private val title = itemView.findViewById<TextView>(R.id.tvTitle)
        private val calories = itemView.findViewById<TextView>(R.id.tvCalories)
        private val btnDiary = itemView.findViewById<ImageButton>(R.id.btnAddToDiary)
        private val btnFav = itemView.findViewById<ImageButton>(R.id.btnAddToFavorites)

        fun bind(recipe: Recipe) {
            title.text = recipe.title
            calories.text = "${recipe.calories} ккал"
            Glide.with(itemView.context).load(recipe.imageUrl).into(img)

            updateFavIcon(recipe)

            btnDiary.setOnClickListener {
                animateClick(btnDiary)
                onAddToDiary(recipe)
                showCustomToast(itemView.context, "Добавлено в дневник")
            }



            btnFav.setOnClickListener {
                if (likedRecipes.contains(recipe.title)) {
                    likedRecipes.remove(recipe.title)
                    updateFavIcon(recipe)
                    onRemoveFromFavorites(recipe)
                    showCustomToast(itemView.context, "Удалено из избранного")
                } else {
                    likedRecipes.add(recipe.title)
                    updateFavIcon(recipe)
                    onAddToFavorites(recipe)
                    showCustomToast(itemView.context, "Добавлено в избранное")
                }
            }

            itemView.setOnClickListener {
                onItemClick(recipe)
            }
        }

        private fun updateFavIcon(recipe: Recipe) {
            val isLiked = likedRecipes.contains(recipe.title)
            btnFav.setImageResource(
                if (isLiked) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
            )
        }
    }
    private fun animateClick(view: View) {
        view.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(100)
            .withEndAction {
                view.animate().scaleX(1f).scaleY(1f).duration = 100
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipes[position])
    }

    override fun getItemCount(): Int = recipes.size
}
