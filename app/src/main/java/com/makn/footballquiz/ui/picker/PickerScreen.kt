package com.makn.footballquiz.ui.picker

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makn.footballquiz.R
import com.makn.footballquiz.domain.model.Difficulty
import com.makn.footballquiz.domain.model.QuizCategory
import com.makn.footballquiz.ui.common.FootballQuizTopBar
import com.makn.footballquiz.ui.common.LeagueFilterRow
import com.makn.footballquiz.ui.common.displayName
import com.makn.footballquiz.ui.common.nameRes

private val SCREEN_PADDING = 20.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickerScreen(
    onStartQuiz: (roundSize: Int, categories: Set<QuizCategory>, league: String?, difficulty: Difficulty) -> Unit,
    viewModel: PickerViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showOptions by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = { FootballQuizTopBar(title = stringResource(R.string.app_name), showAppLogo = true) },
        bottomBar = {
            Column(Modifier.padding(SCREEN_PADDING)) {
                if (uiState.clubCount > 0 && uiState.activeCategories.isEmpty()) {
                    Text(
                        stringResource(R.string.play_need_topic),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                Button(
                    onClick = {
                        onStartQuiz(uiState.roundSize, uiState.activeCategories, uiState.selectedLeague, uiState.difficulty)
                    },
                    enabled = uiState.canStart,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.play_button), style = MaterialTheme.typography.titleMedium)
                }
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
                Text(stringResource(R.string.play_title), style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    if (uiState.clubCount > 0) {
                        stringResource(R.string.play_club_count, uiState.clubCount, uiState.availableLeagues.size)
                    } else {
                        stringResource(R.string.play_loading)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(28.dp))
                SectionLabel(stringResource(R.string.section_difficulty))
                DifficultySelector(uiState.difficulty, viewModel::onDifficultySelected)
                Spacer(Modifier.height(6.dp))
                Text(
                    difficultyHint(uiState.difficulty, uiState.selectedLeague != null),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(24.dp))
                SectionLabel(stringResource(R.string.section_league))
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
                    onSelectAll = viewModel::onSelectAllCategories,
                    onClear = viewModel::onClearCategories,
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(bottom = 8.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DifficultySelector(selected: Difficulty, onSelected: (Difficulty) -> Unit) {
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        val levels = Difficulty.entries
        levels.forEachIndexed { index, level ->
            SegmentedButton(
                selected = selected == level,
                onClick = { onSelected(level) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = levels.size),
            ) {
                Text(stringResource(level.labelRes()))
            }
        }
    }
}

private fun Difficulty.labelRes(): Int = when (this) {
    Difficulty.EASY -> R.string.difficulty_easy
    Difficulty.MEDIUM -> R.string.difficulty_medium
    Difficulty.HARD -> R.string.difficulty_hard
}

@Composable
private fun difficultyHint(difficulty: Difficulty, leagueChosen: Boolean): String {
    val clubs = stringResource(
        when (difficulty) {
            Difficulty.EASY -> if (leagueChosen) R.string.difficulty_easy_hint_league else R.string.difficulty_easy_hint
            Difficulty.MEDIUM -> R.string.difficulty_medium_hint
            Difficulty.HARD -> R.string.difficulty_hard_hint
        },
    )
    return stringResource(R.string.difficulty_hint_format, clubs, difficulty.secondsPerQuestion)
}

@Composable
private fun optionsSummary(state: PickerUiState): String {
    val context = LocalContext.current
    val topics = when {
        state.allCategoriesSelected -> stringResource(R.string.topics_all)
        state.activeCategories.isEmpty() -> stringResource(R.string.topics_none)
        state.activeCategories.size <= 2 -> state.availableCategories.filter { it in state.activeCategories }
            .joinToString { context.getString(it.nameRes()) }
        else -> stringResource(R.string.topics_count, state.activeCategories.size, state.availableCategories.size)
    }
    return stringResource(R.string.options_summary, state.roundSize, topics)
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
                Text(stringResource(R.string.options), style = MaterialTheme.typography.titleMedium)
                Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                Icons.Default.ExpandMore,
                contentDescription = stringResource(if (expanded) R.string.options_hide else R.string.options_show),
                modifier = Modifier.rotate(arrowRotation),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OptionsContent(
    uiState: PickerUiState,
    onRoundSizeSelected: (Int) -> Unit,
    onCategoryToggled: (QuizCategory) -> Unit,
    onSelectAll: () -> Unit,
    onClear: () -> Unit,
) {
    Column(Modifier.padding(horizontal = SCREEN_PADDING)) {
        SectionLabel(stringResource(R.string.section_questions))
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

        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.section_topics),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onSelectAll, enabled = !uiState.allCategoriesSelected) { Text(stringResource(R.string.topics_select_all)) }
            TextButton(onClick = onClear, enabled = uiState.activeCategories.isNotEmpty()) { Text(stringResource(R.string.topics_clear)) }
        }
        TopicGroup(stringResource(R.string.topics_group_club_facts), uiState.clubFactCategories, uiState.activeCategories, onCategoryToggled)
        if (uiState.triviaCategories.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            TopicGroup(stringResource(R.string.topics_group_trivia), uiState.triviaCategories, uiState.activeCategories, onCategoryToggled)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TopicGroup(
    title: String,
    categories: List<QuizCategory>,
    selected: Set<QuizCategory>,
    onToggle: (QuizCategory) -> Unit,
) {
    Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(6.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        categories.forEach { category ->
            val isSelected = category in selected
            FilterChip(
                selected = isSelected,
                onClick = { onToggle(category) },
                label = { Text(category.displayName()) },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                } else {
                    null
                },
            )
        }
    }
}
