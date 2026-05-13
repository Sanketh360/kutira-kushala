package com.example.mystore.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.chatur.frontend.authentication.AuthService
import com.google.firebase.auth.FirebaseAuth

private val Primary = Color(0xFF6C3CE1)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: com.example.mystore.MainStoreViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onLogoutSuccess: () -> Unit,
    onNavigateToLocation: () -> Unit
) {
    val context = LocalContext.current
    val authService = AuthService(context)
    val user = FirebaseAuth.getInstance().currentUser
    
    val selectedState by viewModel.selectedState.collectAsState()
    val selectedDistrict by viewModel.selectedDistrict.collectAsState()
    
    var profileImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) profileImageUri = uri }

    var helpQuery by remember { mutableStateOf("") }
    var showHelpSection by remember { mutableStateOf(false) }
    var showAboutSection by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.ExtraBold, color = Color.Black) },
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
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar (Editable)
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(Primary.copy(alpha = 0.1f))
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileImageUri != null) {
                            AsyncImage(
                                model = profileImageUri,
                                contentDescription = "Profile Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Primary, modifier = Modifier.size(54.dp))
                        }
                        // Edit Badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp)
                                .background(Primary, CircleShape)
                                .border(androidx.compose.foundation.BorderStroke(2.dp, Color.White), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }


                    Spacer(Modifier.height(16.dp))
                    
                    Text(user?.displayName ?: "Store Owner", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    Text(user?.email ?: "No email provided", fontSize = 14.sp, color = Color.Black.copy(alpha = 0.6f))
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStatItem("Orders", "24")
                        Divider(modifier = Modifier.height(30.dp).width(1.dp), color = Color.LightGray)
                        ProfileStatItem("Rating", "4.8")
                        Divider(modifier = Modifier.height(30.dp).width(1.dp), color = Color.LightGray)
                        ProfileStatItem("Stores", "1")
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Options List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .padding(8.dp)
            ) {
                ProfileOptionItem(
                    icon = Icons.Default.LocationOn, 
                    title = "My Location", 
                    subtitle = if (selectedState != "Select State") "$selectedState, $selectedDistrict" else "Manage your delivery zones"
                ) {
                    onNavigateToLocation()
                }
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
                
                ProfileOptionItem(icon = Icons.Default.Help, title = "Help & Support", subtitle = "Submit queries and get help") {
                    showHelpSection = !showHelpSection
                    showAboutSection = false
                }

                if (showHelpSection) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = helpQuery,
                            onValueChange = { helpQuery = it },
                            placeholder = { Text("How can we help you?", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { 
                                helpQuery = ""
                                showHelpSection = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("Send Message", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Divider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))

                ProfileOptionItem(icon = Icons.Default.Info, title = "About App", subtitle = "Version 1.0.0 | Terms & Privacy") {
                    showAboutSection = !showAboutSection
                    showHelpSection = false
                }

                if (showAboutSection) {
                    Column(modifier = Modifier.padding(16.dp).background(Primary.copy(alpha = 0.05f), RoundedCornerShape(16.dp)).padding(16.dp)) {
                        Text("Kutira-Kushala", fontWeight = FontWeight.ExtraBold, color = Primary)
                        Text(
                            "Empowering rural entrepreneurs and cottage industries through technology and local commerce.",
                            fontSize = 13.sp,
                            color = Color.Black.copy(alpha = 0.7f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            // Logout
            TextButton(
                onClick = {
                    authService.signOut {
                        onLogoutSuccess()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Log Out", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
fun ProfileStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.Black)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun ProfileOptionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(Primary.copy(alpha = 0.1f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
    }
}



