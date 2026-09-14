package com.ruflo.footballquiz.ui.quiz

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ruflo.footballquiz.ui.theme.CorrectGreen
import com.ruflo.footballquiz.ui.theme.IncorrectRed

enum class OptionVisualState { DEFAULT, CORRECT, INCORRECT }

fun optionVisualState(state: QuizUiState.InProgress, optionIndex: Int): OptionVisualState {
    if (!state.isAnswerRevealed) return OptionVisualState.DEFAULT
    return when (optionIndex) {
        state.currentQuestion.correctOptionIndex -> OptionVisualState.CORRECT
        state.selectedOptionIndex -> OptionVisualState.INCORRECT
        else -> OptionVisualState.DEFAULT
    }
}

@Composable
fun OptionCard(
    text: String,
    visualState: OptionVisualState,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = when (visualState) {
        OptionVisualState.CORRECT -> CorrectGreen
        OptionVisualState.INCORRECT -> IncorrectRed
        OptionVisualState.DEFAULT -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when (visualState) {
        OptionVisualState.DEFAULT -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onPrimary
    }

    Card(
        modifier = modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp),
        )
    }
}
