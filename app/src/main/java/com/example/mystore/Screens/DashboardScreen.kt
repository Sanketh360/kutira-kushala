package com.example.mystore.Screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mystore.MainStoreViewModel
import com.example.mystore.Product
import com.example.mystore.StoreData
import androidx.lifecycle.viewmodel.compose.viewModel

// --- Colors ---
private val Primary = Color(0xFF6C3CE1)
private val PrimaryLight = Color(0xFF9B6DFF)
private val Accent = Color(0xFFFFB930)
private val BgColor = Color(0xFFF8F9FE)
private val CardBg = Color.White
private val Success = Color(0xFF00C853)
private val Warning = Color(0xFFFFAB00)
private val Gold = Color(0xFFFFD700)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: MainStoreViewModel = viewModel(),
    onNavigateToProduct: (Product, StoreData) -> Unit,
    onNavigateToStore: (StoreData) -> Unit,
    onNavigateToLocation: () -> Unit
) {
    val stores by viewModel.stores.collectAsState()
    val products by viewModel.products.collectAsState()
    val selectedState by viewModel.selectedState.collectAsState()
    val selectedDistrict by viewModel.selectedDistrict.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    val pullToRefreshState = rememberPullToRefreshState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        containerColor = BgColor,
    ) { padding ->
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
            // 1. Top Section (Location, Profile, Search)
            item { TopSection(searchQuery, { searchQuery = it }, onNavigateToLocation, selectedState, selectedDistrict) }
            
            // 8. Smart Insights
            item { SmartInsightsSection() }

            // 2. Category Section
            item { CategorySection() }
            
            // 3. "Available Now" (High Capacity)
            item { 
                val availableNow = products.filter { it.first.stockQuantity > 0 }.sortedByDescending { it.first.stockQuantity }.take(5)
                if (availableNow.isNotEmpty()) {
                    AvailableNowSection(availableNow, onNavigateToProduct)
                }
            }
            
            // 4. Featured / Trusted Businesses
            item {
                if (stores.isNotEmpty()) {
                    FeaturedBusinessesSection(stores.take(5), onNavigateToStore)
                }
            }
            
            // 6. Trending / Popular Section
            item {
                val trending = products.shuffled().take(4) // Mocking trending
                if (trending.isNotEmpty()) {
                    TrendingSection(trending, onNavigateToProduct)
                }
            }
            
            // 5. Product Showcase
            item {
                Text(
                    text = "Top Selling Products",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            val topSellingProducts = products.take(5) // Mocking top selling products
            items(topSellingProducts) { (product, store) ->
                ProductShowcaseCard(product, store) { onNavigateToProduct(product, store) }
            }
            
            item { Spacer(Modifier.height(80.dp)) } // Bottom nav padding
        }
    }
}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSection(
    searchQuery: String, 
    onSearchChange: (String) -> Unit, 
    onLocationClick: () -> Unit,
    state: String,
    district: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Primary, PrimaryLight.copy(alpha = 0.8f))
                ),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .padding(top = 48.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Showing results near:", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLocationClick() }
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Accent, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("$state, $district", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search baskets, papad, agarbatti...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Primary) },
            trailingIcon = { Icon(Icons.Outlined.Mic, contentDescription = "Voice Search", tint = Primary) },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = Primary,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            shape = RoundedCornerShape(16.dp)
        )
        
        Spacer(Modifier.height(16.dp))
        
        // Quick Suggestions
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SuggestionChip("Bulk Orders")
            SuggestionChip("Ready Now")
            SuggestionChip("Women-led")
        }
    }
}

