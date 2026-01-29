package com.example.dormdeli.ui.seller.repository

import android.net.Uri
import com.example.dormdeli.utils.CloudinaryHelper
import com.example.dormdeli.model.Food
import com.example.dormdeli.model.Store
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.coroutines.resume

class SellerRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storesCollection = db.collection("stores")
    private val foodsCollection = db.collection("foods")

    private fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun getStoreFlow(): Flow<Store?> = callbackFlow {
        val userId = getCurrentUserId()
        if (userId == null) {
            trySend(null)
            return@callbackFlow
        }
        val registration = storesCollection.whereEqualTo("ownerId", userId)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val doc = snapshot?.documents?.firstOrNull()
                val store = doc?.toObject(Store::class.java)?.copy(id = doc.id)
                trySend(store)
            }

        awaitClose { registration.remove() }
    }

    suspend fun createStore(
        name: String, 
        description: String, 
        location: String, 
        openTime: String,
        closeTime: String,
        latitude: Double,
        longitude: Double
    ): Result<Unit> = try {
        val userId = getCurrentUserId() ?: throw Exception("User not logged in")
        val store = Store(
            ownerId = userId,
            name = name,
            description = description,
            location = location,
            openTime = openTime,
            closeTime = closeTime,
            latitude = latitude,
            longitude = longitude,
            approved = false, // Require Admin approval
            active = true
        )
        storesCollection.add(store).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateStore(store: Store): Result<Unit> = try {
        storesCollection.document(store.id).set(store).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteStore(storeId: String): Result<Unit> = try {
        storesCollection.document(storeId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // === Menu Item Functions (Foods) ===

    suspend fun uploadImage(uri: Uri): Result<String> = suspendCancellableCoroutine { continuation ->
        CloudinaryHelper.uploadImage(
            uri = uri,
            onSuccess = { imageUrl ->
                if (continuation.isActive) {
                    continuation.resume(Result.success(imageUrl))
                }
            },
            onError = { error ->
                if (continuation.isActive) {
                    continuation.resume(Result.failure(Exception(error)))
                }
            }
        )
    }


    fun getFoodsFlow(storeId: String): Flow<List<Food>> = callbackFlow {
        val listener = foodsCollection.whereEqualTo("storeId", storeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val foods = snapshot?.documents?.mapNotNull { doc ->
                     doc.toObject(Food::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(foods)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addFood(storeId: String, food: Food): Result<Unit> = try {
        val foodWithStoreId = food.copy(storeId = storeId) // Ensure storeId is set
        val docRef = if (food.id.isNotEmpty()) foodsCollection.document(food.id) else foodsCollection.document()
        val finalFood = foodWithStoreId.copy(id = docRef.id)
        
        docRef.set(finalFood).await()
        syncStoreTags(storeId) // Tự động cập nhật tag cho store
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateFood(food: Food): Result<Unit> = try {
        foodsCollection.document(food.id).set(food).await()
        syncStoreTags(food.storeId) // Tự động cập nhật tag cho store
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteFood(foodId: String): Result<Unit> = try {
        // Cần lấy storeId trước khi xóa để sync
        val foodDoc = foodsCollection.document(foodId).get().await()
        val storeId = foodDoc.getString("storeId")
        
        foodsCollection.document(foodId).delete().await()
        
        if (storeId != null) {
            syncStoreTags(storeId)
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Tự động cập nhật trường 'tags' của Store dựa trên các 'category' của Food mà Store đó bán
     */
    private suspend fun syncStoreTags(storeId: String) {
        try {
            // 1. Lấy tất cả món ăn của store
            val foodSnapshot = foodsCollection.whereEqualTo("storeId", storeId).get().await()
            val categories = foodSnapshot.documents.mapNotNull { it.getString("category") }
                .filter { it.isNotBlank() }
                .distinct()

            // 2. Ánh xạ từ mã category sang tên hiển thị tiếng Việt
            val displayTags = categories.map { cat ->
                when(cat.lowercase()) {
                    "Rice" -> "Cơm"
                    "Noodle" -> "Mì/Phở/Bún"
                    "Fast_food" -> "Đồ ăn nhanh"
                    "Drink" -> "Đồ uống"
                    "Dessert" -> "Tráng miệng"
                    else -> "Khác"
                }
            }.distinct()

            // 3. Cập nhật vào Store
            storesCollection.document(storeId).update("tags", displayTags).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
