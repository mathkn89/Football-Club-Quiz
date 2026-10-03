package com.makn.footballquiz.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

/**
 * Renders a curated question's [imageUrl] (a full http(s) URL synced from the server).
 */
@Composable
fun QuizImage(imageUrl: String?, modifier: Modifier = Modifier) {
    if (imageUrl.isNullOrBlank()) return

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier,
        )
    }
}
