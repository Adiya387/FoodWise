package com.example.healthyfoodai

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MealViewModel : ViewModel() {

    private val _mealUpdated = MutableLiveData<Boolean>()
    val mealUpdated: LiveData<Boolean> get() = _mealUpdated

    fun notifyMealAdded() {
        _mealUpdated.value = true
    }

    fun resetFlag() {
        _mealUpdated.value = false
    }
}
