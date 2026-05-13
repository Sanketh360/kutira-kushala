package com.example.mystore

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
//import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

private val Purple = Color(0xFF6C3CE1)
private val PurpleDark = Color(0xFF4A1FA8)
private val PurpleLight = Color(0xFF9B6DFF)
private val Gold = Color(0xFFFFB930)
private val GreenSuccess = Color(0xFF1DB954)
private val Surface1 = Color(0xFFF8F5FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyStorePage(
    viewModel: ProductManagerViewModel = viewModel(),
    onNavigateToCreateStore: () -> Unit,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToProductDetail: (Product) -> Unit,
    onNavigateToEditStore: (StoreData) -> Unit
) {
    val isStoreCreated by viewModel.isStoreCreated.collectAsState()
    val storeData by viewModel.storeData.collectAsState()
    val products by viewModel.products.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
    
    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            viewModel.initializeStore()
            viewModel.loadProducts()
        }
    }

    // Reload products every time we return to this screen
    // (e.g. after adding a product from AddProductPage)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadProducts()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Delete Store",
            message = "This will permanently delete your store and all products. This cannot be undone.",
            confirmLabel = "Delete",
            confirmColor = Color.Red,
            onConfirm = { viewModel.deleteStore(); showDeleteDialog = false },
            onDismiss = { showDeleteDialog = false }
        )
    }
    if (showClearDialog) {
        ConfirmDialog(
            title = "Clear All Products",
            message = "All your products will be removed. Your store will remain active.",
            confirmLabel = "Clear",
            confirmColor = Color(0xFFFFA000),
            onConfirm = { viewModel.clearAllProducts(); showClearDialog = false },
            onDismiss = { showClearDialog = false }
        )
    }

    if (!isStoreCreated) {
        NoStoreScreen(onNavigateToCreateStore)
    } else {
        Scaffold(
            containerColor = Surface1,
            topBar = {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(PurpleDark, Purple, PurpleLight)))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Store logo
                        Box(
                            modifier = Modifier.size(44.dp).clip(CircleShape)
                                .border(2.dp, Gold, CircleShape)
                                .background(Color.White.copy(.15f))
                        ) {
                            if (!storeData?.storeLogoUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = storeData?.storeLogoUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.Store, null, tint = Gold,
                                    modifier = Modifier.size(22.dp).align(Alignment.Center))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(storeData?.storeName ?: "My Store",
                                fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.White)
                            Text("${products.size} product${if (products.size != 1) "s" else ""}",
                                fontSize = 12.sp, color = Color.White.copy(.7f))
                        }
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.background(Color.White.copy(.15f), CircleShape).size(40.dp)
                            ) {
                                Icon(Icons.Default.MoreVert, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text("Edit Store") },
                                    onClick = { showMenu = false; storeData?.let { onNavigateToEditStore(it) } },
                                    leadingIcon = { Icon(Icons.Default.Edit, null, tint = Purple) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Clear All Products") },
                                    onClick = { showMenu = false; showClearDialog = true },
                                    leadingIcon = { Icon(Icons.Default.DeleteSweep, null, tint = Color(0xFFFFA000)) }
                                )
                                Divider(color = Color.LightGray.copy(.5f))
                                DropdownMenuItem(
                                    text = { Text("Delete Store", color = Color.Red) },
                                    onClick = { showMenu = false; showDeleteDialog = true },
                                    leadingIcon = { Icon(Icons.Default.DeleteForever, null, tint = Color.Red) }
                                )
                            }
                        }
                    }
                }
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToAddProduct,
                    containerColor = Purple,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Add Product", fontWeight = FontWeight.Bold) },
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                // Store info cards
                storeData?.let { store ->
                    StoreInfoCards(store, products.size)
                }

                // Products
                if (products.isEmpty()) {
                    EmptyProductsState(onNavigateToAddProduct)
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text("Your Products", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                                color = Color(0xFF1A1A2E), modifier = Modifier.padding(vertical = 4.dp))
                        }
                        itemsIndexed(products) { index, product ->
                            StoreProductCard(
                                product = product,
                                onClick = { onNavigateToProductDetail(product) },
                                onDelete = { viewModel.removeProduct(index) {} }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StoreInfoCards(store: StoreData, productCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InfoCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Inventory2,
            value = "$productCount",
            label = "Products",
            color = Purple
        )
        InfoCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Phone,
            value = store.phoneNumber.take(10),
            label = "Phone",
            color = GreenSuccess,
            small = true
        )
        InfoCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.CheckCircle,
            value = "Active",
            label = "Status",
            color = GreenSuccess
        )
    }
}

