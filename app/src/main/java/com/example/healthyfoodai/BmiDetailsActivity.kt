package com.example.healthyfoodai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class BmiDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bmi_details)

        // Здесь ничего не нужно получать из Intent — экран только информативный
        // Вся информация уже зашита в layout (activity_bmi_details.xml)
    }
}
