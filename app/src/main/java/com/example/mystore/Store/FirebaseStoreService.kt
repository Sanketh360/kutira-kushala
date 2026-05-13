package com.example.mystore

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

object FirebaseStoreService {
    private val firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    val currentUserId: String? get() = auth.currentUser?.uid

    // ============ STORE OPERATIONS ============

    suspend fun createStore(context: Context, storeData: StoreData): Boolean {
        val userId = currentUserId ?: return false

        return try {
            var logoUrl: String? = storeData.storeLogoUrl
            if (storeData.storeLogo != null) {
                logoUrl = CloudinaryStoreService.uploadStoreLogo(context, storeData.storeLogo)
                if (logoUrl == null) return false
            }

            val storeMap = hashMapOf(
                "userId" to userId,
                "storeName" to storeData.storeName,
                "storeDescription" to storeData.storeDescription,
                "storeLogoUrl" to logoUrl,
                "ownerName" to storeData.ownerName,
                "gender" to storeData.gender,
                "phoneNumber" to storeData.phoneNumber,
                "address" to storeData.address,
                "state" to storeData.state,
                "district" to storeData.district,
                "taluk" to storeData.taluk,
                "storeProducts" to storeData.storeProducts,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
                "status" to "active",
                "deactivatedUntil" to null
            )

            firestore.collection("stores").document(userId).set(storeMap).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getStore(): StoreData? {
        val userId = currentUserId ?: return null

        return try {
            val doc = firestore.collection("stores").document(userId).get().await()
            if (!doc.exists()) return null

            StoreData(
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
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateStore(context: Context, storeData: StoreData): Boolean {
        val userId = currentUserId ?: return false

        return try {
            val updateData = mutableMapOf<String, Any>(
                "storeName" to storeData.storeName,
                "storeDescription" to storeData.storeDescription,
                "ownerName" to storeData.ownerName,
                "gender" to storeData.gender,
                "phoneNumber" to storeData.phoneNumber,
                "address" to storeData.address,
                "state" to storeData.state,
                "district" to storeData.district,
                "taluk" to storeData.taluk,
                "storeProducts" to storeData.storeProducts,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            if (storeData.storeLogo != null) {
                val logoUrl = CloudinaryStoreService.uploadStoreLogo(context, storeData.storeLogo)
                if (logoUrl != null) {
                    updateData["storeLogoUrl"] = logoUrl
                }
            }

            firestore.collection("stores").document(userId).update(updateData).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun storeExists(): Boolean {
        val userId = currentUserId ?: return false
        return try {
            val doc = firestore.collection("stores").document(userId).get().await()
            doc.exists()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteStore(): Boolean {
        val userId = currentUserId ?: return false

        return try {
            // Delete products first
            val productsSnapshot = firestore.collection("stores")
                .document(userId)
                .collection("products")
                .get()
                .await()

            for (doc in productsSnapshot.documents) {
                doc.reference.delete().await()
            }

            firestore.collection("stores").document(userId).delete().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deactivateStore(until: Date): Boolean {
        val userId = currentUserId ?: return false

        return try {
            firestore.collection("stores").document(userId).update(
                mapOf(
                    "status" to "deactivated",
                    "deactivatedUntil" to until,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun reactivateStore(): Boolean {
        val userId = currentUserId ?: return false

        return try {
            firestore.collection("stores").document(userId).update(
                mapOf(
                    "status" to "active",
                    "deactivatedUntil" to null,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ============ PRODUCT OPERATIONS ============

    suspend fun addProduct(context: Context, product: Product): Boolean {
        val userId = currentUserId ?: return false

        return try {
            val imageUrls = product.productImageUrls.toMutableList()

            if (product.productImages.isNotEmpty()) {
                val urls = CloudinaryStoreService.uploadProductImages(context, product.productImages)
                imageUrls.addAll(urls)
            }

            if (imageUrls.isEmpty()) return false

            val productRef = firestore.collection("stores")
                .document(userId)
                .collection("products")
                .document()

            val productMap = hashMapOf(
                "productId" to productRef.id,
                "userId" to userId,
                "productName" to product.productName,
                "productDescription" to product.productDescription,
                "productType" to product.productType,
                "productCategories" to product.productCategories,
                "productPrice" to product.productPrice,
                "stockQuantity" to product.stockQuantity,
                "shippingMethod" to product.shippingMethod,
                "shippingAvailability" to product.shippingAvailability,
                "productWholesalePrice" to product.productWholesalePrice,
                "productCapacity" to product.productCapacity,
                "productCapacityUnit" to product.productCapacityUnit,
                "productImageUrls" to imageUrls,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
                "status" to "active"
            )

            productRef.set(productMap).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getProducts(): List<Product> {
        val userId = currentUserId ?: return emptyList()

        return try {
            val snapshot = firestore.collection("stores")
                .document(userId)
                .collection("products")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.map { doc ->
                Product(
                    productId = doc.id,
                    productName = doc.getString("productName") ?: "",
                    productDescription = doc.getString("productDescription") ?: "",
                    productType = doc.getString("productType") ?: "",
                    productPrice = doc.getDouble("productPrice") ?: 0.0,
                    stockQuantity = doc.getLong("stockQuantity")?.toInt() ?: 0,
                    shippingMethod = doc.getString("shippingMethod") ?: "",
                    shippingAvailability = doc.getString("shippingAvailability") ?: "",
                    productWholesalePrice = doc.getDouble("productWholesalePrice") ?: 0.0,
                    productCapacity = doc.getDouble("productCapacity") ?: 0.0,
                    productCapacityUnit = doc.getString("productCapacityUnit") ?: "",
                    productCategories = doc.get("productCategories") as? List<String> ?: emptyList(),
                    productImageUrls = doc.get("productImageUrls") as? List<String> ?: emptyList()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun updateProduct(productId: String, product: Product): Boolean {
        val userId = currentUserId ?: return false

        return try {
            val updateData = mapOf(
                "productName" to product.productName,
                "productDescription" to product.productDescription,
                "productType" to product.productType,
                "productPrice" to product.productPrice,
                "stockQuantity" to product.stockQuantity,
                "shippingMethod" to product.shippingMethod,
                "shippingAvailability" to product.shippingAvailability,
                "productWholesalePrice" to product.productWholesalePrice,
                "productCapacity" to product.productCapacity,
                "productCapacityUnit" to product.productCapacityUnit,
                "productCategories" to product.productCategories,
                "productImageUrls" to product.productImageUrls,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            firestore.collection("stores")
                .document(userId)
                .collection("products")
                .document(productId)
                .update(updateData)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteProduct(productId: String): Boolean {
        val userId = currentUserId ?: return false

        return try {
            firestore.collection("stores")
                .document(userId)
                .collection("products")
                .document(productId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun clearAllProducts(): Boolean {
        val userId = currentUserId ?: return false

        return try {
            val snapshot = firestore.collection("stores")
                .document(userId)
                .collection("products")
                .get()
                .await()

            for (doc in snapshot.documents) {
                doc.reference.delete().await()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getProductCount(): Int {
        val userId = currentUserId ?: return 0
        return try {
            val snapshot = firestore.collection("stores")
                .document(userId)
                .collection("products")
                .count()
                .get(com.google.firebase.firestore.AggregateSource.SERVER)
                .await()
            snapshot.count.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    // ============ STREAM OPERATIONS ============

    fun streamStore(): Flow<StoreData?> = callbackFlow {
        val userId = currentUserId
        if (userId == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("stores").document(userId)
            .addSnapshotListener { doc, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (doc != null && doc.exists()) {
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
                    trySend(storeData)
                } else {
                    trySend(null)
                }
            }

        awaitClose { listener.remove() }
    }

    fun streamProducts(): Flow<List<Product>> = callbackFlow {
        val userId = currentUserId
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("stores")
            .document(userId)
            .collection("products")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val products = snapshot.documents.map { doc ->
                        Product(
                            productId = doc.id,
                            productName = doc.getString("productName") ?: "",
                            productDescription = doc.getString("productDescription") ?: "",
                            productType = doc.getString("productType") ?: "",
                            productPrice = doc.getDouble("productPrice") ?: 0.0,
                            stockQuantity = doc.getLong("stockQuantity")?.toInt() ?: 0,
                            shippingMethod = doc.getString("shippingMethod") ?: "",
                            shippingAvailability = doc.getString("shippingAvailability") ?: "",
                            productWholesalePrice = doc.getDouble("productWholesalePrice") ?: 0.0,
                            productCapacity = doc.getDouble("productCapacity") ?: 0.0,
                            productCapacityUnit = doc.getString("productCapacityUnit") ?: "",
                            productCategories = doc.get("productCategories") as? List<String> ?: emptyList(),
                            productImageUrls = doc.get("productImageUrls") as? List<String> ?: emptyList()
                        )
                    }
                    trySend(products)
                }
            }

        awaitClose { listener.remove() }
    }
}
