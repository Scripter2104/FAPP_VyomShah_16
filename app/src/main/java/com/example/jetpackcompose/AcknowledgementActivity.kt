package com.example.jetpackcompose

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.time.LocalDate
import java.time.LocalTime
import java.util.Base64

class AcknowledgementActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AcknowledgementScreen(intent.getStringExtra("data") ?: "No data")
        }
    }
}

@Composable
fun AcknowledgementScreen(data: String) {
    val qrData = Gson().fromJson(data, QRData::class.java)
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }

    qrCodeBitmap = generateAcknowledgementQRCode(
        qrData.id,
        qrData.user,
        qrData.amount,
        LocalDate.now().toString(),
        LocalTime.now().toString(),
        "Seller"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(Color(0xFFF5F5F5)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Acknowledgement QR Code",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(24.dp))

        qrCodeBitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Generated QR Code",
                modifier = Modifier
                    .size(250.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(8.dp)
            )
        }
    }
}

fun generateAcknowledgementQRCode(id: String, user: String, amount: String, date: String, time: String, paymentType: String): Bitmap? {
    return try {
        val jsonData = Gson().toJson(QRData(id, user, amount, date, time, paymentType))
        val secretKey = GenerateKey().createSecretKey()
        val iv = ByteArray(16) { 0 }

        val encryptedData = Encryption().encryptAES(jsonData, secretKey, iv)
        val secretKeyBase64 = Base64.getEncoder().encodeToString(secretKey.encoded)
        val ivBase64 = Base64.getEncoder().encodeToString(iv)

        val qrContent = Gson().toJson(encryptedData(encryptedData, secretKeyBase64, ivBase64))

        val bitMatrix: BitMatrix = MultiFormatWriter().encode(qrContent, BarcodeFormat.QR_CODE, 512, 512)
        val barcodeEncoder = BarcodeEncoder()
        barcodeEncoder.createBitmap(bitMatrix)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
