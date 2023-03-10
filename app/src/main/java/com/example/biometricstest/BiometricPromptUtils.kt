package com.example.biometricstest

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricPromptUtils {
    private const val TAG = "BiometricPromptUtils"

    fun createBiometricPrompt(
        activity: FragmentActivity, //We need to do something with this AppCompatActivity
        processSuccess: (
            BiometricPrompt.AuthenticationResult
        ) -> Unit
    ): BiometricPrompt {
        Log.d("BioLog", "createBiometricPrompt started")

        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                Log.d("BioLog", "createBiometricPrompt onAuthSuccess")
                super.onAuthenticationSucceeded(result)
                processSuccess(result)
            }
        }
        return BiometricPrompt(activity, executor, callback)
    }

    fun createPromptInfo(activity: FragmentActivity): BiometricPrompt.PromptInfo =
        BiometricPrompt.PromptInfo.Builder().apply {
            setTitle(activity.getString(R.string.prompt_info_title))
            setSubtitle(activity.getString(R.string.prompt_info_subtitle))
            setDescription(activity.getString(R.string.prompt_info_description))
            setConfirmationRequired(false)
            setNegativeButtonText(activity.getString(R.string.prompt_info_use_app_password))
        }.build()
}