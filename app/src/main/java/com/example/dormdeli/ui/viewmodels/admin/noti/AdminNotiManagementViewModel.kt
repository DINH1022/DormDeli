package com.example.dormdeli.ui.viewmodels.admin.noti


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object NotiTabs {
    const val CREATE = "Create Notification"
    const val SHOW_LIST = "Show notification"
}

class AdminNotiManagementViewModel: ViewModel() {
    val tabs = listOf(NotiTabs.CREATE, NotiTabs.SHOW_LIST)

    private val _selectedTab = MutableStateFlow(NotiTabs.CREATE)
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    fun selectTab(tab: String) {
        _selectedTab.value = tab
    }
}