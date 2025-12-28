package com.example.dormdeli.repository.admin

import com.example.dormdeli.firestore.CollectionName
import com.example.dormdeli.firestore.ModelFields
import com.example.dormdeli.repository.admin.dataclass.TopStoreRevenue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class AdminOrderRepository {
    private val db = FirebaseFirestore.getInstance()
    private val orderCol = db.collection(CollectionName.ORDERS.value)
    private val storeCol = db.collection(CollectionName.STORES.value)

    suspend fun getTopStoresByRevenue(limit: Int): List<TopStoreRevenue> {
        val ordersSnapshot = orderCol
            .whereEqualTo("status", "completed")
            .get()
            .await()

        val revenueMap = mutableMapOf<String, Pair<Long, Int>>()

        for (doc in ordersSnapshot.documents) {
            val storeId = doc.getString("storeId") ?: continue
            val price = doc.getLong("totalPrice") ?: 0

            val current = revenueMap[storeId]
            if (current == null) {
                revenueMap[storeId] = price to 1
            } else {
                revenueMap[storeId] =
                    (current.first + price) to (current.second + 1)
            }
        }

        val sorted = revenueMap.entries
            .sortedByDescending { it.value.first }
            .take(limit)

        val result = mutableListOf<TopStoreRevenue>()

        for (entry in sorted) {
            val storeDoc = storeCol.document(entry.key).get().await()
            val storeName = storeDoc.getString("name") ?: "Unknown"

            result.add(
                TopStoreRevenue(
                    storeId = entry.key,
                    storeName = storeName,
                    totalRevenue = entry.value.first,
                    totalOrders = entry.value.second
                )
            )
        }

        return result
    }

    suspend fun getWeeklyRevenue(): List<Long> {
        val calendar = Calendar.getInstance()

        // Đưa về đầu tuần (Thứ 2)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startOfWeek = calendar.timeInMillis
        val today = System.currentTimeMillis()

        val revenuePerDay = MutableList(7) { 0L }

        // Lấy tất cả orders từ đầu tuần, filter trong code thay vì query phức tạp
        val orders = orderCol
            .whereGreaterThanOrEqualTo("createdAt", startOfWeek)
            .get()
            .await()

        for (doc in orders.documents) {
            val createdAt = doc.getLong("createdAt") ?: continue
            val status = doc.getString("status") ?: continue
            val price = doc.getLong("totalPrice") ?: 0

            // Chỉ tính đơn đã hoàn thành
            if (status != "completed") continue
            if (createdAt > today) continue

            val diffDay = ((createdAt - startOfWeek) / (1000 * 60 * 60 * 24)).toInt()

            if (diffDay in 0..6) {
                revenuePerDay[diffDay] += price
            }
        }

        return revenuePerDay
    }

    suspend fun getWeeklyOrderCount(): List<Int> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startOfWeek = calendar.timeInMillis
        val today = System.currentTimeMillis()

        val ordersPerDay = MutableList(7) { 0 }

        // Query đơn giản hơn, không cần composite index
        val orders = orderCol
            .whereGreaterThanOrEqualTo("createdAt", startOfWeek)
            .get()
            .await()

        for (doc in orders.documents) {
            val createdAt = doc.getLong("createdAt") ?: continue
            if (createdAt > today) continue

            val diffDay = ((createdAt - startOfWeek) / (1000 * 60 * 60 * 24)).toInt()

            if (diffDay in 0..6) {
                ordersPerDay[diffDay]++
            }
        }

        return ordersPerDay
    }

    suspend fun getUserSpendingStats(uid: String): Pair<Int, Long> {
        val orders = orderCol
            .whereEqualTo(ModelFields.Order.USER_ID, uid)
            .get()
            .await()

        val count = orders.size()
        val totalSpent = orders.documents.sumOf { it.getLong("totalPrice") ?: 0L }
        return count to totalSpent
    }

    suspend fun countStoreOrdersByStoreId(storeId: String): Int {
        return try {
            val snapshot = orderCol
                .whereEqualTo(ModelFields.Order.STORE_ID, storeId)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }
}