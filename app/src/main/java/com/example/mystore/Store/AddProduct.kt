package com.example.mystore

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

// productCategories is now imported from StoreModels

val shippingMethods = listOf(
    "🚶 Self Delivery / Hand Delivery", "🏘️ Village-Level Delivery", "🛵 Delivery by Two-Wheeler", 
    "🏪 Pickup from Store", "📮 India Post", "📦 Rural Post Office", "🚐 Shared Jeep / Van", "🚌 Bus Parcel Service"
)

val shippingAvailabilityOptions = listOf(
    "📍 Local Area Only", "🗺️ Within District", "🏛️ Within State", "🇮🇳 All India Delivery", "🏪 Pickup Only"
)

val capacityUnits = listOf("Weekly", "Monthly", "Quarterly", "Half a year", "Yearly")


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductPage(
    viewModel: ProductManagerViewModel = viewModel(),
    onBackClick: () -> Unit,
    onProductAdded: () -> Unit
) {
    val context = LocalContext.current
    var productName by remember { mutableStateOf("") }
    var productType by remember { mutableStateOf("") }
    var selectedCategories = remember { mutableStateListOf<String>() }
    var showOthersField by remember { mutableStateOf(false) }
    var customProductType by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }
    var productWholesalePrice by remember { mutableStateOf("") }
    var stockQuantity by remember { mutableStateOf("") }
    var productCapacity by remember { mutableStateOf("") }
    var productCapacityUnit by remember { mutableStateOf("") }
    var productDescription by remember { mutableStateOf("") }
    var shippingMethod by remember { mutableStateOf("") }
    var shippingAvailability by remember { mutableStateOf("") }
    
    val selectedImages = remember { mutableStateListOf<Uri>() }
    var isLoading by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        selectedImages.addAll(uris)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Product", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF673AB7)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFAFAFA))
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text("Product Information", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF673AB7))
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = productName,
                onValueChange = { productName = it },
                label = { Text("Product Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )
            Spacer(modifier = Modifier.height(15.dp))

            var expandedType by remember { mutableStateOf(false) }
            var searchText by remember { mutableStateOf("") }
            val filteredCategories = productCategories.filter { it.contains(searchText, ignoreCase = true) }

            ExposedDropdownMenuBox(
                expanded = expandedType,
                onExpandedChange = { expandedType = !expandedType }
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { 
                        searchText = it 
                        expandedType = true
                    },
                    label = { Text("Search & Select Categories") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(15.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                )
                ExposedDropdownMenu(
                    expanded = expandedType,
                    onDismissRequest = { expandedType = false }
                ) {
                    filteredCategories.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = selectedCategories.contains(selectionOption), onCheckedChange = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(selectionOption)
                                }
                            },
                            onClick = {
                                if (selectionOption == "➕ Others") {
                                    showOthersField = !showOthersField
                                } else {
                                    if (selectedCategories.contains(selectionOption)) {
                                        selectedCategories.remove(selectionOption)
                                    } else {
                                        selectedCategories.add(selectionOption)
                                    }
                                }
                                // Don't close for multiple selection
                            }
                        )
                    }
                }
            }
            
            if (selectedCategories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(selectedCategories) { cat ->
                        Surface(
                            color = Color(0xFF673AB7).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF673AB7).copy(alpha = 0.2f))
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(cat, fontSize = 12.sp, color = Color(0xFF673AB7))
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Close, 
                                    null, 
                                    modifier = Modifier.size(14.dp).clickable { selectedCategories.remove(cat) },
                                    tint = Color(0xFF673AB7)
                                )
                            }
                        }
                    }
                }
            }

            if (showOthersField) {
                Spacer(modifier = Modifier.height(15.dp))
                OutlinedTextField(
                    value = customProductType,
                    onValueChange = { customProductType = it },
                    label = { Text("Enter Custom Product Type") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )
            }
            Spacer(modifier = Modifier.height(15.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                OutlinedTextField(
                    value = productPrice,
                    onValueChange = { productPrice = it },
                    label = { Text("Retail Price (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(15.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )
                OutlinedTextField(
                    value = productWholesalePrice,
                    onValueChange = { productWholesalePrice = it },
                    label = { Text("Wholesale Price (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(15.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )
            }
            Spacer(modifier = Modifier.height(15.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                OutlinedTextField(
                    value = stockQuantity,
                    onValueChange = { stockQuantity = it },
                    label = { Text("Stock Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(15.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )
                OutlinedTextField(
                    value = productCapacity,
                    onValueChange = { productCapacity = it },
                    label = { Text("Capacity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(15.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )
            }
            Spacer(modifier = Modifier.height(15.dp))

            var expandedUnit by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedUnit,
                onExpandedChange = { expandedUnit = !expandedUnit },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = productCapacityUnit,
                    onValueChange = { productCapacityUnit = it },
                    label = { Text("Capacity Unit (e.g. Weekly, Monthly)") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(15.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnit) },
                    readOnly = true
                )
                ExposedDropdownMenu(
                    expanded = expandedUnit,
                    onDismissRequest = { expandedUnit = false }
                ) {
                    capacityUnits.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                productCapacityUnit = selectionOption
                                expandedUnit = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(15.dp))

            OutlinedTextField(
                value = productDescription,
                onValueChange = { productDescription = it },
                label = { Text("Product Description") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text("Product Images", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF673AB7))
            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .border(2.dp, Color(0xFF673AB7), RoundedCornerShape(15.dp))
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF673AB7))
                            Text("Add Photo", color = Color(0xFF673AB7), fontSize = 12.sp)
                        }
                    }
                }
                items(selectedImages) { uri ->
                    Box(modifier = Modifier.size(100.dp)) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(15.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { selectedImages.remove(uri) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .background(Color.Red, RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Shipping Information", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF673AB7))
            Spacer(modifier = Modifier.height(10.dp))

            var expandedShipping by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedShipping,
                onExpandedChange = { expandedShipping = !expandedShipping }
            ) {
                OutlinedTextField(
                    value = shippingMethod,
                    onValueChange = { shippingMethod = it },
                    label = { Text("Shipping Method") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(15.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedShipping) },
                )
                ExposedDropdownMenu(
                    expanded = expandedShipping,
                    onDismissRequest = { expandedShipping = false }
                ) {
                    shippingMethods.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                shippingMethod = selectionOption
                                expandedShipping = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(15.dp))

            var expandedCoverage by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedCoverage,
                onExpandedChange = { expandedCoverage = !expandedCoverage }
            ) {
                OutlinedTextField(
                    value = shippingAvailability,
                    onValueChange = { shippingAvailability = it },
                    label = { Text("Shipping Availability") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(15.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCoverage) },
                )
                ExposedDropdownMenu(
                    expanded = expandedCoverage,
                    onDismissRequest = { expandedCoverage = false }
                ) {
                    shippingAvailabilityOptions.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                shippingAvailability = selectionOption
                                expandedCoverage = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    val price = productPrice.toDoubleOrNull() ?: 0.0
                    val wholesale = productWholesalePrice.toDoubleOrNull() ?: 0.0
                    val stock = stockQuantity.toIntOrNull() ?: 0
                    val capacity = productCapacity.toDoubleOrNull() ?: 0.0
                    if (productName.isNotEmpty() && selectedImages.isNotEmpty()) {
                        isLoading = true
                        val finalCategories = selectedCategories.toList() + if (showOthersField && customProductType.isNotBlank()) listOf(customProductType.trim()) else emptyList()
                        val newProduct = Product(
                            productName = productName,
                            productType = if (finalCategories.isNotEmpty()) finalCategories[0] else "",
                            productCategories = finalCategories,
                            productPrice = price,
                            productWholesalePrice = wholesale,
                            stockQuantity = stock,
                            productCapacity = capacity,
                            productCapacityUnit = productCapacityUnit,
                            productDescription = productDescription,
                            shippingMethod = shippingMethod,
                            shippingAvailability = shippingAvailability,
                            productImages = selectedImages.toList(),
                            productImageUrls = emptyList()
                        )
                        viewModel.addProduct(context, newProduct) { success ->
                            isLoading = false
                            if (success) {
                                onProductAdded()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Product", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
