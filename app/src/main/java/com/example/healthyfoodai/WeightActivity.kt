package com.example.healthyfoodai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class WeightActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weight)

        val name = intent.getStringExtra("USER_NAME")
        val gender = intent.getStringExtra("USER_GENDER")
        val birthdate = intent.getStringExtra("USER_BIRTHDATE")
        val height = intent.getStringExtra("USER_HEIGHT")


        val etWeight = findViewById<EditText>(R.id.etWeight)
        val btnNext = findViewById<Button>(R.id.btnNextWeight)

        btnNext.setOnClickListener {
            val weight = etWeight.text.toString().trim()
            if (weight.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, введите вес", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, GoalActivity::class.java)
                intent.putExtra("USER_NAME", name)
                intent.putExtra("USER_GENDER", gender)
                intent.putExtra("USER_BIRTHDATE", birthdate)
                intent.putExtra("USER_HEIGHT", height)
                intent.putExtra("USER_WEIGHT", weight)
                startActivity(intent)
            }
        }
    }
}