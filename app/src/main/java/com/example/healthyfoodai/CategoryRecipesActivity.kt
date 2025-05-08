package com.example.healthyfoodai

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthyfoodai.CategoryAdapter
import com.example.healthyfoodai.Category

class CategoryRecipesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_recipes)

        recyclerView = findViewById(R.id.rvCategories)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        val categories = listOf(
            Category("Салаты", "https://i.imgur.com/ikG5Gwd.jpeg"),
            Category("Супы", "https://i.imgur.com/x7eB4rQ.jpeg"),
            Category("Завтраки", "https://i.imgur.com/a3YwzWc.jpeg"),
            Category("Основные блюда", "https://i.imgur.com/ROSG8uN.jpeg"),
            Category("Выпечка и десерты", "https://i.imgur.com/dq3yVuU.jpeg"),
            Category("Закуски", "https://i.imgur.com/FYgzywh.jpeg"),
            Category("Гарниры", "https://i.imgur.com/XVgKTN6.jpeg"),
            Category("ПП", "https://i.imgur.com/VsjsvTv.jpeg"),
            Category("Ужин", "https://i.imgur.com/DAZEBhM.jpeg"),
            Category("Обед", "https://i.imgur.com/ndXPdmP.jpeg"),
            Category("Перекус", "https://i.imgur.com/fKzCoFP.jpeg")
        )

        adapter = CategoryAdapter(categories) { selectedCategory ->
            val intent = Intent(this, RecipeListActivity::class.java)
            intent.putExtra("CATEGORY", selectedCategory.name)
            startActivity(intent)
        }

        recyclerView.adapter = adapter
    }
}
