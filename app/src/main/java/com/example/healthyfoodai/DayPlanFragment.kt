package com.example.healthyfoodai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar

class DayPlanFragment : Fragment() {

    companion object {
        fun newInstance(dayIndex: Int): DayPlanFragment {
            val fragment = DayPlanFragment()
            fragment.arguments = Bundle().apply { putInt("dayIndex", dayIndex) }
            return fragment
        }
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MealPlanAdapter
    private lateinit var weeklyMealViewModel: WeeklyMealViewModel
    private lateinit var diaryViewModel: DiaryViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_day_plan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        diaryViewModel = ViewModelProvider(requireActivity())[DiaryViewModel::class.java]

        recyclerView = view.findViewById(R.id.recyclerView)
        adapter = MealPlanAdapter(diaryViewModel)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        val dayIndex = arguments?.getInt("dayIndex") ?: 0
        val dayName = listOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье")[dayIndex]

        weeklyMealViewModel = ViewModelProvider(requireActivity())[WeeklyMealViewModel::class.java]
        weeklyMealViewModel.getMealsForDay(dayName).observe(viewLifecycleOwner) { meals ->
            adapter.submitList(meals)
        }

        val label = view.findViewById<TextView>(R.id.todayLabel)
        if (dayIndex == Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2) {
            label.visibility = View.VISIBLE
        }
    }
}
