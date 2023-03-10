package com.example.biometricstest

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.biometricstest.destinations.DifferentScreenDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

private lateinit var cryptographyManager: CryptographyManager

@Composable
@Destination
fun EnableBiometricScreen(
    navigation: DestinationsNavigator,
    encryptionProcess: (authResult: androidx.biometric.BiometricPrompt.AuthenticationResult) -> Unit
) {

    val context = LocalContext.current
    val fragmentActivity = context as FragmentActivity

    val loginField = remember { mutableStateOf("") }
    val passwordField = remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ENABLE BIOMETRIC LOGIN"
        )

        Spacer(modifier = Modifier.height(25.dp))

        TextField(
            value = loginField.value,
            onValueChange = { loginField.value = it }
        )

        Spacer(modifier = Modifier.height(25.dp))

        TextField(
            value = passwordField.value,
            onValueChange = { passwordField.value = it }
        )

        Spacer(modifier = Modifier.height(25.dp))

        Row() {
            Button(onClick = {
                navigation.popBackStack()
                navigation.navigate(DifferentScreenDestination)
            }) {
                Text("CANCEL")
            }

            Spacer(modifier = Modifier.width(25.dp))

            Button(onClick = {
                login(loginField.value)
                showBiometricPromptForEncryption(context, fragmentActivity, encryptionProcess) //меня беспокоит передача активити
                Log.d("BioLog", "Kill that motherfucker")
                navigation.popBackStack()
                navigation.navigate(DifferentScreenDestination)
            }) {
                Text("AUTHORIZE")
            }
        }
    }
}

//maybe we need to store this function in activity
private fun showBiometricPromptForEncryption(
    context: Context,
    activity: FragmentActivity,
    encryptionProcess: (authResult: androidx.biometric.BiometricPrompt.AuthenticationResult) -> Unit,
) {
    Log.d("BioLog", "showBiometricPromptForEncryption")
    val canAuthenticate = BiometricManager.from(context).canAuthenticate()
    if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
        val secretKeyName = "biometric_sample_encryption_key"
        cryptographyManager = CryptograhyManager()
        val cipher = cryptographyManager.getInitializedCipherForEncryption(secretKeyName)
        val biometricPrompt =
            BiometricPromptUtils.createBiometricPrompt(activity, encryptionProcess)
        val promptInfo = BiometricPromptUtils.createPromptInfo(activity)
        biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }
}

//maybe we need to store this function in activity
//private fun encryptAndStoreServerToken(authResult: BiometricPrompt.AuthenticationResult) {
//    authResult.cryptoObject?.cipher?.apply {
//        SampleUser.fakeToken?.let { token ->
//            Log.d("BioTag", "The token from server is $token")
//            val encryptedServerTokenWrapper = cryptographyManager.encryptData(token, this)
//            cryptographyManager.persistCiphertextWrapperToSharedPrefs(
//                encryptedServerTokenWrapper,
//                LocalContext.current,
//                SHARED_PREFS_FILENAME,
//                Context.MODE_PRIVATE,
//                CIPHERTEXT_WRAPPER
//            )
//        }
//    }
//    //finish() it's a activity's method, we need to use navigation.popBackStack() here
//}

private fun login(login: String) {
    SampleUser.username = login
    SampleUser.fakeToken = java.util.UUID.randomUUID().toString()
    //login result (true)
    //else
    //login result (false)
}