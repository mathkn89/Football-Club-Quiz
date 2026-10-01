package com.ruflo.footballquiz.ui.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
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
    val inProgress = uiState as? QuizUiState.InProgress

    // Back mid-round asks first instead of silently throwing the round away.
    BackHandler(enabled = inProgress != null) { showExitConfirm = true }

    Scaffold(
        topBar = {
            FootballQuizTopBar(
                title = inProgress?.let { "${it.questionNumber} of ${it.totalQuestions}" }.orEmpty(),
                navigationIcon = {
                    IconButton(onClick = { if (inProgress != null) showExitConfirm = true else onExit() }) {
                        Icon(Icons.Default.Close, contentDescription = "Exit quiz")
                    }
                },
                actions = {
                    if (inProgress != null) {
                        Text(
                            "Score ${inProgress.score}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 16.dp),
                        )
                    }
                },
            )
        },
        bottomBar = {
            if (inProgress?.isAnswerRevealed == true) {
                Button(
                    onClick = viewModel::nextQuestion,
                    modifier = Modifier.fillMaxWidth().padding(20.dp).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        if (inProgress.isLastQuestion) "See results" else "Next question",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState) {
                QuizUiState.Loading -> LoadingContent()
                QuizUiState.Empty -> EmptyContent(onExit)
                is QuizUiState.Finished -> LaunchedEffect(state) { onFinished(state.score, state.total) }
                is QuizUiState.InProgress -> QuizContent(state = state, onOptionSelected = viewModel::selectOption)
            }
        }
    }

    if (showExitConfirm) {
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            title = { Text("Leave quiz?") },
            text = { Text("Your progress in this round will be lost.") },
            confirmButton = {
                TextButton(onClick = { showExitConfirm = false; onExit() }) { Text("Leave") }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirm = false }) { Text("Keep playing") }
            },
        )
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyContent(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "Not enough questions for this selection yet. Try another league or more topics.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onBack) { Text("Change options") }
    }
}

@Composable
private fun QuizContent(state: QuizUiState.InProgress, onOptionSelected: (Int) -> Unit) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(
                progress = { timeFraction },
                modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = timerColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            )
            Text(
                "${state.timeRemainingSeconds}s",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.padding(start = 12.dp),
            )
        }

        Spacer(Modifier.height(28.dp))
        if (question.imageUrl != null) {
            QuizImage(imageUrl = question.imageUrl, modifier = Modifier.fillMaxWidth().height(140.dp))
            Spacer(Modifier.height(20.dp))
        }
        Text(question.prompt, style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(24.dp))
        question.options.forEachIndexed { index, option ->
            OptionCard(
                text = option,
                visualState = optionVisualState(state, index),
                enabled = !state.isAnswerRevealed,
                onClick = { onOptionSelected(index) },
            )
            Spacer(Modifier.height(10.dp))
        }

        if (state.isAnswerRevealed) {
            val feedback = when {
                state.selectedOptionIndex == null -> "Time's up."
                state.selectedOptionIndex == question.correctOptionIndex -> "Correct!"
                else -> "Not quite."
            }
            Spacer(Modifier.height(6.dp))
            Text(feedback, style = MaterialTheme.typography.titleMedium)
            question.explanation?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
