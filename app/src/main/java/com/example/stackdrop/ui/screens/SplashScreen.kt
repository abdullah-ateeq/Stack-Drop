package com.example.stackdrop.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stackdrop.model.GameBlockColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val block1Offset = remember { Animatable(-300f) }
    val block2Offset = remember { Animatable(-400f) }
    val block3Offset = remember { Animatable(-500f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Staggered drop animation
        launch {
            block1Offset.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing)
            )
        }
        delay(120)
        launch {
            block2Offset.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing)
            )
        }
        delay(120)
        launch {
            block3Offset.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing)
            )
        }
        delay(80)
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 300)
        )
        delay(300)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "STACK DROP",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.alpha(textAlpha.value)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Animated Blocks Stack
            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier
                    .width(220.dp)
                    .height(130.dp)
            ) {
                // Bottom block (widest)
                Box(
                    modifier = Modifier
                        .offset { IntOffset(0, block1Offset.value.roundToInt()) }
                        .width(180.dp)
                        .height(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(GameBlockColor.COLOR_1.lightColor)
                )

                // Middle block
                Box(
                    modifier = Modifier
                        .offset { IntOffset(0, (-38 + block2Offset.value).roundToInt()) }
                        .width(140.dp)
                        .height(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(GameBlockColor.COLOR_3.lightColor)
                )

                // Top block
                Box(
                    modifier = Modifier
                        .offset { IntOffset(0, (-76 + block3Offset.value).roundToInt()) }
                        .width(100.dp)
                        .height(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(GameBlockColor.COLOR_5.lightColor)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "Drop. Stack. Don't wobble.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(textAlpha.value)
            )
        }
    }
}
