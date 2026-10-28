package com.example.stackdrop.model

data class DifficultyConfig(
    val initialSpeed: Float,
    val speedIncreasePerHeight: Float,
    val perfectToleranceDp: Float,
    val initialWidthRatio: Float,
    val maxSpeed: Float
) {
    companion object {
        val EASY = DifficultyConfig(
            initialSpeed = 180f,
            speedIncreasePerHeight = 4f,
            perfectToleranceDp = 8f,
            initialWidthRatio = 0.70f,
            maxSpeed = 420f
        )

        val MEDIUM = DifficultyConfig(
            initialSpeed = 240f,
            speedIncreasePerHeight = 6f,
            perfectToleranceDp = 6f,
            initialWidthRatio = 0.65f,
            maxSpeed = 520f
        )

        val HARD = DifficultyConfig(
            initialSpeed = 300f,
            speedIncreasePerHeight = 8f,
            perfectToleranceDp = 4f,
            initialWidthRatio = 0.60f,
            maxSpeed = 620f
        )

        fun forDifficulty(difficulty: Difficulty): DifficultyConfig {
            return when (difficulty) {
                Difficulty.EASY -> EASY
                Difficulty.MEDIUM -> MEDIUM
                Difficulty.HARD -> HARD
            }
        }
    }
}
