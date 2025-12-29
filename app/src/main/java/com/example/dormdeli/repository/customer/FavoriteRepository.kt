package com.example.dormdeli.repository.customer

import android.util.Log
import com.example.dormdeli.model.Favorite
import com.example.dormdeli.model.Food
import com.example.dormdeli.model.Store
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FavoriteRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val collectionName = "favorites" // Tên collection trên Firebase

    fun getUserFavorites(): Flow<Favorite> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(Favorite()) // Trả về rỗng nếu chưa đăng nhập
            close()
            return@callbackFlow
        }

        val docRef = db.collection(collectionName).document(userId)

        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FavRepo", "Lỗi lắng nghe: ${error.message}")
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                // Nếu document đã tồn tại -> Convert sang object
                val favDoc = snapshot.toObject<Favorite>() ?: Favorite(userId)
                trySend(favDoc)
            } else {
                // Nếu chưa tồn tại -> Trả về object rỗng (nhưng có userId)
                trySend(Favorite(userId))
            }
        }

        awaitClose { listener.remove() }
    }

    fun toggleFoodFavorite(foodId: String, isCurrentlyFavorite: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        val docRef = db.collection(collectionName).document(userId)

        // Dùng transaction hoặc check đơn giản để đảm bảo document tồn tại
        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)

            if (!snapshot.exists()) {
                // A. Nếu chưa có -> Tạo mới document với món ăn này
                val newDoc = Favorite(
                    userId = userId,
                    foodIds = listOf(foodId),
                    storeIds = emptyList()
                )
                transaction.set(docRef, newDoc)
            } else {
                // B. Nếu đã có -> Update mảng
                if (isCurrentlyFavorite) {
                    // Đang thích -> Bấm cái nữa là Xóa (Remove)
                    transaction.update(docRef, "foodIds", FieldValue.arrayRemove(foodId))
                } else {
                    // Chưa thích -> Bấm là Thêm (Union)
                    transaction.update(docRef, "foodIds", FieldValue.arrayUnion(foodId))
                }
            }
        }.addOnFailureListener { e ->
            Log.e("FavRepo", "Lỗi update favorite: ${e.message}")
        }
    }

    fun toggleStoreFavorite(storeId: String, isCurrentlyFavorite: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        val docRef = db.collection("favorites").document(userId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            if (!snapshot.exists()) {
                // Tạo mới nếu chưa có
                val newDoc = Favorite(userId = userId, storeIds = listOf(storeId))
                transaction.set(docRef, newDoc)
            } else {
                // Update mảng storeIds
                if (isCurrentlyFavorite) {
                    transaction.update(docRef, "storeIds", FieldValue.arrayRemove(storeId))
                } else {
                    transaction.update(docRef, "storeIds", FieldValue.arrayUnion(storeId))
                }
            }
        }.addOnFailureListener { e ->
            Log.e("FavRepo", "Lỗi update store favorite: ${e.message}")
        }
    }

    suspend fun getFavoriteFoodsDetails(foodIds: List<String>): List<Food> {
        if (foodIds.isEmpty()) return emptyList()

        return try {
            val foods = mutableListOf<Food>()

            foodIds.chunked(10).forEach { chunk ->
                val snapshot = db.collection("foods")
                    .whereIn(FieldPath.documentId(), chunk) // Tìm các món có ID nằm trong danh sách này
                    .get()
                    .await()

                val chunkFoods = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Food::class.java)?.copy(id = doc.id)
                }
                foods.addAll(chunkFoods)
            }

            foods
        } catch (e: Exception) {
            Log.e("FavRepo", "Lỗi lấy chi tiết món yêu thích: ${e.message}")
            emptyList()
        }
    }

    suspend fun getFavoriteStoresDetails(storeIds: List<String>): List<Store> {
        if (storeIds.isEmpty()) return emptyList()

        return try {
            val stores = mutableListOf<Store>()

            storeIds.chunked(10).forEach { chunk ->
                val snapshot = db.collection("stores")
                    .whereIn(FieldPath.documentId(), chunk) // Tìm các món có ID nằm trong danh sách này
                    .get()
                    .await()

                val chunkStores = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Store::class.java)?.copy(id = doc.id)
                }
                stores.addAll(chunkStores)
            }

            stores
        } catch (e: Exception) {
            Log.e("FavRepo", "Lỗi lấy chi tiết quán yêu thích: ${e.message}")
            emptyList()
        }
    }
}