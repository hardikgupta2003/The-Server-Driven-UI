package com.hardik.the_server_driven_ui.sdui.model

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Defensive prop coercion — a malformed or missing key degrades to the
 * default instead of throwing, so a bad payload never crashes the page.
 */
fun Map<String, JsonElement>.str(key: String, default: String? = null): String? =
    runCatching { this[key]?.jsonPrimitive?.contentOrNull }.getOrNull() ?: default

fun Map<String, JsonElement>.strOrEmpty(key: String, default: String = ""): String =
    str(key) ?: default

fun Map<String, JsonElement>.intOrNull(key: String): Int? =
    runCatching { this[key]?.jsonPrimitive?.content?.toInt() }.getOrNull()

fun Map<String, JsonElement>.intOr(key: String, default: Int): Int = intOrNull(key) ?: default

fun Map<String, JsonElement>.boolOr(key: String, default: Boolean): Boolean =
    runCatching { this[key]?.jsonPrimitive?.content?.toBoolean() }.getOrNull() ?: default

fun Map<String, JsonElement>.objList(key: String): List<JsonObject> =
    runCatching { this[key]?.jsonArray?.mapNotNull { it as? JsonObject } }.getOrNull() ?: emptyList()

fun Map<String, JsonElement>.strList(key: String): List<String> =
    runCatching {
        this[key]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }
    }.getOrNull() ?: emptyList()

fun JsonObject.str(key: String, default: String = ""): String =
    runCatching { this[key]?.jsonPrimitive?.contentOrNull }.getOrNull() ?: default

fun JsonObject.strOrNull(key: String): String? =
    runCatching { this[key]?.jsonPrimitive?.contentOrNull }.getOrNull()
