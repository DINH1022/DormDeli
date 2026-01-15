package com.example.dormdeli.ui.seller.repository

import android.net.Uri
import com.example.dormdeli.utils.CloudinaryHelper
import com.example.dormdeli.ui.seller.model.MenuItem
import com.example.dormdeli.ui.seller.model.Restaurant
import com.example.dormdeli.ui.seller.model.RestaurantStatus
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
    private val restaurantsCollection = db.collection("restaurants")

    private fun getCurrentUserId(): String = "TEST_SELLER_ID" // Hardcoded for testing

    fun getRestaurantFlow(): Flow<Restaurant?> = callbackFlow {
        val userId = getCurrentUserId()
        val registration = restaurantsCollection.whereEqualTo("ownerId", userId)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val restaurant = snapshot?.documents?.firstOrNull()?.toObject(Restaurant::class.java)
                trySend(restaurant)
            }

        awaitClose { registration.remove() }
    }

    suspend fun createRestaurant(name: String, description: String, location: String, openingHours: String): Result<Unit> = try {
        val userId = getCurrentUserId()
        val restaurant = Restaurant(
            ownerId = userId,
            name = name,
            description = description,
            location = location,
            openingHours = openingHours,
            status = RestaurantStatus.PENDING.name // QUAN TRỌNG: Trở về PENDING để test luồng duyệt
        )
        restaurantsCollection.add(restaurant).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateRestaurant(restaurant: Restaurant): Result<Unit> = try {
        restaurantsCollection.document(restaurant.id).set(restaurant).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteRestaurant(restaurantId: String): Result<Unit> = try {
        restaurantsCollection.document(restaurantId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // === Menu Item Functions ===

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


    fun getMenuItemsFlow(restaurantId: String): Flow<List<MenuItem>> = callbackFlow {
        val menuItemsCollection = restaurantsCollection.document(restaurantId).collection("menuItems")
        val listener = menuItemsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val menuItems = snapshot?.toObjects(MenuItem::class.java) ?: emptyList()
            trySend(menuItems)
        }
        awaitClose { listener.remove() }
    }

    suspend fun addMenuItem(restaurantId: String, item: MenuItem): Result<Unit> = try {
        // Dùng .document(item.id).set(item) để đồng bộ ID
        restaurantsCollection.document(restaurantId)
            .collection("menuItems")
            .document(item.id) // Đảm bảo ID trên Firestore khớp với ID trong object
            .set(item)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateMenuItem(restaurantId: String, item: MenuItem): Result<Unit> = try {
        restaurantsCollection.document(restaurantId).collection("menuItems").document(item.id).set(item).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteMenuItem(restaurantId: String, itemId: String): Result<Unit> = try {
        restaurantsCollection.document(restaurantId).collection("menuItems").document(itemId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}