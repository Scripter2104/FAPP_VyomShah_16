package com.example.jetpackcompose

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class Encryption {
    fun encryptAES(data: String, secretKey: SecretKey, iv: ByteArray): String {

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
        val encryptedData = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encryptedData, Base64.DEFAULT)
    }
}

data class QRData(
    var id: String,
    var user: String,
    var amount: String,
    var date: String,
    var time: String,
    var paymentType: String,
)

data class encryptedData(
    var encryptedData: String,
    var secretKey:String,
    var iv:String,
)