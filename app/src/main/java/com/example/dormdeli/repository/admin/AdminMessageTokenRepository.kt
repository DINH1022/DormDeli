package com.example.dormdeli.repository.admin

import com.example.dormdeli.enums.NotificationTarget
import com.example.dormdeli.firestore.CollectionName
import com.example.dormdeli.firestore.ModelFields
import com.example.dormdeli.model.MessageToken
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.FileInputStream
import java.util.Collections

class AdminMessageTokenRepository {
    private val db = FirebaseFirestore.getInstance()
    private val msgTokenCol = db.collection(CollectionName.MESSAGE_TOKEN.value)

    suspend fun getMessageTokensOfRole(target: NotificationTarget): List<String> {
        return try {
            val query = if (target == NotificationTarget.ALL || target == NotificationTarget.EVERYONE) {
                msgTokenCol.get().await()
            } else {
                msgTokenCol.whereEqualTo(ModelFields.MessageToken.ROLE, target.value).get().await()
            }

            query.documents.mapNotNull { doc ->
                doc.toObject(MessageToken::class.java)?.fcmToken
            }.distinct()
        } catch (e: Exception) {
            emptyList()
        }
    }



}