package com.example.mystore

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class ProductManagerViewModel : ViewModel() {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _storeData = MutableStateFlow<StoreData?>(null)
    val storeData: StateFlow<StoreData?> = _storeData.asStateFlow()

    private val _isStoreCreated = MutableStateFlow(false)
    val isStoreCreated: StateFlow<Boolean> = _isStoreCreated.asStateFlow()

    private val _isStoreDeactivated = MutableStateFlow(false)
    val isStoreDeactivated: StateFlow<Boolean> = _isStoreDeactivated.asStateFlow()

    private val _deactivatedUntil = MutableStateFlow<Date?>(null)
    val deactivatedUntil: StateFlow<Date?> = _deactivatedUntil.asStateFlow()

    private val _currentProduct = MutableStateFlow<Product?>(null)
    val currentProduct: StateFlow<Product?> = _currentProduct.asStateFlow()

    private val _currentStore = MutableStateFlow<StoreData?>(null)
    val currentStore: StateFlow<StoreData?> = _currentStore.asStateFlow()

    fun setCurrentProductAndStore(product: Product, store: StoreData) {
        _currentProduct.value = product
        _currentStore.value = store
    }

    fun setCurrentProduct(product: Product) {
        _currentProduct.value = product
    }

    fun setCurrentStore(store: StoreData) {
        _currentStore.value = store
    }

    val productCount: Int get() = _products.value.size

    fun initializeStore() {
        viewModelScope.launch {
            try {
                val data = FirebaseStoreService.getStore()
                _storeData.value = data
                _isStoreCreated.value = data != null

                if (data != null) {
                    checkStoreStatus()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _isStoreCreated.value = false
            }
        }
    }

    private suspend fun checkStoreStatus() {
        // Since we don't have the raw Firestore doc here directly with status, 
        // ideally we would add status to StoreData model. 
        // For now, we simulate what the Flutter code did.
    }

    fun loadProducts() {
        viewModelScope.launch {
            try {
                val loadedProducts = FirebaseStoreService.getProducts()
                _products.value = loadedProducts
            } catch (e: Exception) {
                e.printStackTrace()
                _products.value = emptyList()
            }
        }
    }

    fun addProduct(context: Context, product: Product, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val success = FirebaseStoreService.addProduct(context, product)
                if (success) {
                    loadProducts()
                }
                onComplete(success)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    fun updateProduct(index: Int, updatedProduct: Product, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val currentProducts = _products.value
            if (index < 0 || index >= currentProducts.size) {
                onComplete(false)
                return@launch
            }

            val productId = currentProducts[index].productId
            if (productId.isNullOrEmpty()) {
                onComplete(false)
                return@launch
            }

            try {
                val success = FirebaseStoreService.updateProduct(productId, updatedProduct)
                if (success) {
                    loadProducts()
                }
                onComplete(success)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    fun removeProductLocally(index: Int) {
        val currentProducts = _products.value.toMutableList()
        if (index in currentProducts.indices) {
            currentProducts.removeAt(index)
            _products.value = currentProducts
        }
    }

    fun deleteProductInBackground(productId: String) {
        viewModelScope.launch {
            try {
                FirebaseStoreService.deleteProduct(productId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeProduct(index: Int, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val currentProducts = _products.value
            if (index < 0 || index >= currentProducts.size) {
                onComplete(false)
                return@launch
            }

            val productId = currentProducts[index].productId
            if (productId == null) {
                onComplete(false)
                return@launch
            }

            try {
                val success = FirebaseStoreService.deleteProduct(productId)
                if (success) {
                    removeProductLocally(index)
                }
                onComplete(success)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    fun clearAllProducts() {
        viewModelScope.launch {
            try {
                val success = FirebaseStoreService.clearAllProducts()
                if (success) {
                    _products.value = emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteStore() {
        viewModelScope.launch {
            try {
                val success = FirebaseStoreService.deleteStore()
                if (success) {
                    resetState()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resetState() {
        _products.value = emptyList()
        _storeData.value = null
        _isStoreCreated.value = false
        _isStoreDeactivated.value = false
        _deactivatedUntil.value = null
        _currentProduct.value = null
        _currentStore.value = null
    }

    fun deactivateStore(until: Date) {
        viewModelScope.launch {
            try {
                val success = FirebaseStoreService.deactivateStore(until)
                if (success) {
                    _isStoreDeactivated.value = true
                    _deactivatedUntil.value = until
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun reactivateStore(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val success = FirebaseStoreService.reactivateStore()
                if (success) {
                    _isStoreDeactivated.value = false
                    _deactivatedUntil.value = null
                }
                onComplete(success)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    fun createStore(context: Context, newStoreData: StoreData, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val success = FirebaseStoreService.createStore(context, newStoreData)
                if (success) {
                    _storeData.value = newStoreData
                    _isStoreCreated.value = true
                }
                onComplete(success)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    fun updateStore(context: Context, newStoreData: StoreData) {
        _storeData.value = newStoreData
        viewModelScope.launch {
            FirebaseStoreService.updateStore(context, newStoreData)
        }
    }
}
