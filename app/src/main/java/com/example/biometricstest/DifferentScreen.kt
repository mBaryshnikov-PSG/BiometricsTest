package com.example.biometricstest

import android.util.Log
import androidx.biometric.BiometricManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext

@Destination
@Composable
fun DifferentScreen(
    navigation: DestinationsNavigator,
    onClick: () -> Unit,
) {
    val context = LocalContext.current

    val loginField = remember { mutableStateOf("") }
    val passwordField = remember { mutableStateOf("") }

    //Отвечает за проверку, есть ли поддержка биометрики в телефоне
    val canAuth = BiometricManager.from(context).canAuthenticate()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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

        Button(
            onClick = {
                Log.d("BioTag", "Login in diff screen")
            }
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(25.dp))

        if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
            Text(
                text = "Use biometrics",
                modifier = Modifier.clickable(
                    onClick = onClick
                )
            )
        }
    }
}