package com.csd3156.game.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// --- Data classes ---

private data class Orb(
    val xFraction: Float,
    val yFraction: Float,
    val radius: Float,
    val color: Color,
    val speed: Float,
    val phase: Float,
    val driftX: Float,
    val driftY: Float
)

private data class Star(
    val xFraction: Float,
    val yFraction: Float,
    val baseRadius: Float,
    val speed: Float,
    val phase: Float
)

private data class ShootingStar(
    val startXFraction: Float,
    val startYFraction: Float,
    val length: Float,
    val angle: Float,
    val speed: Float,
    val phase: Float
)

private data class Bokeh(
    val xFraction: Float,
    val yFraction: Float,
    val radius: Float,
    val color: Color,
    val speed: Float,
    val phase: Float,
    val driftX: Float,
    val driftY: Float
)

private data class MusicNote(
    val xFraction: Float,
    val startYFraction: Float,
    val speed: Float,
    val phase: Float,
    val size: Float
)

// --- Static data ---

private val orbs = listOf(
    Orb(0.15f, 0.20f, 120f, Color(0xFFB388FF), 0.7f, 0.0f, 30f, 20f),
    Orb(0.75f, 0.15f, 90f, Color(0xFFFF80AB), 0.5f, 1.2f, 25f, 35f),
    Orb(0.50f, 0.60f, 150f, Color(0xFF80D8FF), 0.4f, 2.5f, 40f, 25f),
    Orb(0.25f, 0.75f, 100f, Color(0xFFFFAB91), 0.6f, 0.8f, 20f, 30f),
    Orb(0.85f, 0.50f, 110f, Color(0xFFCE93D8), 0.55f, 3.1f, 35f, 20f),
    Orb(0.40f, 0.35f, 80f, Color(0xFFA5D6A7), 0.45f, 1.9f, 25f, 40f),
    Orb(0.60f, 0.85f, 130f, Color(0xFFB39DDB), 0.65f, 4.0f, 30f, 15f),
    Orb(0.10f, 0.50f, 70f, Color(0xFFF48FB1), 0.75f, 2.2f, 15f, 25f),
    Orb(0.90f, 0.80f, 95f, Color(0xFF81D4FA), 0.50f, 0.5f, 20f, 35f),
    Orb(0.35f, 0.10f, 85f, Color(0xFFFFCC80), 0.60f, 3.5f, 30f, 20f),
    Orb(0.70f, 0.40f, 105f, Color(0xFFEF9A9A), 0.35f, 1.5f, 25f, 30f),
    Orb(0.20f, 0.90f, 75f, Color(0xFF90CAF9), 0.80f, 2.8f, 35f, 25f)
)

private val stars = listOf(
    Star(0.05f, 0.08f, 2.5f, 1.2f, 0.0f),
    Star(0.12f, 0.32f, 2.0f, 0.9f, 1.1f),
    Star(0.22f, 0.55f, 3.0f, 1.5f, 2.3f),
    Star(0.30f, 0.18f, 2.0f, 1.0f, 0.5f),
    Star(0.38f, 0.72f, 2.5f, 1.3f, 3.8f),
    Star(0.45f, 0.05f, 2.0f, 0.8f, 1.7f),
    Star(0.52f, 0.42f, 3.0f, 1.1f, 4.2f),
    Star(0.58f, 0.88f, 2.5f, 1.4f, 0.9f),
    Star(0.65f, 0.25f, 2.0f, 0.7f, 2.6f),
    Star(0.72f, 0.62f, 3.0f, 1.2f, 3.3f),
    Star(0.78f, 0.10f, 2.5f, 1.0f, 1.4f),
    Star(0.85f, 0.48f, 2.0f, 1.6f, 4.8f),
    Star(0.92f, 0.78f, 2.5f, 0.9f, 0.3f),
    Star(0.08f, 0.65f, 3.0f, 1.3f, 2.0f),
    Star(0.18f, 0.92f, 2.0f, 1.1f, 3.5f),
    Star(0.28f, 0.40f, 2.5f, 0.8f, 5.0f),
    Star(0.42f, 0.28f, 2.0f, 1.5f, 1.8f),
    Star(0.55f, 0.70f, 3.0f, 1.0f, 4.5f),
    Star(0.68f, 0.95f, 2.0f, 1.2f, 0.7f),
    Star(0.75f, 0.35f, 2.5f, 0.6f, 3.0f),
    Star(0.82f, 0.58f, 2.0f, 1.4f, 2.1f),
    Star(0.95f, 0.22f, 3.0f, 0.9f, 4.0f),
    Star(0.03f, 0.45f, 2.5f, 1.1f, 1.3f),
    Star(0.48f, 0.52f, 2.0f, 1.3f, 5.5f),
    Star(0.33f, 0.85f, 3.0f, 0.7f, 2.8f),
    Star(0.62f, 0.15f, 2.5f, 1.5f, 0.2f),
    Star(0.88f, 0.38f, 2.0f, 1.0f, 3.7f),
    Star(0.15f, 0.78f, 2.5f, 1.2f, 4.3f),
    Star(0.50f, 0.92f, 2.0f, 0.8f, 1.6f),
    Star(0.80f, 0.05f, 3.0f, 1.4f, 5.2f)
)

