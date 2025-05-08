import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.healthyfoodai.RetrofitAdviceInstance
import com.example.healthyfoodai.api.RetrofitOpenAiInstance
import com.example.healthyfoodai.databinding.FragmentHomeBinding
import com.example.healthyfoodai.ChatRequest
import com.example.healthyfoodai.ChatResponse
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt
import com.example.healthyfoodai.Message
import com.example.healthyfoodai.UserManagerr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.healthyfoodai.BmiDetailsActivity
import com.example.healthyfoodai.ChatMessage
import com.example.healthyfoodai.CustomToast
import com.example.healthyfoodai.DiaryViewModel
import com.example.healthyfoodai.ManualMealAddActivity
import com.example.healthyfoodai.R
import com.example.healthyfoodai.WeeklyMealPlanFragment


private lateinit var diaryViewModel: DiaryViewModel


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        diaryViewModel = ViewModelProvider(requireActivity())[DiaryViewModel::class.java]

        // ✅ Вставка WeeklyMealPlanFragment внутрь FrameLayout (R.id.planContainer)
        childFragmentManager.beginTransaction()
            .replace(R.id.planContainer, WeeklyMealPlanFragment())
            .commit()

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Загрузка прогресса и приёмов пищи
        loadNutritionProgress(userId)
        loadMealsSummary(userId, getTodayDate())

        // Обновление UI при изменении данных
        diaryViewModel.mealUpdated.observe(viewLifecycleOwner) { updated ->
            if (updated) {
                loadNutritionProgress(userId)
                loadMealsSummary(userId, getTodayDate())
                diaryViewModel.resetFlag()
            }
        }

        // Обработка кнопки "Добавить вручную"
        binding.btnAddManualMeal.setOnClickListener {
            val intent = Intent(requireContext(), ManualMealAddActivity::class.java)
            startActivityForResult(intent, 1001)
        }

        // Обработка кнопки "История питания"
        binding.btnHistory.setOnClickListener {
            showDatePickerDialog(userId)
        }

        // Загрузка профиля и генерация AI-совета
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val heightStr = doc.getString("height")
                    val weightStr = doc.getString("weight")
                    val goalRaw = doc.getString("goal") ?: return@addOnSuccessListener
                    val ageRaw = doc.get("age")

                    val height = heightStr?.toIntOrNull() ?: return@addOnSuccessListener
                    val weight = weightStr?.toIntOrNull() ?: return@addOnSuccessListener
                    val goal = goalRaw.trim().lowercase()

                    val age = when (ageRaw) {
                        is Number -> ageRaw.toInt()
                        is String -> ageRaw.toIntOrNull() ?: return@addOnSuccessListener
                        else -> return@addOnSuccessListener
                    }

                    val gender = doc.getString("gender") ?: return@addOnSuccessListener
                    val activity = doc.getString("activity") ?: "низкая"

                    calculateAndDisplayBmi(height, weight, goal)

                    // Запрос совета от ИИ после загрузки профиля
                    getDailyAdvice(userId, height, weight, age, gender, goal, activity)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
            }
    }




    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun loadNutritionProgress(userId: String, date: String = getTodayDate()) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { userSnapshot ->
                val weight = userSnapshot.getString("weight")?.toIntOrNull() ?: return@addOnSuccessListener
                val height = userSnapshot.getString("height")?.toIntOrNull() ?: return@addOnSuccessListener
                val ageRaw = userSnapshot.get("age")
                val age = when (ageRaw) {
                    is Number -> ageRaw.toInt()
                    is String -> ageRaw.toIntOrNull() ?: return@addOnSuccessListener
                    else -> return@addOnSuccessListener
                }

                val gender = userSnapshot.getString("gender") ?: return@addOnSuccessListener
                val activity = userSnapshot.getString("activity") ?: "низкая"
                val goal = userSnapshot.getString("goal")?.lowercase() ?: "поддержание формы"

                // 1. Расчёт BMR (Mifflin–St Jeor)
                val bmr = if (gender.lowercase() == "мужчина") {
                    10 * weight + 6.25 * height - 5 * age + 5
                } else {
                    10 * weight + 6.25 * height - 5 * age - 161
                }

                // 2. Уровень активности
                val activityFactor = when (activity.lowercase()) {
                    "низкая" -> 1.2
                    "умеренная" -> 1.55
                    "высокая" -> 1.725
                    else -> 1.2
                }

                val normCalories = (bmr * activityFactor).roundToInt()

                // 3. Нормы БЖУ в зависимости от цели
                val proteinPerKg = when (goal) {
                    "набор массы" -> 2.0
                    "похудение" -> 2.2
                    "поддержание формы" -> 1.6
                    "улучшить здоровье" -> 1.5
                    else -> 1.5
                }
                val fatPerKg = 0.9

                val normProteins = (proteinPerKg * weight).roundToInt()
                val normFats = (fatPerKg * weight).roundToInt()
                val caloriesFromProteins = normProteins * 4
                val caloriesFromFats = normFats * 9
                val remainingCalories = normCalories - (caloriesFromProteins + caloriesFromFats)
                val normCarbs = (remainingCalories / 4.0).roundToInt().coerceAtLeast(0)

                // 4. Получаем данные за день
                db.collection("users").document(userId)
                    .collection("diary").document(date)
                    .get()
                    .addOnSuccessListener { diarySnapshot ->
                        val calories = diarySnapshot.getLong("calories")?.toInt() ?: 0
                        val proteins = diarySnapshot.getLong("proteins")?.toInt() ?: 0
                        val fats = diarySnapshot.getLong("fats")?.toInt() ?: 0
                        val carbs = diarySnapshot.getLong("carbs")?.toInt() ?: 0

                        // 5. Круговая диаграмма через MPAndroidChart
                        val pieChart = binding.pieChartCalories
                        val entries = ArrayList<PieEntry>()
                        if (calories <= normCalories) {
                            entries.add(PieEntry(calories.toFloat(), "Съедено"))
                            entries.add(PieEntry((normCalories - calories).toFloat(), "Осталось"))
                        } else {
                            entries.add(PieEntry(normCalories.toFloat(), "Норма"))
                            entries.add(PieEntry((calories - normCalories).toFloat(), "Перебор"))
                        }

                        val dataSet = PieDataSet(entries, "")
                        dataSet.setDrawValues(false)
                        dataSet.colors = if (calories <= normCalories) {
                            listOf(Color.parseColor("#4CAF50"), Color.parseColor("#D3D3D3"))
                        } else {
                            listOf(Color.parseColor("#4CAF50"), Color.parseColor("#F44336"))
                        }

                        val data = PieData(dataSet)
                        pieChart.data = data
                        pieChart.setCenterText("$calories / $normCalories ккал")
                        pieChart.setCenterTextSize(14f)
                        pieChart.setEntryLabelColor(Color.TRANSPARENT)
                        pieChart.description.isEnabled = false
                        pieChart.legend.isEnabled = false
                        pieChart.setHoleRadius(65f)
                        pieChart.setTransparentCircleAlpha(0)
                        pieChart.invalidate()

                        // 6. Обновляем БЖУ
                        updateProgressWithColor(
                            binding.progressProteins,
                            proteins,
                            normProteins,
                            R.drawable.progress_green,
                            R.drawable.progress_red
                        )
                        binding.tvProteins.text = "$proteins / $normProteins г"

                        updateProgressWithColor(
                            binding.progressFats,
                            fats,
                            normFats,
                            R.drawable.progress_orange,
                            R.drawable.progress_red
                        )
                        binding.tvFats.text = "$fats / $normFats г"

                        updateProgressWithColor(
                            binding.progressCarbs,
                            carbs,
                            normCarbs,
                            R.drawable.progress_blue,
                            R.drawable.progress_red
                        )
                        binding.tvCarbs.text = "$carbs / $normCarbs г"
                    }
            }
    }

    private fun loadMealsSummary(userId: String, date: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId)
            .collection("diary").document(date)
            .collection("meals")
            .get()
            .addOnSuccessListener { snapshot ->
                val parentLayout = binding.layoutTodayMeals
                parentLayout.removeAllViews()

                if (snapshot.isEmpty) {
                    val tvEmpty = TextView(requireContext())
                    tvEmpty.text = "- Пока ничего не добавлено"
                    parentLayout.addView(tvEmpty)
                    return@addOnSuccessListener
                }

                for (doc in snapshot.documents) {
                    val title = doc.getString("title") ?: continue
                    val calories = doc.getLong("calories")?.toInt() ?: 0
                    val docId = doc.id

                    val container = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(0, 8, 0, 8)
                    }

                    val tvMeal = TextView(requireContext()).apply {
                        text = "🍽 $title – $calories ккал"
                        textSize = 16f
                        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                    }

                    val btnDelete = ImageButton(requireContext()).apply {
                        setImageResource(R.drawable.ic_delete)
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        setOnClickListener {
                            AlertDialog.Builder(requireContext())
                                .setTitle("Удалить блюдо")
                                .setMessage("Вы точно хотите удалить \"$title\" из дневника?")
                                .setPositiveButton("Да") { _, _ ->
                                    val recipeRef = db.collection("users").document(userId)
                                        .collection("diary").document(date)
                                        .collection("meals").document(docId)

                                    recipeRef.get().addOnSuccessListener { recipeSnapshot ->
                                        val recipeCalories = recipeSnapshot.getLong("calories") ?: 0
                                        val recipeProteins = recipeSnapshot.getLong("proteins") ?: 0
                                        val recipeFats = recipeSnapshot.getLong("fats") ?: 0
                                        val recipeCarbs = recipeSnapshot.getLong("carbs") ?: 0

                                        val diaryRef = db.collection("users").document(userId)
                                            .collection("diary").document(date)

                                        diaryRef.get().addOnSuccessListener { diarySnapshot ->
                                            val currentCalories = diarySnapshot.getLong("calories") ?: 0
                                            val currentProteins = diarySnapshot.getLong("proteins") ?: 0
                                            val currentFats = diarySnapshot.getLong("fats") ?: 0
                                            val currentCarbs = diarySnapshot.getLong("carbs") ?: 0

                                            val updatedData = mapOf(
                                                "calories" to (currentCalories - recipeCalories).coerceAtLeast(0),
                                                "proteins" to (currentProteins - recipeProteins).coerceAtLeast(0),
                                                "fats" to (currentFats - recipeFats).coerceAtLeast(0),
                                                "carbs" to (currentCarbs - recipeCarbs).coerceAtLeast(0)
                                            )

                                            diaryRef.update(updatedData).addOnSuccessListener {
                                                recipeRef.delete().addOnSuccessListener {
                                                    loadMealsSummary(userId, date)
                                                    loadNutritionProgress(userId, date)
                                                    CustomToast.showCustomToast(requireContext(), "Удалено")
                                                }
                                            }
                                        }
                                    }
                                }
                                .setNegativeButton("Отмена", null)
                                .show()
                        }
                    }

                    container.addView(tvMeal)
                    container.addView(btnDelete)
                    parentLayout.addView(container)
                }
            }
    }

    private fun showDatePickerDialog(userId: String) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(requireContext(), { _, y, m, d ->
            val selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
            loadNutritionProgress(userId, selectedDate)
            loadMealsSummary(userId, selectedDate)
        }, year, month, day)

        datePicker.show()
    }

    private fun updateProgressWithColor(
        bar: ProgressBar,
        value: Int,
        max: Int,
        normalDrawable: Int,
        exceedDrawable: Int
    ) {
        bar.max = max
        bar.progress = value
        bar.progressDrawable = ContextCompat.getDrawable(
            requireContext(),
            if (value <= max) normalDrawable else exceedDrawable
        )
    }

    private fun calculateAndDisplayBmi(height: Int, weight: Int, goal: String) {
        val heightInMeters = height / 100.0
        val bmi = weight / heightInMeters.pow(2)
        val bmiRounded = String.format("%.1f", bmi)

        val bmiCategory = when {
            bmi < 18.5 -> "Дефицит массы"
            bmi < 25 -> "Норма"
            bmi < 30 -> "Избыточный вес"
            else -> "Ожирение"
        }

        val minNormalWeight = (18.5 * heightInMeters.pow(2)).roundToInt()
        val maxNormalWeight = (24.9 * heightInMeters.pow(2)).roundToInt()

        val advice = when {
            bmi < 18.5 && goal == "похудение" ->
                "❌ У вас уже дефицит массы. Цель похудеть может быть вредной. Рекомендуем пересмотреть цель на набор массы."
            bmi < 18.5 && goal == "набор массы" ->
                "✅ У вас дефицит массы, и вы выбрали цель 'набор массы'. Отличное решение!"
            bmi < 18.5 && goal == "поддержание формы" ->
                "⚠️ У вас дефицит массы. Сначала рекомендуется достичь нормального веса."
            bmi < 18.5 && goal == "улучшить здоровье" ->
                "⚠️ У вас дефицит массы. Для здоровья важно набрать массу до нормы."
            bmi in 18.5..24.9 && goal == "похудение" ->
                "⚠️ У вас нормальный вес. Похудение может привести к дефициту."
            bmi in 18.5..24.9 && goal == "набор массы" ->
                "⚠️ Вес в норме. Следите за качеством рациона и активностью."
            bmi in 18.5..24.9 && goal == "поддержание формы" ->
                "✅ Отлично! Поддерживайте форму."
            bmi in 18.5..24.9 && goal == "улучшить здоровье" ->
                "✅ Хороший выбор. Главное — баланс и активность."
            bmi >= 25 && goal == "набор массы" ->
                "❌ У вас избыточный вес. Набор массы не рекомендован."
            bmi >= 25 && goal == "похудение" ->
                "✅ Отлично. Двигайтесь к здоровому весу."
            bmi >= 25 && goal == "поддержание формы" ->
                "⚠️ Рассмотрите снижение веса."
            bmi >= 25 && goal == "улучшить здоровье" ->
                "✅ Правильный выбор — снижение веса и питание."
            else -> "ℹ️ Не удалось дать рекомендацию. Проверьте данные."
        }

        binding.tvUserHeightWeightGoal.text = "Ваш рост: $height см\nВаш вес: $weight кг\nВаша цель: ${goal.replaceFirstChar { it.uppercase() }}"
        binding.tvBmiValue.text = "Ваш ИМТ: $bmiRounded"
        binding.tvBmiStatus.text = "Категория: $bmiCategory"
        binding.tvBmiNormalRange.text = "Нормальный диапазон массы тела: $minNormalWeight–$maxNormalWeight кг"
        binding.tvBmiGoalAdvice.text = "🧠 Рекомендация:\n$advice"

        binding.btnBmiDetails.setOnClickListener {
            val intent = Intent(requireContext(), BmiDetailsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        loadNutritionProgress(userId)
        loadMealsSummary(userId, getTodayDate())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == AppCompatActivity.RESULT_OK) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            loadNutritionProgress(userId)
            loadMealsSummary(userId, getTodayDate())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    private fun getDailyAdvice(
        userId: String,
        height: Int,
        weight: Int,
        age: Int,
        gender: String,
        goal: String,
        activity: String
    ) {
        Log.d("AI_ADVICE", "🔁 getDailyAdvice вызван")

        val today = getTodayDate()
        val firestore = FirebaseFirestore.getInstance()
        val tipsRef = firestore.collection("ai_tips").document(userId)

        val currentProfileHash = generateProfileHash(height, weight, age, gender, goal, activity)

        tipsRef.get().addOnSuccessListener { tipSnapshot ->
            val lastDate = tipSnapshot.getString("lastDate")
            val savedProfileHash = tipSnapshot.getString("profileHash")

            if (lastDate == today && savedProfileHash == currentProfileHash) {
                val cachedTip = tipSnapshot.getString("tip") ?: "Сегодня всё отлично!"
                binding.tvAdviceContent.text = cachedTip
                Log.d("AI_ADVICE", "📦 Показываем кэшированный совет: $cachedTip")
            } else {
                val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                    Calendar.getInstance().apply { add(Calendar.DATE, -1) }.time
                )

                firestore.collection("users").document(userId)
                    .collection("diary").document(yesterday)
                    .get().addOnSuccessListener { diarySnapshot ->
                        val calories = diarySnapshot.getLong("calories") ?: 0
                        val proteins = diarySnapshot.getLong("proteins") ?: 0
                        val fats = diarySnapshot.getLong("fats") ?: 0
                        val carbs = diarySnapshot.getLong("carbs") ?: 0

                        val prompt = """
                        Ты — диетолог. Дай короткий совет (1–2 предложения), опираясь на рацион:
                        Пол: $gender
                        Возраст: $age
                        Рост: $height см
                        Вес: $weight кг
                        Цель: $goal
                        Активность: $activity
                        Вчера:
                        - Калории: $calories
                        - Белки: $proteins г
                        - Жиры: $fats г
                        - Углеводы: $carbs г
                    """.trimIndent()

                        Log.d("AI_ADVICE", "📤 Prompt: $prompt")

                        val request = ChatRequest(
                            model = "gpt-3.5-turbo",
                            messages = listOf(ChatMessage("user", prompt))
                        )


                        RetrofitAdviceInstance.api.sendAdvice(request).enqueue(object : Callback<ChatResponse> {
                            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                                val body = response.body()
                                Log.d("AI_ADVICE", "✅ Ответ: $body")
                                if (body != null && body.choices.isNotEmpty()) {
                                    val aiTip = body.choices.first().message.content
                                    binding.tvAdviceContent.text = aiTip
                                    tipsRef.set(mapOf("lastDate" to today, "tip" to aiTip, "profileHash" to currentProfileHash))
                                } else {
                                    binding.tvAdviceContent.text = "Совет не получен."
                                    Log.d("AI_ADVICE", "⚠️ Пустой ответ от OpenAI")
                                }
                            }

                            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                                Log.d("AI_ADVICE", "❌ Ошибка: ${t.localizedMessage}")
                                binding.tvAdviceContent.text = "Ошибка при получении совета."
                            }
                        })
                    }
                    .addOnFailureListener {
                        Log.d("AI_ADVICE", "📭 Нет вчерашнего рациона — строим совет только по профилю")

                        val prompt = """
                        Ты — ИИ-диетолог. Дай краткий совет по питанию, основываясь только на профиле:
                        Пол: $gender
                        Возраст: $age
                        Рост: $height см
                        Вес: $weight кг
                        Цель: $goal
                        Активность: $activity
                    """.trimIndent()

                        val request = ChatRequest(
                            model = "gpt-3.5-turbo",
                            messages = listOf(ChatMessage("user", prompt))
                        )


                        Log.d("AI_ADVICE", "📤 Prompt без дневника: $prompt")

                        RetrofitAdviceInstance.api.sendAdvice(request).enqueue(object : Callback<ChatResponse> {
                            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                                val body = response.body()
                                Log.d("AI_ADVICE", "✅ Ответ (без дневника): $body")
                                if (body != null && body.choices.isNotEmpty()) {
                                    val aiTip = body.choices.first().message.content
                                    binding.tvAdviceContent.text = aiTip
                                    tipsRef.set(mapOf("lastDate" to today, "tip" to aiTip, "profileHash" to currentProfileHash))
                                } else {
                                    binding.tvAdviceContent.text = "Совет не получен."
                                }
                            }

                            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                                Log.d("AI_ADVICE", "❌ Ошибка (без дневника): ${t.localizedMessage}")
                                binding.tvAdviceContent.text = "Ошибка при получении совета."
                            }
                        })
                    }
            }
        }

    }
    private fun generateProfileHash(
        height: Int,
        weight: Int,
        age: Int,
        gender: String,
        goal: String,
        activity: String
    ): String {
        val data = "$height|$weight|$age|$gender|$goal|$activity"
        return try {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(data.toByteArray(Charsets.UTF_8))
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }





    private fun sendPromptToGpt(prompt: String, callback: (String) -> Unit) {
        Log.d("AI_DEBUG", "▶️ Метод sendPromptToGpt запущен")
        Log.d("AI_DEBUG", "📨 Prompt, отправляемый в OpenAI:\n$prompt")

        val request = ChatRequest(
            model = "gpt-3.5-turbo",
            messages = listOf(ChatMessage("user", prompt))
        )


        RetrofitAdviceInstance.api.sendAdvice(request).enqueue(object : Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                Log.d("AI_DEBUG", "✅ Ответ от OpenAI. Код: ${response.code()}")
                Log.d("AI_DEBUG", "📦 Тело ответа: ${response.body()}")
                Log.d("AI_DEBUG", "❗ Ошибка (если есть): ${response.errorBody()?.string()}")

                val message = response.body()?.choices?.firstOrNull()?.message?.content
                if (!message.isNullOrBlank()) {
                    Log.d("AI_DEBUG", "🎯 Полученный совет: $message")
                    callback(message)
                } else {
                    Log.d("AI_DEBUG", "⚠️ Пустой или недопустимый ответ")
                    callback("Не удалось получить совет.")
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                Log.e("AI_DEBUG", "❌ Ошибка соединения: ${t.message}", t)
                callback("Ошибка соединения с ИИ.")
            }
        })
    }

}






