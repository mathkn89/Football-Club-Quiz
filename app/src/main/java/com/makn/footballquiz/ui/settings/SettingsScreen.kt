package com.makn.footballquiz.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makn.footballquiz.BuildConfig
import com.makn.footballquiz.R
import com.makn.footballquiz.monetization.BillingManager
import com.makn.footballquiz.reminder.DailyReminder
import com.makn.footballquiz.sync.SyncScheduler
import com.makn.footballquiz.ui.common.FootballQuizTopBar
import com.makn.footballquiz.ui.common.LanguageDialog
import com.makn.footballquiz.ui.common.findActivity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val updatingMessage = stringResource(R.string.history_updating)
    val restoringMessage = stringResource(R.string.ads_restoring)

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
                title = stringResource(R.string.settings),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                AdFreeCard(
                    purchase = uiState.purchase,
                    onBuy = { context.findActivity()?.let(viewModel.billing::launchPurchase) },
                    onRestore = {
                        viewModel.billing.refresh()
                        coroutineScope.launch { snackbarHostState.showSnackbar(restoringMessage) }
                    },
                    onDebugToggle = viewModel.billing::setDebugAdFree,
                )
            }
            item { SectionHeader(stringResource(R.string.settings_section_general)) }
            item {
                SettingsRow(
                    icon = Icons.Default.Notifications,
                    title = stringResource(R.string.reminder_title),
                    subtitle = stringResource(R.string.reminder_text),
                    trailing = { Switch(checked = uiState.remindersEnabled, onCheckedChange = ::setReminders) },
                    onClick = { setReminders(!uiState.remindersEnabled) },
                )
            }
            item {
                SettingsRow(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.history_language),
                    onClick = { showLanguageDialog = true },
                )
            }
            item {
                SettingsRow(
                    icon = Icons.Default.Sync,
                    title = stringResource(R.string.history_update_data),
                    onClick = {
                        SyncScheduler.syncNow(context)
                        coroutineScope.launch { snackbarHostState.showSnackbar(updatingMessage) }
                    },
                )
            }
            item { SectionHeader(stringResource(R.string.settings_section_privacy)) }
            if (uiState.privacyOptionsRequired) {
                item {
                    SettingsRow(
                        icon = Icons.Default.PrivacyTip,
                        title = stringResource(R.string.privacy_settings),
                        subtitle = stringResource(R.string.privacy_settings_text),
                        onClick = { context.findActivity()?.let { viewModel.consent.showPrivacyOptions(it) } },
                    )
                }
            }
            item {
                SettingsRow(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.history_about),
                    subtitle = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                    onClick = { showAboutDialog = true },
                )
            }
        }
    }

    if (showLanguageDialog) LanguageDialog(onDismiss = { showLanguageDialog = false })
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            confirmButton = { TextButton(onClick = { showAboutDialog = false }) { Text(stringResource(R.string.ok)) } },
            title = { Text(stringResource(R.string.app_name)) },
            text = { Text(stringResource(R.string.about_text, BuildConfig.VERSION_NAME)) },
        )
    }
}

/** The "Remove ads" offer, or a thank-you once bought. */
@Composable
private fun AdFreeCard(
    purchase: BillingManager.State,
    onBuy: () -> Unit,
    onRestore: () -> Unit,
    onDebugToggle: (Boolean) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (purchase.adFree) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(10.dp))
                    Text(stringResource(R.string.ads_ad_free_title), style = MaterialTheme.typography.titleMedium)
                }
                Text(stringResource(R.string.ads_ad_free_text), style = MaterialTheme.typography.bodyMedium)
            } else {
                Text(stringResource(R.string.ads_remove_title), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.ads_remove_text), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = onBuy,
                    enabled = purchase.purchaseAvailable && !purchase.pending,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    Text(
                        when {
                            purchase.pending -> stringResource(R.string.ads_pending)
                            purchase.price != null -> stringResource(R.string.ads_remove_button_price, purchase.price)
                            purchase.purchaseAvailable -> stringResource(R.string.ads_remove_title)
                            else -> stringResource(R.string.ads_unavailable)
                        },
                    )
                }
                TextButton(onClick = onRestore, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.ads_restore))
                }
            }
            if (BuildConfig.DEBUG) {
                // Real purchases need the app in Play Console; this lets the ad-free state be tested.
                OutlinedButton(onClick = { onDebugToggle(!purchase.adFree) }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (purchase.adFree) "Debug: undo test purchase" else "Debug: test purchase (not charged)")
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 4.dp),
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = trailing,
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
        modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 4.dp),
    )
}
