package com.example.healthyfoodai

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class GenderActivity : AppCompatActivity() {

    private var selectedGender: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gender)

        val name = intent.getStringExtra("USER_NAME")

        val btnMale = findViewById<Button>(R.id.btnMale)
        val btnFemale = findViewById<Button>(R.id.btnFemale)
        val btnNext = findViewById<Button>(R.id.btnNextGender)

        val anim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        btnMale.startAnimation(anim)
        btnFemale.startAnimation(anim)
        btnNext.startAnimation(anim)

        btnMale.setOnClickListener {
            selectedGender = "Мужской"
            highlightSelection(btnMale, btnFemale)
        }

        btnFemale.setOnClickListener {
            selectedGender = "Женский"
            highlightSelection(btnFemale, btnMale)
        }

        btnNext.setOnClickListener {
            if (selectedGender == null) {
                Toast.makeText(this, "Пожалуйста, выберите пол", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, AgeActivity::class.java)
                intent.putExtra("USER_NAME", name)
                intent.putExtra("USER_GENDER", selectedGender)
                startActivity(intent)
            }
        }
    }

    private fun highlightSelection(selected: Button, other: Button) {
        selected.setBackgroundTintList(getColorStateList(R.color.teal_700))
        selected.setTextColor(getColor(R.color.white))
        other.setBackgroundTintList(getColorStateList(R.color.gray))
        other.setTextColor(getColor(R.color.black))
    }
}