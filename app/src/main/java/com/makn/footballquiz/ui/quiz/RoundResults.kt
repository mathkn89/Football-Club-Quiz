package com.makn.footballquiz.ui.quiz

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.makn.footballquiz.R
import com.makn.footballquiz.domain.model.QuizMode
import com.makn.footballquiz.ui.common.KitShirt
import com.makn.footballquiz.ui.theme.CorrectGreen
import com.makn.footballquiz.ui.theme.IncorrectRed

/** What the results screen needs to know about ads and the "Remove ads" purchase. */
data class ResultsMonetization(
    val adFree: Boolean = true,
    val showAdFreeOffer: Boolean = false,
    val rewardedReady: Boolean = false,
    /** Plays a rewarded ad and runs the callback only if it was watched to the end. */
    val watchRewarded: (onRewarded: () -> Unit) -> Unit = {},
    val onRemoveAds: () -> Unit = {},
)

/** End-of-round screen: score, mode-specific extras, share, and a review of the answers. */
@Composable
fun RoundResults(
    state: QuizUiState.Finished,
    onPlayAgain: (() -> Unit)?,
    onDone: () -> Unit,
    monetization: ResultsMonetization = ResultsMonetization(),
    onContinueSurvival: () -> Unit = {},
) {
    val context = LocalContext.current
    var showAll by rememberSaveable { mutableStateOf(state.mistakes.isEmpty()) }
    val reviewed = if (showAll) state.answered else state.mistakes

    BackHandler(onBack = onDone)

    LazyColumn(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Summary(state) }
        if (state.canContinue && (monetization.adFree || monetization.rewardedReady)) {
            item { SecondChanceCard(monetization, onContinueSurvival) }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (onPlayAgain != null) {
                    Button(onClick = onPlayAgain, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                        Text(stringResource(R.string.results_play_again), style = MaterialTheme.typography.titleMedium)
                    }
                }
                OutlinedButton(
                    onClick = { share(context, state) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.share))
                }
                TextButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.results_done)) }
                if (monetization.showAdFreeOffer) {
                    TextButton(onClick = monetization.onRemoveAds, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.ads_go_ad_free), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.review_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                if (state.mode != QuizMode.DUEL) {
                    FilterChip(
                        selected = !showAll,
                        onClick = { showAll = !showAll },
                        label = { Text(stringResource(R.string.review_mistakes_only)) },
                    )
                }
            }
        }
        if (reviewed.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.review_all_correct),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(reviewed) { ReviewRow(it, isDuel = state.mode == QuizMode.DUEL) }
    }
}

/** Survival only: one more life, paid for with a short video (free for ad-free players). */
@Composable
private fun SecondChanceCard(monetization: ResultsMonetization, onContinue: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.second_chance_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.second_chance_text), style = MaterialTheme.typography.bodyMedium)
            Button(
                onClick = { if (monetization.adFree) onContinue() else monetization.watchRewarded(onContinue) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) {
                Icon(if (monetization.adFree) Icons.Default.Favorite else Icons.Default.PlayCircle, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(if (monetization.adFree) R.string.second_chance_free else R.string.second_chance_watch))
            }
        }
    }
}

@Composable
private fun Summary(state: QuizUiState.Finished) {
    var animationTarget by remember { mutableIntStateOf(0) }
    LaunchedEffect(state.score) { animationTarget = state.score }
    val animatedScore by animateIntAsState(animationTarget, tween(durationMillis = 800), label = "animatedScore")

    Column(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            when (state.mode) {
                QuizMode.DAILY -> stringResource(R.string.daily_title, state.dailyNumber ?: 0)
                QuizMode.SURVIVAL -> stringResource(R.string.mode_survival)
                QuizMode.DUEL -> duelHeadline(state)
                QuizMode.STANDARD -> stringResource(resultTitle(state.score, state.total))
            },
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            when (state.mode) {
                QuizMode.SURVIVAL -> stringResource(R.string.survival_result, animatedScore)
                QuizMode.DUEL -> stringResource(R.string.duel_score, state.score, state.secondScore ?: 0)
                else -> stringResource(R.string.results_score, animatedScore, state.total)
            },
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        val detail = when (state.mode) {
            QuizMode.DAILY -> stringResource(R.string.daily_streak, state.streak ?: 1)
            QuizMode.SURVIVAL -> if ((state.survivalBest ?: 0) <= state.score && state.score > 0) {
                stringResource(R.string.survival_new_best)
            } else {
                stringResource(R.string.survival_best, state.survivalBest ?: 0)
            }
            QuizMode.DUEL -> null
            QuizMode.STANDARD -> stringResource(resultMessage(state.score, state.total))
        }
        detail?.let { Text(it, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center) }
    }
}

