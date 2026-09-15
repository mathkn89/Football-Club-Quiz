package com.ruflo.footballquiz.ui.results

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ResultsScreen(score: Int, total: Int, onPlayAgain: () -> Unit) {
    var animationTarget by remember { mutableIntStateOf(0) }
    LaunchedEffect(score) { animationTarget = score }
    val animatedScore by animateIntAsState(
        targetValue = animationTarget,
        animationSpec = tween(durationMillis = 800),
        label = "animatedScore",
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(resultEmoji(score, total), style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(8.dp))
        Text("Quiz complete!", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(24.dp))
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 48.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "$animatedScore / $total",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(resultMessage(score, total), style = MaterialTheme.typography.bodyLarge)

        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onPlayAgain,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text("🔁 Play again", style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun resultEmoji(score: Int, total: Int): String {
    if (total == 0) return "🤔"
    return when (score.toFloat() / total) {
        1f -> "🏆"
        in 0.7f..1f -> "👏"
        in 0.4f..0.7f -> "💪"
        else -> "📚"
    }
}

private fun resultMessage(score: Int, total: Int): String {
    if (total == 0) return "No questions were available."
    return when (score.toFloat() / total) {
        1f -> "Perfect score!"
        in 0.7f..1f -> "Great job!"
        in 0.4f..0.7f -> "Not bad, keep practicing."
        else -> "Room to improve — try again!"
    }
}
