package com.example.mystore

import android.net.Uri

val productCategories = listOf(
    "🥬 Fresh Vegetables", "🍎 Fresh Fruits", "🌾 Grains (rice, wheat, maize, pulses)", "🌶️ Spices & Herbs",
    "🥛 Dairy Products (milk, curd, ghee, butter)", "🥚 Eggs & Poultry", "🍯 Honey & Jaggery", "🥒 Pickles & Papads",
    "🍪 Homemade Snacks (chips, sweets, etc.)", "🌱 Organic Produce", "🌾 Seeds & Fertilizers", "🐄 Animal Feed",
    "👘 Handloom Sarees & Shawls", "👕 Cotton Clothes", "🧥 Woolen Wear", "👔 Tailored Garments", "🥻 Traditional Dress",
    "👜 Handmade Bags & Scarves", "👡 Footwear", "🪑 Wooden Furniture", "🎋 Bamboo & Cane Products", "🧸 Handcrafted Toys",
    "🖼️ Handmade Home Decor", "🏺 Clay / Terracotta Pots", "🔨 Agricultural Tools", "🎁 Handicraft Gift Items",
    "🍽️ Utensils", "🧺 Baskets & Storage Containers", "🧼 Handmade Soaps & Detergents", "🕯️ Candles / Oil Lamps",
    "🧹 Home Cleaning Items", "🛏️ Blankets & Bedsheets", "⚙️ Farming Equipment", "💧 Irrigation Tools",
    "🌿 Livestock Feed & Supplements", "💊 Veterinary Products", "🐔 Poultry Equipment", "🌱 Seeds & Saplings",
    "🧱 Bricks, Cement, Sand", "🎨 Paint & Brushes", "🔩 Iron Rods", "🔨 Nails, Hammers, Wires", "🚰 Plumbing Materials",
    "🏠 Roofing Sheets", "💡 Light Bulbs, LEDs, Fans", "🔌 Switch Boards & Cables", "📱 Mobile Phones & Accessories",
    "📻 Radios & Speakers", "☀️ Solar Lamps / Solar Panels", "🌿 Ayurvedic / Herbal Products", "🧴 Soaps, Shampoo, Toothpaste",
    "🩹 Sanitary Products", "💉 First Aid Items", "😷 Masks & Sanitizers", "📓 Notebooks, Pens, Pencils", "🎒 Bags & School Uniforms",
    "📚 Books", "✏️ Art & Craft Supplies", "🌺 Flower & Vegetable Seeds", "🪴 Gardening Tools", "🍂 Organic Compost",
    "🪴 Pots & Planters", "➕ Others"
)

data class Product(
    val productId: String? = null,
    val productName: String,
    val productDescription: String,
    val productType: String,
    val productCategories: List<String> = emptyList(),
    val productPrice: Double,
    val productWholesalePrice: Double = 0.0,
    val stockQuantity: Int,
    val productCapacity: Double = 0.0,
    val productCapacityUnit: String = "",
    val productImages: List<Uri> = emptyList(), // Use Uri for local files in Android
    val productImageUrls: List<String> = emptyList(),
    val shippingMethod: String,
    val shippingAvailability: String
)

data class StoreData(
    val storeName: String,
    val storeDescription: String,
    val storeLogo: Uri? = null,
    val storeLogoUrl: String? = null,
    val ownerName: String,
    val gender: String = "",
    val phoneNumber: String,
    val address: String,
    val state: String = "",
    val district: String = "",
    val taluk: String = "",
    val storeProducts: List<String> = emptyList()
)

data class CartItem(
    val product: Product,
    val store: StoreData,
    val quantity: Int = 1
)
