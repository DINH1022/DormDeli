package com.example.dormdeli.ui.viewmodels.admin.store

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object StoreTabs {
    const val PENDING = "Pending Stores"
    const val APPROVED = "Approved Stores"
}
class AdminStoreManagementViewModel: ViewModel() {
    val tabs = listOf(StoreTabs.PENDING, StoreTabs.APPROVED)
    private val _selectedTab = MutableStateFlow(StoreTabs.PENDING)
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    fun selectTab(tab: String) {
        _selectedTab.value = tab
    }
}