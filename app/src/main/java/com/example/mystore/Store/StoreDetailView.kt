package com.example.mystore

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun StoreDetailView(
    product: Product,
    storeData: StoreData,
    onBackClick: () -> Unit,
    onNavigateToStore: (StoreData) -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    
    // Show title only when scrolled down a bit
    val showAppBarTitle by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 200 }
    }

    val makePhoneCall = { phoneNumber: String ->
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        context.startActivity(intent)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AnimatedVisibility(
                        visible = showAppBarTitle,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text("Product Detail", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.4f))
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (showAppBarTitle) Color(0xFF673AB7) else Color.Transparent
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(20.dp)) {
                    Button(
                        onClick = { makePhoneCall(storeData.phoneNumber) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Call Now", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding())
        ) {
            // Image Pager
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    if (product.productImageUrls.isNotEmpty()) {
                        val pagerState = rememberPagerState(pageCount = { product.productImageUrls.size })
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            AsyncImage(
                                model = product.productImageUrls[page],
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Gradient at bottom
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                    )
                                )
                        )

                        // Image Counter
                        if (product.productImageUrls.size > 1) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(20.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "${pagerState.currentPage + 1}/${product.productImageUrls.size}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFE0E0E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(100.dp), tint = Color.Gray)
                        }
                    }
                }
            }

            // Product Details
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Spacer(modifier = Modifier.height(20.dp))

                    // Categories
                    if (product.productCategories.isNotEmpty()) {
                        androidx.compose.foundation.layout.FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            product.productCategories.forEach { category ->
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF673AB7).copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF673AB7))
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(category, color = Color(0xFF673AB7), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    } else if (product.productType.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF673AB7).copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF673AB7))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(product.productType, color = Color(0xFF673AB7), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        product.productName,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black.copy(alpha = 0.87f)
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Retail Price",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "₹${String.format("%.2f", product.productPrice)}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF673AB7)
                            )
                        }

                        val inStock = product.stockQuantity > 0
                        Surface(
                            color = if (inStock) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (inStock) Icons.Default.Inventory else Icons.Default.Block,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (inStock) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (inStock) "Stock: ${product.stockQuantity}" else "Out of Stock",
                                    color = if (inStock) Color(0xFF2E7D32) else Color(0xFFC62828),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = if (product.productWholesalePrice > 0) Color(0xFF673AB7).copy(alpha = 0.05f) else Color.Gray.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (product.productWholesalePrice > 0) Color(0xFF673AB7).copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = if (product.productWholesalePrice > 0) Color(0xFF673AB7) else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "Wholesale Price",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.DarkGray
                                )
                            }
                            Text(
                                if (product.productWholesalePrice > 0) "₹${String.format("%.2f", product.productWholesalePrice)}" else "Retail Only",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (product.productWholesalePrice > 0) Color(0xFF673AB7) else Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Production Capacity Card
                    Text(
                        "Production Capacity",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = if (product.productCapacity > 0) Color(0xFFFFF8E1) else Color.Gray.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (product.productCapacity > 0) Color(0xFFFFD54F).copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(if (product.productCapacity > 0) Color(0xFFFFD54F).copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(if (product.productCapacity > 0) Icons.Default.PrecisionManufacturing else Icons.Default.Info, null, tint = if (product.productCapacity > 0) Color(0xFFF57C00) else Color.Gray, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    if (product.productCapacity > 0) "Can produce up to:" else "Production Limit",
                                    fontSize = 12.sp,
                                    color = if (product.productCapacity > 0) Color(0xFF795548) else Color.Gray
                                )
                                Text(
                                    if (product.productCapacity > 0) "${product.productCapacity.toString().removeSuffix(".0")} units / ${product.productCapacityUnit}" else "Contact seller for info",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (product.productCapacity > 0) Color(0xFF5D4037) else Color.Gray
                                )
                            }
                        }
                    }

                    Text("Description", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        product.productDescription,
                        fontSize = 15.sp,
                        color = Color.Gray,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Shipping & Availability Section
                    Text("Delivery & Shipping", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ShippingItem(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.LocalShipping,
                            label = "Method",
                            value = product.shippingMethod,
                            color = Color(0xFF1E88E5)
                        )
                        ShippingItem(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Explore,
                            label = "Coverage",
                            value = product.shippingAvailability,
                            color = Color(0xFF43A047)
                        )
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    // Store Info
                    Text("Seller Information", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF673AB7).copy(alpha = 0.05f))
                                        .border(1.dp, Color(0xFF673AB7).copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                        .clickable { onNavigateToStore(storeData) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!storeData.storeLogoUrl.isNullOrEmpty()) {
                                        AsyncImage(
                                            model = storeData.storeLogoUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(Icons.Default.Store, contentDescription = null, modifier = Modifier.size(28.dp), tint = Color(0xFF673AB7))
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        storeData.storeName,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "Verified Producer",
                                        fontSize = 12.sp,
                                        color = Color(0xFF43A047),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                IconButton(
                                    onClick = { onNavigateToStore(storeData) },
                                    modifier = Modifier.background(Color(0xFF673AB7).copy(alpha = 0.1f), CircleShape).size(36.dp)
                                ) {
                                    Icon(Icons.Default.ArrowForwardIos, null, tint = Color(0xFF673AB7), modifier = Modifier.size(14.dp))
                                }
                            }

                            if (storeData.storeDescription.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    storeData.storeDescription,
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))

                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                CompactContactRow(Icons.Default.Person, "Owner", storeData.ownerName)
                                CompactContactRow(Icons.Default.Phone, "Contact", storeData.phoneNumber)
                                CompactContactRow(Icons.Default.LocationOn, "Location", storeData.address)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
fun ShippingItem(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 13.sp, color = Color.Black.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun CompactContactRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text("$label: ", fontSize = 13.sp, color = Color.Gray)
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
