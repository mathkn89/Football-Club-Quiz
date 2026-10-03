package com.makn.footballquiz.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makn.footballquiz.R
import com.makn.footballquiz.domain.model.QuizAttempt
import com.makn.footballquiz.domain.model.QuizMode
import com.makn.footballquiz.ui.common.FootballQuizTopBar
import com.makn.footballquiz.ui.quiz.ReviewRow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.roundToInt

private val DETAIL_DATE_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)

/** One past round: score, when and how it was played, and every question with the answers given. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttemptDetailScreen(onBack: () -> Unit, viewModel: AttemptDetailViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var onlyMistakes by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            FootballQuizTopBar(
                title = stringResource(R.string.round_details),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { innerPadding ->
        when (val state = uiState) {
            AttemptDetailUiState.Loading -> Unit
            AttemptDetailUiState.NotFound -> Text(
                stringResource(R.string.round_not_found),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(innerPadding).padding(20.dp),
            )
            is AttemptDetailUiState.Content -> {
                val shown = if (onlyMistakes) state.mistakes else state.answered
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item { Header(state.attempt) }
                    if (state.detailsMissing && state.answered.isEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.round_details_missing),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    stringResource(R.string.review_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f),
                                )
                                FilterChip(
                                    selected = onlyMistakes,
                                    onClick = { onlyMistakes = !onlyMistakes },
                                    label = { Text(stringResource(R.string.review_mistakes_only)) },
                                )
                            }
                        }
                        if (shown.isEmpty()) {
                            item {
                                Text(
                                    stringResource(R.string.review_all_correct),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        items(shown) { answer ->
                            ReviewRow(answer)
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(attempt: QuizAttempt) {
    val modeLabel = stringResource(
        when (attempt.mode) {
            QuizMode.DAILY -> R.string.daily_label
            QuizMode.SURVIVAL -> R.string.mode_survival
            QuizMode.DUEL -> R.string.mode_duel
            QuizMode.STANDARD -> R.string.mode_classic
        },
    )
    val date = Instant.ofEpochMilli(attempt.completedAtMillis).atZone(ZoneId.systemDefault()).format(DETAIL_DATE_FORMATTER)
    Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$modeLabel · $date", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.results_score, attempt.score, attempt.total),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            stringResource(R.string.percent, (attempt.percentage * 100).roundToInt()),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}
