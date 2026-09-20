package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.ChatMessage
import com.example.model.ShenModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "shen_zero_preferences")

class ChatRepository(private val context: Context) {

    companion object {
        private val KEY_MESSAGES = stringPreferencesKey("saved_chat_messages")
        private val KEY_SELECTED_MODEL = stringPreferencesKey("selected_shen_model")
        private val KEY_ONBOARDING_SEEN = booleanPreferencesKey("onboarding_completed")
    }

    val messagesFlow: Flow<List<ChatMessage>> = context.dataStore.data.map { preferences ->
        val rawJson = preferences[KEY_MESSAGES] ?: return@map emptyList()
        parseMessages(rawJson)
    }

    val selectedModelFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_MODEL] ?: ShenModel.ALPHA.id
    }

    val onboardingSeenFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_SEEN] ?: false
    }

    suspend fun saveMessages(messages: List<ChatMessage>) {
        val jsonArray = JSONArray()
        for (msg in messages) {
            val obj = JSONObject().apply {
                put("id", msg.id)
                put("text", msg.text)
                put("isUser", msg.isUser)
                put("timestamp", msg.timestamp)
                put("model", msg.model)
                put("isError", msg.isError)
            }
            jsonArray.put(obj)
        }
        context.dataStore.edit { preferences ->
            preferences[KEY_MESSAGES] = jsonArray.toString()
        }
    }

    suspend fun saveSelectedModel(modelId: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SELECTED_MODEL] = modelId
        }
    }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_SEEN] = true
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_MESSAGES)
        }
    }

    private fun parseMessages(json: String): List<ChatMessage> {
        val result = mutableListOf<ChatMessage>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                result.add(
                    ChatMessage(
                        id = item.optString("id"),
                        text = item.optString("text"),
                        isUser = item.optBoolean("isUser"),
                        timestamp = item.optLong("timestamp"),
                        model = item.optString("model", "SHΞN™ Alpha"),
                        isError = item.optBoolean("isError", false)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}
