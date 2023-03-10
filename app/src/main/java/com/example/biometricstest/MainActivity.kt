package com.example.biometricstest

import android.Manifest
import android.Manifest.permission.USE_BIOMETRIC
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.from
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.example.biometricstest.destinations.DifferentScreenDestination
import com.example.biometricstest.destinations.EnableBiometricScreenDestination
import com.example.biometricstest.ui.theme.BiometricsTestTheme
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

class MainActivity : FragmentActivity() {
    private val cryptographyManager = CryptograhyManager()
    private lateinit var biometricPrompt: androidx.biometric.BiometricPrompt
    private val ciphertextWrapper
        get() = cryptographyManager.getCiphertextWrapperFromSharedPrefs(
            applicationContext,
            SHARED_PREFS_FILENAME,
            Context.MODE_PRIVATE,
            CIPHERTEXT_WRAPPER
        )
    private var cancellationSignal: CancellationSignal? = null
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BiometricsTestTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    DestinationsNavHost(navGraph = NavGraphs.root) {
                        composable(DifferentScreenDestination) {
                            DifferentScreen(
                                navigation = destinationsNavigator,
                                onClick = {
                                    if (ciphertextWrapper != null) {
                                        launchBiometric()
                                    } else {
                                        destinationsNavigator.popBackStack()
                                        destinationsNavigator.navigate(
                                            EnableBiometricScreenDestination
                                        )
                                    }
                                }
                            )
                        }
                        composable(EnableBiometricScreenDestination) {
                            EnableBiometricScreen(
                                navigation = destinationsNavigator,
                                encryptionProcess = ::encryptAndStoreServerToken
                            )
                        }
                    }
                }
            }
        }
    }

    private val authenticationCallback: BiometricPrompt.AuthenticationCallback //AuthCallback
        get() = @RequiresApi(Build.VERSION_CODES.P)
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                notifyUser("Error. Code is: $errorCode")
                super.onAuthenticationError(errorCode, errString)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                notifyUser("Auth succeded")
                super.onAuthenticationSucceeded(result)
                //processSuccess(result)
            }
        }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun launchBiometric() {
        if (checkBiometricSupport()) {
            val biometricPrompt = android.hardware.biometrics.BiometricPrompt
                .Builder(this)
                .setTitle("Подтвердите логин по отпечатку")
                .setSubtitle("Вам больше не понадобиться логин и пароль")
                .setDescription("Мы используем биометрию для того что бы...")
                .setNegativeButton("Не сейчас", this.mainExecutor) { dialog, _ ->
                    notifyUser("Авторизация отменена")
                    dialog.cancel()
                }.build()
            biometricPrompt.authenticate(getCancellationSignal(), mainExecutor, authenticationCallback)
        }
    }

    private fun encryptAndStoreServerToken(authResult: androidx.biometric.BiometricPrompt.AuthenticationResult) {
        Log.d("BioTag", "")
        authResult.cryptoObject?.cipher?.apply {
            SampleUser.fakeToken?.let { token ->
                Log.d("BioTag", "The token from server is $token")
                val encryptedServerTokenWrapper = cryptographyManager.encryptData(token, this)
                cryptographyManager.persistCiphertextWrapperToSharedPrefs(
                    encryptedServerTokenWrapper,
                    applicationContext,
                    SHARED_PREFS_FILENAME,
                    Context.MODE_PRIVATE,
                    CIPHERTEXT_WRAPPER
                )
            }
        }
        //finish() it's a activity's method, we need to use navigation.popBackStack() here
    }

    private fun getCancellationSignal(): CancellationSignal {
        cancellationSignal = CancellationSignal()
        cancellationSignal?.setOnCancelListener {
            notifyUser("Auth cancelled via Signal")
        }

        return cancellationSignal as CancellationSignal
    }

    private fun checkBiometricSupport(): Boolean {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if (!keyguardManager.isDeviceSecure) {
            notifyUser("lock screen security not enabled in the setting")
            return false
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED) {
            notifyUser("Finger print auth permission not neabled")
            return false
        }
        return packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
    }

    private fun notifyUser(message: String) {
        Log.d("BioLog", message)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun showBiometricPromptForDecryption() {
        ciphertextWrapper?.let { textWrapper ->
            val secretKeyName = getString(R.string.secret_key_name)
            val cipher = cryptographyManager.getInitializedCipherForDecryption(
                secretKeyName, textWrapper.initializationVector
            )
            biometricPrompt =
                BiometricPromptUtils.createBiometricPrompt(
                    this,
                    ::decryptServerTokenFromStorage
                )
            val promptInfo = BiometricPromptUtils.createPromptInfo(this)
            biometricPrompt.authenticate(promptInfo, androidx.biometric.BiometricPrompt.CryptoObject(cipher))
        }
    }

    private fun decryptServerTokenFromStorage(authResult: androidx.biometric.BiometricPrompt.AuthenticationResult) {
        ciphertextWrapper?.let { textWrapper ->
            authResult.cryptoObject?.cipher?.let {
                val plainText =
                    cryptographyManager.decryptData(textWrapper.ciphertext, it)
                SampleUser.fakeToken = plainText

                Log.d("BioLog", "Logged in (decryptServerTokenFromStorage)")
            }
        }
    }
}

@Destination(
    start = true
)
@Composable
fun Greeting(
    navigation: DestinationsNavigator
) {
    Column(
        Modifier
            .fillMaxHeight()
            .absolutePadding(10.dp, 100.dp, 10.dp, 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            modifier = Modifier.size(100.dp),
            onClick = {
                navigation.popBackStack()
                navigation.navigate(DifferentScreenDestination)
            }
        ) {
            Text("Click me")
        }
    }
}