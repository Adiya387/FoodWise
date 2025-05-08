package com.example.healthyfoodai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HeightActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_height)

        val name = intent.getStringExtra("USER_NAME")
        val gender = intent.getStringExtra("USER_GENDER")
        val birthdate = intent.getStringExtra("USER_BIRTHDATE")

        val etHeight = findViewById<EditText>(R.id.etHeight)
        val btnNext = findViewById<Button>(R.id.btnNextHeight)

        btnNext.setOnClickListener {
            val height = etHeight.text.toString().trim()
            if (height.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, введите рост", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, WeightActivity::class.java)
                intent.putExtra("USER_NAME", name)
                intent.putExtra("USER_GENDER", gender)
                intent.putExtra("USER_BIRTHDATE", birthdate)
                intent.putExtra("USER_HEIGHT", height)
                startActivity(intent)
            }
        }
    }
}