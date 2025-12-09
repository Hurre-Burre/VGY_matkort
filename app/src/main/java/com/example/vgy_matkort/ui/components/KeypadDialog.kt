package com.example.vgy_matkort.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.vgy_matkort.ui.theme.SurfaceDark
import com.example.vgy_matkort.ui.theme.TextSecondary
import com.example.vgy_matkort.ui.theme.TextWhite

@Composable
fun KeypadDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    title: String,
    initialValue: String = "",
    subtitle: @Composable (() -> Unit)? = null
) {
    var amountString by remember { mutableStateOf(initialValue) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Full screen width
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF121212) // Dark background like the screenshot
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding() // Handle keyboard/navigation bar
                    .padding(16.dp),
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
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextWhite
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                    // Empty box to balance the row
                    Box(modifier = Modifier.size(48.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                // Amount Display
                Text(
                    text = if (amountString.isEmpty()) "0 kr" else "$amountString kr",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextWhite
                )

                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    subtitle()
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Confirm Button
                Button(
                    onClick = {
                        val amount = amountString.toIntOrNull()
                        if (amount != null) {
                            onConfirm(amount)
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
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "Bekräfta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Keypad
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        // Empty or Dot
                        Box(modifier = Modifier.size(80.dp)) // Placeholder for alignment
                        
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
                                contentDescription = "Backspace",
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

@Composable
fun KeypadButton(
    number: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .background(Color(0xFF2C2C2C)), // Dark grey background for buttons
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            style = MaterialTheme.typography.headlineMedium,
            color = TextWhite,
            fontWeight = FontWeight.Medium
        )
    }
}
