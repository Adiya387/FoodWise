package com.example.healthyfoodai
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Анимация появления карточки
        val cardView = findViewById<CardView>(R.id.cardView)
        val animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        animation.duration = 600 // 600 мс анимация
        cardView.startAnimation(animation)

        // Почта
        val emailLayout = findViewById<LinearLayout>(R.id.emailLayout)
        emailLayout.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:horimasahiro030206@gmail.com")
                putExtra(Intent.EXTRA_SUBJECT, "Вопрос о приложении FoodWise")
                putExtra(Intent.EXTRA_TEXT, "Здравствуйте! Хотел бы узнать...")
            }
            startActivity(Intent.createChooser(emailIntent, "Отправить письмо через:"))
        }
    }
}