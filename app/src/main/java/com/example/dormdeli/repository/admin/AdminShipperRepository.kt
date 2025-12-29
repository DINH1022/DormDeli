package com.example.dormdeli.repository.admin

import com.example.dormdeli.enums.UserRole
import com.example.dormdeli.firestore.CollectionName
import com.example.dormdeli.firestore.ModelFields
import com.example.dormdeli.model.ShipperProfile
import com.example.dormdeli.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AdminShipperRepository {
    private val db = FirebaseFirestore.getInstance()
    private val shipperCol = db.collection(CollectionName.SHIPPER_PROFILE.value)
    private val userCol = db.collection(CollectionName.USERS.value)
    suspend fun countPendingShippers(): Int {
        return shipperCol
            .whereEqualTo(ModelFields.ShipperProfile.IS_APPROVED, false)
            .get()
            .await()
            .size()
    }

    suspend fun getApprovedShippers(): List<Pair<User, ShipperProfile>> {
        val approvedShippers = shipperCol
            .whereEqualTo(ModelFields.ShipperProfile.IS_APPROVED, true)
            .get()
            .await()
            .toObjects(ShipperProfile::class.java)
        val result = approvedShippers.mapNotNull { profile ->
            val userDoc = userCol.document(profile.userId).get().await()
            val user = userDoc.toObject(User::class.java)
            if (user != null) Pair(user, profile) else null
        }
        println(
            "getApprovedShippers -> profiles=${approvedShippers.size}, result=${result.size}"
        )
        return result;
    }

    suspend fun getPendingShippers(): List<Pair<User, ShipperProfile>> {
        val snapshot = shipperCol
            .whereEqualTo(ModelFields.ShipperProfile.IS_APPROVED, false)
            .get()
            .await()

        val profiles = snapshot.toObjects(ShipperProfile::class.java)
        val results = mutableListOf<Pair<User, ShipperProfile>>()

        for (profile in profiles) {
            val userDoc = userCol.document(profile.userId).get().await()
            val user = userDoc.toObject(User::class.java)
            if (user != null) {
                results.add(user to profile)
            }
        }
        return results
    }

    suspend fun approveShipper(userId: String) {
        try {
            db.runTransaction { transaction ->
                val shipperRef = shipperCol.document(userId)
                val userRef = userCol.document(userId)
                transaction.update(shipperRef, ModelFields.ShipperProfile.IS_APPROVED, true)
                transaction.update(userRef, ModelFields.User.ROLE, UserRole.SHIPPER.value)
            }.await()
        } catch (e: Exception) {
            throw Exception("Không thể duyệt shipper: ${e.message}")
        }
    }

    suspend fun rejectShipper(userId: String) {
        shipperCol.document(userId).delete().await()
    }
}