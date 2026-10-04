package com.makn.footballquiz.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.makn.footballquiz.BuildConfig

/**
 * Anchored adaptive banner, sized for the screen width like Google recommends. The slot keeps
 * its height while loading so the layout doesn't jump when the ad arrives.
 */
@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    BoxWithConstraints(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val widthDp = maxWidth.value.toInt()
        val adSize = remember(widthDp) { AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp) }
        val heightDp = with(LocalDensity.current) { adSize.getHeightInPixels(context).toDp() }
        val adView = remember(adSize) {
            AdView(context).apply {
                setAdSize(adSize)
                adUnitId = BuildConfig.ADMOB_BANNER_ID
                loadAd(AdRequest.Builder().build())
            }
        }
        DisposableEffect(adView) { onDispose { adView.destroy() } }
        Box(Modifier.fillMaxWidth().height(heightDp.coerceAtLeast(50.dp)), contentAlignment = Alignment.Center) {
            AndroidView(factory = { adView })
        }
    }
}
