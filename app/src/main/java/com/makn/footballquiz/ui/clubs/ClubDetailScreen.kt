package com.makn.footballquiz.ui.clubs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.makn.footballquiz.R
import com.makn.footballquiz.domain.model.Club
import com.makn.footballquiz.ui.common.ClubShield
import com.makn.footballquiz.ui.common.FootballQuizTopBar
import com.makn.footballquiz.ui.common.KitShirt
import com.makn.footballquiz.ui.common.rememberQuizStrings
import com.makn.footballquiz.domain.text.kitDescription

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubDetailScreen(onBack: () -> Unit, onPlayLeague: (String) -> Unit, viewModel: ClubDetailViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            FootballQuizTopBar(
                title = "",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { innerPadding ->
        when (val state = uiState) {
            ClubDetailUiState.Loading -> {}
            ClubDetailUiState.NotFound -> {
                Text(
                    stringResource(R.string.club_not_found),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(innerPadding).padding(20.dp),
                )
            }
            is ClubDetailUiState.Content -> ClubDetailContent(
                club = state.club,
                onPlayLeague = { onPlayLeague(state.club.league) },
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun ClubDetailContent(club: Club, onPlayLeague: () -> Unit, modifier: Modifier = Modifier) {
    val strings = rememberQuizStrings()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ClubShield(club.kit, Modifier.height(96.dp))
        Spacer(Modifier.height(16.dp))
        Text(club.name, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        if (club.nickname.isNotBlank()) {
            Text(
                club.nickname,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(28.dp))
        Column(Modifier.fillMaxWidth()) {
            DetailRow(stringResource(R.string.club_league), club.league)
            DetailRow(stringResource(R.string.club_stadium), club.stadiumName)
            if (club.stadiumCapacity > 0) {
                DetailRow(stringResource(R.string.club_capacity), strings.number(club.stadiumCapacity))
            }
            if (club.foundedYear > 0) DetailRow(stringResource(R.string.club_founded), "${club.foundedYear}")
            DetailRow(stringResource(R.string.club_city), club.city)
            DetailRow(stringResource(R.string.club_manager), club.manager, showDivider = club.kit != null)
            club.kit?.let { kit ->
                DetailRow(stringResource(R.string.club_shirt), strings.kitDescription(kit))
                DetailRow(stringResource(R.string.club_shorts), strings.colour(kit.shorts).replaceFirstChar { it.uppercase() }, showDivider = false)
            }
        }

        club.kit?.let { kit ->
            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(R.string.club_home_kit),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            KitShirt(kit, Modifier.height(160.dp))
        }

        Spacer(Modifier.height(28.dp))
        FilledTonalButton(
            onClick = onPlayLeague,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.club_quiz_on_league, club.league))
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun DetailRow(label: String, value: String, showDivider: Boolean = true) {
    if (value.isBlank()) return
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
    if (showDivider) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}
