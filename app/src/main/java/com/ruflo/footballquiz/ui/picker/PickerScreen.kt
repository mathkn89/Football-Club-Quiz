package com.ruflo.footballquiz.ui.picker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ruflo.footballquiz.domain.model.QuizCategory
import com.ruflo.footballquiz.ui.common.FootballQuizTopBar
import com.ruflo.footballquiz.ui.common.LeagueFilterRow
import com.ruflo.footballquiz.ui.common.displayName

private val SCREEN_PADDING = 20.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickerScreen(
    onStartQuiz: (roundSize: Int, categories: Set<QuizCategory>, league: String?) -> Unit,
    viewModel: PickerViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showOptions by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = { FootballQuizTopBar(title = "Football Club Quiz", showAppLogo = true) },
        bottomBar = {
            Button(
                onClick = { onStartQuiz(uiState.roundSize, uiState.activeCategories, uiState.selectedLeague) },
                enabled = uiState.clubCount > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SCREEN_PADDING)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.padding(4.dp))
                Text("Play", style = MaterialTheme.typography.titleMedium)
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(Modifier.padding(horizontal = SCREEN_PADDING)) {
                Spacer(Modifier.height(16.dp))
                Text("Ready to play?", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    if (uiState.clubCount > 0) {
                        "${uiState.clubCount} clubs across ${uiState.availableLeagues.size} leagues"
                    } else {
                        "Loading club data…"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(32.dp))
                SectionLabel("League")
            }
            LeagueFilterRow(
                leagues = uiState.availableLeagues,
                selected = uiState.selectedLeague,
                onSelected = viewModel::onLeagueSelected,
                contentPadding = PaddingValues(horizontal = SCREEN_PADDING),
            )

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(Modifier.padding(horizontal = SCREEN_PADDING), color = MaterialTheme.colorScheme.outlineVariant)
            OptionsHeader(
                summary = optionsSummary(uiState),
                expanded = showOptions,
                onToggle = { showOptions = !showOptions },
            )
            AnimatedVisibility(visible = showOptions) {
                OptionsContent(
                    uiState = uiState,
                    onRoundSizeSelected = viewModel::onRoundSizeSelected,
                    onCategoryToggled = viewModel::onCategoryToggled,
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

private fun optionsSummary(state: PickerUiState): String {
    val topics = if (state.activeCategories.size == state.availableCategories.size) {
        "All topics"
    } else {
        state.availableCategories.filter { it in state.activeCategories }.joinToString { it.displayName() }
    }
    return "${state.roundSize} questions · $topics"
}

@Composable
private fun OptionsHeader(summary: String, expanded: Boolean, onToggle: () -> Unit) {
    val arrowRotation by animateFloatAsState(if (expanded) 180f else 0f, label = "optionsArrow")
    Surface(onClick = onToggle, color = MaterialTheme.colorScheme.background) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SCREEN_PADDING, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Options", style = MaterialTheme.typography.titleMedium)
                Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Hide options" else "Show options",
                modifier = Modifier.rotate(arrowRotation),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun OptionsContent(
    uiState: PickerUiState,
    onRoundSizeSelected: (Int) -> Unit,
    onCategoryToggled: (QuizCategory) -> Unit,
) {
    Column(Modifier.padding(horizontal = SCREEN_PADDING)) {
        SectionLabel("Questions")
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            val options = PickerUiState.ROUND_SIZE_OPTIONS
            options.forEachIndexed { index, size ->
                SegmentedButton(
                    selected = uiState.roundSize == size,
                    onClick = { onRoundSizeSelected(size) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                ) {
                    Text("$size")
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        SectionLabel("Topics")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            uiState.availableCategories.forEach { category ->
                FilterChip(
                    selected = category in uiState.activeCategories,
                    onClick = { onCategoryToggled(category) },
                    label = { Text(category.displayName()) },
                )
            }
        }
    }
}
