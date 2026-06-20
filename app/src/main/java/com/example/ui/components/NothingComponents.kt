package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Nothing OS Style Tokens ---
val NothingBlack = Color(0xFF000000)
val NothingWhite = Color(0xFFFFFFFF)
val NothingGreyDark = Color(0xFF121212)
val NothingGreyMedium = Color(0xFF262626)
val NothingGreyLight = Color(0xFFC0C0C0)
val NothingRed = Color(0xFFFF003C) // Nothing Signature Accent Red

// --- Dot Matrix Raster Grid for Digits (5x7 Grid representation) ---
// Each number is mapped to a 35-bit representation (5 columns x 7 rows)
private val DIGIT_MATRICES = mapOf(
    '0' to listOf(
        1,1,1,1,1,
        1,0,0,0,1,
        1,0,0,0,1,
        1,0,0,0,1,
        1,0,0,0,1,
        1,0,0,0,1,
        1,1,1,1,1
    ),
    '1' to listOf(
        0,0,1,0,0,
        0,1,1,0,0,
        0,0,1,0,0,
        0,0,1,0,0,
        0,0,1,0,0,
        0,0,1,0,0,
        0,1,1,1,0
    ),
    '2' to listOf(
        1,1,1,1,1,
        0,0,0,0,1,
        0,0,0,0,1,
        1,1,1,1,1,
        1,0,0,0,0,
        1,0,0,0,0,
        1,1,1,1,1
    ),
    '3' to listOf(
        1,1,1,1,1,
        0,0,0,0,1,
        0,0,0,0,1,
        1,1,1,1,1,
        0,0,0,0,1,
        0,0,0,0,1,
        1,1,1,1,1
    ),
    '4' to listOf(
        1,0,0,0,1,
        1,0,0,0,1,
        1,0,0,0,1,
        1,1,1,1,1,
        0,0,0,0,1,
        0,0,0,0,1,
        0,0,0,0,1
    ),
    '5' to listOf(
        1,1,1,1,1,
        1,0,0,0,0,
        1,0,0,0,0,
        1,1,1,1,1,
        0,0,0,0,1,
        0,0,0,0,1,
        1,1,1,1,1
    ),
    '6' to listOf(
        1,1,1,1,1,
        1,0,0,0,0,
        1,0,0,0,0,
        1,1,1,1,1,
        1,0,0,0,1,
        1,0,0,0,1,
        1,1,1,1,1
    ),
    '7' to listOf(
        1,1,1,1,1,
        0,0,0,0,1,
        0,0,0,0,1,
        0,0,0,1,0,
        0,0,1,0,0,
        0,1,0,0,0,
        1,0,0,0,0
    ),
    '8' to listOf(
        1,1,1,1,1,
        1,0,0,0,1,
        1,0,0,0,1,
        1,1,1,1,1,
        1,0,0,0,1,
        1,0,0,0,1,
        1,1,1,1,1
    ),
    '9' to listOf(
        1,1,1,1,1,
        1,0,0,0,1,
        1,0,0,0,1,
        1,1,1,1,1,
        0,0,0,0,1,
        0,0,0,0,1,
        1,1,1,1,1
    ),
    ':' to listOf(
        0,0,0,0,0,
        0,0,1,0,0,
        0,0,0,0,0,
        0,0,0,0,0,
        0,0,1,0,0,
        0,0,0,0,0,
        0,0,0,0,0
    )
)

@Composable
fun DotMatrixChar(
    char: Char,
    modifier: Modifier = Modifier,
    dotColor: Color = NothingWhite,
    inactiveDotColor: Color = Color(0xFF1E1E1E),
    cellSize: Dp = 4.dp
) {
    val matrix = DIGIT_MATRICES[char] ?: listOf(
        0,0,0,0,0,
        0,0,0,0,0,
        0,0,0,0,0,
        0,0,0,0,0,
        0,0,0,0,0,
        0,0,0,0,0,
        0,0,0,0,0
    )

    Box(
        modifier = modifier
            .width(cellSize * 5 + 6.dp)
            .height(cellSize * 7 + 10.dp)
            .padding(1.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellPx = cellSize.toPx()
            val paddingPx = 1.dp.toPx()

            for (row in 0 until 7) {
                for (col in 0 until 5) {
                    val index = row * 5 + col
                    val isActive = index < matrix.size && matrix[index] == 1
                    
                    val x = col * (cellPx + paddingPx) + cellPx / 2
                    val y = row * (cellPx + paddingPx) + cellPx / 2
                    
                    drawCircle(
                        color = if (isActive) dotColor else inactiveDotColor,
                        radius = cellPx / 2.5f,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}

@Composable
fun DotMatrixTime(
    hour: Int,
    minute: Int,
    modifier: Modifier = Modifier,
    cellSize: Dp = 5.dp,
    dotColor: Color = NothingWhite
) {
    val hStr = String.format("%02d", hour)
    val mStr = String.format("%02d", minute)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DotMatrixChar(hStr[0], cellSize = cellSize, dotColor = dotColor)
        Spacer(modifier = Modifier.width(3.dp))
        DotMatrixChar(hStr[1], cellSize = cellSize, dotColor = dotColor)
        Spacer(modifier = Modifier.width(6.dp))
        DotMatrixChar(':', cellSize = cellSize, dotColor = dotColor)
        Spacer(modifier = Modifier.width(6.dp))
        DotMatrixChar(mStr[0], cellSize = cellSize, dotColor = dotColor)
        Spacer(modifier = Modifier.width(3.dp))
        DotMatrixChar(mStr[1], cellSize = cellSize, dotColor = dotColor)
    }
}

// --- Premium Card with Fine Nothing OS Styling ---
@Composable
fun NothingCard(
    modifier: Modifier = Modifier,
    borderColor: Color = NothingGreyMedium,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = NothingGreyDark),
        border = CardStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            content()
        }
    }
}

private fun CardStroke(width: Dp, color: Color) = BorderStroke(width, color)

// --- Retro-Minimalist Tactile Press States Button ---
@Composable
fun NothingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSecondary: Boolean = false,
    textTag: String = "",
    isAccent: Boolean = false
) {
    val containerColor = when {
        isAccent -> NothingRed
        isSecondary -> NothingGreyDark
        else -> NothingWhite
    }
    
    val textColor = when {
        isAccent -> NothingWhite
        isSecondary -> NothingWhite
        else -> NothingBlack
    }

    val borderStroke = if (isSecondary) BorderStroke(1.dp, NothingGreyMedium) else null

    Button(
        onClick = onClick,
        modifier = modifier
            .height(52.dp)
            .testTag(textTag.ifEmpty { text.lowercase().replace(" ", "_") + "_btn" }),
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = textColor
        ),
        border = borderStroke,
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 14.sp,
                letterSpacing = 1.5.sp
            )
        )
    }
}

// --- Header Dotted Matrix Line for Branded Separation ---
@Composable
fun NothingDivider(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
    ) {
        val dotRadius = 1.dp.toPx()
        val spacing = 6.dp.toPx()
        var currentX = 0f
        val y = size.height / 2

        while (currentX < size.width) {
            drawCircle(
                color = NothingGreyMedium,
                radius = dotRadius,
                center = Offset(currentX, y)
            )
            currentX += spacing
        }
    }
}

// --- Dotted Status indicators or Progress dots ---
@Composable
fun DottedIndicator(
    activeCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until totalCount) {
            val isActive = i < activeCount
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (isActive) 10.dp else 6.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = if (isActive) NothingRed else NothingGreyMedium,
                        radius = size.width / 2
                    )
                }
            }
        }
    }
}
