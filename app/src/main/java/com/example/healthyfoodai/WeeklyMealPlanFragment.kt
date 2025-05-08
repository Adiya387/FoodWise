package com.example.healthyfoodai

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class WeeklyMealPlanFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private val daysOfWeek = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_weekly_meal_plan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = view.findViewById(R.id.viewPager)
        tabLayout = view.findViewById(R.id.tabLayout)

        val adapter = WeekPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val dayName = daysOfWeek[position]
            val todayIndex = getTodayIndex()
            tab.text = if (position == todayIndex) "📍 $dayName (Сегодня)" else dayName
        }.attach()

        viewPager.setCurrentItem(getTodayIndex(), false)

        // 🔁 Проверка — если плана ещё нет, генерируем через GPT
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "Пользователь не найден", Toast.LENGTH_SHORT).show()
            return
        }

        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("weekly_plan").document(userId).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Log.d("WEEKLY_PLAN", "❌ План не найден. Генерируем через GPT...")
                    Toast.makeText(requireContext(), "Генерируем AI-план на неделю...", Toast.LENGTH_SHORT).show()
                    generateWeeklyPlanFromGPT(requireContext())
                } else {
                    val createdAt = doc.getLong("createdAt") ?: 0L
                    val now = System.currentTimeMillis()
                    val sevenDays = 7 * 24 * 60 * 60 * 1000L

                    if (now - createdAt > sevenDays) {
                        Log.d("WEEKLY_PLAN", "⏳ Плану больше 7 дней — генерируем новый")
                        generateWeeklyPlanFromGPT(requireContext())
                    } else {
                        Log.d("WEEKLY_PLAN", "✅ План найден и актуален — используем")
                    }
                }
            }
            .addOnFailureListener {
                Log.e("WEEKLY_PLAN", "⚠️ Ошибка при получении плана: ${it.message}")
                Toast.makeText(requireContext(), "Ошибка загрузки плана", Toast.LENGTH_SHORT).show()
            }

    }

    private fun getTodayIndex(): Int {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }
    }
}
