package com.example.healthyfoodai

import android.app.Activity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ManualMealAddActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etCalories: EditText
    private lateinit var etProteins: EditText
    private lateinit var etFats: EditText
    private lateinit var etCarbs: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_meal_add)

        etTitle = findViewById(R.id.etMealTitle)
        etCalories = findViewById(R.id.etMealCalories)
        etProteins = findViewById(R.id.etMealProteins)
        etFats = findViewById(R.id.etMealFats)
        etCarbs = findViewById(R.id.etMealCarbs)
        btnSave = findViewById(R.id.btnSaveMeal)

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val calories = etCalories.text.toString().toIntOrNull() ?: 0
            val proteins = etProteins.text.toString().toIntOrNull() ?: 0
            val fats = etFats.text.toString().toIntOrNull() ?: 0
            val carbs = etCarbs.text.toString().toIntOrNull() ?: 0

            if (title.isEmpty()) {
                Toast.makeText(this, "Введите название блюда", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val mealData = hashMapOf(
                "title" to title,
                "calories" to calories,
                "proteins" to proteins,
                "fats" to fats,
                "carbs" to carbs
            )

            val db = FirebaseFirestore.getInstance()
            val diaryRef = db.collection("users").document(userId).collection("diary").document(date)
            val mealsRef = diaryRef.collection("meals")

            // Добавляем еду в подколлекцию meals
            mealsRef.add(mealData).addOnSuccessListener {
                // Обновляем общие значения калорий и БЖУ
                diaryRef.get().addOnSuccessListener { snapshot ->
                    val currentCalories = snapshot.getLong("calories") ?: 0
                    val currentProteins = snapshot.getLong("proteins") ?: 0
                    val currentFats = snapshot.getLong("fats") ?: 0
                    val currentCarbs = snapshot.getLong("carbs") ?: 0

                    val updated = mapOf(
                        "calories" to currentCalories + calories,
                        "proteins" to currentProteins + proteins,
                        "fats" to currentFats + fats,
                        "carbs" to currentCarbs + carbs
                    )

                    diaryRef.set(updated, com.google.firebase.firestore.SetOptions.merge())
                        .addOnSuccessListener {
                            setResult(RESULT_OK) // ✅ ВАЖНО: чтобы HomeFragment понял, что нужно обновиться
                            finish()
                        }
                }
            }
        }

    }

    private fun saveMealToFirestore(title: String, calories: Int, proteins: Int, fats: Int, carbs: Int) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val meal = hashMapOf(
            "title" to title,
            "calories" to calories,
            "proteins" to proteins,
            "fats" to fats,
            "carbs" to carbs
        )

        val mealRef = db.collection("users").document(uid)
            .collection("diary").document(date)
            .collection("meals")

        val summaryRef = db.collection("users").document(uid)
            .collection("diary").document(date)

        mealRef.add(meal).addOnSuccessListener {
            summaryRef.get().addOnSuccessListener { snapshot ->
                val oldCalories = snapshot.getLong("calories") ?: 0
                val oldProteins = snapshot.getLong("proteins") ?: 0
                val oldFats = snapshot.getLong("fats") ?: 0
                val oldCarbs = snapshot.getLong("carbs") ?: 0

                summaryRef.update(
                    mapOf(
                        "calories" to (oldCalories + calories),
                        "proteins" to (oldProteins + proteins),
                        "fats" to (oldFats + fats),
                        "carbs" to (oldCarbs + carbs)
                    )
                )
            }

            Toast.makeText(this, "Блюдо добавлено", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Ошибка при добавлении", Toast.LENGTH_SHORT).show()
        }
    }
}
