package com.example.mystore.Screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.mystore.CartItem
import com.example.mystore.Product
import com.example.mystore.StoreData
import com.example.mystore.MainStoreViewModel

// ─── Color Palette (Consistent with Search Page) ─────────────────────────────
private val Purple = Color(0xFF6C3CE1)
private val PurpleLight = Color(0xFF9B6DFF)
private val PurpleDark = Color(0xFF4A1FA8)
private val Gold = Color(0xFFFFB930)
private val Surface1 = Color(0xFFF8F5FF)
private val CardBg = Color(0xFFFFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCartPage(
    viewModel: MainStoreViewModel,
    onNavigateToProductDetail: (Product, StoreData) -> Unit,
    onBackClick: () -> Unit
) {
    val cartItems by viewModel.cartItems.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Shopping Cart", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PurpleDark
                )
            )
        },
        containerColor = Surface1
    ) { padding ->
        if (cartItems.isEmpty()) {
            EmptyCartState(padding, onBackClick)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cartItems) { item ->
                        CartItemCard(
                            item = item,
                            onQuantityChange = { newQty ->
                                viewModel.updateCartQuantity(item, newQty)
                            },
                            onRemove = { viewModel.removeFromCart(item) },
                            onClick = { onNavigateToProductDetail(item.product, item.store) }
                        )
                    }
                }

                CartSummarySection(cartItems)
            }
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Purple.copy(alpha = 0.1f))
            ) {
                if (item.product.productImageUrls.isNotEmpty()) {
                    AsyncImage(
                        model = item.product.productImageUrls[0],
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.productName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Sold by ${item.store.storeName}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "₹${String.format("%.0f", item.product.productPrice)}",
                    fontWeight = FontWeight.ExtraBold,
                    color = Purple,
                    fontSize = 18.sp
                )
            }

            // Quantity Controls
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { onQuantityChange(item.quantity + 1) }) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Add", tint = Purple)
                }
                Text(
                    text = "${item.quantity}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                IconButton(onClick = { onQuantityChange(item.quantity - 1) }) {
                    Icon(Icons.Default.RemoveCircle, contentDescription = "Decrease", tint = if (item.quantity > 1) Purple else Color.Gray)
                }
            }

            Spacer(Modifier.width(8.dp))

            // Dedicated Remove Button
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = Color.Red)
            }
        }
    }
}

@Composable
private fun CartSummarySection(cartItems: List<CartItem>) {
    val subtotal = cartItems.sumOf { it.product.productPrice * it.quantity }
    val shipping = if (subtotal > 500) 0.0 else 40.0
    val total = subtotal + shipping

    Surface(
        color = CardBg,
        shadowElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal", color = Color.Gray)
                Text("₹${String.format("%.2f", subtotal)}", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Shipping", color = Color.Gray)
                Text(
                    if (shipping == 0.0) "FREE" else "₹${String.format("%.2f", shipping)}",
                    color = if (shipping == 0.0) Color(0xFF00C853) else Color.Black,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Divider(modifier = Modifier.padding(vertical = 16.dp), color = Surface1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total Amount", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("₹${String.format("%.2f", total)}", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Purple)
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { /* Handle Checkout */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple)
            ) {
                Text("Proceed to Checkout", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun EmptyCartState(padding: PaddingValues, onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Purple.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = Purple,
                    modifier = Modifier.size(60.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Your cart is empty",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.DarkGray
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Looks like you haven't added anything to your cart yet.",
                textAlign = TextAlign.Center,
                color = Color.Gray,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onBackClick,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple)
            ) {
                Text("Start Shopping", modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}
