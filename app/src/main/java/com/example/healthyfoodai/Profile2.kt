package com.example.healthyfoodai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Profile2 : AppCompatActivity() {

	private val auth = FirebaseAuth.getInstance()
	private val firestore = FirebaseFirestore.getInstance()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_profile2)

		// Загрузка фонового изображения и аватарки через Glide
		Glide.with(this)
			.load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/HSGZ1nDV93/4lgwif5g_expires_30_days.png")
			.into(findViewById(R.id.rn8xm0wploz)) // фон

		Glide.with(this)
			.load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/HSGZ1nDV93/g4o9iw6o_expires_30_days.png")
			.into(findViewById(R.id.rqwkkws8er1s)) // аватар

		// Загрузка иконок через Glide
		val iconMap = mapOf(
			R.id.rvsterizaiq to "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/HSGZ1nDV93/t4ww4su8_expires_30_days.png",
			R.id.rlozw975kfem to "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/HSGZ1nDV93/n833ohoj_expires_30_days.png",
			R.id.rwb83tol6sb to "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/HSGZ1nDV93/nm7sxv0j_expires_30_days.png",
			R.id.r41scplrbai2 to "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/HSGZ1nDV93/pd1ebnsw_expires_30_days.png",
			R.id.r687j01epp8k to "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/HSGZ1nDV93/v703h2w7_expires_30_days.png",
			R.id.roie31wj153 to "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/HSGZ1nDV93/j10zsm8v_expires_30_days.png",
			R.id.radvvb5lnsz to "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/HSGZ1nDV93/j67wav5b_expires_30_days.png",
			R.id.r739z275dhn to "https://storage.googleapis.com/tagjs-prod.appspot.com/v1/HSGZ1nDV93/4i51oq69_expires_30_days.png"
		)

		for ((viewId, url) in iconMap) {
			Glide.with(this).load(url).into(findViewById(viewId))
		}

		// Подключение TextView’ов (read-only)
		val fields = listOf(
			findViewById<TextView>(R.id.r51ojydjr5nq),  // Имя
			findViewById(R.id.rnj35gnxqtwc),           // Пол
			findViewById(R.id.rar7ejo4k4hj),           // Возраст
			findViewById(R.id.rztz37jtiiok),           // Рост
			findViewById(R.id.rbmuy182tl26),           // Вес
			findViewById(R.id.rtdaz6ua8t7),            // Цель
			findViewById(R.id.rhusf6il3vz),            // Активность
			findViewById(R.id.r0zbsrrv7qwx)            // Ограничения
		)

		val userId = auth.currentUser?.uid ?: return

		// Загрузка данных из Firestore
		firestore.collection("users").document(userId)
			.get()
			.addOnSuccessListener { doc ->
				fields[0].text = "Имя: ${doc.getString("name") ?: ""}"
				fields[1].text = "Пол: ${doc.getString("gender") ?: ""}"
				val ageString = doc.get("age")?.toString() ?: "0"
				val age = ageString.toIntOrNull() ?: 0
				fields[2].text = "Возраст: $age"
				fields[3].text = "Рост: ${doc.getString("height") ?: ""}"
				fields[4].text = "Вес: ${doc.getString("weight") ?: ""}"
				fields[5].text = "Цель: ${doc.getString("goal") ?: ""}"
				fields[6].text = "Активность: ${doc.getString("activity") ?: ""}"
				val allergies = (doc.get("allergies") as? List<*>)?.joinToString(", ") ?: "Нет ограничений"
				fields[7].text = "Ограничения: $allergies"
			}
			.addOnFailureListener {
				fields[0].text = "Ошибка загрузки данных"
			}
	}
}
