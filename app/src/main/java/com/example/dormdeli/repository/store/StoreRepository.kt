package com.example.dormdeli.repository.store

import android.util.Log
import com.example.dormdeli.model.Store
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class StoreRepository {
    private val db = FirebaseFirestore.getInstance()

    fun getStoreById(
        storeId: String,
        onSuccess: (Store?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("stores")
            .document(storeId)
            .get()
            .addOnSuccessListener { doc ->
                onSuccess(doc.toObject(Store::class.java))
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    fun getAllStores(onSuccess: (List<Store>) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("stores")
            .whereEqualTo("active", true) // Chỉ lấy quán đang hoạt động
            // .whereEqualTo("approved", true) // Bỏ comment nếu muốn chỉ lấy quán đã duyệt
            .get()
            .addOnSuccessListener { result ->
                val storeList = result.map { document ->
                    document.toObject(Store::class.java).copy(id = document.id)
                }
                onSuccess(storeList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}