package com.makn.footballquiz.ui.quiz

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makn.footballquiz.R
import com.makn.footballquiz.domain.model.QuizMode
import com.makn.footballquiz.ui.common.FootballQuizTopBar
import com.makn.footballquiz.ui.common.KitShirt
import com.makn.footballquiz.ui.common.QuizImage
import com.makn.footballquiz.ui.theme.TimerCritical
import com.makn.footballquiz.ui.theme.TimerSafe
import com.makn.footballquiz.ui.theme.TimerWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    /** Null when the round can't be replayed (the daily challenge). */
    onPlayAgain: (() -> Unit)?,
    onExit: () -> Unit,
    /** Leaving the results screen; may show a between-rounds ad first. */
    onLeaveResults: (then: () -> Unit) -> Unit = { it() },
    monetization: ResultsMonetization = ResultsMonetization(),
    onRoundCompleted: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showExitConfirm by remember { mutableStateOf(false) }
    val inProgress = uiState as? QuizUiState.InProgress

    // Back mid-round asks first instead of silently throwing the round away.
    BackHandler(enabled = inProgress != null) { showExitConfirm = true }

    val finished = uiState as? QuizUiState.Finished
    if (finished != null) {
        LaunchedEffect(finished) { onRoundCompleted() }
        RoundResults(
            state = finished,
            onPlayAgain = onPlayAgain?.let { playAgain -> { onLeaveResults(playAgain) } },
            onDone = { onLeaveResults(onExit) },
            monetization = monetization,
            onContinueSurvival = viewModel::continueSurvival,
        )
        return
    }

    Scaffold(
        topBar = {
            FootballQuizTopBar(
                title = inProgress?.let { progressTitle(it) }.orEmpty(),
                navigationIcon = {
                    IconButton(onClick = { if (inProgress != null) showExitConfirm = true else onExit() }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.quiz_exit))
                    }
                },
                actions = {
                    if (inProgress != null) {
                        Text(
                            if (inProgress.mode == QuizMode.DUEL) {
                                stringResource(R.string.duel_score, inProgress.score, inProgress.secondScore)
                            } else {
                                stringResource(R.string.quiz_score, inProgress.score)
                            },
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
                        stringResource(if (inProgress.endsRound) R.string.quiz_see_results else R.string.quiz_next),
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
                is QuizUiState.InProgress -> if (state.duelPhase == DuelPhase.HANDOFF) {
                    HandoffContent(onReady = viewModel::startSecondTurn)
                } else {
                    QuizContent(state = state, onOptionSelected = viewModel::selectOption)
                }
                is QuizUiState.Finished -> Unit
            }
        }
    }

    if (showExitConfirm) {
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            title = { Text(stringResource(R.string.quiz_leave_title)) },
            text = { Text(stringResource(R.string.quiz_leave_text)) },
            confirmButton = {
                TextButton(onClick = { showExitConfirm = false; onExit() }) { Text(stringResource(R.string.quiz_leave)) }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirm = false }) { Text(stringResource(R.string.quiz_keep_playing)) }
            },
        )
    }
}

@Composable
private fun progressTitle(state: QuizUiState.InProgress): String = when (state.mode) {
    QuizMode.SURVIVAL -> stringResource(R.string.quiz_survival_progress, state.questionNumber)
    else -> stringResource(R.string.quiz_progress, state.questionNumber, state.totalQuestions)
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
            stringResource(R.string.quiz_empty),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onBack) { Text(stringResource(R.string.quiz_change_options)) }
    }
}

/** Between duel turns: hides the question so the second player starts fresh. */
@Composable
private fun HandoffContent(onReady: () -> Unit) {
    val player2 = stringResource(R.string.duel_player2)
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            stringResource(R.string.duel_handoff_title, player2),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.duel_handoff_text, stringResource(R.string.duel_player1)),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        Button(onClick = onReady, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text(stringResource(R.string.duel_handoff_ready, player2), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun QuizContent(state: QuizUiState.InProgress, onOptionSelected: (Int) -> Unit) {
    val question = state.currentQuestion
    val timeFraction = state.timeFraction
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
                stringResource(R.string.quiz_seconds, state.timeRemainingSeconds),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.padding(start = 12.dp),
            )
        }

        if (state.mode == QuizMode.DUEL && !state.isAnswerRevealed) {
            Spacer(Modifier.height(16.dp))
            val player = stringResource(
                if (state.duelPhase == DuelPhase.FIRST_PLAYER) R.string.duel_player1 else R.string.duel_player2,
            )
            Text(
                stringResource(R.string.duel_turn, player),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(Modifier.height(if (state.mode == QuizMode.DUEL) 12.dp else 28.dp))
        if (question.kit != null) {
            KitShirt(question.kit, Modifier.height(170.dp).align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(20.dp))
        } else if (question.imageUrl != null) {
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
            Spacer(Modifier.height(6.dp))
            Text(feedback(state), style = MaterialTheme.typography.titleMedium)
            question.explanation?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun feedback(state: QuizUiState.InProgress): String {
    val correct = state.currentQuestion.correctOptionIndex
    if (state.mode == QuizMode.DUEL) {
        fun mark(pick: Int?) = if (pick == correct) "✓" else "✗"
        return "${stringResource(R.string.duel_player1)} ${mark(state.firstPlayerPick)} · " +
            "${stringResource(R.string.duel_player2)} ${mark(state.selectedOptionIndex)}"
    }
    return stringResource(
        when (state.selectedOptionIndex) {
            null -> R.string.quiz_times_up
            correct -> R.string.quiz_correct
            else -> R.string.quiz_not_quite
        },
    )
}
