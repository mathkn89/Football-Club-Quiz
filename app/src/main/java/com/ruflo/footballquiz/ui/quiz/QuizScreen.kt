package com.ruflo.footballquiz.ui.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ruflo.footballquiz.ui.common.QuizImage

@Composable
fun QuizScreen(viewModel: QuizViewModel, onFinished: (score: Int, total: Int) -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.finishedEvent.collect { (score, total) -> onFinished(score, total) }
    }

    when (val state = uiState) {
        QuizUiState.Loading -> LoadingContent()
        QuizUiState.Empty -> EmptyContent()
        is QuizUiState.InProgress -> QuizContent(
            state = state,
            onOptionSelected = viewModel::selectOption,
            onNext = viewModel::nextQuestion,
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Question ${state.questionNumber}/${state.totalQuestions}", style = MaterialTheme.typography.labelLarge)
            Text("Score: ${state.score}", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { state.timeRemainingSeconds / QUESTION_TIME_SECONDS.toFloat() },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))
        QuizImage(imageUrl = question.imageUrl, modifier = Modifier.fillMaxWidth().height(160.dp))

        Spacer(Modifier.height(16.dp))
        Text(question.prompt, style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(24.dp))
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
            }
            Spacer(Modifier.weight(1f))
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                Text(if (state.isLastQuestion) "See results" else "Next question")
            }
        }
    }
}
