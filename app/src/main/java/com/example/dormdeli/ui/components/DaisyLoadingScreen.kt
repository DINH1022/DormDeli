package com.example.dormdeli.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dormdeli.ui.theme.OrangePrimary

@Composable
fun DaisyLoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OrangePrimary),
        contentAlignment = Alignment.Center
    ) {
        val petalCount = 12
        val infiniteTransition = rememberInfiniteTransition(label = "daisy")
        
        // progress goes from 0 to petalCount + a pause period
        val progress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = petalCount.toFloat() + 3f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "daisy_progress"
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasCenter = Offset(size.width / 2, size.height / 2)
                    val yellowCenterRadius = 16.dp.toPx()
                    
                    // Draw petals
                    for (i in 0 until petalCount) {
                        // Each petal starts appearing after the previous one
                        // coerced between 0 and 1
                        val petalProgress = (progress - i).coerceIn(0f, 1f)
                        
                        if (petalProgress > 0f) {
                            val angle = i * (360f / petalCount)
                            rotate(angle, canvasCenter) {
                                // Effect: Petal moves from outside towards center and fades in
                                val distanceOffset = (1f - petalProgress) * 30.dp.toPx()
                                val petalWidth = 18.dp.toPx()
                                val petalHeight = 45.dp.toPx()
                                
                                drawOval(
                                    color = Color.White,
                                    topLeft = Offset(
                                        canvasCenter.x - petalWidth / 2,
                                        canvasCenter.y - yellowCenterRadius - petalHeight - 5.dp.toPx() - distanceOffset
                                    ),
                                    size = Size(petalWidth, petalHeight),
                                    alpha = petalProgress
                                )
                            }
                        }
                    }
                    
                    // Draw yellow center
                    drawCircle(
                        color = Color(0xFFFFD700), // Gold/Yellow
                        radius = yellowCenterRadius,
                        center = canvasCenter
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Dorm Deli",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
            Text(
                text = "Waiting a few minutes...",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
