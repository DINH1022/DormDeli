package com.example.dormdeli.repository.admin

import com.example.dormdeli.firestore.CollectionName
import com.example.dormdeli.enums.UserRole
import com.example.dormdeli.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AdminUserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val userCol = db.collection(CollectionName.USERS.value)
    private val shipperCol = db.collection(CollectionName.SHIPPER_PROFILE.value)
    private val storeCol = db.collection(CollectionName.STORES.value)

    suspend fun countNewUsersLast7Days(): Int {
        val sevenDaysAgo =
            System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000

        return userCol
            .whereGreaterThanOrEqualTo("createdAt", sevenDaysAgo)
            .get()
            .await()
            .size()
    }

    suspend fun getPureUsers(): List<User> {
        val allUsers = db.collection(CollectionName.USERS.value)
            .whereNotEqualTo("role", UserRole.ADMIN.value)
            .get().await().toObjects(User::class.java)

        val shipperIds = shipperCol
            .get().await().documents.map { it.id }

        val storeOwnerIds = storeCol
            .get().await().toObjects(com.example.dormdeli.model.Store::class.java)
            .map { it.ownerId }

        val excludedIds = (shipperIds + storeOwnerIds).toSet()

        return allUsers.filter { user ->
            !excludedIds.contains(user.email)
        }
    }

    suspend fun updateUserStatus(uid: String, isActive: Boolean) {
        FirebaseFirestore.getInstance().collection(CollectionName.USERS.value)
            .document(uid)
            .update("active", isActive)
            .await()
    }

}