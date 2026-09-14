package com.ruflo.footballquiz.ui.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ResultsScreen(score: Int, total: Int, onPlayAgain: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Quiz complete!", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Text("$score / $total", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(8.dp))
        Text(resultMessage(score, total), style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onPlayAgain, modifier = Modifier.fillMaxWidth()) {
            Text("Play again")
        }
    }
}

private fun resultMessage(score: Int, total: Int): String {
    if (total == 0) return "No questions were available."
    return when (val ratio = score.toFloat() / total) {
        1f -> "Perfect score!"
        in 0.7f..1f -> "Great job!"
        in 0.4f..0.7f -> "Not bad, keep practicing."
        else -> "Room to improve — try again!"
    }
}
