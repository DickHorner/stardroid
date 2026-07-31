package com.google.android.stardroid.observing

import android.content.SharedPreferences
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObservationListStore @Inject constructor(
    private val sharedPreferences: SharedPreferences,
) {
    enum class AddResult {
        ADDED,
        ALREADY_PRESENT,
        INVALID_NAME,
        LIST_FULL,
    }

    fun entries(): List<String> = readEntries()

    fun add(name: String?): AddResult {
        val normalizedName = name?.trim()?.take(MAX_NAME_LENGTH).orEmpty()
        if (normalizedName.isEmpty()) {
            return AddResult.INVALID_NAME
        }

        val entries = readEntries().toMutableList()
        val normalizedKey = normalizedName.lowercase(Locale.ROOT)
        if (entries.any { it.lowercase(Locale.ROOT) == normalizedKey }) {
            return AddResult.ALREADY_PRESENT
        }
        if (entries.size >= MAX_ENTRIES) {
            return AddResult.LIST_FULL
        }

        entries.add(normalizedName)
        writeEntries(entries)
        return AddResult.ADDED
    }

    fun clear() {
        sharedPreferences.edit().remove(PREFERENCE_KEY).apply()
    }

    private fun readEntries(): List<String> {
        val rawEntries = sharedPreferences.getString(PREFERENCE_KEY, null) ?: return emptyList()
        return try {
            val array = JSONArray(rawEntries)
            val entries = ArrayList<String>(array.length())
            val seen = HashSet<String>()
            for (index in 0 until array.length()) {
                val name = array.optString(index).trim().take(MAX_NAME_LENGTH)
                val key = name.lowercase(Locale.ROOT)
                if (name.isNotEmpty() && seen.add(key)) {
                    entries.add(name)
                }
                if (entries.size == MAX_ENTRIES) {
                    break
                }
            }
            entries
        } catch (exception: JSONException) {
            Log.w(TAG, "Ignoring malformed observation list", exception)
            emptyList()
        }
    }

    private fun writeEntries(entries: List<String>) {
        val array = JSONArray()
        entries.forEach(array::put)
        sharedPreferences.edit().putString(PREFERENCE_KEY, array.toString()).apply()
    }

    companion object {
        private const val TAG = "ObservationListStore"
        private const val PREFERENCE_KEY = "observation_list_json"
        private const val MAX_ENTRIES = 200
        private const val MAX_NAME_LENGTH = 120
    }
}
