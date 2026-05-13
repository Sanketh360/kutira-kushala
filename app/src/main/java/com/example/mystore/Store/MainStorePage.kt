package com.example.mystore

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

// ─── ViewModel ───────────────────────────────────────────────────────────────

class MainStoreViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _stores = MutableStateFlow<List<StoreData>>(emptyList())
    val stores: StateFlow<List<StoreData>> = _stores

    private val _products = MutableStateFlow<List<Pair<Product, StoreData>>>(emptyList())
    val products: StateFlow<List<Pair<Product, StoreData>>> = _products

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _selectedState = MutableStateFlow("Select State")
    val selectedState: StateFlow<String> = _selectedState

    private val _selectedDistrict = MutableStateFlow("Select District")
    val selectedDistrict: StateFlow<String> = _selectedDistrict

    private val _recentHistory = MutableStateFlow<List<Pair<Product, StoreData>>>(emptyList())
    val recentHistory: StateFlow<List<Pair<Product, StoreData>>> = _recentHistory

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems

    fun updateLocation(state: String, district: String) {
        _selectedState.value = state
        _selectedDistrict.value = district
    }

    fun addToCart(product: Product, store: StoreData) {
        val current = _cartItems.value.toMutableList()
        val existing = current.find { it.product.productId == product.productId }
        if (existing != null) {
            val index = current.indexOf(existing)
            current[index] = existing.copy(quantity = existing.quantity + 1)
        } else {
            current.add(CartItem(product, store, 1))
        }
        _cartItems.value = current
    }

    fun removeFromCart(item: CartItem) {
        val current = _cartItems.value.toMutableList()
        current.remove(item)
        _cartItems.value = current
    }

    fun updateCartQuantity(item: CartItem, newQty: Int) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOf(item)
        if (index != -1) {
            if (newQty <= 0) current.removeAt(index)
            else current[index] = item.copy(quantity = newQty)
        }
        _cartItems.value = current
    }

    fun addToHistory(product: Product, store: StoreData) {
        val currentList = _recentHistory.value.toMutableList()
        currentList.removeAll { it.first.productId == product.productId }
        currentList.add(0, Pair(product, store))
        _recentHistory.value = currentList.take(10) // Keep last 10
    }

    init { loadData() }

    fun refresh() { loadData() }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val storesSnapshot = firestore.collection("stores")
                    .get().await()

                val loadedStores = mutableListOf<StoreData>()
                val loadedProducts = mutableListOf<Pair<Product, StoreData>>()

                for (doc in storesSnapshot.documents) {
                    val storeData = StoreData(
                        storeName = doc.getString("storeName") ?: "",
                        storeDescription = doc.getString("storeDescription") ?: "",
                        storeLogoUrl = doc.getString("storeLogoUrl"),
                        ownerName = doc.getString("ownerName") ?: "",
                        gender = doc.getString("gender") ?: "",
                        phoneNumber = doc.getString("phoneNumber") ?: "",
                        address = doc.getString("address") ?: "",
                        state = doc.getString("state") ?: "",
                        district = doc.getString("district") ?: "",
                        taluk = doc.getString("taluk") ?: "",
                        storeProducts = doc.get("storeProducts") as? List<String> ?: emptyList()
                    )
                    loadedStores.add(storeData)

                    val productsSnapshot = doc.reference.collection("products")
                        .whereEqualTo("status", "active")
                        .get().await()

                    for (pDoc in productsSnapshot.documents) {
                        val product = Product(
                            productId = pDoc.id,
                            productName = pDoc.getString("productName") ?: "",
                            productDescription = pDoc.getString("productDescription") ?: "",
                            productType = pDoc.getString("productType") ?: "",
                            productPrice = pDoc.getDouble("productPrice") ?: 0.0,
                            productWholesalePrice = pDoc.getDouble("productWholesalePrice") ?: 0.0,
                            stockQuantity = pDoc.getLong("stockQuantity")?.toInt() ?: 0,
                            productCapacity = pDoc.getDouble("productCapacity") ?: 0.0,
                            productCapacityUnit = pDoc.getString("productCapacityUnit") ?: "",
                            shippingMethod = pDoc.getString("shippingMethod") ?: "",
                            shippingAvailability = pDoc.getString("shippingAvailability") ?: "",
                            productCategories = pDoc.get("productCategories") as? List<String> ?: emptyList(),
                            productImageUrls = pDoc.get("productImageUrls") as? List<String> ?: emptyList()
                        )
                        loadedProducts.add(Pair(product, storeData))
                    }
                }
                _stores.value = loadedStores
                _products.value = loadedProducts
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}

