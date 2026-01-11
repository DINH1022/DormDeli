package com.example.dormdeli.ui.screens.admin.features.noti

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dormdeli.repository.admin.AdminNotiRepository
import com.example.dormdeli.ui.viewmodels.admin.noti.AdminCreateNotiViewModel
import com.example.dormdeli.ui.viewmodels.admin.noti.AdminNotiListViewModel

class AdminNotiViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = AdminNotiRepository(context.applicationContext)

        return when {
            modelClass.isAssignableFrom(AdminCreateNotiViewModel::class.java) -> {
                AdminCreateNotiViewModel(repository) as T
            }
            modelClass.isAssignableFrom(AdminNotiListViewModel::class.java) -> {
                AdminNotiListViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
