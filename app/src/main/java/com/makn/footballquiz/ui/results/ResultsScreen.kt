package com.makn.footballquiz.ui.results

import com.makn.footballquiz.R
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ResultsScreen(score: Int, total: Int, onPlayAgain: () -> Unit, onDone: () -> Unit) {
    var animationTarget by remember { mutableIntStateOf(0) }
    LaunchedEffect(score) { animationTarget = score }
    val animatedScore by animateIntAsState(
        targetValue = animationTarget,
        animationSpec = tween(durationMillis = 800),
        label = "animatedScore",
    )

    BackHandler(onBack = onDone)

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                stringResource(resultTitle(score, total)),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.results_score, animatedScore, total),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(resultMessage(score, total)),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }

        Button(
            onClick = onPlayAgain,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(stringResource(R.string.results_play_again), style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onDone, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text(stringResource(R.string.results_done))
        }
    }
}

private fun resultTitle(score: Int, total: Int): Int {
    if (total == 0) return R.string.results_title_over
    return when (score.toFloat() / total) {
        1f -> R.string.results_title_perfect
        in 0.7f..1f -> R.string.results_title_great
        in 0.4f..0.7f -> R.string.results_title_good
        else -> R.string.results_title_over
    }
}

private fun resultMessage(score: Int, total: Int): Int {
    if (total == 0) return R.string.results_msg_none
    return when (score.toFloat() / total) {
        1f -> R.string.results_msg_perfect
        in 0.7f..1f -> R.string.results_msg_great
        in 0.4f..0.7f -> R.string.results_msg_good
        else -> R.string.results_msg_low
    }
}
