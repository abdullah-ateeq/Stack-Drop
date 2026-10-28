package com.example.stackdrop.util

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class OverlapResult(
    val overlapLeft: Float,
    val overlapRight: Float,
    val overlapWidth: Float,
    val isMiss: Boolean,
    val leftOverhang: Float, // > 0 if overhanging on left
    val rightOverhang: Float // > 0 if overhanging on right
)

object GamePhysics {

    fun calculateOverlap(
        towerLeft: Float,
        towerRight: Float,
        blockLeft: Float,
        blockRight: Float
    ): OverlapResult {
        val overlapLeft = max(blockLeft, towerLeft)
        val overlapRight = min(blockRight, towerRight)
        val overlapWidth = overlapRight - overlapLeft

        if (overlapWidth <= 0f) {
            return OverlapResult(
                overlapLeft = 0f,
                overlapRight = 0f,
                overlapWidth = 0f,
                isMiss = true,
                leftOverhang = 0f,
                rightOverhang = 0f
            )
        }

        val leftOverhang = max(0f, towerLeft - blockLeft)
        val rightOverhang = max(0f, blockRight - towerRight)

        return OverlapResult(
            overlapLeft = overlapLeft,
            overlapRight = overlapRight,
            overlapWidth = overlapWidth,
            isMiss = false,
            leftOverhang = leftOverhang,
            rightOverhang = rightOverhang
        )
    }

    fun isPerfect(
        towerLeft: Float,
        towerRight: Float,
        blockLeft: Float,
        blockRight: Float,
        tolerance: Float
    ): Boolean {
        return abs(blockLeft - towerLeft) <= tolerance || abs(blockRight - towerRight) <= tolerance
    }

    fun calculateScore(height: Int, basePoints: Int = 100): Int {
        val multiplier = 1f + min(height, 20) * 0.05f
        return (basePoints * multiplier).roundToInt()
    }

    fun getMilestoneMessage(height: Int): String? {
        return when (height) {
            5 -> "NICE!"
            10 -> "GREAT!"
            20 -> "IMPRESSIVE!"
            30 -> "TOWER MASTER!"
            50 -> "UNSTOPPABLE!"
            else -> null
        }
    }

    fun lerp(start: Float, stop: Float, fraction: Float): Float {
        return start + (stop - start) * fraction.coerceIn(0f, 1f)
    }
}
