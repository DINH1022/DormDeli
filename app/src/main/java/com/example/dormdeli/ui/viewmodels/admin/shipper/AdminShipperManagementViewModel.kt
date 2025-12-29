package com.example.dormdeli.ui.viewmodels.admin.shipper

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ShipperTabs {
    const val PENDING = "Pending Shippers"
    const val APPROVED = "Approved Shippers"
}

class AdminShipperManagementViewModel : ViewModel() {

    val tabs = listOf(ShipperTabs.PENDING, ShipperTabs.APPROVED)

    private val _selectedTab = MutableStateFlow(ShipperTabs.PENDING)
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    fun selectTab(tab: String) {
        _selectedTab.value = tab
    }
}