package com.example.healthyfoodai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class AgeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_age)

        val name = intent.getStringExtra("USER_NAME")
        val gender = intent.getStringExtra("USER_GENDER")

        val datePicker = findViewById<DatePicker>(R.id.datePicker)
        val btnNext = findViewById<Button>(R.id.btnNextAge)

        // Ограничим выбор — минимальный возраст 5 лет
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -5)
        datePicker.maxDate = calendar.timeInMillis

        btnNext.setOnClickListener {
            val selectedDay = datePicker.dayOfMonth
            val selectedMonth = datePicker.month
            val selectedYear = datePicker.year

            val today = Calendar.getInstance()
            var age = today.get(Calendar.YEAR) - selectedYear

            if (today.get(Calendar.MONTH) < selectedMonth ||
                (today.get(Calendar.MONTH) == selectedMonth && today.get(Calendar.DAY_OF_MONTH) < selectedDay)) {
                age--
            }

            saveAgeToPrefs(age.toString())

            val intent = Intent(this, HeightActivity::class.java)
            intent.putExtra("USER_NAME", name)
            intent.putExtra("USER_GENDER", gender)
            startActivity(intent)
        }
    }

    private fun saveAgeToPrefs(age: String) {
        val sharedPref = getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        sharedPref.edit()
            .putString("age", age) // ✅ Сохраняем именно возраст в годах
            .apply()
    }
}
