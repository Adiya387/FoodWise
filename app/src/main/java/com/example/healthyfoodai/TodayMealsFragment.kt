package com.example.healthyfoodai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthyfoodai.databinding.FragmentTodayMealsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class TodayMealsFragment : Fragment() {

    private var _binding: FragmentTodayMealsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DiaryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodayMealsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerTodayMeals.layoutManager = LinearLayoutManager(requireContext())
        viewModel = ViewModelProvider(requireActivity())[DiaryViewModel::class.java]

        loadTodayMeals()

        viewModel.mealUpdated.observe(viewLifecycleOwner) { updated ->
            if (updated) {
                loadTodayMeals()
                viewModel.resetFlag()
            }
        }
    }

    private fun loadTodayMeals() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val today = getTodayDate()

        db.collection("users").document(userId)
            .collection("diary").document(today)
            .collection("meals")
            .get()
            .addOnSuccessListener { snapshot ->
                val meals = snapshot.documents.mapNotNull { it.toObject(Meal::class.java) }
                if (meals.isNotEmpty()) {
                    binding.recyclerTodayMeals.adapter = TodayMealsAdapter(meals)
                } else {
                    binding.recyclerTodayMeals.adapter = null
                    Toast.makeText(requireContext(), "Вы ещё ничего не добавили в дневник", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Ошибка загрузки блюд", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