private val shootingStars = listOf(
    ShootingStar(0.10f, 0.05f, 180f, 0.5f, 0.3f, 0.0f),
    ShootingStar(0.60f, 0.02f, 150f, 0.6f, 0.25f, 2.1f),
    ShootingStar(0.85f, 0.15f, 200f, 0.45f, 0.35f, 4.2f),
    ShootingStar(0.30f, 0.10f, 160f, 0.55f, 0.28f, 6.0f),
    ShootingStar(0.50f, 0.08f, 170f, 0.5f, 0.32f, 8.5f)
)

private val bokehCircles = listOf(
    Bokeh(0.12f, 0.30f, 180f, Color(0xFFB388FF), 0.15f, 0.0f, 15f, 10f),
    Bokeh(0.80f, 0.20f, 220f, Color(0xFFFF80AB), 0.12f, 1.5f, 12f, 18f),
    Bokeh(0.45f, 0.70f, 160f, Color(0xFF80D8FF), 0.18f, 3.0f, 20f, 12f),
    Bokeh(0.70f, 0.55f, 200f, Color(0xFFCE93D8), 0.10f, 4.5f, 10f, 15f),
    Bokeh(0.25f, 0.85f, 140f, Color(0xFFFFAB91), 0.20f, 2.0f, 18f, 8f),
    Bokeh(0.90f, 0.75f, 170f, Color(0xFFA5D6A7), 0.14f, 5.5f, 14f, 20f),
    Bokeh(0.55f, 0.15f, 190f, Color(0xFFF48FB1), 0.16f, 1.0f, 16f, 14f)
)

private val musicNotes = listOf(
    MusicNote(0.08f, 0.95f, 0.12f, 0.0f, 16f),
    MusicNote(0.22f, 0.90f, 0.10f, 1.5f, 14f),
    MusicNote(0.38f, 0.92f, 0.14f, 3.0f, 18f),
    MusicNote(0.55f, 0.88f, 0.11f, 4.5f, 15f),
    MusicNote(0.70f, 0.93f, 0.13f, 6.0f, 17f),
    MusicNote(0.85f, 0.90f, 0.09f, 7.5f, 13f),
    MusicNote(0.15f, 0.85f, 0.15f, 9.0f, 16f),
    MusicNote(0.48f, 0.96f, 0.10f, 10.5f, 14f)
)

// --- Drawing helpers ---

private fun DrawScope.drawMusicNote(center: Offset, noteSize: Float, alpha: Float) {
    val color = Color.White.copy(alpha = alpha)
    // Note head (oval)
    drawCircle(
        color = color,
        radius = noteSize * 0.4f,
        center = center
    )
    // Stem
    val stemPath = Path().apply {
        moveTo(center.x + noteSize * 0.35f, center.y)
        lineTo(center.x + noteSize * 0.35f, center.y - noteSize * 1.5f)
    }
    drawPath(stemPath, color, style = Stroke(width = noteSize * 0.12f))
    // Flag
    val flagPath = Path().apply {
        moveTo(center.x + noteSize * 0.35f, center.y - noteSize * 1.5f)
        cubicTo(
            center.x + noteSize * 0.8f, center.y - noteSize * 1.3f,
            center.x + noteSize * 0.6f, center.y - noteSize * 0.9f,
            center.x + noteSize * 0.35f, center.y - noteSize * 0.8f
        )
    }
    drawPath(flagPath, color, style = Stroke(width = noteSize * 0.1f))
}

// --- Composable ---

