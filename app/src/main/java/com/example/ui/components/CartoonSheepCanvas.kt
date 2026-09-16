package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun CartoonSheepCanvas(
    isNight: Boolean,
    modifier: Modifier = Modifier
) {
    // Background color animation transitioning after sunset to calming deep blue
    val topColor by animateColorAsState(
        targetValue = if (isNight) Color(0xFF0A1128) else Color(0xFF4EA8DE),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "topColor"
    )
    val midColor by animateColorAsState(
        targetValue = if (isNight) Color(0xFF1C2541) else Color(0xFF72EFDD),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "midColor"
    )
    val plainBlurColor by animateColorAsState(
        targetValue = if (isNight) Color(0xFF0B132B) else Color(0xFFE8F1F5),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "plainColor"
    )

    // Gentle breathing animation for sheep wool and stars
    val infiniteTransition = rememberInfiniteTransition(label = "sheepMotion")
    val breathingOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )
    val sleepZOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sleepZ"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Sky & Ground Gradient: Transitions into a calming deep blue,
            // and blurs into a clean plain color for maximum readability of text & numbers
            val bgGradient = Brush.verticalGradient(
                colors = listOf(
                    topColor,
                    midColor.copy(alpha = 0.85f),
                    plainBlurColor.copy(alpha = 0.95f),
                    plainBlurColor
                ),
                startY = 0f,
                endY = h * 0.72f
            )
            drawRect(brush = bgGradient, size = size)

            if (isNight) {
                // Calming starry night sky
                drawStars(w, h * 0.40f)
                // Gentle crescent moon in the night sky - moved down below control buttons
                drawNightMoon(w * 0.82f, h * 0.16f, 26f)
            } else {
                // Daytime gentle golden sun glow - moved down below control buttons
                drawCircle(
                    color = Color(0xFFFFD166).copy(alpha = 0.35f),
                    radius = 55f,
                    center = Offset(w * 0.82f, h * 0.16f)
                )
                drawCircle(
                    color = Color(0xFFFFE066),
                    radius = 32f,
                    center = Offset(w * 0.82f, h * 0.16f)
                )
            }

            // 2. Cartoon Sheep in the upper backdrop
            // Moved down comfortably so it is never covered by top control buttons
            val sheepCenterX = w * 0.5f
            val sheepCenterY = h * 0.26f + breathingOffset
            val sheepScale = (w / 380f).coerceIn(0.95f, 1.30f)

            drawCartoonSheep(
                centerX = sheepCenterX,
                centerY = sheepCenterY,
                scale = sheepScale,
                isNight = isNight,
                sleepZOffset = sleepZOffset
            )

            // 3. Smooth blur/veil transition to plain colour so foreground numbers & icons have maximum readability
            val plainVeil = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    plainBlurColor.copy(alpha = 0.55f),
                    plainBlurColor.copy(alpha = 0.95f),
                    plainBlurColor
                ),
                startY = h * 0.40f,
                endY = h * 0.55f
            )
            drawRect(
                brush = plainVeil,
                topLeft = Offset(0f, h * 0.40f),
                size = Size(w, h * 0.60f)
            )
        }
    }
}

