package com.lifeflowpro.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun FinancialTrendChart(dataPoints: List<Double>, modifier: Modifier = Modifier) {
    if (dataPoints.isEmpty()) return

    val maxVal = dataPoints.maxOrNull() ?: 1.0
    val minVal = dataPoints.minOrNull() ?: 0.0
    val range = maxVal - minVal

    Canvas(modifier = modifier.fillMaxWidth().height(200.dp)) {
        val width = size.width
        val height = size.height
        val spacing = width / (dataPoints.size - 1)

        val path = Path()
        dataPoints.forEachIndexed { index, value ->
            val x = index * spacing
            val y = height - ((value - minVal) / range * height).toFloat()
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = Color(0xFF6366F1),
            style = Stroke(width = 3.dp.toPx())
        )
        
        // Draw points
        dataPoints.forEachIndexed { index, value ->
            val x = index * spacing
            val y = height - ((value - minVal) / range * height).toFloat()
            drawCircle(color = Color(0xFF6366F1), radius = 4.dp.toPx(), center = Offset(x, y))
        }
    }
}
