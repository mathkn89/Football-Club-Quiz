package com.ruflo.footballquiz.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ruflo.footballquiz.core.AppContainer
import com.ruflo.footballquiz.domain.model.QuizCategory
import com.ruflo.footballquiz.ui.clubs.ClubDetailScreen
import com.ruflo.footballquiz.ui.clubs.ClubDetailViewModel
import com.ruflo.footballquiz.ui.clubs.ClubListScreen
import com.ruflo.footballquiz.ui.clubs.ClubListViewModel
import com.ruflo.footballquiz.ui.core.ViewModelFactory
import com.ruflo.footballquiz.ui.history.HistoryScreen
import com.ruflo.footballquiz.ui.history.HistoryViewModel
import com.ruflo.footballquiz.ui.picker.PickerScreen
import com.ruflo.footballquiz.ui.picker.PickerViewModel
import com.ruflo.footballquiz.ui.quiz.QuizScreen
import com.ruflo.footballquiz.ui.quiz.QuizViewModel
import com.ruflo.footballquiz.ui.results.ResultsScreen
import java.net.URLDecoder
import java.net.URLEncoder

private const val ROUTE_PICKER = "picker"
private const val ROUTE_QUIZ = "quiz/{roundSize}/{categories}/{league}"
private const val ROUTE_RESULTS = "results/{score}/{total}"
private const val ROUTE_HISTORY = "history"
private const val ROUTE_CLUBS = "clubs"
private const val ROUTE_CLUB_DETAIL = "clubs/{clubId}"
private const val ARG_DEFAULT_ROUND_SIZE = 10
private const val ARG_ALL_LEAGUES = "ALL"

private fun clubDetailRoute(clubId: String): String = "clubs/$clubId"

private fun quizRoute(roundSize: Int, categories: Set<QuizCategory>, league: String?): String {
    val categoriesArg = categories.joinToString(",") { it.name }
    val leagueArg = league?.let { URLEncoder.encode(it, "UTF-8") } ?: ARG_ALL_LEAGUES
    return "quiz/$roundSize/$categoriesArg/$leagueArg"
}

private fun resultsRoute(score: Int, total: Int): String = "results/$score/$total"

@Composable
fun FootballQuizNavHost(
    container: AppContainer,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = ROUTE_PICKER) {
        composable(ROUTE_PICKER) {
            val pickerViewModel: PickerViewModel = viewModel(
                factory = ViewModelFactory { PickerViewModel(container.clubDao) },
            )
            PickerScreen(
                onStartQuiz = { roundSize, categories, league ->
                    navController.navigate(quizRoute(roundSize, categories, league))
                },
                onOpenHistory = { navController.navigate(ROUTE_HISTORY) },
                onOpenClubs = { navController.navigate(ROUTE_CLUBS) },
                viewModel = pickerViewModel,
            )
        }

        composable(ROUTE_HISTORY) {
            val historyViewModel: HistoryViewModel = viewModel(
                factory = ViewModelFactory {
                    HistoryViewModel(container.quizAttemptDao, container.userProfilePreferences)
                },
            )
            HistoryScreen(onBack = { navController.popBackStack() }, viewModel = historyViewModel)
        }

        composable(ROUTE_CLUBS) {
            val clubListViewModel: ClubListViewModel = viewModel(
                factory = ViewModelFactory { ClubListViewModel(container.clubDao) },
            )
            ClubListScreen(
                onBack = { navController.popBackStack() },
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
            ClubDetailScreen(onBack = { navController.popBackStack() }, viewModel = clubDetailViewModel)
        }

        composable(
            route = ROUTE_QUIZ,
            arguments = listOf(
                navArgument("roundSize") { type = NavType.IntType },
                navArgument("categories") { type = NavType.StringType },
                navArgument("league") { type = NavType.StringType },
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

            val quizViewModel: QuizViewModel = viewModel(
                factory = ViewModelFactory {
                    QuizViewModel(container.getQuizRoundUseCase, container.quizAttemptDao, roundSize, categories, league)
                },
            )

            QuizScreen(
                viewModel = quizViewModel,
                onFinished = { score, total ->
                    navController.navigate(resultsRoute(score, total)) {
                        popUpTo(ROUTE_PICKER) { inclusive = false }
                    }
                },
                onExit = {
                    navController.popBackStack(ROUTE_PICKER, inclusive = false)
                },
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
                    navController.navigate(ROUTE_PICKER) {
                        popUpTo(ROUTE_PICKER) { inclusive = true }
                    }
                },
            )
        }
    }
}
