package com.ruflo.footballquiz.ui.picker

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ruflo.footballquiz.BuildConfig
import com.ruflo.footballquiz.domain.model.QuizCategory
import com.ruflo.footballquiz.sync.SyncScheduler
import com.ruflo.footballquiz.ui.common.FootballQuizTopBar
import com.ruflo.footballquiz.ui.common.displayName
import com.ruflo.footballquiz.ui.common.emoji
import kotlinx.coroutines.launch

private val ROUND_SIZE_LABELS = mapOf(5 to "Quick", 10 to "Standard", 15 to "Extended")

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PickerScreen(
    onStartQuiz: (roundSize: Int, categories: Set<QuizCategory>) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenClubs: () -> Unit,
    viewModel: PickerViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            FootballQuizTopBar(
                title = "⚽ Football Club Quiz",
                actions = {
                    IconButton(onClick = onOpenClubs) {
                        Icon(Icons.Default.Shield, contentDescription = "Club directory")
                    }
                    IconButton(onClick = onOpenHistory) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                    IconButton(onClick = { showOverflowMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(expanded = showOverflowMenu, onDismissRequest = { showOverflowMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Sync now") },
                            leadingIcon = { Icon(Icons.Default.Sync, contentDescription = null) },
                            onClick = {
                                showOverflowMenu = false
                                SyncScheduler.syncNow(context)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Syncing club data in the background…")
                                }
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("About") },
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                            onClick = {
                                showOverflowMenu = false
                                showAboutDialog = true
                            },
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Test your Premier League knowledge",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(32.dp))
            Text("Round length", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.selectableGroup(),
            ) {
                PickerUiState.ROUND_SIZE_OPTIONS.forEach { size ->
                    RoundSizeCard(
                        size = size,
                        label = ROUND_SIZE_LABELS[size].orEmpty(),
                        selected = uiState.roundSize == size,
                        onClick = { viewModel.onRoundSizeSelected(size) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
            Text("Categories", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                QuizCategory.entries.forEach { category ->
                    FilterChip(
                        selected = category in uiState.selectedCategories,
                        onClick = { viewModel.onCategoryToggled(category) },
                        label = { Text("${category.emoji()} ${category.displayName()}") },
                    )
                }
            }

            Spacer(Modifier.weight(1f))
            Button(
                onClick = { onStartQuiz(uiState.roundSize, uiState.selectedCategories) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("Start Quiz", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) { Text("OK") }
            },
            title = { Text("Football Club Quiz") },
            text = {
                Text(
                    "Version ${BuildConfig.VERSION_NAME}\n\n" +
                        "Trivia about English football clubs — stadiums, nicknames, history, and more. " +
                        "Club data syncs in the background from GitHub Pages.",
                )
            },
        )
    }
}

@Composable
private fun RoundSizeCard(
    size: Int,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    Card(
        modifier = modifier
            .selectable(selected = selected, onClick = onClick)
            .border(width = if (selected) 2.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("$size", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}
