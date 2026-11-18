package com.charlenferreira.prism.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.charlenferreira.prism.ui.theme.PrismTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "prism_settings")

enum class LiquidGlassPreset(val displayName: String) {
    TRANSPARENT("Transparente"),
    FROSTED("Fosco"),
    CRYSTAL("Cristal"),
    LIGHT("Leve")
}

data class LiquidGlassConfig(
    val preset: LiquidGlassPreset = LiquidGlassPreset.FROSTED,
    val frostDp: Float = 14f,
    val lensDepthDp: Float = 14f,
    val lensStrengthDp: Float = 24f,
    val tintAlpha: Float = 0.38f,
    val rimLightAlpha: Float = 0.85f,
    val depthShadow: Boolean = true,
    val prismEdge: Boolean = false
)

class SettingsRepository(private val context: Context) {

    companion object {
        const val MIN_COLUMNS = 3
        const val MAX_COLUMNS = 5
        const val DEFAULT_GRID_COLUMNS = 3

        private val KEY_THEME = stringPreferencesKey("selected_theme")
        private val KEY_GRID_COLUMNS = intPreferencesKey("grid_columns")
        private val KEY_GLASS_PRESET = stringPreferencesKey("glass_preset")
        private val KEY_GLASS_FROST = floatPreferencesKey("glass_frost_dp")
        private val KEY_GLASS_LENS_DEPTH = floatPreferencesKey("glass_lens_depth_dp")
        private val KEY_GLASS_LENS_STRENGTH = floatPreferencesKey("glass_lens_strength_dp")
        private val KEY_GLASS_TINT = floatPreferencesKey("glass_tint_alpha")
        private val KEY_GLASS_RIM = floatPreferencesKey("glass_rim_alpha")
        private val KEY_GLASS_DEPTH = booleanPreferencesKey("glass_depth_shadow")
        private val KEY_GLASS_PRISM_EDGE = booleanPreferencesKey("glass_prism_edge")
    }

    val themeFlow: Flow<PrismTheme> = context.settingsDataStore.data.map { prefs ->
        val raw = prefs[KEY_THEME]
        PrismTheme.entries.firstOrNull { it.name == raw } ?: PrismTheme.WHITE
    }

    val gridColumnsFlow: Flow<Int> = context.settingsDataStore.data.map { prefs ->
        (prefs[KEY_GRID_COLUMNS] ?: DEFAULT_GRID_COLUMNS).coerceIn(MIN_COLUMNS, MAX_COLUMNS)
    }

    val glassConfigFlow: Flow<LiquidGlassConfig> = context.settingsDataStore.data.map { prefs ->
        val presetName = prefs[KEY_GLASS_PRESET]
        val preset = LiquidGlassPreset.entries.firstOrNull { it.name == presetName }
            ?: LiquidGlassPreset.FROSTED
        LiquidGlassConfig(
            preset = preset,
            frostDp = prefs[KEY_GLASS_FROST] ?: 14f,
            lensDepthDp = prefs[KEY_GLASS_LENS_DEPTH] ?: 14f,
            lensStrengthDp = prefs[KEY_GLASS_LENS_STRENGTH] ?: 24f,
            tintAlpha = prefs[KEY_GLASS_TINT] ?: 0.38f,
            rimLightAlpha = prefs[KEY_GLASS_RIM] ?: 0.85f,
            depthShadow = prefs[KEY_GLASS_DEPTH] ?: true,
            prismEdge = prefs[KEY_GLASS_PRISM_EDGE] ?: false
        )
    }

    suspend fun setTheme(theme: PrismTheme) {
        context.settingsDataStore.edit { prefs ->
            prefs[KEY_THEME] = theme.name
        }
    }

    suspend fun setGridColumns(columns: Int) {
        context.settingsDataStore.edit { prefs ->
            prefs[KEY_GRID_COLUMNS] = columns.coerceIn(MIN_COLUMNS, MAX_COLUMNS)
        }
    }

    suspend fun applyGlassPreset(preset: LiquidGlassPreset) {
        val config = when (preset) {
            LiquidGlassPreset.TRANSPARENT -> LiquidGlassConfig(
                preset = preset,
                frostDp = 6f,
                lensDepthDp = 16f,
                lensStrengthDp = 28f,
                tintAlpha = 0.22f,
                rimLightAlpha = 0.95f,
                depthShadow = true,
                prismEdge = false
            )
            LiquidGlassPreset.FROSTED -> LiquidGlassConfig(
                preset = preset,
                frostDp = 14f,
                lensDepthDp = 14f,
                lensStrengthDp = 24f,
                tintAlpha = 0.38f,
                rimLightAlpha = 0.85f,
                depthShadow = true,
                prismEdge = false
            )
            LiquidGlassPreset.CRYSTAL -> LiquidGlassConfig(
                preset = preset,
                frostDp = 10f,
                lensDepthDp = 18f,
                lensStrengthDp = 32f,
                tintAlpha = 0.28f,
                rimLightAlpha = 1.0f,
                depthShadow = true,
                prismEdge = true
            )
            LiquidGlassPreset.LIGHT -> LiquidGlassConfig(
                preset = preset,
                frostDp = 8f,
                lensDepthDp = 10f,
                lensStrengthDp = 16f,
                tintAlpha = 0.45f,
                rimLightAlpha = 0.65f,
                depthShadow = false,
                prismEdge = false
            )
        }
        updateGlassConfig(config)
    }

    suspend fun updateGlassConfig(config: LiquidGlassConfig) {
        context.settingsDataStore.edit { prefs ->
            prefs[KEY_GLASS_PRESET] = config.preset.name
            prefs[KEY_GLASS_FROST] = config.frostDp
            prefs[KEY_GLASS_LENS_DEPTH] = config.lensDepthDp
            prefs[KEY_GLASS_LENS_STRENGTH] = config.lensStrengthDp
            prefs[KEY_GLASS_TINT] = config.tintAlpha
            prefs[KEY_GLASS_RIM] = config.rimLightAlpha
            prefs[KEY_GLASS_DEPTH] = config.depthShadow
            prefs[KEY_GLASS_PRISM_EDGE] = config.prismEdge
        }
    }
}
