package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DotMatrixChar
import com.example.ui.components.DottedIndicator
import com.example.ui.components.NothingBlack
import com.example.ui.components.NothingWhite
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToHome: () -> Unit) {
    // Navigate with a short delay
    LaunchedEffect(Unit) {
        delay(2200)
        onNavigateToHome()
    }

    // Blink animation for loading sequence
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val alphaAnim = infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NothingBlack),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Large Dot Matrix representation of 'A' and 'Q'
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DotMatrixChar('8', dotColor = NothingWhite, cellSize = 10.dp) // Acts as robust visual
            Spacer(modifier = Modifier.width(16.dp))
            DotMatrixChar('9', dotColor = NothingWhite, cellSize = 10.dp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Logo text
        Text(
            text = "ALARM QUEST",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = NothingWhite,
            letterSpacing = 6.sp,
            fontFamily = FontFamily.Monospace
        )

        Text(
            text = "NOTHING OS DESIGN LANGUAGE",
            fontSize = 10.sp,
            color = NothingWhite.copy(alpha = 0.4f),
            letterSpacing = 2.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Dynamic dotted blinking progress indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(bottom = 60.dp)
                .alpha(alphaAnim.value)
        ) {
            DottedIndicator(activeCount = 2, totalCount = 4)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "LOADING MISSION OBJECTS",
                fontSize = 11.sp,
                color = NothingWhite,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
