package com.lifeflowpro.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PieChart(
    data: Map<String, Double>, 
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val total = data.values.sum()
    if (total == 0.0) return

    Canvas(modifier = modifier) {
        var startAngle = 0f
        var index = 0
        data.values.forEach { value ->
            val sweepAngle = (value.toFloat() / total.toFloat()) * 360f
            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                size = Size(size.width, size.height)
            )
            startAngle += sweepAngle
            index++
        }
    }
}

@Composable
fun DonutChart(
    percentage: Float, // 0f to 1f
    color: Color = Color(0xFF10B981),
    backgroundColor: Color = Color(0xFFE5E7EB),
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        // Draw background track
        drawArc(
            color = backgroundColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = 30f, cap = StrokeCap.Round)
        )
        // Draw progress
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = percentage * 360f,
            useCenter = false,
            style = Stroke(width = 30f, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun HorizontalBarChart(
    spent: Float,
    limit: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val percentage = if (limit > 0f) spent / limit else 0f
    val safePercentage = percentage.coerceIn(0f, 1f)
    
    Canvas(modifier = modifier) {
        // Draw background
        drawRoundRect(
            color = Color(0xFFE5E7EB),
            size = Size(size.width, size.height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
        )
        // Draw usage
        drawRoundRect(
            color = color,
            size = Size(size.width * safePercentage, size.height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
        )
    }
}
