package com.example.healthyfoodai

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.healthyfoodai.R
import com.example.healthyfoodai.Recipe
import com.example.healthyfoodai.RecipeListActivity

class MealCategoryAdapter(
    private val context: Context,
    private val meals: List<Recipe>
) : RecyclerView.Adapter<MealCategoryAdapter.MealViewHolder>() {

    inner class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val img = itemView.findViewById<ImageView>(R.id.imgCategory)
        private val name = itemView.findViewById<TextView>(R.id.tvCategoryName)

        fun bind(meal: Recipe) {
            name.text = meal.title
            Glide.with(context).load(meal.imageUrl).into(img)

            itemView.setOnClickListener {
                val intent = Intent(context, RecipeListActivity::class.java)
                intent.putExtra("CATEGORY", meal.title)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_meal_category, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        holder.bind(meals[position])
    }

    override fun getItemCount(): Int = meals.size
}
