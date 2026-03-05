package com.example.vgy_matkort.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.vgy_matkort.data.Restaurant
import com.example.vgy_matkort.ui.theme.SurfaceDark
import com.example.vgy_matkort.ui.theme.TextSecondary
import com.example.vgy_matkort.ui.theme.TextWhite
import com.example.vgy_matkort.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    amount: Int,
    restaurants: List<Restaurant>,
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var customRestaurantName by remember { mutableStateOf("") }
    var selectedRestaurant by remember { mutableStateOf<String?>(null) }
    
    // Default system names that shouldn't typically be selectable manually
    val filteredRestaurants = restaurants.filter { it.name != "System" && it.name != "Okänd" }.sortedBy { it.name }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Full screen width
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF121212)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 24.dp),
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
                        text = "Välj Restaurang",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                    // Empty box to balance the row
                    Box(modifier = Modifier.size(48.dp))
                }

                // Amount Display
                Text(
                    text = "$amount kr",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = PrimaryBlue
                )
                
                Spacer(modifier = Modifier.height(32.dp))

                // Custom Input Field
                OutlinedTextField(
                    value = customRestaurantName,
                    onValueChange = { 
                        customRestaurantName = it
                        selectedRestaurant = null // Deselect chip if typing custom
                    },
                    label = { Text("Ny restaurang / Sök", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = SurfaceDark,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        cursorColor = PrimaryBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Existing Restaurants List
                if (filteredRestaurants.isNotEmpty()) {
                    Text(
                        text = "Tidigare val",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextSecondary,
                        modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
                    )
                    
                    val displayList = if (customRestaurantName.isNotBlank()) {
                        filteredRestaurants.filter { it.name.contains(customRestaurantName, ignoreCase = true) }
                    } else {
                        filteredRestaurants
                    }
                    
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 260.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(displayList) { restaurant ->
                            RestaurantChip(
                                name = restaurant.name,
                                isSelected = selectedRestaurant == restaurant.name,
                                onClick = {
                                    selectedRestaurant = restaurant.name
                                    customRestaurantName = "" // Clear input if selecting existing
                                }
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Confirm Button
                val finalName = selectedRestaurant ?: customRestaurantName.trim()
                val isEnabled = finalName.isNotBlank()

                Button(
                    onClick = {
                        if (isEnabled) {
                            onConfirm(amount, finalName)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = isEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = TextWhite,
                        disabledContainerColor = SurfaceDark,
                        disabledContentColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "Bekräfta köp",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun RestaurantChip(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PrimaryBlue else SurfaceDark.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = TextWhite
            )
        }
    }
}
