package com.example.stackdrop.model

data class MovingBlock(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val velocityX: Float,
    val velocityY: Float,
    val color: GameBlockColor,
    val isDropping: Boolean
)