// ─── Color Palette ────────────────────────────────────────────────────────────
private val Purple = Color(0xFF6C3CE1)
private val PurpleLight = Color(0xFF9B6DFF)
private val PurpleDark = Color(0xFF4A1FA8)
private val Gold = Color(0xFFFFB930)
private val Surface1 = Color(0xFFF8F5FF)
private val CardBg = Color(0xFFFFFFFF)

// ─── Main Composable ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MainStorePage(
    viewModel: MainStoreViewModel = viewModel(),
    onNavigateToMyStore: () -> Unit,
    onNavigateToProductDetail: (Product, StoreData) -> Unit,
    onNavigateToStoreProducts: (StoreData) -> Unit,
    onNavigateToCart: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0=Stores, 1=Products
    var selectedCategory by remember { mutableStateOf("All") }
    
    // Filter State
    var filterState by remember { mutableStateOf("All") }
    var filterDistrict by remember { mutableStateOf("All") }
    var filterTaluk by remember { mutableStateOf("All") }
    var filterServiceType by remember { mutableStateOf("All") } // Home Delivery, Self-Pickup
    var filterAvailability by remember { mutableStateOf("All") } // Available Now, Pre-order
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val categories =
        listOf("All", "Fresh Vegetable", "Pickles & Papads", "Dairy Products", "Grains", "Others")
    val stores by viewModel.stores.collectAsState()
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Surface1)) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.75f),
                drawerContainerColor = Surface1
            ) {
                FilterDrawerContent(
                    stores = stores,
                    currentState = filterState,
                    currentDistrict = filterDistrict,
                    currentTaluk = filterTaluk,
                    onStateChange = { filterState = it; filterDistrict = "All"; filterTaluk = "All" },
                    onDistrictChange = { filterDistrict = it; filterTaluk = "All" },
                    onTalukChange = { filterTaluk = it },
                    currentServiceType = filterServiceType,
                    onServiceTypeChange = { filterServiceType = it },
                    currentAvailability = filterAvailability,
                    onAvailabilityChange = { filterAvailability = it },
                    onClose = { scope.launch { drawerState.close() } }
                )
            }
        }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                MainTopBar(
                    isSearchActive = isSearchActive,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onSearchToggle = {
                        isSearchActive = !isSearchActive; if (!isSearchActive) searchQuery = ""
                    },
                    onCartClick = onNavigateToCart,
                    onFilterClick = { scope.launch { drawerState.open() } }
                )
            }
        ) { padding ->
            val storesFiltered = stores.filter { store ->
                (searchQuery.isEmpty() || 
                    store.storeName.contains(searchQuery, ignoreCase = true) || 
                    store.storeProducts.any { it.contains(searchQuery, ignoreCase = true) }) &&
                (filterState == "All" || store.state.trim().equals(filterState.trim(), ignoreCase = true)) &&
                (filterDistrict == "All" || store.district.trim().equals(filterDistrict.trim(), ignoreCase = true)) &&
                (filterTaluk == "All" || store.taluk.trim().contains(filterTaluk.trim(), ignoreCase = true))
            }
            val productsFiltered = products.filter { (p, store) ->
                (searchQuery.isEmpty() || 
                    p.productName.contains(searchQuery, ignoreCase = true) || 
                    store.storeName.contains(searchQuery, ignoreCase = true) ||
                    store.storeProducts.any { it.contains(searchQuery, ignoreCase = true) }) &&
                (selectedCategory == "All" || p.productType.contains(selectedCategory, ignoreCase = true)) &&
                (filterState == "All" || store.state.trim().equals(filterState.trim(), ignoreCase = true)) &&
                (filterDistrict == "All" || store.district.trim().equals(filterDistrict.trim(), ignoreCase = true)) &&
                (filterTaluk == "All" || store.taluk.trim().contains(filterTaluk.trim(), ignoreCase = true)) &&
                (filterServiceType == "All" || p.shippingMethod.contains(filterServiceType, ignoreCase = true)) &&
                (filterAvailability == "All" || p.shippingAvailability.contains(filterAvailability, ignoreCase = true))
            }

            val pullToRefreshState = rememberPullToRefreshState()

            PullToRefreshBox(
                state = pullToRefreshState,
                isRefreshing = isLoading,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    // ── Header Section (Hero Banner) ──────────────────────────
                    if (!isSearchActive) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            HeroBanner(storeCount = stores.size, productCount = products.size)
                        }
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            RecentHistorySection(viewModel, onNavigateToProductDetail)
                        }
                    }

                    // ── Tab Switcher ──────────────────────────────────────────
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(modifier = Modifier.background(Surface1)) {
                            TabSwitcher(
                                selectedTab = selectedTab,
                                onTabChange = { selectedTab = it })

                            // ── Category chips (Products tab only) ───────────────
                            AnimatedVisibility(visible = selectedTab == 1) {
                                CategoryChips(categories, selectedCategory) {
                                    selectedCategory = it
                                }
                            }
                        }
                    }

                    // ── Content Section ───────────────────────────────────────
                    if (isLoading) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            LoadingGrid()
                        }
                    } else if (selectedTab == 0) {
                        if (storesFiltered.isEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                EmptyState("No stores found", Icons.Outlined.Store)
                            }
                        } else {
                            items(storesFiltered) { store ->
                                Box(modifier = Modifier.padding(16.dp)) {
                                    StoreCard(store, onNavigateToStoreProducts)
                                }
                            }
                        }
                    } else {
                        if (productsFiltered.isEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                EmptyState("No products found", Icons.Outlined.Inventory2)
                            }
                        } else {
                            items(
                                productsFiltered,
                                span = { GridItemSpan(maxLineSpan) }) { (product, store) ->
                                Box(
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    )
                                ) {
                                    ProductFeedCard(
                                        product, store,
                                        onAddToCart = {
                                            viewModel.addToCart(product, store)
                                            Toast.makeText(
                                                context,
                                                "${product.productName} added to cart",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onClick = {
                                            viewModel.addToHistory(product, store)
                                            onNavigateToProductDetail(product, store)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}

// ─── Top Bar ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopBar(
        isSearchActive: Boolean,
        searchQuery: String,
        onSearchQueryChange: (String) -> Unit,
        onSearchToggle: () -> Unit,
        onCartClick: () -> Unit,
        onFilterClick: () -> Unit
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(PurpleDark, Purple, PurpleLight))
                )
        ) {
            if (isSearchActive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                "Search stores, products…",
                                color = Color.White.copy(.5f)
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White) },
                        trailingIcon = {
                            IconButton(onClick = onSearchToggle) {
                                Icon(Icons.Default.Close, null, tint = Color.White)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White.copy(.6f),
                            unfocusedBorderColor = Color.White.copy(.3f),
                            cursorColor = Color.White
                        ),
                        shape = RoundedCornerShape(30.dp),
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        singleLine = true
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Filter Icon
                    IconButton(
                        onClick = onFilterClick,
                        modifier = Modifier.background(Color.White.copy(.15f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    
                    // Logo + Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Storefront,
                                null,
                                tint = Gold,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Village Market",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                "Fresh from local sellers",
                                fontSize = 11.sp,
                                color = Color.White.copy(.7f)
                            )
                        }
                    }
                    IconButton(
                        onClick = onSearchToggle,
                        modifier = Modifier.background(Color.White.copy(.15f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = onCartClick,
                        modifier = Modifier.background(Gold.copy(.25f), CircleShape).size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            null,
                            tint = Gold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

@Composable
private fun HeroBanner(storeCount: Int, productCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(Brush.horizontalGradient(listOf(PurpleDark, Purple)))
    ) {
        // decorative circles
        Box(
            modifier = Modifier.size(130.dp).offset((-30).dp, (-30).dp)
                .background(Color.White.copy(.05f), CircleShape)
        )
        Box(
            modifier = Modifier.size(80.dp).align(Alignment.TopEnd).offset(20.dp, (-20).dp)
                .background(Gold.copy(.15f), CircleShape)
        )

        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "Discover local\ngoodness 🌿", fontWeight = FontWeight.Bold,
                    fontSize = 17.sp, color = Color.White, lineHeight = 22.sp
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatPill(count = storeCount, label = "Stores", icon = Icons.Default.Store)
                StatPill(
                    count = productCount,
                    label = "Products",
                    icon = Icons.Default.Inventory2
                )
            }
        }
    }
}

@Composable
private fun StatPill(
    count: Int,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White.copy(.15f)) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Gold, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(5.dp))
            Column {
                Text(
                    "$count",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = Color.White
                )
                Text(label, fontSize = 9.sp, color = Color.White.copy(.7f))
            }
        }
    }
}

