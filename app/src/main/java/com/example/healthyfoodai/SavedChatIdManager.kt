package com.example.healthyfoodai

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth

object SavedChatIdManager {

    private const val PREFS_NAME = "foodwise_prefs"
    private const val KEY_CHAT_ID_PREFIX = "chat_id"

    // Получаем уникальный ключ с userId, чтобы chatId был индивидуален
    private fun getUserScopedKey(): String {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown_user"
        return "${KEY_CHAT_ID_PREFIX}_$userId"
    }

    fun saveChatId(context: Context, chatId: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = getUserScopedKey()
        prefs.edit().putString(key, chatId).apply()
    }

    fun getChatId(context: Context): String? {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = getUserScopedKey()
        return prefs.getString(key, null)
    }

    fun clearChatId(context: Context) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = getUserScopedKey()
        prefs.edit().remove(key).apply()
    }
}
