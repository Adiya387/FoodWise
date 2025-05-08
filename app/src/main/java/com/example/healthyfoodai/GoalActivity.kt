package com.example.healthyfoodai


import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class GoalActivity : AppCompatActivity() {

    private var selectedGoal: String? = null
    private lateinit var nextBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal)

        val name = intent.getStringExtra("USER_NAME")
        val gender = intent.getStringExtra("USER_GENDER")
        val weight = intent.getStringExtra("USER_WEIGHT")
        val height = intent.getStringExtra("USER_HEIGHT")
        val birthdate = intent.getStringExtra("USER_BIRTHDATE")

        nextBtn = findViewById(R.id.btnNext)
        val buttons = mapOf(
            R.id.btnGoalLose to "Похудение",
            R.id.btnGoalGain to "Набор массы",
            R.id.btnGoalMaintain to "Поддержание формы",
            R.id.btnGoalHealth to "Улучшить здоровье",
            R.id.btnGoalEnergy to "Больше энергии"
        )

        // Устанавливаем начальный цвет для всех кнопок как невыбранный (бежевый)
        buttons.keys.forEach {
            val button = findViewById<Button>(it)
            button.setBackgroundTintList(getColorStateList(R.color.gray)) // Бежевый цвет
            button.setTextColor(getColor(R.color.black)) // Черный цвет текста
        }

        buttons.forEach { (id, value) ->
            val btn = findViewById<Button>(id)
            btn.setOnClickListener {
                selectedGoal = value
                highlightSelection(btn, buttons.keys.map { findViewById<Button>(it) })
                nextBtn.isEnabled = true
            }
        }

        nextBtn.setOnClickListener {
            val intent = Intent(this, ActivityLevelActivity::class.java)
            intent.putExtra("USER_NAME", name)
            intent.putExtra("USER_GENDER", gender)
            intent.putExtra("USER_BIRTHDATE", birthdate)
            intent.putExtra("USER_HEIGHT", height)
            intent.putExtra("USER_WEIGHT", weight)
            intent.putExtra("USER_GOAL", selectedGoal)
            startActivity(intent)
        }
    }

    // Метод для изменения цвета кнопок
    private fun highlightSelection(selected: Button, allButtons: List<Button>) {
        // Для всех кнопок устанавливаем цвет для неактивного состояния
        allButtons.forEach {
            it.setBackgroundTintList(getColorStateList(R.color.gray)) // Неактивный цвет (бежевый)
            it.setTextColor(getColor(R.color.black))
        }
        // Для выбранной кнопки устанавливаем цвет для активного состояния
        selected.setBackgroundTintList(getColorStateList(R.color.teal_700)) // Активный цвет (фиолетовый)
        selected.setTextColor(getColor(R.color.white)) // Белый текст для выбранной кнопки
    }
}