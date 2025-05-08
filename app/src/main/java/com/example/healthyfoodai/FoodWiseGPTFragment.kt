package com.example.healthyfoodai

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthyfoodai.api.RetrofitOpenAiInstance
import com.example.healthyfoodai.databinding.FragmentFoodwiseGptBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import android.content.Context
import androidx.appcompat.app.AlertDialog



class FoodWiseGPTFragment : Fragment() {

    private var _binding: FragmentFoodwiseGptBinding? = null
    private val binding get() = _binding!!

    private lateinit var messageAdapter: MessageAdapter
    private val messageHistory = mutableListOf<Message>()

    private lateinit var firestore: FirebaseFirestore
    private var chatId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VOICE_INPUT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.get(0) ?: ""
            binding.etUserQuestion.setText(spokenText)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoodwiseGptBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        chatId = SavedChatIdManager.getChatId(requireContext()) ?: UUID.randomUUID().toString()
        SavedChatIdManager.saveChatId(requireContext(), chatId)
        setupUI()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbarGpt)
        (requireActivity() as? MainActivity)?.setToolbarTitle("FoodWise GPT-S")
        loadMessagesFromFirestore()

        // ✅ Проверка: нужно ли показать диалог обновления AI-плана
        val prefs = requireContext().getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        val shouldShowDialog = prefs.getBoolean("show_ai_plan_update_dialog", false)

        if (shouldShowDialog) {
            AlertDialog.Builder(requireContext())
                .setTitle("Обновить AI-план?")
                .setMessage("Вы изменили профиль. Хотите обновить AI-план питания на неделю?")
                .setPositiveButton("Да") { _, _ ->
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton
                    FirebaseFirestore.getInstance()
                        .collection("weekly_plan")
                        .document(userId)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "План будет обновлён при следующем входе", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Нет", null)
                .setCancelable(false)
                .show()

            // Сброс флага
            prefs.edit().putBoolean("show_ai_plan_update_dialog", false).apply()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.gpt_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_chat_history -> {
                (activity as? MainActivity)?.loadFragment(ChatListFragment(), "История чатов")
                true
            }
            R.id.action_new_chat -> {
                startNewChat()
                true
            }
            R.id.action_clear_chat -> {
                clearCurrentChat()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupUI() {
        binding.tvDescriptionGpt.text = "Ваш личный помощник по питанию и рецептам. Спросите совета у ИИ!"

        messageAdapter = MessageAdapter(messageHistory)
        binding.recyclerViewMessages.apply {
            adapter = messageAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.btnSendQuestion.setOnClickListener {
            val userInput = binding.etUserQuestion.text.toString().trim()
            if (userInput.isNotEmpty()) {
                sendMessage(userInput)
                binding.etUserQuestion.text.clear()
            } else {
                Toast.makeText(requireContext(), "Введите сообщение", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnVoiceInput.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Говорите...")
            }

            try {
                startActivityForResult(intent, VOICE_INPUT_REQUEST_CODE)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка голосового ввода: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendMessage(userInput: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userProfile = UserManagerr.getUserProfile(requireContext())
                val allergies = userProfile.allergies
                // Словарь групп продуктов
                val productGroups = mapOf(
                    "молочные продукты" to listOf("молоко", "сыр", "творог", "йогурт", "кефир", "сливки", "сметана", "масло"),
                    "овощи" to listOf("капуста", "морковь", "помидор", "огурец", "лук", "чеснок", "баклажан", "перец"),
                    "фрукты" to listOf("яблоко", "банан", "апельсин", "груша", "виноград", "киви", "ананас"),
                    "мясо" to listOf("курица", "говядина", "свинина", "баранина", "индейка", "печень", "колбаса"),
                    "рыба" to listOf("лосось", "тунец", "треска", "сёмга", "хек", "креветки", "мидии"),
                    "мука" to listOf("пшеничная мука", "ржаная мука", "овсяная мука", "хлеб", "булочки", "лепёшки", "блины", "лаваш", "пицца", "макароны", "панировка")
                )

                val forbiddenProductsExpanded = mutableSetOf<String>()

                for (allergy in allergies) {
                    val normalized = allergy.lowercase()
                    if (productGroups.containsKey(normalized)) {
                        forbiddenProductsExpanded.addAll(productGroups[normalized]!!)
                    } else {
                        forbiddenProductsExpanded.add(normalized)
                    }
                }


                // Формируем строку с ограничениями
                val forbiddenProducts = allergies.joinToString(", ").lowercase()

                // Проверка на запрещённые продукты в запросе пользователя
                val userInputLowerCase = userInput.lowercase()
                val forbiddenProductInInput = forbiddenProductsExpanded.find { userInputLowerCase.contains(it) }


                val warningMessage: String? = forbiddenProductsExpanded.find { userInputLowerCase.contains(it) }?.let { matched ->
                    "⚠️ Внимание: запрос пользователя содержит продукт \"$matched\", который входит в список запрещённых. Не предлагай рецепты с этим продуктом. Предложи альтернативу."
                }


                // Создание системного сообщения для GPT с учётом ограничений
                val systemMessage = Message(
                    role = "system",
                    content = """
                    Ты — FoodWise GPT, умный ИИ-ассистент по питанию, диете, рецептам, здоровью и физической активности.
                    
                    ✅ Ты всегда дружелюбный, краткий и полезный. Вот как ты действуешь:
                    
                    1. Если пользователь просто поздоровался («Привет», «Доброе утро») — ответь дружелюбно и предложи помощь по питанию.
                    2. Если он говорит: «Напиши рецепт» — спроси: «Какой именно рецепт вас интересует?».
                    3. Если он просит конкретный рецепт — напиши в читаемом формате (пример ниже).
                    4. Если запрос не про питание, здоровье или спорт — вежливо объясни, что ты работаешь только по этим темам.
                    5. Если пользователь просит продукт из запрещённого — предупреди и предложи альтернативу.
                    
                    📌 Формат рецепта (ВСЕГДА без JSON, на русском языке):
                    
                    🍽 Вот рецепт, основанный на твоей цели и ограничениях:
                    
                    Название: Название рецепта  
                    Калории: 280 ккал  
                    Белки: 12 г, Жиры: 10 г, Углеводы: 32 г
                    
                    Ингредиенты:
                    - Ингредиент 1
                    - Ингредиент 2
                    - ...
                    
                    Приготовление:
                    1. Шаг 1
                    2. Шаг 2
                    
                    Приятного аппетита! 😋
                    
                    ❌ Не используй в рецептах: ${forbiddenProductsExpanded.joinToString(", ")}
                    📌 Учитывай цель пользователя: ${userProfile.goal}
                """.trimIndent()
                )

                // Создание сообщения пользователя
                val userMessage = Message(role = "user", content = userInput)

                withContext(Dispatchers.IO) {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@withContext

                    val chatMeta = mapOf(
                        "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                        "title" to "Чат от ${SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()).format(Date())}"
                    )

                    val chatDocRef = firestore.collection("chat_history")
                        .document(userId)
                        .collection("chatDocuments")
                        .document(chatId)

                    val chatSnapshot = chatDocRef.get().await()
                    if (!chatSnapshot.exists()) {
                        chatDocRef.set(chatMeta).await()
                    }

                    // сохраняем сообщение
                    saveMessageToFirestore(userMessage)
                }

                withContext(Dispatchers.Main) {
                    messageHistory.add(userMessage)
                    messageAdapter.notifyItemInserted(messageHistory.size - 1)
                    binding.recyclerViewMessages.scrollToPosition(messageHistory.size - 1)

                    messageHistory.add(Message(role = "assistant", content = "✍️ FoodWise GPT печатает..."))
                    messageAdapter.notifyItemInserted(messageHistory.size - 1)
                    binding.recyclerViewMessages.scrollToPosition(messageHistory.size - 1)
                }


                // Преобразование Message → ChatMessage
                val messagesToSend = mutableListOf<ChatMessage>()

                messagesToSend.add(ChatMessage("system", systemMessage.content))

                if (warningMessage != null) {
                    messagesToSend.add(ChatMessage("system", warningMessage))
                }

                messagesToSend.addAll(messageHistory.takeLast(4).map {
                    ChatMessage(it.role, it.content)
                })


                val request = ChatRequest(
                    model = "gpt-3.5-turbo",
                    messages = messagesToSend
                )

                RetrofitOpenAiInstance.api.sendMessage(request).enqueue(object : retrofit2.Callback<ChatResponse> {
                    override fun onResponse(
                        call: retrofit2.Call<ChatResponse>,
                        response: retrofit2.Response<ChatResponse>
                    ) {
                        if (response.isSuccessful) {
                            val reply = response.body()?.choices?.firstOrNull()?.message?.content ?: "Нет ответа от ИИ"

                            CoroutineScope(Dispatchers.Main).launch {
                                if (messageHistory.isNotEmpty() && messageHistory.last().content.contains("печатает")) {
                                    messageHistory.removeAt(messageHistory.size - 1)
                                    messageAdapter.notifyItemRemoved(messageHistory.size)
                                }

                                val aiMessage = Message(role = "assistant", content = reply)
                                messageHistory.add(aiMessage)
                                saveMessageToFirestore(aiMessage)
                                messageAdapter.notifyItemInserted(messageHistory.size - 1)
                                binding.recyclerViewMessages.scrollToPosition(messageHistory.size - 1)
                            }
                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(requireContext(), "Ошибка от GPT: ${response.message()}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<ChatResponse>, t: Throwable) {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(requireContext(), "Ошибка: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                })

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun saveMessageToFirestore(message: Message) {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return

        val data = hashMapOf(
            "role" to message.role,
            "content" to message.content,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("chat_history")
            .document(userId)
            .collection("chatDocuments")
            .document(chatId)
            .collection("messages")
            .add(data)
    }


    private fun loadMessagesFromFirestore() {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("chat_history")
            .document(userId)
            .collection("chatDocuments")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { result ->
                messageHistory.clear()
                result.forEach {
                    val role = it.getString("role") ?: "assistant"
                    val content = it.getString("content") ?: ""
                    messageHistory.add(Message(role, content))
                }
                messageAdapter.notifyDataSetChanged()
                binding.recyclerViewMessages.scrollToPosition(messageHistory.size - 1)
            }
    }


    private fun startNewChat() {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return

        chatId = UUID.randomUUID().toString()
        SavedChatIdManager.saveChatId(requireContext(), chatId)

        val chatMeta = mapOf(
            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
            "title" to "Чат от ${SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()).format(Date())}"
        )

        firestore.collection("chat_history")
            .document(userId)
            .collection("chatDocuments")
            .document(chatId)
            .set(chatMeta)
            .addOnSuccessListener {
                messageHistory.clear()
                messageAdapter.notifyDataSetChanged()
                Toast.makeText(requireContext(), "Новый чат создан", Toast.LENGTH_SHORT).show()
            }
    }


    private fun clearCurrentChat() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("chat_history")
            .document(userId)
            .collection("chatDocuments")
            .document(chatId)
            .collection("messages")
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                snapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit().addOnSuccessListener {
                    messageHistory.clear()
                    messageAdapter.notifyDataSetChanged()
                    Toast.makeText(requireContext(), "Чат очищен", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Ошибка при очистке чата", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val VOICE_INPUT_REQUEST_CODE = 1001
    }
}
