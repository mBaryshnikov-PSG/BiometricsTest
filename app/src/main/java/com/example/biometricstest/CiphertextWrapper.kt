package com.example.biometricstest

data class CiphertextWrapper(
    val ciphertext: ByteArray,
    val initializationVector: ByteArray
)