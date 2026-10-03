package com.makn.footballquiz.ui.history

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makn.footballquiz.BuildConfig
import com.makn.footballquiz.R
import com.makn.footballquiz.data.local.UserProfilePreferences
import com.makn.footballquiz.domain.model.QuizAttempt
import com.makn.footballquiz.domain.model.QuizCategory
import com.makn.footballquiz.domain.model.QuizMode
import com.makn.footballquiz.reminder.DailyReminder
import com.makn.footballquiz.sync.SyncScheduler
import com.makn.footballquiz.ui.common.FootballQuizTopBar
import com.makn.footballquiz.ui.common.LanguageDialog
import com.makn.footballquiz.ui.common.displayName
import com.makn.footballquiz.ui.common.nameRes
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private val DATE_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(onPractise: (Set<QuizCategory>) -> Unit, viewModel: HistoryViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showRenameDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    val updatingMessage = stringResource(R.string.history_updating)
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            viewModel.onRemindersChanged(true)
            DailyReminder.enable(context)
        }
    }
    fun setReminders(enabled: Boolean) {
        when {
            !enabled -> {
                viewModel.onRemindersChanged(false)
                DailyReminder.disable(context)
            }
            DailyReminder.canNotify(context) -> {
                viewModel.onRemindersChanged(true)
                DailyReminder.enable(context)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        topBar = {
            FootballQuizTopBar(
                title = stringResource(R.string.tab_history),
                actions = {
                    IconButton(onClick = {
                        SyncScheduler.syncNow(context)
                        coroutineScope.launch { snackbarHostState.showSnackbar(updatingMessage) }
                    }) {
                        Icon(Icons.Default.Sync, contentDescription = stringResource(R.string.history_update_data))
                    }
                    IconButton(onClick = { showLanguageDialog = true }) {
                        Icon(Icons.Default.Language, contentDescription = stringResource(R.string.history_language))
                    }
                    IconButton(onClick = { showAboutDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = stringResource(R.string.history_about))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        val state = uiState as? HistoryUiState.Content ?: return@Scaffold

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            item {
                ProfileRow(displayName = state.displayName.localizedName(), onEditClick = { showRenameDialog = true })
                StatsRow(state)
                ReminderRow(enabled = state.remindersEnabled, onChange = ::setReminders)
                TopicsSection(state, onPractise)
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.history_recent),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
            }
            if (state.isEmpty) {
                item {
                    Text(
                        stringResource(R.string.history_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
            } else {
                items(state.attempts, key = { it.id }) { attempt -> AttemptRow(attempt) }
            }
        }

        if (showRenameDialog) {
            RenameDialog(
                currentName = state.displayName.localizedName(),
                onConfirm = { newName ->
                    viewModel.onRenameConfirmed(newName)
                    showRenameDialog = false
                },
                onDismiss = { showRenameDialog = false },
            )
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            confirmButton = { TextButton(onClick = { showAboutDialog = false }) { Text(stringResource(R.string.ok)) } },
            title = { Text(stringResource(R.string.app_name)) },
            text = {
                Text(stringResource(R.string.about_text, BuildConfig.VERSION_NAME))
            },
        )
    }

    if (showLanguageDialog) {
        LanguageDialog(onDismiss = { showLanguageDialog = false })
    }
}

@Composable
private fun ProfileRow(displayName: String, onEditClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 20.dp, end = 8.dp),
    ) {
        Text(
            displayName,
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        IconButton(onClick = onEditClick) {
            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.history_edit_name))
        }
    }
}

@Composable
private fun StatsRow(state: HistoryUiState.Content) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        StatTile(stringResource(R.string.history_played), "${state.attempts.size}", Modifier.weight(1f))
        StatTile(stringResource(R.string.history_average), state.averagePercent?.let { stringResource(R.string.percent, it) } ?: "–", Modifier.weight(1f))
        StatTile(stringResource(R.string.history_best), state.bestPercent?.let { stringResource(R.string.percent, it) } ?: "–", Modifier.weight(1f))
    }
}

@Composable
private fun ReminderRow(enabled: Boolean, onChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.reminder_title)) },
        supportingContent = { Text(stringResource(R.string.reminder_text)) },
        trailingContent = { Switch(checked = enabled, onCheckedChange = onChange) },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
    )
}

/** Accuracy per topic, best first, with a shortcut to practise the weakest ones. */
@Composable
private fun TopicsSection(state: HistoryUiState.Content, onPractise: (Set<QuizCategory>) -> Unit) {
    Column(Modifier.padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.stats_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        if (state.topicStats.isEmpty()) {
            Text(
                stringResource(R.string.stats_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@Column
        }
        state.topicStats.forEach { stat ->
            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stat.category.displayName(), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(110.dp))
                LinearProgressIndicator(
                    progress = { stat.percent / 100f },
                    modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = if (stat.category in state.weakTopics) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                )
                Text(
                    stringResource(R.string.percent, stat.percent),
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(52.dp),
                )
            }
        }
        if (state.weakTopics.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            FilledTonalButton(
                onClick = { onPractise(state.weakTopics) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) {
                Text(stringResource(R.string.practise_weak))
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier,
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AttemptRow(attempt: QuizAttempt) {
    val context = LocalContext.current
    val date = Instant.ofEpochMilli(attempt.completedAtMillis).atZone(ZoneId.systemDefault()).format(DATE_FORMATTER)
    ListItem(
        headlineContent = { Text(stringResource(R.string.results_score, attempt.score, attempt.total)) },
        supportingContent = {
            val modeLabel = when (attempt.mode) {
                QuizMode.DAILY -> context.getString(R.string.daily_label)
                QuizMode.SURVIVAL -> context.getString(R.string.mode_survival)
                else -> null
            }
            val topics = listOfNotNull(modeLabel, attempt.categories.joinToString { context.getString(it.nameRes()) }.ifEmpty { null })
                .joinToString(" · ")
            Text(
                if (topics.isEmpty()) date else "$date\n$topics",
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            Text(
                stringResource(R.string.percent, (attempt.percentage * 100).roundToInt()),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

@Composable
private fun RenameDialog(currentName: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.history_your_name)) },
        text = {
            OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true)
        },
        confirmButton = { TextButton(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) { Text(stringResource(R.string.save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

/** The stored default name is English; show the translated default instead. */
@Composable
private fun String.localizedName(): String =
    if (this == UserProfilePreferences.DEFAULT_DISPLAY_NAME) stringResource(R.string.history_default_name) else this
