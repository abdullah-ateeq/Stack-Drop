package com.example.stackdrop

import com.example.stackdrop.model.Difficulty
import com.example.stackdrop.model.DifficultyConfig
import com.example.stackdrop.model.StackDropResult
import com.example.stackdrop.util.GamePhysics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GamePhysicsTest {

    @Test
    fun overlap_calculation_correct() {
        // Given:
        // towerLeft = 100, towerRight = 300
        // blockLeft = 200, blockRight = 400
        val result = GamePhysics.calculateOverlap(
            towerLeft = 100f,
            towerRight = 300f,
            blockLeft = 200f,
            blockRight = 400f
        )

        // Expected:
        // overlapLeft = 200, overlapRight = 300, overlapWidth = 100
        assertEquals(200f, result.overlapLeft, 0.001f)
        assertEquals(300f, result.overlapRight, 0.001f)
        assertEquals(100f, result.overlapWidth, 0.001f)
        assertFalse(result.isMiss)
        assertEquals(0f, result.leftOverhang, 0.001f)
        assertEquals(100f, result.rightOverhang, 0.001f)
    }

    @Test
    fun miss_calculation_correct() {
        // Given:
        // tower: 100–300
        // block: 350–500
        val result = GamePhysics.calculateOverlap(
            towerLeft = 100f,
            towerRight = 300f,
            blockLeft = 350f,
            blockRight = 500f
        )

        // Expected:
        // overlapWidth <= 0, isMiss = true
        assertTrue(result.overlapWidth <= 0f)
        assertTrue(result.isMiss)
    }

    @Test
    fun perfect_tolerance_test() {
        // Given:
        // towerLeft = 100, blockLeft = 104, tolerance = 8
        val isPerf = GamePhysics.isPerfect(
            towerLeft = 100f,
            towerRight = 300f,
            blockLeft = 104f,
            blockRight = 304f,
            tolerance = 8f
        )

        // Expected:
        // perfect = true
        assertTrue(isPerf)

        // Beyond tolerance
        val notPerf = GamePhysics.isPerfect(
            towerLeft = 100f,
            towerRight = 300f,
            blockLeft = 112f,
            blockRight = 312f,
            tolerance = 8f
        )
        assertFalse(notPerf)
    }

    @Test
    fun partial_drop_test() {
        // Given:
        // towerWidth = 200 (100 to 300)
        // blockWidth = 200 (150 to 350)
        // overlapWidth = 150 (150 to 300)
        val result = GamePhysics.calculateOverlap(
            towerLeft = 100f,
            towerRight = 300f,
            blockLeft = 150f,
            blockRight = 350f
        )

        assertEquals(150f, result.overlapWidth, 0.001f)
        assertEquals(50f, result.rightOverhang, 0.001f)
    }

    @Test
    fun score_calculation_and_multiplier_test() {
        // Given:
        // height = 10
        // multiplier = 1 + min(10, 20) * 0.05 = 1 + 0.5 = 1.5
        // basePoints = 100
        // points = round(100 * 1.5) = 150
        val score = GamePhysics.calculateScore(height = 10, basePoints = 100)
        assertEquals(150, score)

        // At height 0
        val score0 = GamePhysics.calculateScore(height = 0, basePoints = 100)
        assertEquals(100, score0)

        // At height 20 (max multiplier = 1 + 20 * 0.05 = 2.0)
        val score20 = GamePhysics.calculateScore(height = 20, basePoints = 100)
        assertEquals(200, score20)

        // Beyond height 20 (multiplier clamped at 2.0)
        val score30 = GamePhysics.calculateScore(height = 30, basePoints = 100)
        assertEquals(200, score30)
    }

    @Test
    fun height_milestones_test() {
        assertEquals("NICE!", GamePhysics.getMilestoneMessage(5))
        assertEquals("GREAT!", GamePhysics.getMilestoneMessage(10))
        assertEquals("IMPRESSIVE!", GamePhysics.getMilestoneMessage(20))
        assertEquals("TOWER MASTER!", GamePhysics.getMilestoneMessage(30))
        assertEquals("UNSTOPPABLE!", GamePhysics.getMilestoneMessage(50))
        assertNull(GamePhysics.getMilestoneMessage(7))
    }

    @Test
    fun result_rating_tiers() {
        val r1 = StackDropResult(Difficulty.EASY, height = 3, score = 300, perfectDrops = 0, bestPerfectStreak = 0, gameOver = true)
        assertEquals("KEEP BUILDING", r1.rating)

        val r2 = StackDropResult(Difficulty.EASY, height = 7, score = 800, perfectDrops = 2, bestPerfectStreak = 1, gameOver = true)
        assertEquals("GOOD START", r2.rating)

        val r3 = StackDropResult(Difficulty.EASY, height = 15, score = 2000, perfectDrops = 5, bestPerfectStreak = 3, gameOver = true)
        assertEquals("GREAT TOWER", r3.rating)

        val r4 = StackDropResult(Difficulty.EASY, height = 25, score = 4000, perfectDrops = 10, bestPerfectStreak = 5, gameOver = true)
        assertEquals("EXCELLENT", r4.rating)

        val r5 = StackDropResult(Difficulty.EASY, height = 35, score = 6000, perfectDrops = 15, bestPerfectStreak = 8, gameOver = true)
        assertEquals("STACK MASTER", r5.rating)
    }

    @Test
    fun difficulty_configs_clamp_speed() {
        val easy = DifficultyConfig.forDifficulty(Difficulty.EASY)
        assertEquals(180f, easy.initialSpeed, 0.001f)
        assertEquals(420f, easy.maxSpeed, 0.001f)
        assertEquals(8f, easy.perfectToleranceDp, 0.001f)

        val hard = DifficultyConfig.forDifficulty(Difficulty.HARD)
        assertEquals(300f, hard.initialSpeed, 0.001f)
        assertEquals(620f, hard.maxSpeed, 0.001f)
        assertEquals(4f, hard.perfectToleranceDp, 0.001f)
    }

    @Test
    fun camera_lerp_test() {
        val start = 0f
        val stop = 100f
        val lerped = GamePhysics.lerp(start, stop, 0.5f)
        assertEquals(50f, lerped, 0.001f)
    }
}
