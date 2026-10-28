package com.example.stackdrop.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import com.example.stackdrop.model.AppTheme
import com.example.stackdrop.model.FallingFragment
import com.example.stackdrop.model.MovingBlock
import com.example.stackdrop.model.TowerBlock
import com.example.stackdrop.model.UserSettings
import com.example.stackdrop.viewmodel.GameUiState

@Composable
fun GameCanvas(
    uiState: GameUiState,
    userSettings: UserSettings,
    modifier: Modifier = Modifier
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (userSettings.theme) {
        AppTheme.SYSTEM -> isSystemDark
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
    }

    val backgroundColor = if (isDark) Color(0xFF12141A) else Color(0xFFF7F8FA)
    val gridLineColor = if (isDark) Color(0xFF1E222D) else Color(0xFFE8EBF0)
    val baseColor = if (isDark) Color(0xFF2E3440) else Color(0xFF3B4252)

    Canvas(modifier = modifier.fillMaxSize()) {
        // Draw subtle minimalist background grid lines
        drawBackgroundGrid(gridLineColor)

        // Translate everything by camera offset Y (smooth tower climb)
        translate(top = uiState.cameraOffsetY) {

            // 1. Draw Stationary Tower Base & Tower Blocks
            val totalBlocks = uiState.towerBlocks.size
            uiState.towerBlocks.forEachIndexed { index, block ->
                val isBase = (index == 0)

                // Apply subtle wobble to the highest blocks when tower wobbles
                val wobbleFraction = if (totalBlocks > 1 && index >= totalBlocks - 3) {
                    (index - (totalBlocks - 3) + 1) / 3f
                } else 0f
                val rotationAngle = uiState.towerWobbleDeg * wobbleFraction

                if (rotationAngle != 0f) {
                    rotate(
                        degrees = rotationAngle,
                        pivot = Offset(block.x + block.width / 2f, block.y + block.height)
                    ) {
                        drawBlock(block, isDark, isBase, baseColor)
                    }
                } else {
                    drawBlock(block, isDark, isBase, baseColor)
                }
            }

            // 2. Draw Falling Offcut Fragments
            for (fragment in uiState.fallingFragments) {
                drawFragment(fragment, isDark)
            }

            // 3. Draw Active Moving Block
            uiState.movingBlock?.let { moving ->
                drawMovingBlock(moving, isDark)
            }
        }
    }
}

private fun DrawScope.drawBlock(
    block: TowerBlock,
    isDark: Boolean,
    isBase: Boolean,
    baseColor: Color
) {
    val cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
    val mainColor = if (isBase) baseColor else block.color.getColor(isDark)
    val highlightColor = if (isBase) {
        if (isDark) Color(0xFF434C5E) else Color(0xFF4C566A)
    } else {
        block.color.highlightColor
    }

    // Shadow / base depth
    drawRoundRect(
        color = mainColor,
        topLeft = Offset(block.x, block.y),
        size = Size(block.width, block.height),
        cornerRadius = cornerRadius
    )

    // Top highlight rim for crisp 3D-like tactile feel
    val rimHeight = (block.height * 0.16f).coerceAtLeast(3f)
    drawRoundRect(
        color = highlightColor.copy(alpha = if (isDark) 0.35f else 0.45f),
        topLeft = Offset(block.x + 2f, block.y + 1f),
        size = Size(block.width - 4f, rimHeight),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
    )
}

private fun DrawScope.drawMovingBlock(moving: MovingBlock, isDark: Boolean) {
    val cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
    val mainColor = moving.color.getColor(isDark)
    val highlightColor = moving.color.highlightColor

    drawRoundRect(
        color = mainColor,
        topLeft = Offset(moving.x, moving.y),
        size = Size(moving.width, moving.height),
        cornerRadius = cornerRadius
    )

    // Highlight
    val rimHeight = (moving.height * 0.16f).coerceAtLeast(3f)
    drawRoundRect(
        color = highlightColor.copy(alpha = if (isDark) 0.4f else 0.5f),
        topLeft = Offset(moving.x + 2f, moving.y + 1f),
        size = Size(moving.width - 4f, rimHeight),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
    )
}

private fun DrawScope.drawFragment(fragment: FallingFragment, isDark: Boolean) {
    val cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
    val color = fragment.color.getColor(isDark).copy(alpha = fragment.alpha)

    rotate(
        degrees = fragment.rotation,
        pivot = Offset(fragment.x + fragment.width / 2f, fragment.y + fragment.height / 2f)
    ) {
        drawRoundRect(
            color = color,
            topLeft = Offset(fragment.x, fragment.y),
            size = Size(fragment.width, fragment.height),
            cornerRadius = cornerRadius
        )
    }
}

private fun DrawScope.drawBackgroundGrid(color: Color) {
    val spacing = 64.dp.toPx()
    var x = spacing
    while (x < size.width) {
        drawLine(
            color = color,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1f
        )
        x += spacing
    }

    var y = spacing
    while (y < size.height) {
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f
        )
        y += spacing
    }
}
