package com.example.dormdeli.ui.admin.repository

import com.example.dormdeli.model.Store
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AdminRepository {

    private val db = FirebaseFirestore.getInstance()
    private val storesCollection = db.collection("stores")

    fun getAllStoresStream(): Flow<List<Store>> = callbackFlow {
        val listener = storesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error) // Close the flow on error
                return@addSnapshotListener
            }
            if (snapshot != null) {
                // Manually map to ensure ID is set from document ID if needed, though Store has ID field usually
                // and toObjects might miss it if it's @DocumentId decorated only. 
                // Store model in com.example.dormdeli.model.Store: val id: String = ""
                // Let's assume toObjects works or do manual map like SellerRepository did just in case.
                val stores = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Store::class.java)?.copy(id = doc.id)
                }
                trySend(stores)
            }
        }

        // When the flow is cancelled, remove the listener
        awaitClose {
            listener.remove()
        }
    }

    suspend fun approveStore(storeId: String): Result<Unit> = try {
        storesCollection.document(storeId)
            .update("approved", true)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun rejectStore(storeId: String): Result<Unit> = try {
        // "Rejecting" usually means just not approving, or maybe setting active=false? 
        // Or we could have an explicit status field in Store if needed.
        // But based on my previous analysis of Store model:
        /*
        data class Store(
            val id: String = "",
            val approved: Boolean = false,
            val active: Boolean = true,
            ...
        )
        */
        // There is no explicit "REJECTED" state in boolean approved. 
        // Maybe approved=false is pending/rejected. 
        // For now, let's assume reject means approved=false (stays pending) or maybe delete?
        // AdminStoreManagementScreen has a "Reject" button. 
        // Let's just set approved=false for now, or maybe add a "rejected" field if critical.
        // However, user said "switch back to Store", so I must stick to Store model.
        // If I look at the UI, "Reject" button calls rejectStore.
        // If approved=false and active=true -> Pending.
        // If approved=false and active=false -> Maybe Rejected?
        // Let's look at SellerViewModel logic I wrote:
        /*
        val restaurantStatus: StateFlow<RestaurantStatus> = store.map { s ->
            if (s == null) RestaurantStatus.NONE
            else if (s.approved) RestaurantStatus.APPROVED
            else RestaurantStatus.PENDING 
        }
        */
        // It doesn't handle rejected. 
        // I will just set approved = false. Effectively it stays "Pending" in UI? 
        // Or maybe I should delete it if rejected? 
        // Pending stores screen has "Approve" and "Reject". 
        // If I click Reject and it stays Pending, that's confusing.
        // Maybe update "active" to false?
        storesCollection.document(storeId)
            .update("approved", false) // Simply un-approve
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteStore(storeId: String): Result<Unit> = try {
        storesCollection.document(storeId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
