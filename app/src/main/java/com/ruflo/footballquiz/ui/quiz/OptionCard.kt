package com.ruflo.footballquiz.ui.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
    val targetContainerColor = when (visualState) {
        OptionVisualState.CORRECT -> CorrectGreen
        OptionVisualState.INCORRECT -> IncorrectRed
        OptionVisualState.DEFAULT -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val targetContentColor = when (visualState) {
        OptionVisualState.DEFAULT -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onPrimary
    }
    val containerColor by animateColorAsState(targetContainerColor, tween(250), label = "optionContainer")
    val contentColor by animateColorAsState(targetContentColor, tween(250), label = "optionContent")

    Card(
        modifier = modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (visualState == OptionVisualState.DEFAULT) 0.dp else 2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))

            AnimatedVisibility(visible = visualState != OptionVisualState.DEFAULT) {
                Icon(
                    imageVector = if (visualState == OptionVisualState.CORRECT) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = if (visualState == OptionVisualState.CORRECT) "Correct" else "Incorrect",
                )
            }
        }
    }
}