@Composable
fun LofiBackground(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "lofi")

    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 125.66371f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 240000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Dark gradient background
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1A1034),
                    Color(0xFF0D1B2A),
                    Color(0xFF1B2838)
                )
            )
        )

        // Bokeh circles (behind everything)
        for (bokeh in bokehCircles) {
            val animX = bokeh.xFraction * w + sin(time * bokeh.speed + bokeh.phase) * bokeh.driftX
            val animY = bokeh.yFraction * h + sin(time * bokeh.speed * 0.6f + bokeh.phase + 1.0f) * bokeh.driftY
            val pulseAlpha = (sin(time * bokeh.speed * 0.5f + bokeh.phase) * 0.03f + 0.06f).coerceIn(0.03f, 0.09f)
            drawCircle(
                color = bokeh.color.copy(alpha = pulseAlpha),
                radius = bokeh.radius,
                center = Offset(animX, animY)
            )
            // Soft ring
            drawCircle(
                color = bokeh.color.copy(alpha = pulseAlpha * 0.5f),
                radius = bokeh.radius * 1.3f,
                center = Offset(animX, animY)
            )
        }

        // Floating orbs
        for (orb in orbs) {
            val animX = orb.xFraction * w + sin(time * orb.speed + orb.phase) * orb.driftX
            val animY = orb.yFraction * h + sin(time * orb.speed * 0.7f + orb.phase + 1.5f) * orb.driftY
            drawCircle(
                color = orb.color.copy(alpha = 0.18f),
                radius = orb.radius,
                center = Offset(animX, animY)
            )
            drawCircle(
                color = orb.color.copy(alpha = 0.08f),
                radius = orb.radius * 1.6f,
                center = Offset(animX, animY)
            )
        }

        // Sparkle stars (pulse size + brightness with flash)
        for (star in stars) {
            val sparkle = sin(time * star.speed * 2f + star.phase)
            val alpha = (sparkle * 0.45f + 0.5f).coerceIn(0.05f, 0.95f)
            val radius = star.baseRadius * (1f + sparkle * 0.5f).coerceIn(0.5f, 2.0f)

            // Bright flash when sparkle peaks
            if (sparkle > 0.7f) {
                val flashAlpha = ((sparkle - 0.7f) / 0.3f * 0.3f).coerceIn(0f, 0.3f)
                drawCircle(
                    color = Color.White.copy(alpha = flashAlpha),
                    radius = radius * 3f,
                    center = Offset(star.xFraction * w, star.yFraction * h)
                )
            }

            // Star cross rays
            val rayLen = radius * 2.5f
            val rayAlpha = alpha * 0.6f
            val cx = star.xFraction * w
            val cy = star.yFraction * h
            drawLine(
                Color.White.copy(alpha = rayAlpha),
                Offset(cx - rayLen, cy),
                Offset(cx + rayLen, cy),
                strokeWidth = 1f
            )
            drawLine(
                Color.White.copy(alpha = rayAlpha),
                Offset(cx, cy - rayLen),
                Offset(cx, cy + rayLen),
                strokeWidth = 1f
            )

            // Core dot
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = radius,
                center = Offset(cx, cy)
            )
        }

        // Shooting stars
        for (ss in shootingStars) {
            // Each shooting star appears briefly in a cycle
            val cycle = (time * ss.speed + ss.phase) % 12.566371f // 2 full cycles of 2*PI
            val progress = (sin(cycle) + 1f) / 2f // 0 to 1
            val visible = progress > 0.85f // only visible for a brief window
            if (visible) {
                val streakProgress = (progress - 0.85f) / 0.15f // 0 to 1 within visible window
                val startX = ss.startXFraction * w + streakProgress * ss.length * cos(ss.angle) * 2f
                val startY = ss.startYFraction * h + streakProgress * ss.length * sin(ss.angle) * 2f
                val endX = startX + ss.length * cos(ss.angle)
                val endY = startY + ss.length * sin(ss.angle)
                val streakAlpha = (1f - abs(streakProgress - 0.5f) * 2f).coerceIn(0f, 0.8f)

                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = streakAlpha),
                            Color.White.copy(alpha = 0f)
                        ),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY)
                    ),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 2.5f
                )
                // Bright head
                drawCircle(
                    color = Color.White.copy(alpha = streakAlpha),
                    radius = 3f,
                    center = Offset(startX, startY)
                )
            }
        }

        // Floating music notes
        for (note in musicNotes) {
            // Notes float upward and fade, cycling continuously
            val cycle = (time * note.speed + note.phase) % 6.2831855f
            val floatProgress = cycle / 6.2831855f // 0 to 1
            val noteX = note.xFraction * w + sin(time * 0.3f + note.phase) * 20f
            val noteY = note.startYFraction * h - floatProgress * h * 0.6f
            val noteAlpha = if (floatProgress < 0.1f) {
                floatProgress / 0.1f * 0.4f
            } else if (floatProgress > 0.7f) {
                (1f - (floatProgress - 0.7f) / 0.3f) * 0.4f
            } else {
                0.4f
            }

            if (noteAlpha > 0.01f) {
                drawMusicNote(Offset(noteX, noteY), note.size, noteAlpha)
            }
        }
    }
}
