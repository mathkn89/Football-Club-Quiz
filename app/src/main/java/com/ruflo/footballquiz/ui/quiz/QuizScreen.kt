package com.ruflo.footballquiz.ui.quiz

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ruflo.footballquiz.ui.common.FootballQuizTopBar
import com.ruflo.footballquiz.ui.common.QuizImage
import com.ruflo.footballquiz.ui.theme.TimerCritical
import com.ruflo.footballquiz.ui.theme.TimerSafe
import com.ruflo.footballquiz.ui.theme.TimerWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    onFinished: (score: Int, total: Int) -> Unit,
    onExit: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showExitConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            FootballQuizTopBar(
                title = "",
                navigationIcon = {
                    IconButton(onClick = { showExitConfirm = true }) {
                        Icon(Icons.Default.Close, contentDescription = "Exit quiz")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState) {
                QuizUiState.Loading -> LoadingContent()
                QuizUiState.Empty -> EmptyContent()
                is QuizUiState.Finished -> LaunchedEffect(state) { onFinished(state.score, state.total) }
                is QuizUiState.InProgress -> QuizContent(
                    state = state,
                    onOptionSelected = viewModel::selectOption,
                    onNext = viewModel::nextQuestion,
                )
            }
        }
    }

    if (showExitConfirm) {
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            title = { Text("Leave quiz?") },
            text = { Text("Your progress in this round will be lost.") },
            confirmButton = {
                Button(onClick = { showExitConfirm = false; onExit() }) { Text("Leave") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showExitConfirm = false }) { Text("Keep playing") }
            },
        )
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Building your quiz round…", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun EmptyContent() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "No questions available for the selected categories yet.",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun QuizContent(
    state: QuizUiState.InProgress,
    onOptionSelected: (Int) -> Unit,
    onNext: () -> Unit,
) {
    val question = state.currentQuestion
    val timeFraction = state.timeRemainingSeconds / QUESTION_TIME_SECONDS.toFloat()
    val timerColor by animateColorAsState(
        targetValue = when {
            timeFraction > 0.5f -> TimerSafe
            timeFraction > 0.2f -> TimerWarning
            else -> TimerCritical
        },
        label = "timerColor",
    )

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "Question ${state.questionNumber}/${state.totalQuestions}",
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                "Score: ${state.score}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { timeFraction },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = timerColor,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        )

        Spacer(Modifier.height(20.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                QuizImage(imageUrl = question.imageUrl, modifier = Modifier.fillMaxWidth().height(140.dp))
                if (question.imageUrl != null) Spacer(Modifier.height(16.dp))
                Text(question.prompt, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(20.dp))
        question.options.forEachIndexed { index, option ->
            OptionCard(
                text = option,
                visualState = optionVisualState(state, index),
                enabled = !state.isAnswerRevealed,
                onClick = { onOptionSelected(index) },
            )
            Spacer(Modifier.height(12.dp))
        }

        if (state.isAnswerRevealed) {
            question.explanation?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(if (state.isLastQuestion) "See results" else "Next question")
            }
            Spacer(Modifier.height(16.dp))
        } else {
            Spacer(Modifier.weight(1f))
        }
    }
}
