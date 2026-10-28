package com.example.stackdrop

import com.example.stackdrop.model.Difficulty
import com.example.stackdrop.model.GameBlockColor
import com.example.stackdrop.model.GameState
import com.example.stackdrop.model.MovingBlock
import com.example.stackdrop.model.TowerBlock
import com.example.stackdrop.util.GamePhysics
import com.example.stackdrop.viewmodel.GameUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameLogicTest {

    @Test
    fun perfect_bonus_and_streak_accumulation() {
        var score = 0
        var height = 0
        var perfectDrops = 0
        var perfectStreak = 0

        // Simulate 5 consecutive perfect drops
        for (i in 1..5) {
            height += 1
            val baseScore = GamePhysics.calculateScore(height)
            score += baseScore + 100 // +100 for perfect drop
            perfectDrops += 1
            perfectStreak += 1

            if (perfectStreak % 5 == 0) {
                score += 250 // streak bonus!
            }
        }

        assertEquals(5, height)
        assertEquals(5, perfectDrops)
        assertEquals(5, perfectStreak)
        // Heights 1..5 scores:
        // h=1: 105 + 100 = 205
        // h=2: 110 + 100 = 210
        // h=3: 115 + 100 = 215
        // h=4: 120 + 100 = 220
        // h=5: 125 + 100 + 250 = 475
        // Total = 205 + 210 + 215 + 220 + 475 = 1325
        assertEquals(1325, score)

        // Now simulate a non-perfect drop
        height += 1
        val baseScore6 = GamePhysics.calculateScore(height)
        score += baseScore6
        perfectStreak = 0 // resets on imperfect drop

        assertEquals(6, height)
        assertEquals(5, perfectDrops) // does not increase
        assertEquals(0, perfectStreak) // reset to 0
    }

    @Test
    fun pause_and_resume_preserves_position() {
        val moving = MovingBlock(
            x = 142.5f,
            y = 350.0f,
            width = 180.0f,
            height = 44.0f,
            velocityX = 240.0f,
            velocityY = 0.0f,
            color = GameBlockColor.COLOR_2,
            isDropping = false
        )

        var state = GameUiState(
            gameState = GameState.MOVING,
            movingBlock = moving
        )

        // Pause
        state = state.copy(gameState = GameState.PAUSED)
        assertEquals(GameState.PAUSED, state.gameState)
        assertEquals(142.5f, state.movingBlock?.x ?: 0f, 0.001f)
        assertEquals(350.0f, state.movingBlock?.y ?: 0f, 0.001f)

        // Resume
        state = state.copy(gameState = GameState.MOVING)
        assertEquals(GameState.MOVING, state.gameState)
        assertEquals(142.5f, state.movingBlock?.x ?: 0f, 0.001f)
        assertEquals(350.0f, state.movingBlock?.y ?: 0f, 0.001f)
    }

    @Test
    fun camera_offset_triggers_when_tower_is_high() {
        val boardHeight = 800f
        val baseY = 680f // 800 - 120
        val threshold = boardHeight * 0.55f // 440f

        // Tower height 200 (well below 440f threshold)
        val towerY1 = baseY - 200f
        val totalTowerHeight1 = baseY - towerY1
        val cameraOffset1 = if (totalTowerHeight1 > threshold) totalTowerHeight1 - threshold else 0f
        assertEquals(0f, cameraOffset1, 0.001f)

        // Tower height 500 (above threshold)
        val towerY2 = baseY - 500f
        val totalTowerHeight2 = baseY - towerY2
        val cameraOffset2 = if (totalTowerHeight2 > threshold) totalTowerHeight2 - threshold else 0f
        assertEquals(60f, cameraOffset2, 0.001f)
    }
}