@Composable
private fun InfoCard(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color,
    small: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(36.dp).background(color.copy(.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(value, fontWeight = FontWeight.ExtraBold,
                fontSize = if (small) 12.sp else 16.sp, color = color,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(label, fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun EmptyProductsState(onAdd: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Box(
                modifier = Modifier.size(100.dp).background(Purple.copy(.08f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Inventory2, null, tint = Purple.copy(.5f), modifier = Modifier.size(52.dp))
            }
            Spacer(Modifier.height(20.dp))
            Text("No Products Yet", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            Spacer(Modifier.height(8.dp))
            Text("Add your first product and start selling!", color = Color.Gray, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onAdd,
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(50.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Add First Product", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun NoStoreScreen(onNavigateToCreate: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Surface1, Color.White))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Illustration placeholder
            Box(
                modifier = Modifier.size(140.dp)
                    .background(Brush.radialGradient(listOf(PurpleLight.copy(.2f), Purple.copy(.05f))), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Storefront, null, tint = Purple, modifier = Modifier.size(72.dp))
            }
            Spacer(Modifier.height(28.dp))
            Text("No Store Yet", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFF1A1A2E))
            Spacer(Modifier.height(12.dp))
            Text(
                "Create your store to start listing products and reaching local customers.",
                color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 22.sp, fontSize = 15.sp
            )
            Spacer(Modifier.height(32.dp))

            // Feature bullets
            listOf(
                Pair(Icons.Default.Image, "Upload product photos"),
                Pair(Icons.Default.LocalShipping, "Set shipping options"),
                Pair(Icons.Default.Phone, "Connect with buyers directly")
            ).forEach { (icon, text) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(32.dp).background(Purple.copy(.1f), CircleShape),
                        contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = Purple, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(text, fontSize = 14.sp, color = Color(0xFF444444))
                }
            }

            Spacer(Modifier.height(36.dp))
            Button(
                onClick = onNavigateToCreate,
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(58.dp),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) {
                Icon(Icons.Default.Rocket, null, tint = Color.White)
                Spacer(Modifier.width(10.dp))
                Text("Create My Store", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
            }
        }
    }
}

@Composable
fun StoreProductCard(product: Product, onClick: () -> Unit, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Delete Product",
            message = "Remove \"${product.productName}\" from your store?",
            confirmLabel = "Delete",
            confirmColor = Color.Red,
            onConfirm = { onDelete(); showDeleteDialog = false },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 3.dp,
        color = Color.White,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            // Image
            Box(
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp))
                    .background(Purple.copy(.07f))
            ) {
                if (product.productImageUrls.isNotEmpty()) {
                    AsyncImage(model = product.productImageUrls[0], contentDescription = null,
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Default.Image, null, tint = Color.Gray,
                        modifier = Modifier.size(30.dp).align(Alignment.Center))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.productName, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("₹${String.format("%.0f", product.productPrice)}",
                        fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Purple)
                    Spacer(Modifier.width(10.dp))
                    Surface(shape = RoundedCornerShape(6.dp),
                        color = if (product.stockQuantity > 0) GreenSuccess.copy(.12f) else Color.Red.copy(.1f)) {
                        Text(
                            "Stock: ${product.stockQuantity}",
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                            color = if (product.stockQuantity > 0) GreenSuccess else Color.Red
                        )
                    }
                }
                if (product.productWholesalePrice > 0 || product.productCapacity > 0) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (product.productWholesalePrice > 0) {
                            Text("Wholesale: ₹${String.format("%.0f", product.productWholesalePrice)}",
                                fontSize = 12.sp, color = Color.DarkGray)
                            Spacer(Modifier.width(10.dp))
                        }
                        if (product.productCapacity > 0) {
                            Text("Cap: ${product.productCapacity.toString().removeSuffix(".0")} ${product.productCapacityUnit}",
                                fontSize = 12.sp, color = Color.DarkGray)
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(product.productType, fontSize = 11.sp, color = Color.Gray)
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, null, tint = Color.Red.copy(.6f))
            }
        }
    }
}

@Composable
private fun ConfirmDialog(
    title: String, message: String,
    confirmLabel: String, confirmColor: Color,
    onConfirm: () -> Unit, onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text(title, fontWeight = FontWeight.ExtraBold) },
        text = { Text(message, color = Color.DarkGray, lineHeight = 20.sp) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = confirmColor),
                shape = RoundedCornerShape(10.dp)
            ) { Text(confirmLabel, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("Cancel")
            }
        }
    )
}