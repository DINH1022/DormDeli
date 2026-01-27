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

    private fun getCurrentUserId(): String = "TEST_SELLER_ID" // Hardcoded for testing, should use auth.currentUser?.uid in prod

    fun getStoreFlow(): Flow<Store?> = callbackFlow {
        val userId = getCurrentUserId()
        val registration = storesCollection.whereEqualTo("ownerId", userId)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val store = snapshot?.documents?.firstOrNull()?.toObject(Store::class.java)?.copy(id = snapshot.documents.first().id)
                trySend(store)
            }

        awaitClose { registration.remove() }
    }

    suspend fun createStore(name: String, description: String, address: String, latitude: Double, longitude: Double, openingHours: String): Result<Unit> = try {
        val userId = getCurrentUserId()
        val store = Store(
            ownerId = userId,
            name = name,
            description = description,
            location = address, // Use address for location field too for now
            address = address,
            latitude = latitude,
            longitude = longitude,
            openTime = openingHours, 
            approved = true, // Auto-approve as requested
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
        // Use document(food.id).set() to allow generated IDs if provided or update if exists
        val docRef = if (food.id.isNotEmpty()) foodsCollection.document(food.id) else foodsCollection.document()
        val finalFood = foodWithStoreId.copy(id = docRef.id)
        
        docRef.set(finalFood).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateFood(food: Food): Result<Unit> = try {
        foodsCollection.document(food.id).set(food).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteFood(foodId: String): Result<Unit> = try {
        foodsCollection.document(foodId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}