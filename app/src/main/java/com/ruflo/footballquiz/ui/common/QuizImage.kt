package com.ruflo.footballquiz.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.ruflo.footballquiz.domain.generator.BadgeQuestionGenerator.Companion.DRAWABLE_SCHEME

/**
 * Renders [imageUrl] for a quiz question. Handles both remote URLs (badge/custom-question images
 * synced from the server) and the `drawable://` scheme used for bundled badge drawables.
 */
@Composable
fun QuizImage(imageUrl: String?, modifier: Modifier = Modifier) {
    if (imageUrl.isNullOrBlank()) return
    val context = LocalContext.current

    val model = remember(imageUrl) {
        if (imageUrl.startsWith(DRAWABLE_SCHEME)) {
            val name = imageUrl.removePrefix(DRAWABLE_SCHEME)
            context.resources.getIdentifier(name, "drawable", context.packageName)
                .takeIf { it != 0 }
        } else {
            imageUrl
        }
    }

    if (model == null) return

    Box(modifier = modifier) {
        AsyncImage(
            model = model,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier,
        )
    }
}
