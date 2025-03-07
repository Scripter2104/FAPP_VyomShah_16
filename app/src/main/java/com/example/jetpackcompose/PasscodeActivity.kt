package com.example.jetpackcompose

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PasscodeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent{
            PasscodeScreen(intent.getStringExtra("data")?:"No data")

        }

    }
}

@Composable
fun PasscodeScreen(data:String){
    var passcode by remember { mutableStateOf("") }
    val maxPasscodeLength = 4
    val context = LocalContext.current
    val qrData = Gson().fromJson(data,QRData::class.java)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)), // Dark mode
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Enter Your Passcode", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(maxPasscodeLength) { index ->
                AnimatedVisibility(visible = index < passcode.length, enter = androidx.compose.animation.fadeIn(tween(300))) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
                if (index >= passcode.length) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        NumberPad(onNumberClick = { number ->
            if (passcode.length < maxPasscodeLength) {
                passcode += number
            }
        }, onDeleteClick = {
            if (passcode.isNotEmpty()) {
                passcode = passcode.dropLast(1)
            }
        })

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { if(passcode.equals("1234")){
                val intent = Intent(context,Dashboard::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                val newTransaction = Transaction(LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy")).toString(),qrData.user,"₹"+qrData.amount+".00",R.drawable.img_2)
                TransactionHistory.getInstance().transactionList.add(newTransaction)
                context.startActivity(intent)
            } },
            enabled = passcode.length == maxPasscodeLength,
            modifier = Modifier.fillMaxWidth(0.5f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008CFF))
        ) {
            Text("Confirm", fontSize = 18.sp, color = Color.White)
        }
    }
}

@Composable
fun NumberPad(onNumberClick: (String) -> Unit, onDeleteClick: () -> Unit) {
    val numbers = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "←")
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        numbers.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { number ->
                    if (number.isNotEmpty()) {
                        Button(
                            onClick = {
                                if (number == "←") onDeleteClick() else onNumberClick(number)
                            },
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E))
                        ) {
                            Text(
                                number,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(70.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

