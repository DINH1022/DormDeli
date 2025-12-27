package com.example.dormdeli.repository.admin

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ShipperRepository {
    private val db = FirebaseFirestore.getInstance()
    private val shipperCol = db.collection("shipperProfiles")
    suspend fun countPendingShippers(): Int {
        return shipperCol
            .whereEqualTo("isApproved", false)
            .get()
            .await()
            .size()
    }

}