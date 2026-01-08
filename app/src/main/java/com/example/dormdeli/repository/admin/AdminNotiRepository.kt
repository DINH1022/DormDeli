package com.example.dormdeli.repository.admin

import com.example.dormdeli.firestore.CollectionName
import com.example.dormdeli.firestore.ModelFields
import com.example.dormdeli.model.Notification
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Query.Direction.DESCENDING

class AdminNotiRepository {
    private val db = FirebaseFirestore.getInstance()
    private val notiCol = db.collection(CollectionName.NOTIFICATION.value)

    suspend fun createNotification(notification: Notification): Result<Unit> {
        return try {
            val docRef = notiCol.document()
            val finalNoti = notification.copy(id = docRef.id)
            docRef.set(finalNoti).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllNotifications(): List<Notification> {
        return try {
            notiCol.orderBy(ModelFields.Notification.CREATED_AT, DESCENDING)
                .get().await().toObjects(Notification::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun deleteNotification(id: String): Result<Unit> {
        return try {
            notiCol.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNotificationCount(): Int {
        return try {
            val snapshot = notiCol.get().await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }
}