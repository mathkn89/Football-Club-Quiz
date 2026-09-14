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
import com.ruflo.footballquiz.ui.core.ViewModelFactory
import com.ruflo.footballquiz.ui.picker.PickerScreen
import com.ruflo.footballquiz.ui.quiz.QuizScreen
import com.ruflo.footballquiz.ui.quiz.QuizViewModel
import com.ruflo.footballquiz.ui.results.ResultsScreen

private const val ROUTE_PICKER = "picker"
private const val ROUTE_QUIZ = "quiz/{roundSize}/{categories}"
private const val ROUTE_RESULTS = "results/{score}/{total}"
private const val ARG_DEFAULT_ROUND_SIZE = 10

private fun quizRoute(roundSize: Int, categories: Set<QuizCategory>): String {
    val categoriesArg = categories.joinToString(",") { it.name }
    return "quiz/$roundSize/$categoriesArg"
}

private fun resultsRoute(score: Int, total: Int): String = "results/$score/$total"

@Composable
fun FootballQuizNavHost(
    container: AppContainer,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = ROUTE_PICKER) {
        composable(ROUTE_PICKER) {
            PickerScreen(
                onStartQuiz = { roundSize, categories ->
                    navController.navigate(quizRoute(roundSize, categories))
                },
            )
        }

        composable(
            route = ROUTE_QUIZ,
            arguments = listOf(
                navArgument("roundSize") { type = NavType.IntType },
                navArgument("categories") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val roundSize = backStackEntry.arguments?.getInt("roundSize") ?: ARG_DEFAULT_ROUND_SIZE
            val categories = backStackEntry.arguments?.getString("categories").orEmpty()
                .split(",")
                .mapNotNull { runCatching { QuizCategory.valueOf(it) }.getOrNull() }
                .toSet()
                .ifEmpty { QuizCategory.entries.toSet() }

            val quizViewModel: QuizViewModel = viewModel(
                factory = ViewModelFactory {
                    QuizViewModel(container.getQuizRoundUseCase, roundSize, categories)
                },
            )

            QuizScreen(
                viewModel = quizViewModel,
                onFinished = { score, total ->
                    navController.navigate(resultsRoute(score, total)) {
                        popUpTo(ROUTE_PICKER) { inclusive = false }
                    }
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
