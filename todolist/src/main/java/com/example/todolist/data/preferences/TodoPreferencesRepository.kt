package com.example.todolist.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val TODO_PREFS = "todo_prefs"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = TODO_PREFS)

class TodoPreferencesRepository(private val context: Context) {
    private companion object {
        val DONE_COLOR_ENABLED = booleanPreferencesKey("done_color_enabled")
        val JSON_IMPORTED = booleanPreferencesKey("json_imported")
    }

    val doneColorEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[DONE_COLOR_ENABLED] ?: true
    }

    suspend fun setDoneColorEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DONE_COLOR_ENABLED] = enabled
        }
    }

    val isJsonImported: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[JSON_IMPORTED] ?: false
    }

    suspend fun setJsonImported() {
        context.dataStore.edit { prefs ->
            prefs[JSON_IMPORTED] = true
        }
    }
}
