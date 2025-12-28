package com.example.dormdeli.repository.admin

import com.example.dormdeli.firestore.CollectionName
import com.example.dormdeli.firestore.ModelFields
import com.example.dormdeli.model.Store
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class AdminStoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storeCol= db.collection(CollectionName.STORES.value)

    suspend fun countPendingStores(): Int {
        return storeCol
            .whereEqualTo(ModelFields.Store.APPROVED, false)
            .get()
            .await()
            .size()
    }

    suspend fun getPendingStores(): List<Store> {
        return try {
            val snapshot = storeCol
                .whereEqualTo(ModelFields.Store.APPROVED, false)
                .orderBy(ModelFields.Store.CREATED_AT, Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Store::class.java)
            }
        } catch (e: Exception) {
            throw Exception("Không thể lấy danh sách cửa hàng: ${e.message}")
        }
    }

    /**
     * Duyệt cửa hàng
     */
    suspend fun approveStore(storeId: String) {
        try {
            storeCol.document(storeId)
                .update(
                    mapOf(
                        ModelFields.Store.APPROVED to true
                    )
                )
                .await()
        } catch (e: Exception) {
            throw Exception("Không thể duyệt cửa hàng: ${e.message}")
        }
    }

    /**
     * Từ chối cửa hàng (xóa hoặc đánh dấu rejected)
     * Option 1: Xóa hoàn toàn
     */
    suspend fun rejectStore(storeId: String) {
        try {
            storeCol.document(storeId)
                .delete()
                .await()
        } catch (e: Exception) {
            throw Exception("Không thể từ chối cửa hàng: ${e.message}")
        }
    }

    /**
     * Từ chối cửa hàng (giữ lại với trạng thái rejected)
     * Option 2: Đánh dấu rejected thay vì xóa
     */
//    suspend fun rejectStoreWithStatus(storeId: String, reason: String = "") {
//        try {
//            storeCol.document(storeId)
//                .update(
//                    mapOf(
//                        ModelFields.Store.REJECTED to true,
//                    )
//                )
//                .await()
//        } catch (e: Exception) {
//            throw Exception("Không thể từ chối cửa hàng: ${e.message}")
//        }
//    }

    /**
     * Lấy thông tin chi tiết một cửa hàng
     */
    suspend fun getStoreById(storeId: String): Store? {
        return try {
            val snapshot = storeCol.document(storeId).get().await()
            snapshot.toObject(Store::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // Thêm vào class AdminStoreRepository
    suspend fun getApprovedStores(): List<Store> {
        return try {
            val snapshot = storeCol
                .whereEqualTo(ModelFields.Store.APPROVED, true) // Lấy các store đã duyệt
                .orderBy(ModelFields.Store.CREATED_AT, Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Store::class.java)
            }
        } catch (e: Exception) {
            throw Exception("Không thể lấy danh sách cửa hàng đã duyệt: ${e.message}")
        }
    }

}