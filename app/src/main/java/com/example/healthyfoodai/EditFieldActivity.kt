package com.example.healthyfoodai

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditFieldActivity : AppCompatActivity() {

    private lateinit var tvFieldTitle: TextView
    private lateinit var etFieldValue: EditText
    private lateinit var spinnerFieldValue: Spinner
    private lateinit var btnSave: Button

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private var fieldName: String = ""
    private var currentValue: String = ""

    private val genderOptions = listOf("Мужской", "Женский")
    private val goalOptions = listOf("Похудение", "Набор массы", "Поддержание формы", "Улучшить здоровье")
    private val activityOptions = listOf("Низкий", "Средний", "Высокий")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_field)

        tvFieldTitle = findViewById(R.id.tvFieldTitle)
        etFieldValue = findViewById(R.id.etFieldValue)
        spinnerFieldValue = findViewById(R.id.spinnerFieldValue)
        btnSave = findViewById(R.id.btnSaveField)

        fieldName = intent.getStringExtra("field_name") ?: ""
        currentValue = intent.getStringExtra("current_value") ?: ""

        title = "Изменить $fieldName"
        tvFieldTitle.text = "Изменить $fieldName"

        setupField()

        btnSave.setOnClickListener {
            confirmSave()
        }
    }

    private fun setupField() {
        when (fieldName) {
            "Пол" -> {
                etFieldValue.visibility = View.GONE
                spinnerFieldValue.visibility = View.VISIBLE
                spinnerFieldValue.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genderOptions)
                spinnerFieldValue.setSelection(genderOptions.indexOf(currentValue).coerceAtLeast(0))
            }
            "Цель" -> {
                etFieldValue.visibility = View.GONE
                spinnerFieldValue.visibility = View.VISIBLE
                spinnerFieldValue.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, goalOptions)
                spinnerFieldValue.setSelection(goalOptions.indexOf(currentValue).coerceAtLeast(0))
            }
            "Активность" -> {
                etFieldValue.visibility = View.GONE
                spinnerFieldValue.visibility = View.VISIBLE
                spinnerFieldValue.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, activityOptions)
                spinnerFieldValue.setSelection(activityOptions.indexOf(currentValue).coerceAtLeast(0))
            }
            else -> {
                etFieldValue.visibility = View.VISIBLE
                spinnerFieldValue.visibility = View.GONE
                etFieldValue.setText(currentValue)
            }
        }
    }

    private fun confirmSave() {
        val newValue = when (fieldName) {
            "Пол", "Цель", "Активность" -> spinnerFieldValue.selectedItem.toString()
            else -> etFieldValue.text.toString().trim()
        }

        if (newValue.isEmpty()) {
            Toast.makeText(this, "Поле не может быть пустым", Toast.LENGTH_SHORT).show()
            return
        }

        if (newValue == currentValue) {
            Toast.makeText(this, "Изменений не обнаружено", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        saveField(newValue)
    }

    private fun saveField(newValue: String) {
        val userId = auth.currentUser?.uid ?: return

        val keyInFirestore = when (fieldName) {
            "Имя" -> "name"
            "Пол" -> "gender"
            "Возраст" -> "age"
            "Рост" -> "height"
            "Вес" -> "weight"
            "Цель" -> "goal"
            "Активность" -> "activity"
            "Ограничения" -> "allergies"
            else -> ""
        }

        if (keyInFirestore.isEmpty()) {
            Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_SHORT).show()
            return
        }

        val valueForFirestore: Any = if (fieldName == "Ограничения") {
            newValue.split(",").map { it.trim() }
        } else {
            newValue
        }

        firestore.collection("users").document(userId)
            .update(keyInFirestore, valueForFirestore)
            .addOnSuccessListener {
                saveLocally(keyInFirestore, newValue)
                Toast.makeText(this, "Данные обновлены", Toast.LENGTH_SHORT).show()

//                // 💡 Если поле влияет на план — ставим флаг
//                if (keyInFirestore in listOf("goal", "weight", "height", "gender", "activity", "allergies")) {
//                    val prefs = getSharedPreferences("user_profile", Context.MODE_PRIVATE)
//                    prefs.edit().putBoolean("show_ai_plan_update_dialog", true).apply()
//                }

                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Ошибка сохранения: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveLocally(key: String, value: String) {
        val prefs = getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        prefs.edit().putString(key, value).apply()
    }

    override fun onResume() {
        super.onResume()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}