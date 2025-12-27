package com.example.dormdeli.ui.viewmodels.admin

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.dormdeli.enums.AdminFeature

class AdminViewModel : ViewModel() {
    private val _features = mutableStateOf(AdminFeature.entries)
    val features: State<List<AdminFeature>> = _features

    private val _selectedFeature = mutableStateOf<AdminFeature?>(_features.value.first())
    val selectedFeature: State<AdminFeature?> = _selectedFeature

    fun selectFeature(feature: AdminFeature) {
        _selectedFeature.value = feature
    }
}