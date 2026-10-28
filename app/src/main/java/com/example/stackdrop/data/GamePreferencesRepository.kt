package com.example.stackdrop.data

import android.content.Context
import android.content.SharedPreferences
import com.example.stackdrop.model.AppTheme
import com.example.stackdrop.model.GameStatistics
import com.example.stackdrop.model.StackDropResult
import com.example.stackdrop.model.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GamePreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private val _statistics = MutableStateFlow(loadStatistics())
    val statistics: StateFlow<GameStatistics> = _statistics.asStateFlow()

    private fun loadSettings(): UserSettings {
        val themeOrdinal = prefs.getInt(KEY_THEME, AppTheme.SYSTEM.ordinal)
        val theme = AppTheme.entries.getOrElse(themeOrdinal) { AppTheme.SYSTEM }
        val soundEnabled = prefs.getBoolean(KEY_SOUND, true)
        val hapticsEnabled = prefs.getBoolean(KEY_HAPTICS, true)
        val reducedMotion = prefs.getBoolean(KEY_REDUCED_MOTION, false)
        return UserSettings(
            theme = theme,
            soundEnabled = soundEnabled,
            hapticsEnabled = hapticsEnabled,
            reducedMotion = reducedMotion
        )
    }

    private fun loadStatistics(): GameStatistics {
        return GameStatistics(
            gamesPlayed = prefs.getInt(KEY_GAMES_PLAYED, 0),
            bestHeight = prefs.getInt(KEY_BEST_HEIGHT, 0),
            bestScore = prefs.getInt(KEY_BEST_SCORE, 0),
            totalBlocksDropped = prefs.getInt(KEY_TOTAL_BLOCKS, 0),
            perfectDrops = prefs.getInt(KEY_PERFECT_DROPS, 0),
            bestPerfectStreak = prefs.getInt(KEY_BEST_STREAK, 0)
        )
    }

    fun updateSettings(newSettings: UserSettings) {
        prefs.edit()
            .putInt(KEY_THEME, newSettings.theme.ordinal)
            .putBoolean(KEY_SOUND, newSettings.soundEnabled)
            .putBoolean(KEY_HAPTICS, newSettings.hapticsEnabled)
            .putBoolean(KEY_REDUCED_MOTION, newSettings.reducedMotion)
            .apply()
        _settings.value = newSettings
    }

    /**
     * Record a completed game and update statistics.
     * Returns a Pair(isNewBestHeight, isNewBestScore).
     */
    fun recordGame(result: StackDropResult): Pair<Boolean, Boolean> {
        val current = _statistics.value
        val isNewBestHeight = result.height > current.bestHeight
        val isNewBestScore = result.score > current.bestScore

        val updated = current.copy(
            gamesPlayed = current.gamesPlayed + 1,
            bestHeight = maxOf(current.bestHeight, result.height),
            bestScore = maxOf(current.bestScore, result.score),
            totalBlocksDropped = current.totalBlocksDropped + result.height,
            perfectDrops = current.perfectDrops + result.perfectDrops,
            bestPerfectStreak = maxOf(current.bestPerfectStreak, result.bestPerfectStreak)
        )

        prefs.edit()
            .putInt(KEY_GAMES_PLAYED, updated.gamesPlayed)
            .putInt(KEY_BEST_HEIGHT, updated.bestHeight)
            .putInt(KEY_BEST_SCORE, updated.bestScore)
            .putInt(KEY_TOTAL_BLOCKS, updated.totalBlocksDropped)
            .putInt(KEY_PERFECT_DROPS, updated.perfectDrops)
            .putInt(KEY_BEST_STREAK, updated.bestPerfectStreak)
            .apply()

        _statistics.value = updated
        return Pair(isNewBestHeight, isNewBestScore)
    }

    /**
     * Resets only player gameplay records, keeping settings intact.
     */
    fun resetStatistics() {
        val reset = GameStatistics()
        prefs.edit()
            .putInt(KEY_GAMES_PLAYED, 0)
            .putInt(KEY_BEST_HEIGHT, 0)
            .putInt(KEY_BEST_SCORE, 0)
            .putInt(KEY_TOTAL_BLOCKS, 0)
            .putInt(KEY_PERFECT_DROPS, 0)
            .putInt(KEY_BEST_STREAK, 0)
            .apply()

        _statistics.value = reset
    }

    companion object {
        private const val PREFS_NAME = "stack_drop_prefs"
        private const val KEY_THEME = "pref_theme"
        private const val KEY_SOUND = "pref_sound"
        private const val KEY_HAPTICS = "pref_haptics"
        private const val KEY_REDUCED_MOTION = "pref_reduced_motion"

        private const val KEY_GAMES_PLAYED = "stat_games_played"
        private const val KEY_BEST_HEIGHT = "stat_best_height"
        private const val KEY_BEST_SCORE = "stat_best_score"
        private const val KEY_TOTAL_BLOCKS = "stat_total_blocks"
        private const val KEY_PERFECT_DROPS = "stat_perfect_drops"
        private const val KEY_BEST_STREAK = "stat_best_streak"
    }
}
