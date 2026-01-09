package com.example.dormdeli.repository.admin

import com.example.dormdeli.enums.OrderStatus
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
            .whereEqualTo(ModelFields.Order.STATUS, OrderStatus.COMPLETED.value)
            .get()
            .await()

        val revenueMap = mutableMapOf<String, Pair<Long, Int>>()

        for (doc in ordersSnapshot.documents) {
            val storeId = doc.getString(ModelFields.Order.STORE_ID) ?: continue
            val price = doc.getLong(ModelFields.Order.TOTAL_PRICE) ?: 0

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
        println("Found ${result.size} top stores")
        return result
    }

    private fun getStartOfCurrentWeek(): Long {
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        calendar.firstDayOfWeek = Calendar.MONDAY

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        if (calendar.timeInMillis > System.currentTimeMillis()) {
            calendar.add(Calendar.WEEK_OF_YEAR, -1)
        }

        return calendar.timeInMillis
    }

    suspend fun getWeeklyRevenue(): List<Long> {

        val startOfWeek = getStartOfCurrentWeek()
        val today = System.currentTimeMillis()

        val revenuePerDay = MutableList(7) { 0L }

        val orders = orderCol
            .whereGreaterThanOrEqualTo(ModelFields.Order.CREATED_AT, startOfWeek)
            .get()
            .await()
        println("Found ${orders.size()} orders weekly revenue this week")
        for (doc in orders.documents) {
            val createdAt = doc.getLong(ModelFields.Order.CREATED_AT) ?: continue
            val status = doc.getString(ModelFields.Order.STATUS) ?: continue
            val price = doc.getLong(ModelFields.Order.TOTAL_PRICE) ?: 0

            if (status != OrderStatus.COMPLETED.value) continue
            if (createdAt > today) continue

            val diffDay = ((createdAt - startOfWeek) / (1000 * 60 * 60 * 24)).toInt()

            if (diffDay in 0..6) {
                revenuePerDay[diffDay] += price
            }
        }
        println("Found ${revenuePerDay.size} revenue days")
        return revenuePerDay
    }

    suspend fun getWeeklyOrderCount(): List<Int> {
        val startOfWeek = getStartOfCurrentWeek()
        val today = System.currentTimeMillis()
        println("Start of the week ${startOfWeek}")
        println("Today time ${today}")
        val ordersPerDay = MutableList(7) { 0 }

        val orders = orderCol
            .whereGreaterThanOrEqualTo(ModelFields.Order.CREATED_AT, startOfWeek)
            .get()
            .await()
        println("Number of orders in this week ${orders.size()}")
        for (doc in orders.documents) {
            val createdAt = doc.getLong(ModelFields.Order.CREATED_AT) ?: continue
            val status = doc.getString(ModelFields.Order.STATUS) ?: continue
            if (createdAt > today) continue
            if (status != OrderStatus.COMPLETED.value) continue

            val diffDay = ((createdAt - startOfWeek) / (1000 * 60 * 60 * 24)).toInt()

            if (diffDay in 0..6) {
                ordersPerDay[diffDay]++
            }
        }
        println("Total orders in week ${ordersPerDay.sum()}")
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