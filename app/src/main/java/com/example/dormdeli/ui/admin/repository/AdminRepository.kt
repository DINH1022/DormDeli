package com.example.dormdeli.ui.admin.repository

import com.example.dormdeli.ui.seller.model.Restaurant
import com.example.dormdeli.ui.seller.model.RestaurantStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AdminRepository {

    private val db = FirebaseFirestore.getInstance()
    private val restaurantsCollection = db.collection("restaurants")

    fun getAllStoresStream(): Flow<List<Restaurant>> = callbackFlow {
        val listener = restaurantsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error) // Close the flow on error
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val stores = snapshot.toObjects(Restaurant::class.java)
                trySend(stores) // Send the latest list of stores
            }
        }

        // When the flow is cancelled, remove the listener
        awaitClose {
            listener.remove()
        }
    }

    suspend fun approveStore(storeId: String): Result<Unit> = try {
        restaurantsCollection.document(storeId)
            .update("status", RestaurantStatus.APPROVED.name)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun rejectStore(storeId: String): Result<Unit> = try {
        restaurantsCollection.document(storeId)
            .update("status", RestaurantStatus.REJECTED.name)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteStore(storeId: String): Result<Unit> = try {
        restaurantsCollection.document(storeId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
