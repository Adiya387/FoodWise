package com.example.healthyfoodai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ActivityLevelActivity : AppCompatActivity() {

    private var selectedLevel: String? = null
    private lateinit var finishBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activity_level)

        val name = intent.getStringExtra("USER_NAME")
        val gender = intent.getStringExtra("USER_GENDER")
        val birthdate = intent.getStringExtra("USER_BIRTHDATE")
        val height = intent.getStringExtra("USER_HEIGHT")
        val weight = intent.getStringExtra("USER_WEIGHT")
        val goal = intent.getStringExtra("USER_GOAL")

        finishBtn = findViewById(R.id.btnFinish)
        finishBtn.isEnabled = false

        val buttons = mapOf(
            R.id.btnLow to "Низкий",
            R.id.btnMedium to "Средний",
            R.id.btnHigh to "Высокий"
        )

        buttons.keys.forEach {
            val button = findViewById<Button>(it)
            button.setBackgroundTintList(getColorStateList(R.color.gray))
            button.setTextColor(getColor(R.color.black))
        }

        buttons.forEach { (id, value) ->
            val btn = findViewById<Button>(id)
            btn.setOnClickListener {
                selectedLevel = value
                highlightSelection(btn, buttons.keys.map { findViewById<Button>(it) })
                finishBtn.isEnabled = true
            }
        }

        finishBtn.setOnClickListener {
            saveToPrefs(name, gender, birthdate, height, weight, goal, selectedLevel ?: "")

            val intent = Intent(this, AllergyActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun highlightSelection(selected: Button, allButtons: List<Button>) {
        allButtons.forEach {
            it.setBackgroundTintList(getColorStateList(R.color.gray))
            it.setTextColor(getColor(R.color.black))
        }
        selected.setBackgroundTintList(getColorStateList(R.color.teal_700))
        selected.setTextColor(getColor(R.color.white))
    }

    private fun saveToPrefs(
        name: String?, gender: String?, birthdate: String?,
        height: String?, weight: String?, goal: String?, activity: String
    ) {
        val sharedPref = getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        sharedPref.edit()
            .putString("name", name)
            .putString("gender", gender)
            .putString("birthdate", birthdate)
            .putString("height", height)
            .putString("weight", weight)
            .putString("goal", goal)
            .putString("activity", activity) // ✅ используем правильный ключ
            .apply()
    }
}