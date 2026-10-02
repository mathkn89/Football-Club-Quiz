package com.ruflo.footballquiz.ui.navigation

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ruflo.footballquiz.core.AppContainer
import com.ruflo.footballquiz.domain.model.Difficulty
import com.ruflo.footballquiz.domain.model.QuizCategory
import com.ruflo.footballquiz.ui.clubs.ClubDetailScreen
import com.ruflo.footballquiz.ui.clubs.ClubDetailViewModel
import com.ruflo.footballquiz.ui.clubs.ClubListScreen
import com.ruflo.footballquiz.ui.clubs.ClubListViewModel
import com.ruflo.footballquiz.ui.core.ViewModelFactory
import com.ruflo.footballquiz.ui.history.HistoryScreen
import com.ruflo.footballquiz.ui.history.HistoryViewModel
import com.ruflo.footballquiz.ui.picker.PickerScreen
import com.ruflo.footballquiz.ui.picker.PickerUiState
import com.ruflo.footballquiz.ui.picker.PickerViewModel
import com.ruflo.footballquiz.ui.quiz.QuizScreen
import com.ruflo.footballquiz.ui.quiz.QuizViewModel
import com.ruflo.footballquiz.ui.results.ResultsScreen
import java.net.URLDecoder
import java.net.URLEncoder

private const val ROUTE_PICKER = "picker"
private const val ROUTE_QUIZ = "quiz/{roundSize}/{categories}/{league}/{difficulty}"
private const val ROUTE_RESULTS = "results/{score}/{total}"
private const val ROUTE_HISTORY = "history"
private const val ROUTE_CLUBS = "clubs"
private const val ROUTE_CLUB_DETAIL = "clubs/{clubId}"
private const val ARG_DEFAULT_ROUND_SIZE = 10
private const val ARG_ALL_LEAGUES = "ALL"

private enum class TopLevelDestination(val route: String, val label: String, val icon: ImageVector) {
    PLAY(ROUTE_PICKER, "Play", Icons.Default.PlayArrow),
    CLUBS(ROUTE_CLUBS, "Clubs", Icons.Default.Shield),
    HISTORY(ROUTE_HISTORY, "History", Icons.Default.History),
}

/** Routes that show the bottom bar, mapped to the tab they belong under. */
private val TAB_FOR_ROUTE = mapOf(
    ROUTE_PICKER to TopLevelDestination.PLAY,
    ROUTE_CLUBS to TopLevelDestination.CLUBS,
    ROUTE_CLUB_DETAIL to TopLevelDestination.CLUBS,
    ROUTE_HISTORY to TopLevelDestination.HISTORY,
)

private fun clubDetailRoute(clubId: String): String = "clubs/$clubId"

private fun quizRoute(roundSize: Int, categories: Set<QuizCategory>, league: String?, difficulty: Difficulty): String {
    val categoriesArg = categories.joinToString(",") { it.name }
    val leagueArg = league?.let { URLEncoder.encode(it, "UTF-8") } ?: ARG_ALL_LEAGUES
    return "quiz/$roundSize/$categoriesArg/$leagueArg/${difficulty.name}"
}

private fun resultsRoute(score: Int, total: Int): String = "results/$score/$total"

@Composable
fun FootballQuizNavHost(
    container: AppContainer,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTab = TAB_FOR_ROUTE[backStackEntry?.destination?.route]
    // Remembered so "Play again" on the results screen can replay the exact same round setup.
    var lastQuizRoute by rememberSaveable { mutableStateOf<String?>(null) }

    fun startQuiz(route: String) {
        lastQuizRoute = route
        navController.navigate(route)
    }

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
                            label = { Text(tab.label) },
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
                    factory = ViewModelFactory { PickerViewModel(container.clubDao, container.customQuestionDao) },
                )
                PickerScreen(
                    onStartQuiz = { roundSize, categories, league, difficulty ->
                        startQuiz(quizRoute(roundSize, categories, league, difficulty))
                    },
                    viewModel = pickerViewModel,
                )
            }

            composable(ROUTE_HISTORY) {
                val historyViewModel: HistoryViewModel = viewModel(
                    factory = ViewModelFactory {
                        HistoryViewModel(container.quizAttemptDao, container.userProfilePreferences)
                    },
                )
                HistoryScreen(viewModel = historyViewModel)
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
                        startQuiz(quizRoute(PickerUiState.DEFAULT_ROUND_SIZE, QuizCategory.entries.toSet(), league, Difficulty.MEDIUM))
                    },
                    viewModel = clubDetailViewModel,
                )
            }

            composable(
                route = ROUTE_QUIZ,
                arguments = listOf(
                    navArgument("roundSize") { type = NavType.IntType },
                    navArgument("categories") { type = NavType.StringType },
                    navArgument("league") { type = NavType.StringType },
                    navArgument("difficulty") { type = NavType.StringType },
                ),
            ) { backStackEntry ->
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

                val quizViewModel: QuizViewModel = viewModel(
                    factory = ViewModelFactory {
                        QuizViewModel(container.getQuizRoundUseCase, container.quizAttemptDao, roundSize, categories, league, difficulty)
                    },
                )

                QuizScreen(
                    viewModel = quizViewModel,
                    onFinished = { score, total ->
                        navController.navigate(resultsRoute(score, total)) {
                            popUpTo(ROUTE_QUIZ) { inclusive = true }
                        }
                    },
                    // Back to wherever the round was started from (Play tab or a club page).
                    onExit = { navController.popBackStack() },
                )
            }

            composable(
                route = ROUTE_RESULTS,
                arguments = listOf(
                    navArgument("score") { type = NavType.IntType },
                    navArgument("total") { type = NavType.IntType },
                ),
            ) { backStackEntry ->
                val score = backStackEntry.arguments?.getInt("score") ?: 0
                val total = backStackEntry.arguments?.getInt("total") ?: 0

                ResultsScreen(
                    score = score,
                    total = total,
                    onPlayAgain = {
                        val replay = lastQuizRoute
                        if (replay == null) {
                            navController.popBackStack()
                        } else {
                            navController.navigate(replay) { popUpTo(ROUTE_RESULTS) { inclusive = true } }
                        }
                    },
                    onDone = { navController.popBackStack() },
                )
            }
        }
    }
}
