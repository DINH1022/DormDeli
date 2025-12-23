package com.example.dormdeli.repository.store

import com.example.dormdeli.model.Store
import com.google.firebase.firestore.FirebaseFirestore

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
}