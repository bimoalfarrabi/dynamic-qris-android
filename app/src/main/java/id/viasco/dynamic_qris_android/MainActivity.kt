package id.viasco.dynamic_qris_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import id.viasco.dynamic_qris_android.ui.auth.BiometricAuthHelper
import id.viasco.dynamic_qris_android.ui.navigation.AppNavHost
import id.viasco.dynamic_qris_android.ui.theme.DynamicQrisTheme

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private var isAuthenticated by mutableStateOf(false)
    private var authError by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DynamicQrisTheme {
                if (isAuthenticated) {
                    MainApp()
                } else {
                    AuthGate(
                        errorMessage = authError,
                        onUnlock = ::promptBiometric,
                    )
                }
            }
        }

        // Auto-prompt on launch.
        if (!isAuthenticated) {
            promptBiometric()
        }
    }

    private fun promptBiometric() {
        authError = null
        BiometricAuthHelper.prompt(
            activity = this,
            title = getString(R.string.auth_title),
            subtitle = getString(R.string.auth_subtitle),
            onResult = { result ->
                when (result) {
                    BiometricAuthHelper.Result.Success -> {
                        isAuthenticated = true
                    }
                    BiometricAuthHelper.Result.Failure -> {
                        authError = getString(R.string.auth_failed)
                    }
                    is BiometricAuthHelper.Result.Error -> {
                        authError = result.message
                    }
                    BiometricAuthHelper.Result.Unavailable -> {
                        // No biometric enrolled — allow through (single user, personal device).
                        isAuthenticated = true
                    }
                }
            },
        )
    }
}

@Composable
private fun MainApp() {
    val navController = rememberNavController()
    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            AppNavHost(navController = navController)
        }
    }
}

@Composable
private fun AuthGate(
    errorMessage: String?,
    onUnlock: () -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.auth_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 16.dp),
            )
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            Button(
                onClick = onUnlock,
                modifier = Modifier.padding(top = 24.dp),
            ) {
                Text(stringResource(R.string.auth_unlock))
            }
        }
    }
}
