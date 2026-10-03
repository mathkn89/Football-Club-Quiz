package com.makn.footballquiz.ui.common

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.makn.footballquiz.R

/** Languages the app ships, each shown in its own language. Empty tag = follow the phone. */
private val LANGUAGES = listOf(
    "" to null,
    "en" to "English",
    "de" to "Deutsch",
    "fr" to "Français",
    "nb" to "Norsk bokmål",
    "sv" to "Svenska",
)

/** Per-app language picker; the choice is remembered by the system and survives restarts. */
@Composable
fun LanguageDialog(onDismiss: () -> Unit) {
    val current = AppCompatDelegate.getApplicationLocales().takeIf { !it.isEmpty }?.get(0)?.language.orEmpty()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.history_language)) },
        text = {
            Column(Modifier.selectableGroup()) {
                LANGUAGES.forEach { (tag, name) ->
                    val selected = current == tag
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected = selected, role = Role.RadioButton) {
                                AppCompatDelegate.setApplicationLocales(
                                    if (tag.isEmpty()) LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(tag),
                                )
                                onDismiss()
                            }
                            .padding(vertical = 10.dp),
                    ) {
                        RadioButton(selected = selected, onClick = null)
                        Text(
                            name ?: stringResource(R.string.language_system),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 12.dp),
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
