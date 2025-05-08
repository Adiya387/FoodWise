package com.example.healthyfoodai

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileOptionsActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var profileData: MutableMap<String, String>

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val fields = listOf("Имя", "Пол", "Возраст", "Рост", "Вес", "Цель", "Активность", "Ограничения")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile_options)

        listView = findViewById(R.id.listViewProfile)
        profileData = mutableMapOf()

        loadUserProfile()

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedField = fields[position]
            val currentValue = profileData[selectedField] ?: ""

            val intent = Intent(this, EditFieldActivity::class.java)
            intent.putExtra("field_name", selectedField)
            intent.putExtra("current_value", currentValue)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    profileData = mutableMapOf(
                        "Имя" to (document.getString("name") ?: ""),
                        "Пол" to (document.getString("gender") ?: ""),
                        "Возраст" to (document.get("age")?.toString() ?: ""),
                        "Рост" to (document.getString("height") ?: ""),
                        "Вес" to (document.getString("weight") ?: ""),
                        "Цель" to (document.getString("goal") ?: ""),
                        "Активность" to (document.getString("activity") ?: ""),
                        "Ограничения" to ((document.get("allergies") as? List<*>)?.joinToString(", ") ?: "Нет ограничений")
                    )

                    val displayList = fields.map { field ->
                        "$field: ${profileData[field] ?: ""}"
                    }

                    val adapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_list_item_1,
                        displayList
                    )

                    listView.adapter = adapter
                } else {
                    Toast.makeText(this, "Профиль не найден", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Ошибка загрузки профиля", Toast.LENGTH_SHORT).show()
            }
    }

}
