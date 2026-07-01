package id.viasco.dynamic_qris_android.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import id.viasco.dynamic_qris_android.domain.model.TransactionStatus
import id.viasco.dynamic_qris_android.ui.create.CreateTransactionScreen
import id.viasco.dynamic_qris_android.ui.detail.TransactionDetailScreen
import id.viasco.dynamic_qris_android.ui.history.HistoryScreen
import id.viasco.dynamic_qris_android.ui.qr.QrDisplayScreen
import id.viasco.dynamic_qris_android.ui.status.ConnectionStatusScreen

private const val TRANSITION_DURATION = 300

/** Routes a transaction to its correct destination based on status. */
private fun navRoute(status: TransactionStatus, transactionId: String): String =
    if (status == TransactionStatus.PENDING) Screen.QrDisplay.createRoute(transactionId)
    else Screen.Detail.createRoute(transactionId)

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.History.route,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(TRANSITION_DURATION))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(TRANSITION_DURATION))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(TRANSITION_DURATION))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(TRANSITION_DURATION))
        },
    ) {
        composable(Screen.History.route) {
            HistoryScreen(
                onCreate = { navController.navigate(Screen.Create.route) },
                onItemClick = { trx ->
                    navController.navigate(navRoute(trx.status, trx.id))
                },
                onStatusClick = { navController.navigate(Screen.ConnectionStatus.route) },
            )
        }

        composable(Screen.Create.route) {
            CreateTransactionScreen(
                onBack = { navController.popBackStack() },
                onCreated = { id ->
                    navController.navigate(Screen.QrDisplay.createRoute(id))
                },
            )
        }

        composable(
            route = Screen.QrDisplay.route,
            arguments = listOf(
                navArgument(Screen.QrDisplay.ARG_TRANSACTION_ID) { type = NavType.StringType },
            ),
        ) {
            QrDisplayScreen(
                onBack = {
                    navController.popBackStack(Screen.History.route, inclusive = false)
                },
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument(Screen.Detail.ARG_TRANSACTION_ID) { type = NavType.StringType },
            ),
        ) {
            TransactionDetailScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.ConnectionStatus.route) {
            ConnectionStatusScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
