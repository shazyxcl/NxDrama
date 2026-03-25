package com.example.myapplication.data

import com.google.gson.JsonElement
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

fun JsonElement.getAsJsonObject(key: String): JsonObject? {
    val element = this.asJsonObject.get(key)
    return if (element != null && element.isJsonObject) element.asJsonObject else null
}

fun JsonElement.getAsJsonArray(key: String): List<JsonElement>? {
    val element = this.asJsonObject.get(key)
    return if (element != null && element.isJsonArray) element.asJsonArray.map { it } else null
}