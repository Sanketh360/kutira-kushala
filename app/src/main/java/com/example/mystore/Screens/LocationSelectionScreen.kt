package com.example.mystore.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mystore.indianStatesAndDistricts

private val Primary = Color(0xFF6C3CE1)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSelectionScreen(
    onBackClick: () -> Unit,
    onLocationSelected: (String, String, String, String) -> Unit,
    initialAddress: String = "",
    initialState: String = "",
    initialDistrict: String = ""
) {
    var state by remember { mutableStateOf(initialState) }
    var district by remember { mutableStateOf(initialDistrict) }
    var address by remember { mutableStateOf(initialAddress) }
    
    var stateFilter by remember { mutableStateOf("") }
    var expandedState by remember { mutableStateOf(false) }
    var expandedDistrict by remember { mutableStateOf(false) }

    val filteredStates = indianStatesAndDistricts.keys.filter { 
        it.contains(stateFilter, ignoreCase = true) 
    }

    val districts = indianStatesAndDistricts[state] ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose Your Location", fontWeight = FontWeight.ExtraBold, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Primary.copy(alpha = 0.2f)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Illustration Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.LocationOn, 
                    contentDescription = null, 
                    tint = Primary, 
                    modifier = Modifier.size(64.dp)
                )
            }

            Text(
                "Set your delivery zone to find local products near you.",
                fontSize = 16.sp,
                color = Color.Black.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )

            // Country
            LocationField(
                value = "India",
                onValueChange = {},
                label = "Country",
                icon = Icons.Default.Public,
                readOnly = true,
                enabled = false
            )

            // State Selection
            ExposedDropdownMenuBox(
                expanded = expandedState,
                onExpandedChange = { expandedState = !expandedState }
            ) {
                LocationField(
                    value = state,
                    onValueChange = {},
                    label = "State",
                    icon = Icons.Default.Map,
                    readOnly = true,
                    placeholder = "Select State",
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedState) },
                    modifier = Modifier.menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = expandedState,
                    onDismissRequest = { expandedState = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    OutlinedTextField(
                        value = stateFilter,
                        onValueChange = { stateFilter = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        placeholder = { Text("Search state...", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Primary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = Primary
                        )
                    )
                    
                    filteredStates.forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s, color = Color.Black) },
                            onClick = {
                                state = s
                                stateFilter = ""
                                district = ""
                                expandedState = false
                            }
                        )
                    }
                }
            }

            // District Selection
            ExposedDropdownMenuBox(
                expanded = expandedDistrict,
                onExpandedChange = { if (state.isNotEmpty()) expandedDistrict = !expandedDistrict }
            ) {
                LocationField(
                    value = district,
                    onValueChange = {},
                    label = "District",
                    icon = Icons.Default.LocationCity,
                    readOnly = true,
                    placeholder = if (state.isEmpty()) "Select state first" else "Select District",
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDistrict) },
                    enabled = state.isNotEmpty(),
                    modifier = Modifier.menuAnchor()
                )
                
                if (districts.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = expandedDistrict,
                        onDismissRequest = { expandedDistrict = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        districts.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(d, color = Color.Black) },
                                onClick = {
                                    district = d
                                    expandedDistrict = false
                                }
                            )
                        }
                    }
                }
            }

            // Address
            LocationField(
                value = address,
                onValueChange = { address = it },
                label = "Full Address",
                icon = Icons.Default.Home,
                minLines = 3,
                placeholder = "Enter your street, village, etc."
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (state.isNotEmpty() && district.isNotEmpty()) {
                        onLocationSelected("India", state, district, address)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .shadow(12.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                enabled = state.isNotEmpty() && district.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    disabledContainerColor = Primary.copy(alpha = 0.5f)
                )
            ) {
                Text("Confirm Location", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    placeholder: String = "",
    minLines: Int = 1,
    trailingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontWeight = FontWeight.SemiBold) },
        modifier = modifier.fillMaxWidth(),
        readOnly = readOnly,
        enabled = enabled,
        leadingIcon = { Icon(icon, contentDescription = null, tint = Primary) },
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(16.dp),
        minLines = minLines,
        placeholder = if (placeholder.isNotEmpty()) { { Text(placeholder, color = Color.Gray) } } else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            disabledTextColor = Color.Black,
            focusedBorderColor = Primary,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White.copy(alpha = 0.5f),
            focusedLabelColor = Primary,
            unfocusedLabelColor = Color.Black.copy(alpha = 0.6f)
        )
    )
}
