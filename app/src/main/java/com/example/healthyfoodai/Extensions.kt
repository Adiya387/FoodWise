package com.example.healthyfoodai

import org.json.JSONArray

fun JSONArray.toListString(): List<String> {
    val list = mutableListOf<String>()
    for (i in 0 until length()) {
        list.add(getString(i))
    }
    return list
}
