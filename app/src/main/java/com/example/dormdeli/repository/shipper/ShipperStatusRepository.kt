package com.example.dormdeli.repository.shipper

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await


class ShipperStatusRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val usersCollection = "users"

    fun getShipperOnlineStatusFlow(): Flow<Boolean> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(false)
            return@callbackFlow
        }

        val listener = db.collection(usersCollection).document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val isOnline = snapshot?.getBoolean("isOnline") ?: false
                trySend(isOnline)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateOnlineStatus(isOnline: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        try {
            db.collection(usersCollection).document(userId).update("isOnline", isOnline).await()
        } catch (e: Exception) {
            Log.e("ShipperStatusRepo", "Error updating online status: ${e.message}")
        }
    }
}