@Composable
private fun duelHeadline(state: QuizUiState.Finished): String {
    val second = state.secondScore ?: 0
    return when {
        state.score > second -> stringResource(R.string.duel_winner, stringResource(R.string.duel_player1))
        second > state.score -> stringResource(R.string.duel_winner, stringResource(R.string.duel_player2))
        else -> stringResource(R.string.duel_draw)
    }
}

/** One reviewed question; shared with the round details in History. */
@Composable
fun ReviewRow(answer: AnsweredQuestion, isDuel: Boolean = false) {
    val question = answer.question
    val correctText = question.options[question.correctOptionIndex]
    fun pickText(pick: Int?) = pick?.let { question.options[it] }
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            question.kit?.let { kit ->
                KitShirt(kit, Modifier.height(56.dp))
                Spacer(Modifier.width(12.dp))
            }
            Text(question.prompt, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(4.dp))
        if (isDuel) {
            ReviewLine("${stringResource(R.string.duel_player1)}: ${pickText(answer.selectedOptionIndex) ?: stringResource(R.string.review_no_answer)}", answer.isCorrect)
            ReviewLine("${stringResource(R.string.duel_player2)}: ${pickText(answer.secondSelectedOptionIndex) ?: stringResource(R.string.review_no_answer)}", answer.isSecondCorrect)
        } else {
            ReviewLine(
                stringResource(R.string.review_your_answer, pickText(answer.selectedOptionIndex) ?: stringResource(R.string.review_no_answer)),
                answer.isCorrect,
            )
        }
        if (!answer.isCorrect || (isDuel && !answer.isSecondCorrect)) {
            Text(
                stringResource(R.string.review_correct_answer, correctText),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ReviewLine(text: String, correct: Boolean) {
    Text(
        "${if (correct) "✓" else "✗"}  $text",
        style = MaterialTheme.typography.bodyMedium,
        color = if (correct) CorrectGreen else IncorrectRed,
    )
}

private fun share(context: Context, state: QuizUiState.Finished) {
    val app = context.getString(R.string.app_name)
    val text = when (state.mode) {
        QuizMode.DAILY -> buildString {
            appendLine(context.getString(R.string.share_daily_title, app, state.dailyNumber ?: 0))
            appendLine(context.getString(R.string.results_score, state.score, state.total))
            appendLine(state.answered.joinToString("") { if (it.isCorrect) "🟩" else "🟥" })
            append("🔥 ").append(context.getString(R.string.daily_streak, state.streak ?: 1))
        }
        QuizMode.SURVIVAL -> context.getString(R.string.share_survival, state.score, app)
        QuizMode.DUEL -> context.getString(
            R.string.share_duel,
            context.getString(R.string.duel_player1), state.score, state.secondScore ?: 0, context.getString(R.string.duel_player2), app,
        )
        QuizMode.STANDARD -> context.getString(R.string.share_standard, state.score, state.total, app)
    }
    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
    context.startActivity(Intent.createChooser(send, context.getString(R.string.share)))
}

private fun resultTitle(score: Int, total: Int): Int {
    if (total == 0) return R.string.results_title_over
    return when (score.toFloat() / total) {
        1f -> R.string.results_title_perfect
        in 0.7f..1f -> R.string.results_title_great
        in 0.4f..0.7f -> R.string.results_title_good
        else -> R.string.results_title_over
    }
}

private fun resultMessage(score: Int, total: Int): Int {
    if (total == 0) return R.string.results_msg_none
    return when (score.toFloat() / total) {
        1f -> R.string.results_msg_perfect
        in 0.7f..1f -> R.string.results_msg_great
        in 0.4f..0.7f -> R.string.results_msg_good
        else -> R.string.results_msg_low
    }
}