internal fun DrawScope.drawCartoonSheep(
    centerX: Float,
    centerY: Float,
    scale: Float,
    isNight: Boolean,
    sleepZOffset: Float
) {
    // Defined cartoon styling colors
    val outlineColor = Color(0xFF263238) // Crisp slate dark outline for definition
    val woolColor = Color.White
    val woolShadow = Color(0xFFE2E8F0) // Dimensional shadow on lower wool
    val skinColor = Color(0xFFFCE7E9) // Soft rosy skin
    val earInner = Color(0xFFF9A8D4) // Cheerful pink inner ear
    val hoofColor = Color(0xFF1E293B) // Dark charcoal hoof

    // 0. Soft ground shadow under sheep
    drawOval(
        color = if (isNight) Color(0xFF0F172A).copy(alpha = 0.35f) else Color(0xFF64748B).copy(alpha = 0.18f),
        topLeft = Offset(centerX - 48f * scale, centerY + 58f * scale),
        size = Size(96f * scale, 12f * scale)
    )

    // 1. Little tail on the left with outline
    drawCircle(
        color = outlineColor,
        radius = 12f * scale,
        center = Offset(centerX - 56f * scale, centerY + 14f * scale)
    )
    drawCircle(
        color = woolColor,
        radius = 10f * scale,
        center = Offset(centerX - 56f * scale, centerY + 14f * scale)
    )

    // 2. Defined sheep legs with hooves and split notch
    val legW = 12f * scale
    val legH = 24f * scale
    val legY = centerY + 36f * scale
    val legPositions = listOf(
        centerX - 32f * scale,
        centerX - 10f * scale,
        centerX + 12f * scale,
        centerX + 34f * scale
    )
    legPositions.forEach { legX ->
        // Leg outline
        drawRoundRect(
            color = outlineColor,
            topLeft = Offset(legX - 1.2f * scale, legY - 1.2f * scale),
            size = Size(legW + 2.4f * scale, legH + 2.4f * scale),
            cornerRadius = CornerRadius(6f * scale, 6f * scale)
        )
        // Leg skin fill
        drawRoundRect(
            color = skinColor,
            topLeft = Offset(legX, legY),
            size = Size(legW, legH),
            cornerRadius = CornerRadius(5f * scale, 5f * scale)
        )
        // Hoof cap (dark lower portion)
        val hoofH = 9f * scale
        drawRoundRect(
            color = hoofColor,
            topLeft = Offset(legX, legY + legH - hoofH),
            size = Size(legW, hoofH),
            cornerRadius = CornerRadius(5f * scale, 5f * scale)
        )
        // Split hoof line
        drawLine(
            color = outlineColor,
            start = Offset(legX + legW / 2f, legY + legH - hoofH + 2f * scale),
            end = Offset(legX + legW / 2f, legY + legH),
            strokeWidth = 1.5f * scale,
            cap = StrokeCap.Round
        )
    }

    // 3. Fluffy cloud wool body puffs
    val puffs = listOf(
        Offset(-42f, 10f) to 34f,
        Offset(42f, 10f) to 34f,
        Offset(-25f, -22f) to 36f,
        Offset(25f, -22f) to 36f,
        Offset(0f, -32f) to 38f,
        Offset(-50f, -6f) to 30f,
        Offset(50f, -6f) to 30f,
        Offset(0f, 16f) to 36f,
        Offset(0f, -2f) to 48f
    )

    // A. Outer outline of all wool puffs for clear silhouette definition
    puffs.forEach { (offset, r) ->
        drawCircle(
            color = outlineColor,
            radius = (r + 2.2f) * scale,
            center = Offset(centerX + offset.x * scale, centerY + offset.y * scale)
        )
    }

    // B. Dimensional shading on lower wool puffs
    puffs.forEach { (offset, r) ->
        drawCircle(
            color = woolShadow,
            radius = r * scale,
            center = Offset(centerX + offset.x * scale, centerY + offset.y * scale + 2.5f * scale)
        )
    }

    // C. Bright white wool fill
    puffs.forEach { (offset, r) ->
        drawCircle(
            color = woolColor,
            radius = r * scale,
            center = Offset(centerX + offset.x * scale, centerY + offset.y * scale)
        )
    }

    // D. Curly fleece swirl accents for cartoon wool texture
    val curlAccents = listOf(
        Triple(-30f, 6f, 10f),
        Triple(32f, 8f, 10f),
        Triple(-18f, -14f, 12f),
        Triple(20f, -14f, 12f),
        Triple(0f, -24f, 11f),
        Triple(-38f, -8f, 8f),
        Triple(38f, -6f, 8f)
    )
    curlAccents.forEach { (cx, cy, r) ->
        val curlPath = Path().apply {
            moveTo(
                centerX + (cx - r * 0.6f) * scale,
                centerY + (cy + r * 0.2f) * scale
            )
            quadraticTo(
                centerX + cx * scale,
                centerY + (cy - r * 0.6f) * scale,
                centerX + (cx + r * 0.5f) * scale,
                centerY + cy * scale
            )
            quadraticTo(
                centerX + (cx + 0.1f * r) * scale,
                centerY + (cy + r * 0.5f) * scale,
                centerX + (cx - 0.2f * r) * scale,
                centerY + (cy + 0.2f * r) * scale
            )
        }
        drawPath(
            path = curlPath,
            color = Color(0xFFCBD5E1),
            style = Stroke(width = 1.8f * scale, cap = StrokeCap.Round)
        )
    }

    // 4. Sheep Ears (defined outlines & inner pink)
    val earY = centerY - 6f * scale
    // Left ear outline & fill
    drawOval(
        color = outlineColor,
        topLeft = Offset(centerX - 49f * scale, earY - 1.5f * scale),
        size = Size(23f * scale, 15f * scale)
    )
    drawOval(
        color = skinColor,
        topLeft = Offset(centerX - 47f * scale, earY),
        size = Size(20f * scale, 12f * scale)
    )
    drawOval(
        color = earInner,
        topLeft = Offset(centerX - 43f * scale, earY + 2f * scale),
        size = Size(12f * scale, 8f * scale)
    )

    // Right ear outline & fill
    drawOval(
        color = outlineColor,
        topLeft = Offset(centerX + 26f * scale, earY - 1.5f * scale),
        size = Size(23f * scale, 15f * scale)
    )
    drawOval(
        color = skinColor,
        topLeft = Offset(centerX + 27f * scale, earY),
        size = Size(20f * scale, 12f * scale)
    )
    drawOval(
        color = earInner,
        topLeft = Offset(centerX + 31f * scale, earY + 2f * scale),
        size = Size(12f * scale, 8f * scale)
    )

    // 5. Sheep Head / Face oval with defined dark outline
    val headW = 56f * scale
    val headH = 52f * scale
    drawOval(
        color = outlineColor,
        topLeft = Offset(centerX - (headW + 3f * scale) / 2f, centerY - 15.5f * scale),
        size = Size(headW + 3f * scale, headH + 3f * scale)
    )
    drawOval(
        color = skinColor,
        topLeft = Offset(centerX - headW / 2f, centerY - 14f * scale),
        size = Size(headW, headH)
    )

    // 6. Wool crown puffs on top of head with defined outlines
    val crownPuffs = listOf(
        Triple(-10f, -14f, 13f),
        Triple(0f, -16f, 15f),
        Triple(10f, -14f, 13f)
    )
    crownPuffs.forEach { (ox, oy, r) ->
        drawCircle(
            color = outlineColor,
            radius = (r + 1.8f) * scale,
            center = Offset(centerX + ox * scale, centerY + oy * scale)
        )
    }
    crownPuffs.forEach { (ox, oy, r) ->
        drawCircle(
            color = woolColor,
            radius = r * scale,
            center = Offset(centerX + ox * scale, centerY + oy * scale)
        )
    }
    // Crown swirl accent
    val crownSwirl = Path().apply {
        moveTo(centerX - 4f * scale, centerY - 13f * scale)
        quadraticTo(centerX, centerY - 18f * scale, centerX + 4f * scale, centerY - 14f * scale)
    }
    drawPath(
        path = crownSwirl,
        color = Color(0xFFCBD5E1),
        style = Stroke(width = 1.6f * scale, cap = StrokeCap.Round)
    )

    // 7. Rosy Blush Cheeks
    drawCircle(
        color = Color(0xFFFF8FAB).copy(alpha = 0.55f),
        radius = 6.5f * scale,
        center = Offset(centerX - 18f * scale, centerY + 14f * scale)
    )
    drawCircle(
        color = Color(0xFFFF8FAB).copy(alpha = 0.55f),
        radius = 6.5f * scale,
        center = Offset(centerX + 18f * scale, centerY + 14f * scale)
    )

    // 8. Cute nose & smiling mouth
    val nosePath = Path().apply {
        moveTo(centerX - 4f * scale, centerY + 16f * scale)
        lineTo(centerX + 4f * scale, centerY + 16f * scale)
        lineTo(centerX, centerY + 20f * scale)
        close()
    }
    drawPath(path = nosePath, color = Color(0xFFF06292), style = Fill)

    drawLine(
        color = outlineColor,
        start = Offset(centerX, centerY + 20f * scale),
        end = Offset(centerX, centerY + 23f * scale),
        strokeWidth = 1.8f * scale,
        cap = StrokeCap.Round
    )

    val smilePath = Path().apply {
        moveTo(centerX - 7f * scale, centerY + 23f * scale)
        quadraticTo(centerX - 3.5f * scale, centerY + 27f * scale, centerX, centerY + 23.5f * scale)
        quadraticTo(centerX + 3.5f * scale, centerY + 27f * scale, centerX + 7f * scale, centerY + 23f * scale)
    }
    drawPath(
        path = smilePath,
        color = outlineColor,
        style = Stroke(width = 2.2f * scale, cap = StrokeCap.Round)
    )

    // 9. EYES:
    // "After sunset the sheeps eyes in the cartoon version should close"
    if (isNight) {
        // Peaceful Closed Eyes: well-defined curved arcs with cute eyelashes
        val eyeLeftArc = Path().apply {
            moveTo(centerX - 18f * scale, centerY + 5f * scale)
            quadraticTo(centerX - 12f * scale, centerY + 12f * scale, centerX - 6f * scale, centerY + 5f * scale)
        }
        val eyeRightArc = Path().apply {
            moveTo(centerX + 6f * scale, centerY + 5f * scale)
            quadraticTo(centerX + 12f * scale, centerY + 12f * scale, centerX + 18f * scale, centerY + 5f * scale)
        }
        drawPath(path = eyeLeftArc, color = outlineColor, style = Stroke(width = 2.8f * scale, cap = StrokeCap.Round))
        drawPath(path = eyeRightArc, color = outlineColor, style = Stroke(width = 2.8f * scale, cap = StrokeCap.Round))

        // Cute little eyelashes
        drawLine(
            color = outlineColor,
            start = Offset(centerX - 17f * scale, centerY + 5.5f * scale),
            end = Offset(centerX - 21f * scale, centerY + 3f * scale),
            strokeWidth = 2f * scale,
            cap = StrokeCap.Round
        )
        drawLine(
            color = outlineColor,
            start = Offset(centerX + 17f * scale, centerY + 5.5f * scale),
            end = Offset(centerX + 21f * scale, centerY + 3f * scale),
            strokeWidth = 2f * scale,
            cap = StrokeCap.Round
        )

        // Sleeping 'z Z Z' floating peacefully
        drawSleepZ(centerX + 34f * scale, centerY - 20f * scale - sleepZOffset, scale * 1.05f)
        drawSleepZ(centerX + 46f * scale, centerY - 35f * scale - sleepZOffset * 0.7f, scale * 0.8f)
        drawSleepZ(centerX + 55f * scale, centerY - 48f * scale - sleepZOffset * 0.5f, scale * 0.6f)
    } else {
        // Daytime Open Eyes: Joyful pupils with twin white specular highlights and delicate eyebrows
        drawCircle(color = outlineColor, radius = 5.5f * scale, center = Offset(centerX - 12f * scale, centerY + 7f * scale))
        drawCircle(color = Color.White, radius = 2.2f * scale, center = Offset(centerX - 13.8f * scale, centerY + 5.2f * scale))
        drawCircle(color = Color.White, radius = 1.0f * scale, center = Offset(centerX - 10.2f * scale, centerY + 8.5f * scale))

        drawCircle(color = outlineColor, radius = 5.5f * scale, center = Offset(centerX + 12f * scale, centerY + 7f * scale))
        drawCircle(color = Color.White, radius = 2.2f * scale, center = Offset(centerX + 10.2f * scale, centerY + 5.2f * scale))
        drawCircle(color = Color.White, radius = 1.0f * scale, center = Offset(centerX + 13.8f * scale, centerY + 8.5f * scale))

        // Tiny expressive eyebrows
        val leftBrow = Path().apply {
            moveTo(centerX - 17f * scale, centerY + 0.5f * scale)
            quadraticTo(centerX - 12f * scale, centerY - 2.5f * scale, centerX - 7f * scale, centerY + 0.5f * scale)
        }
        val rightBrow = Path().apply {
            moveTo(centerX + 7f * scale, centerY + 0.5f * scale)
            quadraticTo(centerX + 12f * scale, centerY - 2.5f * scale, centerX + 17f * scale, centerY + 0.5f * scale)
        }
        drawPath(path = leftBrow, color = outlineColor, style = Stroke(width = 1.8f * scale, cap = StrokeCap.Round))
        drawPath(path = rightBrow, color = outlineColor, style = Stroke(width = 1.8f * scale, cap = StrokeCap.Round))
    }
}

