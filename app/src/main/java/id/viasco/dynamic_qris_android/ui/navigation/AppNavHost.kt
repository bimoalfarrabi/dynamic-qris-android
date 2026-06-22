package id.viasco.dynamic_qris_android.ui.navigation

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

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.History.route,
    ) {
        composable(Screen.History.route) {
            HistoryScreen(
                onCreate = { navController.navigate(Screen.Create.route) },
                onItemClick = { trx ->
                    if (trx.status == TransactionStatus.PENDING) {
                        navController.navigate(Screen.QrDisplay.createRoute(trx.id))
                    } else {
                        navController.navigate(Screen.Detail.createRoute(trx.id))
                    }
                },
                onStatusClick = { navController.navigate(Screen.ConnectionStatus.route) },
            )
        }

        composable(Screen.Create.route) {
            CreateTransactionScreen(
                onBack = { navController.popBackStack() },
                onCreated = { id ->
                    navController.navigate(Screen.QrDisplay.createRoute(id)) {
                        popUpTo(Screen.Create.route) { inclusive = true }
                    }
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
