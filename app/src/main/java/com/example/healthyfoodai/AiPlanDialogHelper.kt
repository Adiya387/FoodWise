package com.example.healthyfoodai

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object AiPlanDialogHelper {

    fun checkAndShowPlanDialogIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        val shouldShowDialog = prefs.getBoolean("show_ai_plan_update_dialog", false)

        if (shouldShowDialog) {
            AlertDialog.Builder(context)
                .setTitle("Обновить AI-план?")
                .setMessage("Вы изменили профиль. Хотите обновить AI-план питания на неделю?")
                .setPositiveButton("Да") { _, _ ->
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton
                    FirebaseFirestore.getInstance()
                        .collection("weekly_plan")
                        .document(userId)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(context, "План будет обновлён при следующем входе", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Нет", null)
                .setCancelable(false)
                .show()

            prefs.edit().putBoolean("show_ai_plan_update_dialog", false).apply()
        }
    }
}
