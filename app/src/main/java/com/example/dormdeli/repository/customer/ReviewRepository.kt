package com.example.dormdeli.repository.customer

import android.util.Log
import com.example.dormdeli.model.Food
import com.example.dormdeli.model.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ReviewRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val reviewCollection = db.collection("reviews")
    private val foodCollection = db.collection("foods")

    suspend fun submitReview(
        foodId: String,
        rating: Int,
        comment: String
    ): Boolean {
        val currentUser = auth.currentUser ?: return false
        val reviewId = UUID.randomUUID().toString()

        return try {
            db.runTransaction { transaction ->
                val foodRef = foodCollection.document(foodId)
                val foodSnapshot = transaction.get(foodRef)
                val currentFood = foodSnapshot.toObject(Food::class.java)
                    ?: throw Exception("Food not found")

                val userRef = db.collection("users").document(currentUser.uid)
                val userSnapshot = transaction.get(userRef)
                val dbAvatarUrl = userSnapshot.getString("avatarUrl")

                val reviewData = hashMapOf(
                    "userId" to currentUser.uid,
                    "userName" to (currentUser.displayName ?: "Anonymous"),
                    "userAvatarUrl" to (dbAvatarUrl?: ""),
                    "storeId" to currentFood.storeId,
                    "foodId" to foodId,
                    "rating" to rating,
                    "comment" to comment,
                    "createdAt" to System.currentTimeMillis()
                )

                // 3. Tính toán Rating mới cho Food
                val currentCount = currentFood.ratingCount
                val currentAvg = currentFood.ratingAvg

                val newCount = currentCount + 1
                // Công thức: ((Trung bình cũ * Số lượng cũ) + Điểm mới) / Số lượng mới
                val newAvg = ((currentAvg * currentCount) + rating) / newCount

                // 4. Thực hiện lưu và cập nhật
                val reviewRef = reviewCollection.document(reviewId)
                transaction.set(reviewRef, reviewData)

                transaction.update(foodRef,
                    "ratingAvg", newAvg,
                    "ratingCount", newCount
                )
            }.await()

            true
        } catch (e: Exception) {
            Log.e("ReviewRepo", "Lỗi submit review: ${e.message}")
            false
        }
    }
    suspend fun getReviewsByFoodId(foodId: String): List<Review> {
        return try {
            val snapshot = reviewCollection
                .whereEqualTo("foodId", foodId)
                .orderBy("createdAt", Query.Direction.DESCENDING) // Mới nhất lên đầu
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Review::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("ReviewRepo", "Lỗi lấy reviews: ${e.message}")
            emptyList()
        }
    }
}