package id.viasco.dynamic_qris_android.ui.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import androidx.core.content.ContextCompat

/**
 * Biometric helper.
 *
 * Falls back to device credential (PIN/password/pattern) when biometric is
 * unavailable, so users with non-biometric lock screens can still unlock.
 */
object BiometricAuthHelper {

    sealed interface Result {
        data object Success : Result
        data object Failure : Result
        data class Error(val code: Int, val message: String) : Result
        data object Unavailable : Result
    }

    private const val ALLOWED_AUTHENTICATORS =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

    fun canAuthenticate(activity: FragmentActivity): Boolean {
        val manager = BiometricManager.from(activity)
        return when (manager.canAuthenticate(ALLOWED_AUTHENTICATORS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    fun prompt(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        onResult: (Result) -> Unit,
    ) {
        if (!canAuthenticate(activity)) {
            onResult(Result.Unavailable)
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onResult(Result.Success)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onResult(Result.Error(errorCode, errString.toString()))
            }

            override fun onAuthenticationFailed() {
                onResult(Result.Failure)
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
            .build()

        prompt.authenticate(info)
    }
}
