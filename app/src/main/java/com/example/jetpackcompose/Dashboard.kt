package com.example.jetpackcompose

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.google.gson.Gson
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.crypto.spec.SecretKeySpec

class Dashboard : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        val transactionHistory = TransactionHistory.getInstance().transactionList

        setContent {
            UserDashboard(intent.getStringExtra("user") ?: "No user",transactionHistory)
        }
    }
}

@Composable
fun CustomStyledButton(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.padding(horizontal = 2.dp) // Reduced padding
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = Color.White,
            modifier = Modifier.size(10.dp)
        )
        Spacer(modifier = Modifier.width(4.dp)) // Adjust space between icon and text if needed
        Text(
            text = text,
            color = Color.White,
            maxLines = 1,
            modifier = Modifier.padding(0.dp),
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
fun UserDashboard(username: String, initialTransactions: List<Transaction>) {
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }
    var scannedData by remember { mutableStateOf("No QR Scanned yet") }

    // Convert the provided list to a mutable state list for instant UI updates
    val transactions = remember { mutableStateListOf<Transaction>().apply { addAll(initialTransactions) } }
    // Mutable state for total tokens (starting value: 1000, adjust as needed)
    var totalTokens by remember { mutableDoubleStateOf(TransactionHistory.getInstance().tokens) }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            scannedData = result.contents
        }

        val qrData = Gson().fromJson(scannedData, encryptedData::class.java)

        val secretKeyBytes = Base64.decode(qrData.secretKey, Base64.DEFAULT)
        val secretKey = SecretKeySpec(secretKeyBytes, "AES")
        val ivBytes = Base64.decode(qrData.iv, Base64.DEFAULT)
        val decryptedData = Decryption().decryptAES(qrData.encryptedData, secretKey, ivBytes)
        val decryptedJsonData = Gson().fromJson(decryptedData, QRData::class.java)
        val id = decryptedJsonData.id
        val amountValue = decryptedJsonData.amount.toIntOrNull() ?: 0

        if(TransactionHistory.getInstance().qrID.add(id)) {
            if (decryptedJsonData.paymentType == "Buyer") {
                totalTokens -= amountValue
                TransactionHistory.getInstance().tokens = totalTokens
                context.startActivity(
                    Intent(context, PaymentActivity::class.java).putExtra("data", decryptedData)
                )

            } else if (decryptedJsonData.paymentType == "Seller") {
                val formattedDate = LocalDate.parse(decryptedJsonData.date, DateTimeFormatter.ISO_LOCAL_DATE)
                    .format(DateTimeFormatter.ofPattern("MMM d, yyyy"))

                val newTransaction = Transaction(
                    formattedDate,
                    decryptedJsonData.user,
                    "+₹${decryptedJsonData.amount}",
                    R.drawable.img_2,
                    "Seller"
                )

                // Add the new transaction only to the mutable state list so the UI refreshes
                transactions.add(newTransaction)
                // Otherwise, omit the next line to avoid duplicate additions:
                TransactionHistory.getInstance().transactionList.add(newTransaction)
                // Assuming decryptedJsonData.amount is convertible to Int; adjust logic if needed.

                totalTokens += amountValue


                TransactionHistory.getInstance().tokens = totalTokens
            }
        } else {
            Toast.makeText(context, "QR Already scanned", Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Text(
                "QR Wallet",
                color = Color.White,
                fontSize = 28.sp,
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
            ) {
                BasicTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CustomStyledButton("Filter", Icons.Default.FilterList) {}
                CustomStyledButton("Generate QR", Icons.Default.QrCodeScanner) {
                    val intent = Intent(context, QRGeneration::class.java)
                    intent.putExtra("user", username)
                    context.startActivity(intent)
                }
                CustomStyledButton("Sort", Icons.Default.Sort) {}
            }
            Spacer(modifier = Modifier.height(20.dp))

            TotalTokenDisplay(totalTokens = totalTokens)

            Spacer(modifier = Modifier.height(20.dp))

            // Transactions Section
            Text("Recent Transactions", color = Color.White, fontSize = 20.sp)

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn {
                items(transactions) { transaction ->
                    TransactionItem(transaction)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }


        }

        FloatingActionButton(
            onClick = {
                val options = ScanOptions().apply {
                    setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                    setPrompt("Scan a QR Code")
                    setCameraId(0)
                    setBeepEnabled(true)
                    setOrientationLocked(true)
                    setBarcodeImageEnabled(true)
                }
                scanLauncher.launch(options)
            },
            containerColor = Color(0xFF1E88E5),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = "Scan QR Code",
                tint = Color.White
            )
        }
    }
}



//@Composable
//fun StyledButton(text: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
//    Button(
//        onClick = onClick,
//        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
//        shape = RoundedCornerShape(8.dp),
//        modifier = modifier.padding(horizontal = 4.dp) // Move padding inside
//    ) {
//        Icon(imageVector = icon, contentDescription = text, tint = Color.White, modifier = Modifier.size(10.dp))
//        Spacer(modifier = Modifier.width(8.dp))
//        Text(text, color = Color.White)
//    }
//}

@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.date, color = Color.White.copy(0.8f), fontSize = 14.sp)
                Text(
                    "${if (transaction.paymentType == "Buyer") "To" else "From"}: ${transaction.receiver}",
                    color = Color.White,
                    fontSize = 18.sp
                )
                Text(transaction.amount, color = Color.White.copy(0.8f), fontSize = 14.sp)
            }
            Icon(
                painter = painterResource(id = transaction.icon),
                contentDescription = "Transaction Status",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

data class Transaction(val date: String, val receiver: String, val amount: String, val icon: Int,val paymentType:String)

@Composable
fun TotalTokenDisplay(totalTokens: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E88E5))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Total Tokens",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = totalTokens.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Preview(widthDp = 360)
@Composable
fun UserDashboardPreview() {
    UserDashboard("John Doe", emptyList())
}
