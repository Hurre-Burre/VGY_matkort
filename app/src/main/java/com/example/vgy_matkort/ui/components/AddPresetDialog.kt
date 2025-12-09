package com.example.vgy_matkort.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.vgy_matkort.ui.theme.TextWhite

@Composable
fun AddPresetDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1 = amount, 2 = name
    var amount by remember { mutableStateOf<Int?>(null) }
    var amountString by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF121212)
        ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding() // Handle keyboard
                        .navigationBarsPadding() // Handle navigation bar
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()), // Allow scrolling if needed
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (step == 2) {
                            step = 1
                        } else {
                            onDismiss()
                        }
                    }) {
                        Icon(
                            imageVector = if (step == 2) Icons.Default.ArrowBack else Icons.Default.Close,
                            contentDescription = if (step == 2) "Tillbaka" else "Stäng",
                            tint = TextWhite
                        )
                    }
                    Text(
                        text = if (step == 1) "Ange belopp" else "Ange namn",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                    Box(modifier = Modifier.size(48.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                if (step == 1) {
                    // Amount Display
                    Text(
                        text = if (amountString.isEmpty()) "0 kr" else "$amountString kr",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextWhite
                    )
                    
                    Spacer(modifier = Modifier.height(48.dp))
                } else {
                    // Name Input - Modern Design
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = name,
                            onValueChange = { name = it },
                            textStyle = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                            singleLine = true,
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(TextWhite),
                            decorationBox = { innerTextField ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (name.isEmpty()) {
                                        Text(
                                            text = "Namn",
                                            style = MaterialTheme.typography.displayLarge.copy(
                                                fontSize = 48.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = TextWhite.copy(alpha = 0.3f)
                                        )
                                    } else {
                                        innerTextField()
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(200.dp)
                                            .height(2.dp)
                                            .background(TextWhite.copy(alpha = 0.5f))
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "t.ex. Nocco, Coca-Cola, Munk",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextWhite.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        )
                    }
                }

                if (step == 1) {
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Confirm Button
                Button(
                    onClick = {
                        if (step == 1) {
                            val parsedAmount = amountString.toIntOrNull()
                            if (parsedAmount != null) {
                                amount = parsedAmount
                                step = 2
                            }
                        } else {
                            if (name.isNotBlank() && amount != null) {
                                onConfirm(amount!!, name)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TextWhite,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(28.dp),
                    enabled = if (step == 1) amountString.isNotEmpty() else name.isNotBlank()
                ) {
                    Text(
                        text = if (step == 1) "Nästa" else "Spara",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (step == 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (step == 1) {
                    // Keypad - Reduced spacing
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp) // Reduced from 16dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            KeypadButton("1") { amountString += "1" }
                            KeypadButton("2") { amountString += "2" }
                            KeypadButton("3") { amountString += "3" }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            KeypadButton("4") { amountString += "4" }
                            KeypadButton("5") { amountString += "5" }
                            KeypadButton("6") { amountString += "6" }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            KeypadButton("7") { amountString += "7" }
                            KeypadButton("8") { amountString += "8" }
                            KeypadButton("9") { amountString += "9" }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Box(modifier = Modifier.size(80.dp))
                            
                            KeypadButton("0") { 
                                if (amountString.isNotEmpty()) amountString += "0" 
                            }
                            
                            // Backspace
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        if (amountString.isNotEmpty()) {
                                            amountString = amountString.dropLast(1)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Radera",
                                    tint = TextWhite,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(96.dp))
                }
            }
        }
    }
}