private fun DrawScope.drawSleepZ(x: Float, y: Float, s: Float) {
    val zPath = Path().apply {
        moveTo(x, y)
        lineTo(x + 10f * s, y)
        lineTo(x, y + 12f * s)
        lineTo(x + 10f * s, y + 12f * s)
    }
    drawPath(
        path = zPath,
        color = Color(0xFF90CAF9).copy(alpha = 0.85f),
        style = Stroke(width = 2f * s, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawNightMoon(cx: Float, cy: Float, r: Float) {
    // Glowing crescent moon
    drawCircle(
        color = Color(0xFFFFEAA7).copy(alpha = 0.25f),
        radius = r * 1.4f,
        center = Offset(cx, cy)
    )
    val moonPath = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(cx - r, cy - r, cx + r, cy + r))
    }
    drawPath(moonPath, color = Color(0xFFFFF9DB), style = Fill)
    // Dark cutout to make crisp crescent
    val shadowPath = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(cx - r * 0.65f, cy - r * 1.15f, cx + r * 1.25f, cy + r * 0.85f))
    }
    drawPath(shadowPath, color = Color(0xFF0A1128), style = Fill)
}

private fun DrawScope.drawStars(width: Float, maxH: Float) {
    val starCoords = listOf(
        Offset(0.12f, 0.08f) to 2.2f,
        Offset(0.25f, 0.16f) to 1.6f,
        Offset(0.38f, 0.06f) to 2.8f,
        Offset(0.62f, 0.14f) to 1.8f,
        Offset(0.74f, 0.05f) to 2.5f,
        Offset(0.88f, 0.18f) to 2.0f,
        Offset(0.08f, 0.24f) to 1.5f,
        Offset(0.92f, 0.28f) to 1.9f,
        Offset(0.48f, 0.11f) to 2.0f
    )
    starCoords.forEach { (rel, r) ->
        drawCircle(
            color = Color(0xFFFFF9DB).copy(alpha = 0.85f),
            radius = r,
            center = Offset(rel.x * width, rel.y * maxH)
        )
    }
}
