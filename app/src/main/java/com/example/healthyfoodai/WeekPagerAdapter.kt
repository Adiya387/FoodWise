package com.example.healthyfoodai

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class WeekPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 7
    override fun createFragment(position: Int): Fragment {
        return DayPlanFragment.newInstance(position)
    }
}
