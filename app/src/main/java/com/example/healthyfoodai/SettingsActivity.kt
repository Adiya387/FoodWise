package com.example.healthyfoodai

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val editProfile = findViewById<TextView>(R.id.editProfile)
        val changeTheme = findViewById<TextView>(R.id.changeTheme)
        val aboutApp = findViewById<TextView>(R.id.aboutApp)

        // 🔧 Переход на экран редактирования профиля
        editProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileOptionsActivity::class.java))
        }

        // 🌗 Переключение светлой / тёмной темы
        changeTheme.setOnClickListener {
            val nightMode = AppCompatDelegate.getDefaultNightMode()
            if (nightMode == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            recreate()
        }

        // ℹ️ О приложении
        aboutApp.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }
}
