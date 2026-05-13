package com.example.mystore

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

private val Purple = Color(0xFF6C3CE1)
private val PurpleDark = Color(0xFF4A1FA8)
private val PurpleLight = Color(0xFF9B6DFF)
private val Gold = Color(0xFFFFB930)
private val GreenSuccess = Color(0xFF1DB954)
private val Surface1 = Color(0xFFF8F5FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicStoreView(
    store: StoreData,
    products: List<Product>,
    onNavigateToProductDetail: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        containerColor = Surface1,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(PurpleDark, Purple, PurpleLight)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 12.dp)
                        .padding(top = 24.dp), // status bar padding
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    
                    // Store logo
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(2.dp, Gold, CircleShape)
                            .background(Color.White.copy(.15f))
                    ) {
                        if (!store.storeLogoUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = store.storeLogoUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Store, null, tint = Gold,
                                modifier = Modifier.size(22.dp).align(Alignment.Center)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            store.storeName,
                            fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.White,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${products.size} product${if (products.size != 1) "s" else ""}",
                            fontSize = 12.sp, color = Color.White.copy(.7f)
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // Store info cards
            PublicStoreInfoCards(store, products.size)

            // Products
            if (products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("This store hasn't added any products yet.", color = Color.Gray, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("Products Available", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                            color = Color(0xFF1A1A2E), modifier = Modifier.padding(vertical = 4.dp))
                    }
                    itemsIndexed(products) { _, product ->
                        PublicStoreProductCard(
                            product = product,
                            onAddToCart = {
                                onAddToCart(product)
                                Toast.makeText(context, "${product.productName} added to cart", Toast.LENGTH_SHORT).show()
                            },
                            onClick = { onNavigateToProductDetail(product) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PublicStoreInfoCards(store: StoreData, productCount: Int) {
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
            value = "Verified",
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
fun PublicStoreProductCard(
    product: Product, 
    onAddToCart: () -> Unit,
    onClick: () -> Unit
) {
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

            // Add to Cart Button
            IconButton(
                onClick = onAddToCart,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .background(Purple.copy(.1f), CircleShape)
            ) {
                Icon(Icons.Default.AddShoppingCart, null, tint = Purple, modifier = Modifier.size(20.dp))
            }
        }
    }
}
