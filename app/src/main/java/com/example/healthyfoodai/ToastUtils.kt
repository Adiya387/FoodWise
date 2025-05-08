package com.example.healthyfoodai

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast

fun showCustomToast(context: Context, message: String) {
    val inflater = LayoutInflater.from(context)
    val view: View = inflater.inflate(R.layout.custom_toast, null)
    val textView: TextView = view.findViewById(R.id.toast_text)
    textView.text = message

    val toast = Toast(context.applicationContext)
    toast.duration = Toast.LENGTH_SHORT
    toast.view = view
    toast.show()
}
