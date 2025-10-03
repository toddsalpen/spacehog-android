package com.spacehog.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import com.spacehog.model.Star
import kotlin.random.Random

// This is just a simple data class to hold the state of a single star for Compose
data class StarState(
    var x: Float,
    var y: Float,
    var speed: Float,
    var alpha: Float
)

// A Composable that knows how to draw a list of stars.
@Composable
fun StarfieldBackground(stars: List<StarState>) {
    val paint = remember { Paint().apply { color = Color.White } }

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawIntoCanvas { canvas ->
            for (star in stars) {
                paint.alpha = star.alpha
                // We use drawCircle for a slightly nicer look in Compose
                canvas.drawCircle(Offset(star.x, star.y), 2f, paint)
            }
        }
    }
}