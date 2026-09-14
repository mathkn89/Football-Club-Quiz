package com.ruflo.footballquiz.ui.picker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ruflo.footballquiz.domain.model.QuizCategory
import com.ruflo.footballquiz.ui.common.displayName

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PickerScreen(
    onStartQuiz: (roundSize: Int, categories: Set<QuizCategory>) -> Unit,
    viewModel: PickerViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Football Club Quiz", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Pick a round length and categories", style = MaterialTheme.typography.bodyMedium)

        Spacer(Modifier.height(24.dp))
        Text("Round length", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PickerUiState.ROUND_SIZE_OPTIONS.forEach { size ->
                FilterChip(
                    selected = uiState.roundSize == size,
                    onClick = { viewModel.onRoundSizeSelected(size) },
                    label = { Text("$size questions") },
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Categories", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            QuizCategory.entries.forEach { category ->
                FilterChip(
                    selected = category in uiState.selectedCategories,
                    onClick = { viewModel.onCategoryToggled(category) },
                    label = { Text(category.displayName()) },
                )
            }
        }

        Spacer(Modifier.weight(1f))
        Button(
            onClick = { onStartQuiz(uiState.roundSize, uiState.selectedCategories) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Start Quiz")
        }
    }
}
