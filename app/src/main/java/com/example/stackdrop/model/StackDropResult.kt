package com.example.stackdrop.model

data class StackDropResult(
    val difficulty: Difficulty,
    val height: Int,
    val score: Int,
    val perfectDrops: Int,
    val bestPerfectStreak: Int,
    val gameOver: Boolean,
    val isNewBestHeight: Boolean = false,
    val isNewBestScore: Boolean = false
) {
    val rating: String
        get() = when {
            height < 5 -> "KEEP BUILDING"
            height in 5..9 -> "GOOD START"
            height in 10..19 -> "GREAT TOWER"
            height in 20..29 -> "EXCELLENT"
            else -> "STACK MASTER"
        }
}
