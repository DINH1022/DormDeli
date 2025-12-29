package com.example.dormdeli.repository

import com.example.dormdeli.enums.UserRole
import com.example.dormdeli.model.User
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val userCol = db.collection("users")


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
