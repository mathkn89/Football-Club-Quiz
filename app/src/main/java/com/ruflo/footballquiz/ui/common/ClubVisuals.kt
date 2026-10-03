package com.ruflo.footballquiz.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.ruflo.footballquiz.domain.model.Kit
import com.ruflo.footballquiz.domain.model.KitColour
import com.ruflo.footballquiz.domain.model.KitPattern

/** The fixed RGB value behind each kit colour name. */
fun KitColour.color(): Color = when (this) {
    KitColour.RED -> Color(0xFFD71920)
    KitColour.WHITE -> Color(0xFFFFFFFF)
    KitColour.BLACK -> Color(0xFF1A1A1A)
    KitColour.NAVY -> Color(0xFF14254F)
    KitColour.BLUE -> Color(0xFF1F4FBF)
    KitColour.SKY -> Color(0xFF7DB7E8)
    KitColour.CLARET -> Color(0xFF7A1F3D)
    KitColour.AMBER -> Color(0xFFF5A300)
    KitColour.GOLD -> Color(0xFFE5A92E)
    KitColour.YELLOW -> Color(0xFFFFD400)
    KitColour.ORANGE -> Color(0xFFF26A1B)
    KitColour.GREEN -> Color(0xFF1E7B3A)
    KitColour.LIME -> Color(0xFFA6CE39)
}

/**
 * Our own plain shield in the club's colours: body in the main shirt colour, a band in the
 * second colour (or the shorts colour for one-colour shirts). Not the club's crest.
 */
@Composable
fun ClubShield(kit: Kit?, modifier: Modifier = Modifier) {
    val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    val neutral = MaterialTheme.colorScheme.surfaceContainerHigh
    val body = kit?.primary?.color() ?: neutral
    val band = when {
        kit == null -> neutral
        kit.secondary != null -> kit.secondary.color()
        kit.shorts != kit.primary -> kit.shorts.color()
        else -> if (kit.primary == KitColour.WHITE) KitColour.BLACK.color() else Color.White
    }
    Canvas(
        modifier
            .aspectRatio(100f / 120f)
            .semantics { kit?.let { contentDescription = "${it.shirtDescription} shield" } },
    ) {
        scale(size.width / 100f, size.height / 120f, pivot = Offset.Zero) {
            val shield = Path().apply {
                moveTo(6f, 6f); lineTo(94f, 6f); lineTo(94f, 60f)
                cubicTo(94f, 90f, 72f, 106f, 50f, 115f)
                cubicTo(28f, 106f, 6f, 90f, 6f, 60f); close()
            }
            clipPath(shield) {
                drawRect(body, size = Size(100f, 120f))
                drawRect(band, topLeft = Offset(0f, 50f), size = Size(100f, 16f))
            }
            drawPath(shield, outline, style = Stroke(width = 2.5f))
        }
    }
}

/** Generic home kit (shirt + shorts) in the club's colours — no crest, sponsor or real design. */
@Composable
fun KitShirt(kit: Kit, modifier: Modifier = Modifier) {
    val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    Canvas(
        modifier
            .aspectRatio(120f / 160f)
            .semantics { contentDescription = "${kit.shirtDescription} shirt, ${kit.shorts.label} shorts" },
    ) {
        scale(size.width / 120f, size.height / 160f, pivot = Offset.Zero) {
            drawShorts(kit.shorts.color(), outline)
            drawShirt(kit, outline)
        }
    }
}

private val shirtBody = Path().apply {
    moveTo(38f, 6f); cubicTo(40f, 16f, 48f, 22f, 60f, 22f); cubicTo(72f, 22f, 80f, 16f, 82f, 6f)
    lineTo(92f, 9f); lineTo(92f, 108f); lineTo(28f, 108f); lineTo(28f, 9f); close()
}
private val leftSleeve = Path().apply {
    moveTo(38f, 6f); lineTo(22f, 10f); lineTo(4f, 32f); lineTo(20f, 46f); lineTo(28f, 38f); lineTo(28f, 9f); close()
}
private val rightSleeve = Path().apply {
    moveTo(82f, 6f); lineTo(98f, 10f); lineTo(116f, 32f); lineTo(100f, 46f); lineTo(92f, 38f); lineTo(92f, 9f); close()
}
private val shorts = Path().apply {
    moveTo(30f, 104f); lineTo(90f, 104f); lineTo(96f, 154f); lineTo(64f, 154f); lineTo(60f, 136f)
    lineTo(56f, 154f); lineTo(24f, 154f); close()
}

private fun DrawScope.drawShorts(colour: Color, outline: Color) {
    drawPath(shorts, colour)
    drawPath(shorts, outline, style = Stroke(width = 1.5f))
}

private fun DrawScope.drawShirt(kit: Kit, outline: Color) {
    val main = kit.primary.color()
    val second = kit.secondary?.color() ?: main
    val (leftSleeveColour, rightSleeveColour) = when (kit.pattern) {
        KitPattern.SLEEVES -> second to second
        KitPattern.HALVES, KitPattern.QUARTERS -> main to second
        else -> main to main
    }
    drawPath(leftSleeve, leftSleeveColour)
    drawPath(rightSleeve, rightSleeveColour)
    clipPath(shirtBody) {
        drawRect(main, topLeft = Offset(0f, 0f), size = Size(120f, 110f))
        when (kit.pattern) {
            KitPattern.STRIPES -> for (x in listOf(36f, 52f, 68f, 84f)) {
                drawRect(second, topLeft = Offset(x, 0f), size = Size(8f, 110f))
            }
            KitPattern.HOOPS -> for (y in listOf(30f, 52f, 74f, 96f)) {
                drawRect(second, topLeft = Offset(0f, y), size = Size(120f, 11f))
            }
            KitPattern.HALVES -> drawRect(second, topLeft = Offset(60f, 0f), size = Size(60f, 110f))
            KitPattern.QUARTERS -> {
                drawRect(second, topLeft = Offset(60f, 0f), size = Size(60f, 56f))
                drawRect(second, topLeft = Offset(0f, 56f), size = Size(60f, 54f))
            }
            KitPattern.PLAIN, KitPattern.SLEEVES -> Unit
        }
    }
    listOf(shirtBody, leftSleeve, rightSleeve).forEach { drawPath(it, outline, style = Stroke(width = 1.5f)) }
}