@Composable
fun SuggestionChip(text: String) {
    Surface(
        color = Color.White.copy(alpha = 0.2f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun SmartInsightsSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Smart Insight", color = Primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Papad demand is high this week in your area!", color = Color.DarkGray, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun CategorySection() {
    val categories = listOf(
        Pair("Food", "🍱"), Pair("Crafts", "🧺"), Pair("Handmade", "🪔"), Pair("Eco", "🌿"),
        Pair("Textile", "🧵"), Pair("Bamboo", "🎋"), Pair("Incense", "🪔"), Pair("Leather", "👞")
    )
    
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Categories", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Trending 🔥", color = Primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { cat ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { /* Filter */ }
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(Color.White, RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(cat.second, fontSize = 28.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(cat.first, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun AvailableNowSection(products: List<Pair<Product, StoreData>>, onClick: (Product, StoreData) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = "⚡ Available Now",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(products) { (product, store) ->
                Card(
                    modifier = Modifier.width(220.dp).clickable { onClick(product, store) },
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(Success, CircleShape))
                            Spacer(Modifier.width(6.dp))
                            Text("${product.stockQuantity} units available", color = Success, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(product.productName, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.Black)
                        Text(store.storeName, fontSize = 12.sp, color = Color.Gray)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("₹${String.format("%.0f", product.productPrice)}", fontWeight = FontWeight.ExtraBold, color = Primary, fontSize = 18.sp)
                            if (product.productWholesalePrice > 0) {
                                Spacer(Modifier.width(8.dp))
                                Text("Bulk: ₹${String.format("%.0f", product.productWholesalePrice)}", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (product.productCapacity > 0) {
                            Spacer(Modifier.height(4.dp))
                            Text("🏭 ${product.productCapacity.toString().removeSuffix(".0")} / ${product.productCapacityUnit}", color = Color.DarkGray, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(Modifier.height(12.dp))
                        Surface(color = Primary.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                            Text("Fast Delivery", color = Primary, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeaturedBusinessesSection(stores: List<StoreData>, onClick: (StoreData) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = "⭐ Featured Businesses",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(stores) { store ->
                var isExpanded by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier
                        .width(if (isExpanded) 200.dp else 160.dp)
                        .animateContentSize()
                        .clickable { onClick(store) },
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box {
                            if (!store.storeLogoUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = store.storeLogoUrl, contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(60.dp).clip(CircleShape)
                                )
                            } else {
                                Box(modifier = Modifier.size(60.dp).background(Primary.copy(alpha=0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Store, contentDescription = null, tint = Primary)
                                }
                            }
                            // Verified Badge
                            Icon(
                                Icons.Default.CheckCircle, 
                                contentDescription = "Verified", 
                                tint = Primary,
                                modifier = Modifier.size(18.dp).align(Alignment.BottomEnd).background(Color.White, CircleShape)
                            )
                            
                            // Expand Icon
                            IconButton(
                                onClick = { isExpanded = !isExpanded },
                                modifier = Modifier.align(Alignment.TopEnd).offset(x = 10.dp, y = (-10).dp).size(24.dp)
                            ) {
                                Icon(
                                    if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Show Products",
                                    tint = Primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(store.storeName, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.Black)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Accent, modifier = Modifier.size(12.dp))
                            Text("4.9 (5+ years)", fontSize = 10.sp, color = Color.Gray)
                        }
                        
                        if (isExpanded) {
                            Spacer(Modifier.height(8.dp))
                            Text("Products:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Primary)
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                store.storeProducts.forEach { product ->
                                    Surface(
                                        color = Primary.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(product, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontSize = 8.sp, color = Primary)
                                    }
                                }
                            }
                        } else {
                            if (store.storeProducts.isNotEmpty()) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = store.storeProducts.take(2).joinToString(", "),
                                    fontSize = 10.sp,
                                    color = Primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        if (store.gender.trim().equals("Female", ignoreCase = true)) {
                            Surface(color = Color(0xFFFFE0E0), shape = RoundedCornerShape(8.dp)) {
                                Text("👩 Women-led", color = Color(0xFFD32F2F), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrendingSection(products: List<Pair<Product, StoreData>>, onClick: (Product, StoreData) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = "🔥 Trending",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(products) { (product, store) ->
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onClick(product, store) }
                ) {
                    if (product.productImageUrls.isNotEmpty()) {
                        AsyncImage(
                            model = product.productImageUrls[0], contentDescription = null,
                            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(Primary.copy(alpha = 0.2f)))
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))))
                    )
                    
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Surface(color = Color.Red, shape = RoundedCornerShape(4.dp)) {
                            Text("High Demand", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(product.productName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("₹${String.format("%.0f", product.productPrice)}", color = Gold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        if (product.productCapacity > 0) {
                            Text("Cap: ${product.productCapacity.toString().removeSuffix(".0")} ${product.productCapacityUnit}", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductShowcaseCard(product: Product, store: StoreData, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            // Image
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BgColor)
            ) {
                if (product.productImageUrls.isNotEmpty()) {
                    AsyncImage(
                        model = product.productImageUrls[0], contentDescription = null,
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.Image, null, tint = Color.Gray, modifier = Modifier.size(36.dp).align(Alignment.Center))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.productName, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, color = Color.Black)
                Text(store.storeName, fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("Bulk Price", fontSize = 10.sp, color = Color.Gray)
                        Text("₹${String.format("%.0f", if(product.productWholesalePrice > 0) product.productWholesalePrice else product.productPrice)}", fontWeight = FontWeight.Bold, color = Primary, fontSize = 14.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Availability", fontSize = 10.sp, color = Color.Gray)
                        Text(product.shippingAvailability.split(" ").last(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                if (product.productCapacity > 0) {
                    Surface(color = Success.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                        Text("📦 ${product.productCapacity.toString().removeSuffix(".0")} ${product.productCapacityUnit} capacity", color = Success, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
        }
    }
}
