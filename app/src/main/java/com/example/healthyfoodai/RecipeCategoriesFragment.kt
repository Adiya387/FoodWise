package com.example.healthyfoodai

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthyfoodai.MealCategoryAdapter
import com.example.healthyfoodai.databinding.FragmentRecipeCategoriesBinding
import com.google.firebase.firestore.FirebaseFirestore

class RecipeCategoriesFragment : Fragment() {

    private var _binding: FragmentRecipeCategoriesBinding? = null
    private val binding get() = _binding!!

    private val categories = listOf(
        Category("Супы", "https://sugarspunrun.com/wp-content/uploads/2024/12/Vegetable-soup-recipe-2-of-2.jpg"),
        Category("Салаты", "https://menunedeli.ru/wp-content/uploads/2023/02/Salat_s_jarenoy_indeykoy_i_ovochami_22_500%D1%85350-1200x800.jpg"),
        Category("Основные блюда", "https://images.mrcook.app/recipe-image/0191183a-8f60-7993-9d3f-3c1b0c39c57f"),
        Category("Выпечка и десерты", "https://img.povar.ru/mobile/1c/9e/65/dd/desert_bez_vipechki_i_muki-795535.jpg"),
        Category("Закуски", "https://eda.ru/images/RecipePhoto/4x3/kanape-s-pryanoy-fetoy-i-olivkami_175311_photo_182963.jpg"),
        Category("Гарниры", "https://www.rbc.ua/static/img/_/f/_freepik_com_43_1200x675.jpg"),
        Category("Здоровое питание", "https://i.obozrevatel.com/food/recipemain/2019/3/15/tosty-s-avokado15216289801max.jpg?size=636x424")
    )

    private val mealCategories = listOf(
        Recipe("Завтрак", "", "Завтрак", "https://prime-star.ru/upload/blog-files/zachem-nuzhno-zavtrakat-top-5-poleznykh-zavtrakov-1.jpg", 0, 0, 0, 0, emptyList(), emptyList(), emptyList()),
        Recipe("Обед", "", "Обед", "https://www.edimdoma.ru/system/images/contents/0001/4781/wide/147004-original.jpg?1639684939", 0, 0, 0, 0, emptyList(), emptyList(), emptyList()),
        Recipe("Ужин", "", "Ужин", "https://s13.stc.all.kpcdn.net/family/wp-content/uploads/2023/08/kurinye-serdechki-630.jpg", 0, 0, 0, 0, emptyList(), emptyList(), emptyList()),
        Recipe("Перекус", "", "Перекус", "https://cdn.food.ru/unsigned/fit/640/480/ce/0/czM6Ly9tZWRpYS9waWN0dXJlcy9yZWNpcGVzLzU0NTE4L2NvdmVycy8zZXZGenEuanBlZw.jpg", 0, 0, 0, 0, emptyList(), emptyList(), emptyList())
    )

    private lateinit var mealCategoryAdapter: MealCategoryAdapter
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeCategoriesBinding.inflate(inflater, container, false)

        // Верхний блок: категории в GridLayout
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position == categories.size - 1) 2 else 1
            }
        }

        binding.rvCategories.layoutManager = gridLayoutManager
        binding.rvCategories.adapter = CategoryAdapter(categories) { category ->
            val intent = Intent(requireContext(), RecipeListActivity::class.java)
            intent.putExtra("CATEGORY", category.name)
            startActivity(intent)
        }

        // Нижний блок: Завтрак, Обед, Ужин, Перекус
        mealCategoryAdapter = MealCategoryAdapter(requireContext(), mealCategories)
        binding.rvMealsHorizontal.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvMealsHorizontal.adapter = mealCategoryAdapter

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
