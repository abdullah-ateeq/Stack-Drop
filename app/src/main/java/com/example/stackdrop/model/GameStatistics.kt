package com.example.stackdrop.model

data class GameStatistics(
    val gamesPlayed: Int = 0,
    val bestHeight: Int = 0,
    val bestScore: Int = 0,
    val totalBlocksDropped: Int = 0,
    val perfectDrops: Int = 0,
    val bestPerfectStreak: Int = 0
)
