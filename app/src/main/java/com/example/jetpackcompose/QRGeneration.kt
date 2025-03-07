package com.example.jetpackcompose

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.util.UUID
import com.google.gson.Gson
import java.time.LocalTime
import java.time.LocalDate
import java.util.Base64 // ✅ Corrected import

class QRGeneration : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        setContent {
            GenerateQRScreen(intent.getStringExtra("user") ?: "No user")
        }
    }
}

@Composable
fun GenerateQRScreen(username: String) {
    var amount by remember { mutableStateOf("") }
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Enter Amount", fontSize = 20.sp)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    qrCodeBitmap = generateQRCode(
                        UUID.randomUUID().toString(),
                        username,
                        amount,
                        LocalTime.now().toString(),
                        LocalDate.now().toString(),
                        "Buyer"
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generate QR", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            qrCodeBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Generated QR Code",
                    modifier = Modifier.size(200.dp)
                )
            }
        }
    }
}

fun generateQRCode(
    id: String, user: String, amount: String, date: String, time: String, paymentType: String
): Bitmap {
    val size = 512
    val jsonData = Gson().toJson(QRData(id, user, amount, date, time, paymentType))


    val secretKey = GenerateKey().createSecretKey()
    val iv = ByteArray(16) { 0 }


    val encryptedData = Encryption().encryptAES(jsonData, secretKey, iv)


    val secretKeyBase64 = Base64.getEncoder().encodeToString(secretKey.encoded)
    val ivBase64 = Base64.getEncoder().encodeToString(iv)


    val qrContent = Gson().toJson(encryptedData(encryptedData,secretKeyBase64,ivBase64))


    val bitMatrix: BitMatrix = MultiFormatWriter().encode(qrContent, BarcodeFormat.QR_CODE, size, size)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)

    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.Black.toArgb() else Color.White.toArgb())
        }
    }
    return bitmap
}
