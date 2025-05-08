package com.example.healthyfoodai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthyfoodai.databinding.FragmentChatListBinding
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatListFragment : Fragment() {

    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatAdapter: ChatListAdapter
    private lateinit var firestore: FirebaseFirestore
    private val chatList = mutableListOf<Pair<String, String>>() // (chatId, title)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        setupRecyclerView()
        loadChats()
        (activity as? MainActivity)?.setToolbarTitle("История чатов")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Обработка кнопки "Назад"
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            (activity as? MainActivity)?.loadFragment(FoodWiseGPTFragment(), "FoodWise GPT-S")
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatListAdapter(chatList) { chatId ->
            // Сохраняем выбранный chatId и переходим в GPT-фрагмент
            SavedChatIdManager.saveChatId(requireContext(), chatId)
            (activity as? MainActivity)?.loadFragment(FoodWiseGPTFragment(), "FoodWise GPT-S")
        }
        binding.recyclerViewChats.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun loadChats() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("chat_history")
            .document(userId)
            .collection("chatDocuments")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                chatList.clear()

                val docs = snapshot.documents
                val tasks = docs.map { doc ->
                    val chatId = doc.id
                    val title = doc.getString("title") ?: "Чат: $chatId"

                    // Проверяем, есть ли сообщения в этом чате
                    firestore.collection("chat_history")
                        .document(userId)
                        .collection("chatDocuments")
                        .document(chatId)
                        .collection("messages")
                        .limit(1)
                        .get()
                        .continueWith { task ->
                            if (task.isSuccessful && !task.result.isEmpty) {
                                chatList.add(chatId to title)
                            }
                        }
                }

                Tasks.whenAllComplete(tasks).addOnSuccessListener {
                    chatAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Ошибка загрузки истории чатов", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
