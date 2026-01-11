package com.example.dormdeli.ui.viewmodels.admin.noti

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.enums.NotificationTarget
import com.example.dormdeli.model.Notification
import com.example.dormdeli.repository.admin.AdminNotiRepository
import kotlinx.coroutines.launch


class AdminNotiListViewModel(
    private val notiRepo: AdminNotiRepository
) : ViewModel() {
    var allNotifications = mutableStateOf<List<Notification>>(emptyList())
    var totalCount = mutableStateOf(0)
    var filteredNotifications = mutableStateOf<List<Notification>>(emptyList())

    var searchQuery = mutableStateOf("")
    var selectedTarget = mutableStateOf("ALL")

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            val list = notiRepo.getAllNotifications()
            allNotifications.value = list
            totalCount.value = list.size
            applyFilterAndSearch()
        }
    }

    fun applyFilterAndSearch() {
        val currentQuery = searchQuery.value.trim().lowercase()
        val currentTarget = selectedTarget.value

        // 1. Thực hiện lọc danh sách
        val filteredList = allNotifications.value.filter { noti ->
            val matchesSearch = noti.subject.lowercase().contains(currentQuery) ||
                    noti.message.lowercase().contains(currentQuery)

            val matchesTarget = if (currentTarget == NotificationTarget.ALL.value) {
                true
            } else {
                noti.target == currentTarget
            }

            matchesSearch && matchesTarget
        }
        filteredNotifications.value = filteredList
        totalCount.value = filteredList.size
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            notiRepo.deleteNotification(id).onSuccess { loadNotifications() }
        }
    }
}