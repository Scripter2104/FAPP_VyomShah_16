package com.example.jetpackcompose

import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class GenerateKey {

    fun createSecretKey():SecretKey{
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        return keyGen.generateKey()
    }
}