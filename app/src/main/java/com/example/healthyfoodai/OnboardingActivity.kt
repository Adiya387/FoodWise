package com.example.healthyfoodai

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.healthyfoodai.databinding.ActivityOnboardingBinding
import com.example.healthyfoodai.model.OnboardingItem

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    private val onboardingItems = listOf(
        OnboardingItem(
            title = "Добро пожаловать в FoodWise",
            description = "Умный подход к здоровью и питанию — с заботой о вашем теле и уме."
        ),
        OnboardingItem(
            title = "Управляй рационом легко",
            description = "Автоматический подбор рецептов, учет калорий и ИИ-рекомендации."
        )
    )

    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showOnboardingItem(currentIndex)
        setSlideIndicators()  // инициализация индикаторов
        setBackgroundForCurrentSlide(currentIndex)  // установка фона для первого слайда

        binding.btnNext.setOnClickListener {
            if (currentIndex < onboardingItems.lastIndex) {
                currentIndex++
                showOnboardingItem(currentIndex)
                setSlideIndicators()  // обновляем индикаторы
                setBackgroundForCurrentSlide(currentIndex)  // обновляем фон
            } else {
                startActivity(Intent(this, RegisterActivity::class.java))
                finish()
            }
        }

        binding.tvSkip.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun showOnboardingItem(index: Int) {
        val item = onboardingItems[index]
        binding.tvTitle.text = item.title
        binding.tvDescription.text = item.description
        binding.btnNext.text = if (index == onboardingItems.lastIndex) "Начать" else "Далее"

        // Применение анимации для элементов
        val anim = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        binding.tvTitle.startAnimation(anim)
        binding.tvDescription.startAnimation(anim)
        binding.btnNext.startAnimation(anim)
    }

    private fun setSlideIndicators() {
        val dots = listOf(binding.dot1, binding.dot2) // добавьте больше точек, если нужно
        dots.forEachIndexed { index, view ->
            if (index == currentIndex) {
                view.setBackgroundResource(R.drawable.indicator_active)  // активная точка
            } else {
                view.setBackgroundResource(R.drawable.indicator_inactive)  // неактивная точка
            }
        }
    }

    private fun setBackgroundForCurrentSlide(index: Int) {
        when (index) {
            0 -> binding.root.setBackgroundResource(R.drawable.onboard1)
            1 -> binding.root.setBackgroundResource(R.drawable.onboard1)
            // Добавьте другие фоны по необходимости
        }
    }
}
