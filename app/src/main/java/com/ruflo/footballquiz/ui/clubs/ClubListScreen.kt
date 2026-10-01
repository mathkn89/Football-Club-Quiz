package com.ruflo.footballquiz.ui.clubs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ruflo.footballquiz.domain.model.Club
import com.ruflo.footballquiz.ui.common.FootballQuizTopBar
import com.ruflo.footballquiz.ui.common.LeagueFilterRow
import com.ruflo.footballquiz.ui.common.QuizImage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ClubListScreen(onClubSelected: (String) -> Unit, viewModel: ClubListViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    Scaffold(topBar = { FootballQuizTopBar(title = "Clubs") }) { innerPadding ->
        val state = uiState as? ClubListUiState.Content ?: return@Scaffold

        LaunchedEffect(state.selectedLeague, state.query) { listState.scrollToItem(0) }

        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChanged,
                placeholder = { Text("Search clubs, cities, stadiums") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(12.dp))
            LeagueFilterRow(
                leagues = state.availableLeagues,
                selected = state.selectedLeague,
                onSelected = viewModel::onLeagueSelected,
            )
            Spacer(Modifier.height(8.dp))

            if (state.isEmpty) {
                Text(
                    "No clubs match \"${state.query.trim()}\".",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(20.dp),
                )
            } else {
                LazyColumn(state = listState, contentPadding = PaddingValues(bottom = 16.dp)) {
                    state.sections.forEach { section ->
                        stickyHeader(key = "header-${section.league}") {
                            LeagueHeader(section.league, section.clubs.size)
                        }
                        items(section.clubs, key = { it.id }) { club ->
                            ClubRow(club = club, onClick = { onClubSelected(club.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeagueHeader(league: String, count: Int) {
    Text(
        "$league · $count",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 10.dp),
    )
}

@Composable
private fun ClubRow(club: Club, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(club.shortName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            Text(
                listOf(club.stadiumName, club.city).filter { it.isNotBlank() }.joinToString(" · "),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        leadingContent = { ClubBadge(club.badgeImageRef, Modifier.size(40.dp)) },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
        modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 4.dp),
    )
}

/** Fixed-size slot so rows don't shift while badges load (or when a club has none). */
@Composable
fun ClubBadge(imageRef: String?, modifier: Modifier = Modifier) {
    Box(modifier) { QuizImage(imageUrl = imageRef, modifier = Modifier.fillMaxSize()) }
}

