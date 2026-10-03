package com.makn.footballquiz.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.makn.footballquiz.R
import com.makn.footballquiz.core.AppContainer
import com.makn.footballquiz.domain.model.Difficulty
import com.makn.footballquiz.domain.model.QuizCategory
import com.makn.footballquiz.domain.model.QuizMode
import com.makn.footballquiz.ui.clubs.ClubDetailScreen
import com.makn.footballquiz.ui.clubs.ClubDetailViewModel
import com.makn.footballquiz.ui.clubs.ClubListScreen
import com.makn.footballquiz.ui.clubs.ClubListViewModel
import com.makn.footballquiz.ui.common.rememberQuizStrings
import com.makn.footballquiz.ui.core.ViewModelFactory
import com.makn.footballquiz.ui.history.AttemptDetailScreen
import com.makn.footballquiz.ui.history.AttemptDetailViewModel
import com.makn.footballquiz.ui.history.HistoryScreen
import com.makn.footballquiz.ui.history.HistoryViewModel
import com.makn.footballquiz.ui.picker.PickerScreen
import com.makn.footballquiz.ui.picker.PickerUiState
import com.makn.footballquiz.ui.picker.PickerViewModel
import com.makn.footballquiz.ui.quiz.QuizScreen
import com.makn.footballquiz.ui.quiz.QuizViewModel
import java.net.URLDecoder
import java.net.URLEncoder

private const val ROUTE_PICKER = "picker"
private const val ROUTE_QUIZ = "quiz/{mode}/{roundSize}/{categories}/{league}/{difficulty}"
private const val ROUTE_HISTORY = "history"
private const val ROUTE_ATTEMPT_DETAIL = "history/{attemptId}"
private const val ROUTE_CLUBS = "clubs"
private const val ROUTE_CLUB_DETAIL = "clubs/{clubId}"
private const val ARG_DEFAULT_ROUND_SIZE = 10
private const val ARG_ALL_LEAGUES = "ALL"

private enum class TopLevelDestination(val route: String, @StringRes val label: Int, val icon: ImageVector) {
    PLAY(ROUTE_PICKER, R.string.tab_play, Icons.Default.PlayArrow),
    CLUBS(ROUTE_CLUBS, R.string.tab_clubs, Icons.Default.Shield),
    HISTORY(ROUTE_HISTORY, R.string.tab_history, Icons.Default.History),
}

/** Routes that show the bottom bar, mapped to the tab they belong under. */
private val TAB_FOR_ROUTE = mapOf(
    ROUTE_PICKER to TopLevelDestination.PLAY,
    ROUTE_CLUBS to TopLevelDestination.CLUBS,
    ROUTE_CLUB_DETAIL to TopLevelDestination.CLUBS,
    ROUTE_HISTORY to TopLevelDestination.HISTORY,
    ROUTE_ATTEMPT_DETAIL to TopLevelDestination.HISTORY,
)

private fun clubDetailRoute(clubId: String): String = "clubs/$clubId"

private fun quizRoute(
    mode: QuizMode,
    roundSize: Int,
    categories: Set<QuizCategory>,
    league: String?,
    difficulty: Difficulty,
): String {
    val categoriesArg = categories.joinToString(",") { it.name }
    val leagueArg = league?.let { URLEncoder.encode(it, "UTF-8") } ?: ARG_ALL_LEAGUES
    return "quiz/${mode.name}/$roundSize/$categoriesArg/$leagueArg/${difficulty.name}"
}

