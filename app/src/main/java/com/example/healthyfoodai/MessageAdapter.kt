package com.example.healthyfoodai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(private val messages: List<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_USER = 0
        private const val TYPE_BOT = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].role == "user") TYPE_USER else TYPE_BOT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout = if (viewType == TYPE_USER)
            R.layout.item_user_message
        else
            R.layout.item_ai_message

        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as MessageViewHolder).bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.textMessage)
        private val btnAddToDiary: Button? = itemView.findViewById(R.id.btnAddToDiary)

        fun bind(message: Message) {
            tvMessage.text = message.content

            if (message.role == "assistant" && isReadableRecipe(message.content)) {
                btnAddToDiary?.visibility = View.VISIBLE
                btnAddToDiary?.setOnClickListener {
                    val meal = parseMealFromText(message.content)
                    if (meal != null) {
                        saveMealToFirestore(meal)
                    } else {
                        showCustomToast("❌ Ошибка в структуре рецепта.")
                    }
                }
            } else {
                btnAddToDiary?.visibility = View.GONE
            }
        }

        private fun isReadableRecipe(content: String): Boolean {
            return content.contains("Название:", ignoreCase = true) &&
                    content.contains("Калории:", ignoreCase = true) &&
                    content.contains("Белки:", ignoreCase = true) &&
                    content.contains("Жиры:", ignoreCase = true) &&
                    content.contains("Углеводы:", ignoreCase = true) &&
                    content.contains("Ингредиенты:", ignoreCase = true) &&
                    content.contains("Приготовление:", ignoreCase = true)
        }

        private fun parseMealFromText(content: String): Meal? {
            return try {
                val name = Regex("Название: (.+)").find(content)?.groupValues?.get(1)?.trim() ?: return null
                val calories = Regex("Калории: (\\d+)").find(content)?.groupValues?.get(1)?.toIntOrNull() ?: return null
                val proteins = Regex("Белки: (\\d+)").find(content)?.groupValues?.get(1)?.toIntOrNull() ?: return null
                val fats = Regex("Жиры: (\\d+)").find(content)?.groupValues?.get(1)?.toIntOrNull() ?: return null
                val carbs = Regex("Углеводы: (\\d+)").find(content)?.groupValues?.get(1)?.toIntOrNull() ?: return null

                Meal(name, calories, proteins, fats, carbs)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        private fun saveMealToFirestore(meal: Meal) {
            val user = FirebaseAuth.getInstance().currentUser ?: return
            val uid = user.uid
            val db = FirebaseFirestore.getInstance()

            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val mealRef = db.collection("users").document(uid)
                .collection("diary").document(date)
                .collection("meals").document()

            val mealData = hashMapOf(
                "title" to meal.name,
                "calories" to meal.calories,
                "proteins" to meal.proteins,
                "fats" to meal.fats,
                "carbs" to meal.carbs,
                "timestamp" to System.currentTimeMillis()
            )

            mealRef.set(mealData)

            val dayRef = db.collection("users").document(uid)
                .collection("diary").document(date)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(dayRef)
                val currentCalories = snapshot.getLong("calories") ?: 0
                val currentProteins = snapshot.getLong("proteins") ?: 0
                val currentFats = snapshot.getLong("fats") ?: 0
                val currentCarbs = snapshot.getLong("carbs") ?: 0

                transaction.set(dayRef, mapOf(
                    "calories" to (currentCalories + meal.calories),
                    "proteins" to (currentProteins + meal.proteins),
                    "fats" to (currentFats + meal.fats),
                    "carbs" to (currentCarbs + meal.carbs)
                ), com.google.firebase.firestore.SetOptions.merge())
            }

            DiaryViewModel().notifyMealAdded()
            showCustomToast("✅ ${meal.name} добавлен в дневник!")
        }

        private fun showCustomToast(message: String) {
            val inflater = LayoutInflater.from(itemView.context)
            val layout = inflater.inflate(R.layout.custom_toast_layout, null)

            val toastText = layout.findViewById<TextView>(R.id.toast_text)
            if (toastText == null) {
                Toast.makeText(itemView.context, message, Toast.LENGTH_SHORT).show()
                return
            }

            toastText.text = message

            val toast = Toast(itemView.context)
            toast.duration = Toast.LENGTH_SHORT
            toast.view = layout
            toast.show()
        }
    }
}
