package com.example.dormdeli.repository.admin

import android.content.Context
import android.util.Log
import com.example.dormdeli.enums.NotificationTarget
import com.example.dormdeli.firestore.CollectionName
import com.example.dormdeli.firestore.ModelFields
import com.example.dormdeli.model.Notification
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Query.Direction.DESCENDING
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Collections

class AdminNotiRepository(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val notiCol = db.collection(CollectionName.NOTIFICATION.value)
    private val PROJECT_ID = "dormdeli"
    private val FCM_URL = "https://fcm.googleapis.com/v1/projects/$PROJECT_ID/messages:send"
    private val adminMessageTokenRepository= AdminMessageTokenRepository()

    suspend fun createNotification(notification: Notification): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val docRef = notiCol.document()
                val finalNoti = notification.copy(id = docRef.id)
                docRef.set(finalNoti).await()
                val target= NotificationTarget.fromString(notification.target)
                val tokens = adminMessageTokenRepository.getMessageTokensOfRole(target)
                tokens.forEach { token ->
                    sendFCMToSingleToken(token, finalNoti)
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun getAccessToken(): String {
        // Đọc từ thư mục assets
        val serviceAccountStream = context.assets.open("credentials.json")
        val credentials = GoogleCredentials
            .fromStream(serviceAccountStream)
            .createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"))

        credentials.refreshIfExpired()
        return credentials.accessToken.tokenValue
    }

    private fun sendFCMToSingleToken(deviceToken: String, notification: Notification) {
        val accessToken=getAccessToken()
        val client = OkHttpClient()

        // Tạo cấu trúc JSON cho FCM HTTP v1
        val root = JSONObject()
        val message = JSONObject()
        val notiJson = JSONObject()
        val dataJson = JSONObject()

        notiJson.put("title", notification.subject)
        notiJson.put("body", notification.message)

        dataJson.put("notificationId", notification.id)
        dataJson.put("type", "system_notification")

        message.put("token", deviceToken)
        message.put("notification", notiJson)
        message.put("data", dataJson)

        root.put("message", message)

        val body = root.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(FCM_URL)
            .post(body)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Log.d("FCM_SUCCESS", "Gửi thành công cho: $deviceToken")
                } else {
                    Log.e("FCM_ERROR", "Lỗi: ${response.code} - ${response.body?.string()}")
                }
            }
        } catch (e: Exception) {
            Log.e("FCM_ERROR", "Exception: ${e.message}")
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