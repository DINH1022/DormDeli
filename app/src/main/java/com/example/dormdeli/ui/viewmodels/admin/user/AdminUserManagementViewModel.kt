package com.example.dormdeli.ui.viewmodels.admin.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormdeli.model.User
import com.example.dormdeli.repository.admin.AdminOrderRepository
import com.example.dormdeli.repository.admin.AdminUserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UserUIState(
    val user: User,
    val orderCount: Int = 0,
    val totalSpent: Long = 0L
)

enum class UserStatusFilter {
    ALL,        // Tất cả
    ACTIVE,     // Đang hoạt động
    LOCKED      // Bị khóa
}

class AdminUserManagementViewModel(
    private val userRepository: AdminUserRepository = AdminUserRepository(),
    private val orderRepository: AdminOrderRepository = AdminOrderRepository()
) : ViewModel() {

    private val _searchTerm = MutableStateFlow("")
    val searchTerm = _searchTerm.asStateFlow()

    private val _statusFilter = MutableStateFlow(UserStatusFilter.ALL)
    val statusFilter = _statusFilter.asStateFlow()

    private val _userList = MutableStateFlow<List<UserUIState>>(emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent = _errorEvent.asSharedFlow()

    val displayUsers = combine(_userList, _searchTerm, _statusFilter) { list, query, filter ->
        list.filter { userState ->
            val matchesSearch = userState.user.fullName.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                UserStatusFilter.ALL -> true
                UserStatusFilter.ACTIVE -> userState.user.active
                UserStatusFilter.LOCKED -> !userState.user.active
            }
            matchesSearch && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchTermChange(newTerm: String) {
        _searchTerm.value = newTerm
    }

    fun onStatusFilterChange(filter: UserStatusFilter) {
        _statusFilter.value = filter
    }

    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val pureUsers = userRepository.getPureUsers()
                val uiStates = pureUsers.map { user ->
                    val stats = orderRepository.getUserSpendingStats(user.uid)
                    UserUIState(user, stats.first, stats.second)
                }
                _userList.value = uiStates
            } catch (e: Exception) {
                _userList.value = emptyList()
                _errorEvent.emit("Lỗi tải dữ liệu: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleUserStatus(userState: UserUIState) {
        viewModelScope.launch {
            userRepository.updateUserStatus(userState.user.uid, !userState.user.active)
            loadUsers()
        }
    }
}