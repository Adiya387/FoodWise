package com.example.healthyfoodai

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.healthyfoodai.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Анимация логотипа
        val anim = AnimationUtils.loadAnimation(this, R.anim.splash_zoom)
        binding.splashLogo.startAnimation(anim)

        // Задержка перед переходом
        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                // Если пользователь авторизован
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // Если не авторизован, показать онбординг
                startActivity(Intent(this, OnboardingActivity::class.java))
            }
            finish()
        }, 2500)
    }
}