@Composable
private fun WomenLedBadge(small: Boolean = false) {
    Surface(
        color = Color(0xFFFFE0E0),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (small) 4.dp else 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("👩", fontSize = if (small) 10.sp else 12.sp)
            Spacer(Modifier.width(4.dp))
            Text(
                "Women-led",
                color = Color(0xFFD32F2F),
                fontSize = if (small) 9.sp else 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TabSwitcher(selectedTab: Int, onTabChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .background(Color.White, RoundedCornerShape(14.dp))
            .padding(4.dp)
    ) {
        listOf("🏪  Stores", "🛒  Products").forEachIndexed { idx, label ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selectedTab == idx) Purple else Color.Transparent)
                    .clickable { onTabChange(idx) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (selectedTab == idx) Color.White else Color(0xFF888888)
                )
            }
        }
    }
}

@Composable
private fun CategoryChips(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { cat ->
            val isSelected = selected == cat
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) Purple else Color.White,
                border = if (!isSelected) BorderStroke(1.dp, Purple.copy(.3f)) else null,
                modifier = Modifier.clickable { onSelect(cat) }
            ) {
                Text(
                    cat, modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) Color.White else Purple
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StoreCard(store: StoreData, onClick: (StoreData) -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 5.dp,
        color = CardBg,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!isExpanded) Modifier.aspectRatio(0.78f) else Modifier)
            .animateContentSize()
            .clickable { onClick(store) }
    ) {
        Column {
            // Image area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isExpanded) 120.dp else 140.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                PurpleLight.copy(.15f),
                                Purple.copy(.08f)
                            )
                        )
                    )
            ) {
                if (!store.storeLogoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = store.storeLogoUrl, contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Store, null, tint = Purple.copy(.4f),
                        modifier = Modifier.size(48.dp).align(Alignment.Center)
                    )
                }
                // Gradient overlay
                Box(
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(.35f))
                            )
                        )
                )

                // Expand Icon
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(32.dp)
                        .background(Color.White.copy(.7f), CircleShape)
                ) {
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Show Products",
                        tint = Purple,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            // Info
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    store.storeName, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp,
                    color = Color.Black,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                if (store.gender.trim().equals("Female", ignoreCase = true)) {
                    Spacer(Modifier.height(4.dp))
                    WomenLedBadge()
                }
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        null,
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        store.ownerName, fontSize = 11.sp, color = Color.Gray,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }

                if (isExpanded) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Store Products:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Purple
                    )
                    Spacer(Modifier.height(4.dp))
                    if (store.storeProducts.isEmpty()) {
                        Text("No products listed", fontSize = 10.sp, color = Color.Gray)
                    } else {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            store.storeProducts.forEach { product ->
                                Surface(
                                    color = Purple.copy(.1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        product,
                                        modifier = Modifier.padding(
                                            horizontal = 6.dp,
                                            vertical = 2.dp
                                        ),
                                        fontSize = 9.sp,
                                        color = Purple,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                } else {
                    if (store.address.isNotEmpty()) {
                        Spacer(Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                null,
                                tint = Purple,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                store.address, fontSize = 10.sp, color = Color.Gray,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductFeedCard(
    product: Product,
    store: StoreData,
    onAddToCart: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 4.dp,
        color = CardBg,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            // Image
            Box(
                modifier = Modifier.size(95.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Purple.copy(.08f))
            ) {
                if (product.productImageUrls.isNotEmpty()) {
                    AsyncImage(
                        model = product.productImageUrls[0], contentDescription = null,
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Image, null, tint = Color.Gray,
                        modifier = Modifier.size(36.dp).align(Alignment.Center)
                    )
                }
                // Stock badge
                if (product.stockQuantity <= 0) {
                    Box(
                        modifier = Modifier.align(Alignment.TopStart).padding(4.dp)
                            .background(Color.Red, RoundedCornerShape(6.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "Out",
                            fontSize = 9.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Category pill
                Surface(shape = RoundedCornerShape(8.dp), color = Purple.copy(.1f)) {
                    Text(
                        product.productType,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Purple
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    product.productName, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                if (store.gender.trim().equals("Female", ignoreCase = true)) {
                    Spacer(Modifier.height(4.dp))
                    WomenLedBadge(small = true)
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "₹${String.format("%.0f", product.productPrice)}",
                        fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = Purple
                    )
                    if (product.productWholesalePrice > 0) {
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Bulk: ₹${String.format("%.0f", product.productWholesalePrice)}",
                            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Gold
                        )
                    }
                }
                
                if (product.productCapacity > 0) {
                    Spacer(Modifier.height(4.dp))
                    Surface(color = Color(0xFFFFF8E1), shape = RoundedCornerShape(6.dp)) {
                        Text(
                            "🏭 ${product.productCapacity.toString().removeSuffix(".0")} / ${product.productCapacityUnit}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF57C00)
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!store.storeLogoUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = store.storeLogoUrl, contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(16.dp).clip(CircleShape)
                        )
                        Spacer(Modifier.width(5.dp))
                    } else {
                        Icon(
                            Icons.Default.Store,
                            null,
                            tint = Color.Gray,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(
                        store.storeName, fontSize = 12.sp, color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Add to Cart Button
            IconButton(
                onClick = onAddToCart,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .background(Purple.copy(.1f), CircleShape)
            ) {
                Icon(
                    Icons.Default.AddShoppingCart,
                    null,
                    tint = Purple,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun RecentHistorySection(
    viewModel: MainStoreViewModel,
    onNavigateToProduct: (Product, StoreData) -> Unit
) {
    val history by viewModel.recentHistory.collectAsState()

    AnimatedVisibility(visible = history.isNotEmpty()) {
        Column(modifier = Modifier.padding(top = 16.dp)) {
            Text(
                "Recently Viewed",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = PurpleDark,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(history) { (product, store) ->
                    HistoryCard(product, store) { onNavigateToProduct(product, store) }
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(product: Product, store: StoreData, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CardBg,
        shadowElevation = 2.dp,
        modifier = Modifier
            .width(130.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Purple.copy(.05f))
            ) {
                if (product.productImageUrls.isNotEmpty()) {
                    AsyncImage(
                        model = product.productImageUrls[0],
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Image,
                        null,
                        tint = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                product.productName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "₹${String.format("%.0f", product.productPrice)}",
                fontSize = 11.sp,
                color = Purple,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun LoadingGrid() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = Purple,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text("Loading market…", color = Purple, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun EmptyState(message: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier.size(80.dp).background(Purple.copy(.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Purple.copy(.5f), modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(
                message,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color.DarkGray
            )
        }
    }
}

// ─── Filter Drawer Content ───────────────────────────────────────────────────

@Composable
private fun FilterDrawerContent(
    stores: List<StoreData>,
    currentState: String,
    currentDistrict: String,
    currentTaluk: String,
    onStateChange: (String) -> Unit,
    onDistrictChange: (String) -> Unit,
    onTalukChange: (String) -> Unit,
    currentServiceType: String,
    onServiceTypeChange: (String) -> Unit,
    currentAvailability: String,
    onAvailabilityChange: (String) -> Unit,
    onClose: () -> Unit
) {
    val states = remember { listOf("All") + indianStatesAndDistricts.keys.sorted() }
    val districts = remember(currentState) { 
        listOf("All") + (indianStatesAndDistricts[currentState] ?: emptyList()).sorted() 
    }

    val serviceTypeOptions = listOf(
        "All",
        "🚶 Self Delivery / Hand Delivery",
        "🏘️ Village-Level Delivery",
        "🛵 Delivery by Two-Wheeler",
        "🏪 Pickup from Store",
        "📮 India Post",
        "📦 Rural Post Office",
        "🚐 Shared Jeep / Van",
        "🚌 Bus Parcel Service"
    )

    val shippingAvailabilityOptions = listOf(
        "All",
        "📍 Local Area Only",
        "🗺️ Within District",
        "🏛️ Within State",
        "🇮🇳 All India Delivery",
        "🏪 Pickup Only"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Filters", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Purple)
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, null, tint = Color.Gray)
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        FilterSectionTitle("Location")
        
        FilterDropdown("State", currentState, states, onStateChange, isSearchable = true)
        Spacer(Modifier.height(16.dp))
        FilterDropdown("District", currentDistrict, districts, onDistrictChange, isSearchable = true)
        Spacer(Modifier.height(16.dp))
        
        // Taluk is now a searchable text field
        Column {
            Text("Taluk", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = if (currentTaluk == "All") "" else currentTaluk,
                onValueChange = { onTalukChange(if (it.isEmpty()) "All" else it) },
                placeholder = { Text("Search Taluk...", color = Color.Gray, fontSize = 14.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple,
                    unfocusedBorderColor = Purple.copy(.2f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = Color.Black)
            )
        }
        
        Spacer(Modifier.height(24.dp))
        FilterSectionTitle("Logistics")
        FilterDropdown("Service Type", currentServiceType, serviceTypeOptions, onServiceTypeChange, isSearchable = true)
        
        Spacer(Modifier.height(16.dp))
        FilterSectionTitle("Availability")
        FilterDropdown("Shipping Availability", currentAvailability, shippingAvailabilityOptions, onAvailabilityChange, isSearchable = true)
        
        Spacer(Modifier.height(32.dp))
        
        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple)
        ) {
            Text("Apply Filters", fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(12.dp))
        
        TextButton(
            onClick = {
                onStateChange("All")
                onDistrictChange("All")
                onTalukChange("All")
                onServiceTypeChange("All")
                onAvailabilityChange("All")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset All", color = Purple.copy(.7f))
        }
    }
}

@Composable
private fun FilterSectionTitle(title: String) {
    Text(
        title,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = Color.Black,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDropdown(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    isSearchable: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    
    val filteredOptions = remember(searchText, options) {
        if (!isSearchable || searchText.isEmpty()) options
        else options.filter { it.contains(searchText, ignoreCase = true) }
    }

    Column {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(Modifier.height(6.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = if (isSearchable && expanded) searchText else selected,
                onValueChange = { if (isSearchable) searchText = it },
                readOnly = !isSearchable || !expanded,
                placeholder = { if (isSearchable && expanded) Text("Search...", color = Color.Gray) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple,
                    unfocusedBorderColor = Purple.copy(.2f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = Color.Black)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { 
                    expanded = false
                    searchText = ""
                },
                modifier = Modifier.background(Color.White)
            ) {
                filteredOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, fontSize = 14.sp, color = Color.Black) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                            searchText = ""
                        }
                    )
                }
                if (filteredOptions.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No results", fontSize = 14.sp, color = Color.Gray) },
                        onClick = { }
                    )
                }
            }
        }
    }
}
