package com.example.healthyfoodai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AllergyActivity : AppCompatActivity() {

    private lateinit var btnNext: Button
    private lateinit var btnOtherAllergy: Button
    private lateinit var btnNoAllergy: Button

    private val selectedAllergies = mutableListOf<String>()
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private var isNoAllergySelected = false
    private var isOtherAllergySelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_allergy)

        btnOtherAllergy = findViewById(R.id.btnOtherAllergy)
        btnNoAllergy = findViewById(R.id.btnNoAllergy)
        btnNext = findViewById(R.id.btnNextAllergy)

        val buttons = listOf(
            R.id.btnMilk to "Молоко и молочные продукты",
            R.id.btnEggs to "Яйца",
            R.id.btnNuts to "Орехи",
            R.id.btnGluten to "Глютен",
            R.id.btnAlcohol to "Алкоголь",
            R.id.btnFish to "Рыбные продукты",
            R.id.btnShellfish to "Моллюски, креветки",
            R.id.btnSoy to "Соя",
            R.id.btnPeanuts to "Арахис",
            R.id.btnSugar to "Сахар"
        )

        btnNext.isEnabled = false

        buttons.forEach { (id, _) ->
            setButtonDefaultColor(findViewById(id))
        }
        setButtonDefaultColor(btnOtherAllergy)
        setButtonDefaultColor(btnNoAllergy)

        buttons.forEach { (id, allergyName) ->
            val button = findViewById<Button>(id)
            button.setOnClickListener {
                toggleSelection(button, allergyName)
                resetSpecialButtons()
            }
        }

        btnOtherAllergy.setOnClickListener {
            isOtherAllergySelected = true
            isNoAllergySelected = false
            highlightButtonSelected(btnOtherAllergy)
            setButtonDefaultColor(btnNoAllergy)

            val fragment = CustomAllergyFragment { customAllergy ->
                selectedAllergies.add(customAllergy)
                Toast.makeText(this, "Добавлено: $customAllergy", Toast.LENGTH_SHORT).show()
                btnNext.isEnabled = true
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        btnNoAllergy.setOnClickListener {
            selectedAllergies.clear()
            selectedAllergies.add("Нет ограничений")
            isNoAllergySelected = true
            isOtherAllergySelected = false
            highlightButtonSelected(btnNoAllergy)
            setButtonDefaultColor(btnOtherAllergy)
            btnNext.isEnabled = true
        }

        btnNext.setOnClickListener {
            if (selectedAllergies.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, выберите ограничение или укажите вручную", Toast.LENGTH_SHORT).show()
            } else {
                saveAllergies(selectedAllergies)
            }
        }
    }

    private fun toggleSelection(button: Button, allergy: String) {
        if (selectedAllergies.contains(allergy)) {
            selectedAllergies.remove(allergy)
            setButtonDefaultColor(button)
        } else {
            selectedAllergies.add(allergy)
            highlightButtonSelected(button)
        }
        btnNext.isEnabled = selectedAllergies.isNotEmpty()
    }

    private fun resetSpecialButtons() {
        isNoAllergySelected = false
        isOtherAllergySelected = false
        setButtonDefaultColor(btnOtherAllergy)
        setButtonDefaultColor(btnNoAllergy)
    }

    private fun highlightButtonSelected(button: Button) {
        button.setBackgroundTintList(getColorStateList(R.color.teal_700))
        button.setTextColor(getColor(android.R.color.white))
    }

    private fun setButtonDefaultColor(button: Button) {
        button.setBackgroundTintList(getColorStateList(R.color.gray))
        button.setTextColor(getColor(android.R.color.black))
    }

    private fun saveAllergies(allergies: List<String>) {
        val userId = auth.currentUser?.uid ?: return

        val prefs = applicationContext.getSharedPreferences("user_profile", Context.MODE_PRIVATE)

        val userProfile = hashMapOf(
            "name" to (prefs.getString("name", "") ?: ""),
            "gender" to (prefs.getString("gender", "") ?: ""),
            "age" to (prefs.getString("age", "")?.toIntOrNull() ?: 0),
            "height" to (prefs.getString("height", "") ?: ""),
            "weight" to (prefs.getString("weight", "") ?: ""),
            "goal" to (prefs.getString("goal", "") ?: ""),
            "activity" to (prefs.getString("activity", "") ?: ""),
            "allergies" to allergies
        )

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                val existingAllergies = (doc.get("allergies") as? List<*>)?.filterIsInstance<String>() ?: emptyList()

                if (existingAllergies.toSet() == allergies.toSet()) {
                    Toast.makeText(this, "Изменений не обнаружено", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ProfileCompleteActivity::class.java))
                    finish()
                    return@addOnSuccessListener
                }

                // Если аллергии изменились — обновляем профиль и ставим флаг
                firestore.collection("users").document(userId)
                    .set(userProfile)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Профиль успешно сохранён", Toast.LENGTH_SHORT).show()

                        // 💾 Ставим флаг для HomeFragment
//                        prefs.edit().putBoolean("show_ai_plan_update_dialog", true).apply()

                        startActivity(Intent(this, ProfileCompleteActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Ошибка сохранения профиля: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Ошибка при получении текущего профиля", Toast.LENGTH_SHORT).show()
            }
    }
}