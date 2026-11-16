package com.charlenferreira.prism.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.charlenferreira.prism.ui.theme.PrismTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.prismDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "prism_settings"
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("selected_theme")
        val GRID_COLUMNS = intPreferencesKey("grid_columns")
    }

    val themeFlow: Flow<PrismTheme> = context.prismDataStore.data.map { preferences ->
        val rawName = preferences[Keys.THEME]
        PrismTheme.entries.firstOrNull { it.name == rawName } ?: PrismTheme.DEEP_TECH_BLUE
    }

    val gridColumnsFlow: Flow<Int> = context.prismDataStore.data.map { preferences ->
        (preferences[Keys.GRID_COLUMNS] ?: DEFAULT_GRID_COLUMNS).coerceIn(MIN_COLUMNS, MAX_COLUMNS)
    }

    suspend fun setTheme(theme: PrismTheme) {
        context.prismDataStore.edit { preferences ->
            preferences[Keys.THEME] = theme.name
        }
    }

    suspend fun setGridColumns(columns: Int) {
        context.prismDataStore.edit { preferences ->
            preferences[Keys.GRID_COLUMNS] = columns.coerceIn(MIN_COLUMNS, MAX_COLUMNS)
        }
    }

    companion object {
        const val MIN_COLUMNS = 3
        const val MAX_COLUMNS = 5
        const val DEFAULT_GRID_COLUMNS = 3
    }
}
