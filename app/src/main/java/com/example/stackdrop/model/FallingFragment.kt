package com.example.stackdrop.model

data class FallingFragment(
    val id: Long,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val velocityX: Float,
    val velocityY: Float,
    val rotation: Float,
    val rotationSpeed: Float,
    val alpha: Float,
    val color: GameBlockColor
)
