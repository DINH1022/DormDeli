package com.example.dormdeli.repository.admin

import com.example.dormdeli.enums.UserRole
import com.example.dormdeli.firestore.CollectionName
import com.example.dormdeli.firestore.ModelFields
import com.example.dormdeli.model.ShipperProfile
import com.example.dormdeli.model.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AdminShipperRepository {
    private val db = FirebaseFirestore.getInstance()
    private val shipperCol = db.collection(CollectionName.SHIPPER_PROFILE.value)
    private val userCol = db.collection(CollectionName.USERS.value)

    suspend fun countPendingShippers(): Int {
        return try {
            shipperCol
                .whereEqualTo(ModelFields.ShipperProfile.IS_APPROVED, false)
                .get()
                .await()
                .size()
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getApprovedShippers(): List<Pair<User, ShipperProfile>> {
        return try {
            val approvedShippers = shipperCol
                .whereEqualTo(ModelFields.ShipperProfile.IS_APPROVED, true)
                .get()
                .await()
                .toObjects(ShipperProfile::class.java)
            
            approvedShippers.mapNotNull { profile ->
                val userDoc = userCol.document(profile.userId).get().await()
                // Gán UID từ ID của document nếu object bị thiếu
                val user = userDoc.toObject(User::class.java)?.copy(uid = userDoc.id)
                if (user != null) Pair(user, profile) else null
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPendingShippers(): List<Pair<User, ShipperProfile>> {
        return try {
            val snapshot = shipperCol
                .whereEqualTo(ModelFields.ShipperProfile.IS_APPROVED, false)
                .get()
                .await()

            val profiles = snapshot.toObjects(ShipperProfile::class.java)
            val results = mutableListOf<Pair<User, ShipperProfile>>()

            for (profile in profiles) {
                if (profile.userId.isEmpty()) continue
                
                val userDoc = userCol.document(profile.userId).get().await()
                // Gán UID từ ID của document nếu object bị thiếu
                val user = userDoc.toObject(User::class.java)?.copy(uid = userDoc.id)
                if (user != null) {
                    results.add(user to profile)
                }
            }
            results
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun approveShipper(userId: String) {
        if (userId.isBlank()) {
            throw Exception("Mã người dùng không hợp lệ.")
        }
        
        try {
            val shipperRef = shipperCol.document(userId)
            val userRef = userCol.document(userId)

            db.runTransaction { transaction ->
                transaction.update(shipperRef, ModelFields.ShipperProfile.IS_APPROVED, true)
                transaction.update(userRef, ModelFields.User.ROLE, UserRole.SHIPPER.value)
                transaction.update(userRef, "roles", FieldValue.arrayUnion(UserRole.SHIPPER.value))
                null
            }.await()
        } catch (e: Exception) {
            throw Exception("Lỗi khi duyệt shipper: ${e.message}")
        }
    }

    suspend fun rejectShipper(userId: String) {
        if (userId.isBlank()) return
        try {
            shipperCol.document(userId).delete().await()
        } catch (e: Exception) {
            throw Exception("Lỗi khi từ chối shipper: ${e.message}")
        }
    }
}
