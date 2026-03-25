package com.example.myapplication.data

import com.google.gson.JsonObject

fun JsonObject.getString(key: String): String? {
    val value = get(key) ?: return null
    if (value.isJsonNull) return null
    return runCatching { value.asString }.getOrNull()
}

fun JsonObject.getInt(key: String): Int? {
    val value = get(key) ?: return null
    if (value.isJsonNull) return null
    return runCatching { value.asInt }.getOrNull()
}

fun JsonObject.getLong(key: String): Long? {
    val value = get(key) ?: return null
    if (value.isJsonNull) return null
    return runCatching { value.asLong }.getOrNull()
}
