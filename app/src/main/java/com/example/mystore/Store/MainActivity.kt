package com.example.mystore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.mystore.ui.theme.MyStoreKotlinTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mystore.Screens.MyCartPage
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyStoreKotlinTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val productManagerViewModel: ProductManagerViewModel = viewModel()
    val mainStoreViewModel: MainStoreViewModel = viewModel()

    // Determine initial destination
    val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val startDest = if (currentUser != null) "home" else "onboarding"

    // Watch Auth state and refresh store initialization when user changes
    DisposableEffect(Unit) {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val listener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { currentAuth ->
            if (currentAuth.currentUser != null) {
                productManagerViewModel.initializeStore()
            } else {
                productManagerViewModel.resetState()
            }
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomNav = currentRoute in listOf("home", "search_tab", "my_store", "profile")

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentRoute == "home",
                        onClick = {
                            navController.navigate("home") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            unselectedIconColor = Color.Black,
                            selectedTextColor = Color.Black,
                            unselectedTextColor = Color.Black,
                            indicatorColor = Color.Black
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        label = { Text("Search") },
                        selected = currentRoute == "search_tab",
                        onClick = {
                            navController.navigate("search_tab") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            unselectedIconColor = Color.Black,
                            selectedTextColor = Color.Black,
                            unselectedTextColor = Color.Black,
                            indicatorColor = Color.Black
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Store, contentDescription = "My Store") },
                        label = { Text("My Store") },
                        selected = currentRoute == "my_store",
                        onClick = {
                            navController.navigate("my_store") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            unselectedIconColor = Color.Black,
                            selectedTextColor = Color.Black,
                            unselectedTextColor = Color.Black,
                            indicatorColor = Color.Black
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        selected = currentRoute == "profile",
                        onClick = {
                            navController.navigate("profile") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            unselectedIconColor = Color.Black,
                            selectedTextColor = Color.Black,
                            unselectedTextColor = Color.Black,
                            indicatorColor = Color.Black
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController, 
            startDestination = startDest,
            modifier = Modifier.padding(bottom = if (showBottomNav) paddingValues.calculateBottomPadding() else 0.dp)
        ) {

            // ── Onboarding & Auth ──────────────────────────────────────────────────
            composable("onboarding") {
                com.example.mystore.Screens.OnboardingScreen(navController = navController)
            }
            composable("login") {
                com.example.mystore.Authentication.LoginScreen(
                    onNavigateToStore = {
                        navController.navigate("home") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate("register")
                    }
                )
            }
            composable("register") {
                com.example.mystore.Authentication.RegisterScreen(
                    onNavigateToStore = {
                        navController.navigate("home") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    }
                )
            }

            // ── Main Dashboard (Home) ─────────────────────────────────────────────
            composable("home") {
                com.example.mystore.Screens.DashboardScreen(
                    viewModel = mainStoreViewModel,
                    onNavigateToProduct = { product, store ->
                        productManagerViewModel.setCurrentProductAndStore(product, store)
                        navController.navigate("store_detail_view")
                    },
                    onNavigateToStore = { store ->
                        productManagerViewModel.setCurrentStore(store)
                        navController.navigate("store_view")
                    },
                    onNavigateToLocation = {
                        navController.navigate("location_selection")
                    }
                )
            }
            
            // ── Search Tab ────────────────────────────────────────────────────────
            composable("search_tab") {
                val isStoreCreated by productManagerViewModel.isStoreCreated.collectAsState()
                // For now, reusing MainStorePage as the Search/Marketplace full view
                MainStorePage(
                    onNavigateToMyStore = {
                        if (isStoreCreated) {
                            navController.navigate("my_store")
                        } else {
                            navController.navigate("create_store")
                        }
                    },
                    onNavigateToProductDetail = { product, store ->
                        productManagerViewModel.setCurrentProductAndStore(product, store)
                        navController.navigate("store_detail_view")
                    },
                    onNavigateToStoreProducts = { store ->
                        productManagerViewModel.setCurrentStore(store)
                        navController.navigate("store_view")
                    },
                    onNavigateToCart = { navController.navigate("my_cart") }
                )
            }
            
            // ── Profile ───────────────────────────────────────────────────────────
            composable("profile") {
                com.example.mystore.Screens.ProfileScreen(
                    viewModel = mainStoreViewModel,
                    onLogoutSuccess = {
                        navController.navigate("onboarding") {
                            popUpTo(0) // Clear back stack
                        }
                    },
                    onNavigateToLocation = {
                        navController.navigate("location_selection")
                    }
                )
            }

            composable("location_selection") {
                com.example.mystore.Screens.LocationSelectionScreen(
                    onBackClick = { navController.popBackStack() },
                    onLocationSelected = { country, state, district, address ->
                        mainStoreViewModel.updateLocation(state, district)
                        navController.popBackStack()
                    }
                )
            }

            // ── My store dashboard ────────────────────────────────────────────────
            composable("my_store") {
                MyStorePage(
                    viewModel = productManagerViewModel,
                    onNavigateToCreateStore = { navController.navigate("create_store") },
                    onNavigateToAddProduct = { navController.navigate("add_product") },
                    onNavigateToProductDetail = { product ->
                        productManagerViewModel.setCurrentProduct(product)
                        navController.navigate("store_detail_view")
                    },
                    onNavigateToEditStore = { store ->
                        productManagerViewModel.setCurrentStore(store)
                        navController.navigate("edit_store")
                    }
                )
            }

            // ── Create Store ──────────────────────────────────────────────────────
            composable("create_store") {
                CreateStorePage(
                    viewModel = productManagerViewModel,
                    onBackClick = { navController.popBackStack() },
                    onStoreCreated = {
                        productManagerViewModel.initializeStore()
                        productManagerViewModel.loadProducts()
                        navController.navigate("my_store") {
                            popUpTo("create_store") { inclusive = true }
                        }
                    },
                    initialStoreData = null
                )
            }

            // ── Edit Store ────────────────────────────────────────────────────────
            composable("edit_store") {
                val currentStore by productManagerViewModel.currentStore.collectAsState()
                val storeData by productManagerViewModel.storeData.collectAsState()
                val store = currentStore ?: storeData
                
                CreateStorePage(
                    viewModel = productManagerViewModel,
                    onBackClick = { navController.popBackStack() },
                    onStoreCreated = { navController.popBackStack() },
                    initialStoreData = store
                )
            }

            // ── Add Product ───────────────────────────────────────────────────────
            composable("add_product") {
                AddProductPage(
                    viewModel = productManagerViewModel,
                    onBackClick = { navController.popBackStack() },
                    onProductAdded = { navController.popBackStack() }
                )
            }

            // ── Cart ──────────────────────────────────────────────────────────────
            composable("my_cart") {
                MyCartPage(
                    viewModel = mainStoreViewModel,
                    onNavigateToProductDetail = { product, store ->
                        productManagerViewModel.setCurrentProductAndStore(product, store)
                        navController.navigate("store_detail_view")
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            // ── Store view (from marketplace store card) ───────────────────────
            composable("store_view") {
                val store by productManagerViewModel.currentStore.collectAsState()
                val allProducts by mainStoreViewModel.products.collectAsState()
                
                store?.let { s ->
                    val storeProducts = allProducts.filter { it.second.storeName == s.storeName }.map { it.first }
                    PublicStoreView(
                        store = s,
                        products = storeProducts,
                        onNavigateToProductDetail = { product ->
                            productManagerViewModel.setCurrentProductAndStore(product, s)
                            navController.navigate("store_detail_view")
                        },
                        onAddToCart = { product ->
                            mainStoreViewModel.addToCart(product, s)
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }

            // ── Product Detail ────────────────────────────────────────────────────
            composable("store_detail_view") {
                val product by productManagerViewModel.currentProduct.collectAsState()
                val currentStore by productManagerViewModel.currentStore.collectAsState()
                val storeData by productManagerViewModel.storeData.collectAsState()
                val store = currentStore ?: storeData

                if (product != null && store != null) {
                    StoreDetailView(
                        product = product!!,
                        storeData = store!!,
                        onBackClick = { navController.popBackStack() },
                        onNavigateToStore = {
                            productManagerViewModel.setCurrentStore(it)
                            navController.navigate("store_view")
                        }
                    )
                }
            }
        }
    }
}