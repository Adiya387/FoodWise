package com.example.healthyfoodai

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast

object CustomToast {

    fun showCustomToast(context: Context, message: String) {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.custom_toast_layout, null)

        val tvToastMessage = layout.findViewById<TextView>(R.id.tvToastMessage)
        tvToastMessage.text = message

        Toast(context).apply {
            duration = Toast.LENGTH_SHORT
            view = layout
            show()
        }
    }
}
