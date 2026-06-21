package id.viasco.dynamic_qris_android.ui.navigation

/** Single source of truth for navigation routes. */
sealed class Screen(val route: String) {
    data object History : Screen("history")
    data object Create : Screen("create")
    data object QrDisplay : Screen("qr/{transactionId}") {
        const val ARG_TRANSACTION_ID = "transactionId"
        fun createRoute(id: String) = "qr/$id"
    }
    data object Detail : Screen("detail/{transactionId}") {
        const val ARG_TRANSACTION_ID = "transactionId"
        fun createRoute(id: String) = "detail/$id"
    }
}
