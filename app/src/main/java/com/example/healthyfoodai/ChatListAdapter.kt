package com.example.healthyfoodai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatListAdapter(
    private val chats: MutableList<Pair<String, String>>, // (chatId, title)
    private val onChatSelected: (String) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvChatTitle: TextView = view.findViewById(R.id.tvChatId)
        val btnMore: ImageButton = view.findViewById(R.id.btnMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val (chatId, title) = chats[position]
        holder.tvChatTitle.text = title

        holder.itemView.setOnClickListener {
            onChatSelected(chatId)
        }

        holder.btnMore.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.menu_chat_item, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_delete_chat -> {
                        deleteChatCompletely(holder, chatId, position)
                        true
                    }
                    R.id.menu_rename_chat -> {
                        renameChat(view, chatId, position)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun deleteChatCompletely(holder: ChatViewHolder, chatId: String, position: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()

        val messagesRef = firestore.collection("chat_history")
            .document(userId)
            .collection("chatDocuments")
            .document(chatId)
            .collection("messages")

        messagesRef.get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                snapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }

                // Удаляем документ самого чата
                val chatDocRef = firestore.collection("chat_history")
                    .document(userId)
                    .collection("chatDocuments")
                    .document(chatId)

                batch.delete(chatDocRef)

                batch.commit().addOnSuccessListener {
                    chats.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, chats.size)
                    Toast.makeText(holder.itemView.context, "Чат удалён", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(holder.itemView.context, "Ошибка удаления чата", Toast.LENGTH_SHORT).show()
            }
    }

    private fun renameChat(view: View, chatId: String, position: Int) {
        val context = view.context
        val editText = EditText(context)
        editText.hint = "Новое имя чата"

        AlertDialog.Builder(context)
            .setTitle("Переименовать чат")
            .setView(editText)
            .setPositiveButton("Сохранить") { _, _ ->
                val newTitle = editText.text.toString().trim()
                if (newTitle.isNotEmpty()) {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton
                    FirebaseFirestore.getInstance()
                        .collection("chat_history")
                        .document(userId)
                        .collection("chatDocuments")
                        .document(chatId)
                        .update("title", newTitle)
                        .addOnSuccessListener {
                            chats[position] = chatId to newTitle
                            notifyItemChanged(position)
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Ошибка переименования", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun getItemCount(): Int = chats.size
}
