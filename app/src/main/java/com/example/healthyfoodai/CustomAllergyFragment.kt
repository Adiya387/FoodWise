package com.example.healthyfoodai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

class CustomAllergyFragment(private val onAllergyAdded: (String) -> Unit) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_custom_allergy, container, false)

        val allergyEditText = view.findViewById<EditText>(R.id.etCustomAllergy)
        val addAllergyButton = view.findViewById<Button>(R.id.btnAddAllergy)

        addAllergyButton.setOnClickListener {
            val customAllergy = allergyEditText.text.toString().trim()

            if (customAllergy.isNotEmpty()) {
                onAllergyAdded(customAllergy)
                parentFragmentManager.popBackStack() // Закрываем фрагмент и возвращаемся
            } else {
                Toast.makeText(activity, "Пожалуйста, введите аллергию", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
