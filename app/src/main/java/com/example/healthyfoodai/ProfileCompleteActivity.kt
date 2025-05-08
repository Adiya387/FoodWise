package com.example.healthyfoodai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProfileCompleteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_complete)

        val tvGreeting = findViewById<TextView>(R.id.tvNameGreeting)
        val btnGoToMain = findViewById<Button>(R.id.btnGoToMain)

        // Получаем имя из SharedPreferences
        val prefs = getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        val name = prefs.getString("name", "пользователь")

        // Устанавливаем приветствие
        tvGreeting.text = "Добро пожаловать, $name!"

        btnGoToMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            // Вот это исправление:
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
