package com.example.dormdeli.utils

import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlin.collections.get

object CloudinaryHelper {

    fun uploadImage(
        uri: Uri,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        MediaManager.get().upload(uri)
            .unsigned("dormdeli_upload") // Thay tên Preset bạn tạo ở Bước 1 vào đây
            .option("folder", "dormdeli_foods") // (Tùy chọn) Lưu vào thư mục riêng
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    Log.d("Cloudinary", "Bắt đầu upload...")
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    // Có thể tính % để hiện thanh loading
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    // Upload xong, Cloudinary trả về link ảnh (secure_url)
                    val imageUrl = resultData["secure_url"].toString()
                    Log.d("Cloudinary", "Link ảnh: $imageUrl")
                    onSuccess(imageUrl)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    Log.e("Cloudinary", "Lỗi: ${error.description}")
                    onError(error.description)
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }
}