@Composable
fun FootballQuizNavHost(
    container: AppContainer,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTab = TAB_FOR_ROUTE[backStackEntry?.destination?.route]
    fun startQuiz(route: String) = navController.navigate(route)

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (currentTab != null) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = tab == currentTab,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.label)) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_PICKER,
            modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding),
        ) {
            composable(ROUTE_PICKER) {
                val pickerViewModel: PickerViewModel = viewModel(
                    factory = ViewModelFactory {
                        PickerViewModel(container.clubDao, container.customQuestionDao, container.playProgressPreferences)
                    },
                )
                PickerScreen(
                    onStartQuiz = { mode, roundSize, categories, league, difficulty ->
                        startQuiz(quizRoute(mode, roundSize, categories, league, difficulty))
                    },
                    viewModel = pickerViewModel,
                )
            }

            composable(ROUTE_HISTORY) {
                val historyViewModel: HistoryViewModel = viewModel(
                    factory = ViewModelFactory {
                        HistoryViewModel(container.quizAttemptDao, container.userProfilePreferences, container.playProgressPreferences)
                    },
                )
                HistoryScreen(
                    onPractise = { categories ->
                        startQuiz(quizRoute(QuizMode.STANDARD, PickerUiState.DEFAULT_ROUND_SIZE, categories, null, Difficulty.MEDIUM))
                    },
                    onAttemptSelected = { attemptId -> navController.navigate("history/$attemptId") },
                    viewModel = historyViewModel,
                )
            }

            composable(
                route = ROUTE_ATTEMPT_DETAIL,
                arguments = listOf(navArgument("attemptId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val attemptId = backStackEntry.arguments?.getString("attemptId").orEmpty()
                val detailViewModel: AttemptDetailViewModel = viewModel(
                    factory = ViewModelFactory { AttemptDetailViewModel(container.quizAttemptDao, attemptId) },
                )
                AttemptDetailScreen(onBack = { navController.popBackStack() }, viewModel = detailViewModel)
            }

            composable(ROUTE_CLUBS) {
                val clubListViewModel: ClubListViewModel = viewModel(
                    factory = ViewModelFactory { ClubListViewModel(container.clubDao) },
                )
                ClubListScreen(
                    onClubSelected = { clubId -> navController.navigate(clubDetailRoute(clubId)) },
                    viewModel = clubListViewModel,
                )
            }

            composable(
                route = ROUTE_CLUB_DETAIL,
                arguments = listOf(navArgument("clubId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val clubId = backStackEntry.arguments?.getString("clubId").orEmpty()
                val clubDetailViewModel: ClubDetailViewModel = viewModel(
                    factory = ViewModelFactory { ClubDetailViewModel(container.clubDao, clubId) },
                )
                ClubDetailScreen(
                    onBack = { navController.popBackStack() },
                    onPlayLeague = { league ->
                        startQuiz(quizRoute(QuizMode.STANDARD, PickerUiState.DEFAULT_ROUND_SIZE, QuizCategory.entries.toSet(), league, Difficulty.MEDIUM))
                    },
                    viewModel = clubDetailViewModel,
                )
            }

            composable(
                route = ROUTE_QUIZ,
                arguments = listOf(
                    navArgument("mode") { type = NavType.StringType },
                    navArgument("roundSize") { type = NavType.IntType },
                    navArgument("categories") { type = NavType.StringType },
                    navArgument("league") { type = NavType.StringType },
                    navArgument("difficulty") { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                val mode = backStackEntry.arguments?.getString("mode")
                    ?.let { runCatching { QuizMode.valueOf(it) }.getOrNull() }
                    ?: QuizMode.STANDARD
                val roundSize = backStackEntry.arguments?.getInt("roundSize") ?: ARG_DEFAULT_ROUND_SIZE
                val categories = backStackEntry.arguments?.getString("categories").orEmpty()
                    .split(",")
                    .mapNotNull { runCatching { QuizCategory.valueOf(it) }.getOrNull() }
                    .toSet()
                    .ifEmpty { QuizCategory.entries.toSet() }
                val league = backStackEntry.arguments?.getString("league")
                    ?.takeIf { it != ARG_ALL_LEAGUES }
                    ?.let { URLDecoder.decode(it, "UTF-8") }
                val difficulty = backStackEntry.arguments?.getString("difficulty")
                    ?.let { runCatching { Difficulty.valueOf(it) }.getOrNull() }
                    ?: Difficulty.MEDIUM

                val strings = rememberQuizStrings()
                val quizViewModel: QuizViewModel = viewModel(
                    factory = ViewModelFactory {
                        QuizViewModel(
                            getQuizRoundUseCase = container.getQuizRoundUseCase,
                            quizAttemptDao = container.quizAttemptDao,
                            progress = container.playProgressPreferences,
                            mode = mode,
                            roundSize = roundSize,
                            categories = categories,
                            league = league,
                            difficulty = difficulty,
                            strings = strings,
                        )
                    },
                )

                QuizScreen(
                    viewModel = quizViewModel,
                    // The daily challenge is once a day; every other mode can be replayed as-is.
                    onPlayAgain = if (mode == QuizMode.DAILY) {
                        null
                    } else {
                        {
                            navController.navigate(quizRoute(mode, roundSize, categories, league, difficulty)) {
                                popUpTo(ROUTE_QUIZ) { inclusive = true }
                            }
                        }
                    },
                    // Back to wherever the round was started from (Play tab, a club page or History).
                    onExit = { navController.popBackStack() },
                )
            }
        }
    }
}
