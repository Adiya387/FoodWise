package com.example.healthyfoodai

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

class RecipesUploaderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(this, "⏳ Загружаем рецепты...", Toast.LENGTH_SHORT).show()

        try {
            val inputStream = assets.open("recipes1.json")
            val reader = InputStreamReader(inputStream)
            val gson = Gson()
            val listType = object : TypeToken<List<Recipe>>() {}.type
            val recipes: List<Recipe> = gson.fromJson(reader, listType)

            if (recipes.isEmpty()) {
                Toast.makeText(this, "❌ JSON пустой", Toast.LENGTH_LONG).show()
                finish()
                return
            }

            val firestore = FirebaseFirestore.getInstance()
            val batch = firestore.batch()

            for (recipe in recipes) {
                val docRef = firestore.collection("recipes").document(recipe.title) // 🔁 Без дубликатов!
                batch.set(docRef, recipe)
            }

            batch.commit()
                .addOnSuccessListener {
                    Toast.makeText(this, "✅ Загружено рецептов: ${recipes.size}", Toast.LENGTH_LONG).show()
                    Log.d("Uploader", "Загружено ${recipes.size} рецептов")
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "❌ Ошибка загрузки: ${it.message}", Toast.LENGTH_LONG).show()
                }

        } catch (e: Exception) {
            Toast.makeText(this, "❌ Ошибка чтения файла: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
