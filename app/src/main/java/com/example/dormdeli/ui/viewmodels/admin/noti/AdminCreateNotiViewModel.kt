package com.example.dormdeli.ui.viewmodels.admin.noti

import androidx.lifecycle.ViewModel
import com.example.dormdeli.repository.admin.AdminNotiRepository
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

import com.example.dormdeli.model.Notification
import com.example.dormdeli.enums.NotificationTarget


class AdminCreateNotiViewModel(
    private val notiRepo: AdminNotiRepository
) : ViewModel() {

    var subject by mutableStateOf("")
    var message by mutableStateOf("")
    var target by mutableStateOf(NotificationTarget.EVERYONE)

    var isLoading by mutableStateOf(false)
    var showSuccess by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun sendNotification() {
        if (subject.isBlank() || message.isBlank()) {
            errorMessage = "Vui lòng nhập đầy đủ tiêu đề và nội dung"
            return
        }

        viewModelScope.launch {
            isLoading = true
            val newNoti = Notification(
                target = target.name,
                subject = subject,
                message = message
            )

            notiRepo.createNotification(newNoti).onSuccess {
                showSuccess = true
                clearForm()
            }.onFailure {
                errorMessage = "Gửi thất bại: ${it.localizedMessage}"
            }
            isLoading = false
        }
    }

    private fun clearForm() {
        subject = ""
        message = ""
        target = NotificationTarget.EVERYONE
    }
}