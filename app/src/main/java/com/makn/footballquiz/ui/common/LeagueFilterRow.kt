package com.makn.footballquiz.ui.common

import com.makn.footballquiz.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

/**
 * Single-select league filter: "All" plus one chip per league, scrolling horizontally so it stays
 * one line no matter how many leagues the data holds. [selected] null means "All".
 */
@Composable
fun LeagueFilterRow(
    leagues: List<String>,
    selected: String?,
    onSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp),
) {
    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "all") {
            FilterChip(selected = selected == null, onClick = { onSelected(null) }, label = { Text(stringResource(R.string.league_all)) })
        }
        items(leagues, key = { it }) { league ->
            FilterChip(selected = selected == league, onClick = { onSelected(league) }, label = { Text(league) })
        }
    }
}
