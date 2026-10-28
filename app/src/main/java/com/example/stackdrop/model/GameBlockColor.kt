package com.example.stackdrop.model

import androidx.compose.ui.graphics.Color

enum class GameBlockColor(
    val lightColor: Color,
    val darkColor: Color,
    val highlightColor: Color
) {
    COLOR_1(
        lightColor = Color(0xFFFF5252), // Coral Red
        darkColor = Color(0xFFFF6E6E),
        highlightColor = Color(0xFFFF8A80)
    ),
    COLOR_2(
        lightColor = Color(0xFFFF7A00), // Tangerine Orange
        darkColor = Color(0xFFFF9100),
        highlightColor = Color(0xFFFFAB40)
    ),
    COLOR_3(
        lightColor = Color(0xFFFFC107), // Amber Gold
        darkColor = Color(0xFFFFD54F),
        highlightColor = Color(0xFFFFE082)
    ),
    COLOR_4(
        lightColor = Color(0xFF00C853), // Emerald Green
        darkColor = Color(0xFF69F0AE),
        highlightColor = Color(0xFFB9F6CA)
    ),
    COLOR_5(
        lightColor = Color(0xFF00B0FF), // Electric Cyan
        darkColor = Color(0xFF40C4FF),
        highlightColor = Color(0xFF80D8FF)
    ),
    COLOR_6(
        lightColor = Color(0xFF2979FF), // Royal Blue
        darkColor = Color(0xFF448AFF),
        highlightColor = Color(0xFF82B1FF)
    ),
    COLOR_7(
        lightColor = Color(0xFF7C4DFF), // Deep Violet
        darkColor = Color(0xFFB388FF),
        highlightColor = Color(0xFFD1C4E9)
    ),
    COLOR_8(
        lightColor = Color(0xFFFF4081), // Vivid Magenta
        darkColor = Color(0xFFFF80AB),
        highlightColor = Color(0xFFFFB6C1)
    );

    fun getColor(isDark: Boolean): Color = if (isDark) darkColor else lightColor

    companion object {
        private val allValues = entries.toTypedArray()
        fun fromIndex(index: Int): GameBlockColor {
            return allValues[(index % allValues.size + allValues.size) % allValues.size]
        }
    }
}
