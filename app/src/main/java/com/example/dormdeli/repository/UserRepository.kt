package com.example.dormdeli.repository

import com.example.dormdeli.enums.UserRole
import com.example.dormdeli.firestore.CollectionName
import com.example.dormdeli.model.MessageToken
import com.example.dormdeli.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val userCol = db.collection(CollectionName.USERS.value)
    private val tokenCol = db.collection(CollectionName.MESSAGE_TOKEN.value)

    fun createUser(
        userId: String,
        user: User,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userCol.document(userId)
            .set(user)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }


    fun getUserById(
        userId: String,
        onSuccess: (User?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userCol.document(userId)
            .get()
            .addOnSuccessListener { doc ->
                onSuccess(doc.toObject(User::class.java))
            }
            .addOnFailureListener { onFailure(it) }
    }

    suspend fun updateFcmToken(userId: String, token: String) {
        try {
            // 1. Cập nhật fcmToken trong collection users (để dự phòng)
            userCol.document(userId).update("fcmToken", token).await()
            
            // 2. Cập nhật/Thêm vào collection messageToken (để Admin dùng)
            val userDoc = userCol.document(userId).get().await()
            val role = userDoc.getString("role") ?: "customer"
            
            tokenCol.document(userId).set(
                MessageToken(userId = userId, fcmToken = token, role = role)
            ).await()
            
        } catch (e: Exception) {
            // Log error
        }
    }

    fun getUserByPhone(
        phone: String,
        onSuccess: (User?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userCol.whereEqualTo("phone", phone)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    onSuccess(snapshot.documents[0].toObject(User::class.java))
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun getUserByEmail(
        email: String,
        onSuccess: (User?, String?) -> Unit, // Trả về User object và Document ID (UID)
        onFailure: (Exception) -> Unit
    ) {
        userCol.whereEqualTo("email", email)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val doc = snapshot.documents[0]
                    onSuccess(doc.toObject(User::class.java), doc.id)
                } else {
                    onSuccess(null, null)
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun getUserByStudentId(
        studentId: String,
        onSuccess: (User?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userCol.whereEqualTo("studentId", studentId)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    onSuccess(snapshot.documents[0].toObject(User::class.java))
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { onFailure(it) }
    }


    fun getUsersByRole(
        role: UserRole,
        onSuccess: (List<User>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userCol.whereEqualTo("role", role.value)
            .get()
            .addOnSuccessListener { snapshot ->
                onSuccess(snapshot.toObjects(User::class.java))
            }
            .addOnFailureListener { onFailure(it) }
    }


    fun updateUserFields(
        userId: String,
        fields: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userCol.document(userId)
            .update(fields)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }


    fun updateUserRole(
        userId: String,
        role: UserRole,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        updateUserFields(
            userId,
            mapOf("role" to role.value),
            onSuccess,
            onFailure
        )
    }


    fun setUserActive(
        userId: String,
        isActive: Boolean,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        updateUserFields(
            userId,
            mapOf("isActive" to isActive),
            onSuccess,
            onFailure
        )
    }

    fun deleteUser(
        userId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userCol.document(userId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}
