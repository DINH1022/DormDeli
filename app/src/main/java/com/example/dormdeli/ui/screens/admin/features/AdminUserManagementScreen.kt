package com.example.dormdeli.ui.screens.admin.features


// Import các màu sắc bạn đã định nghĩa

// Import ViewModel và Model
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dormdeli.ui.theme.CardBackground
import com.example.dormdeli.ui.theme.CardBorder
import com.example.dormdeli.ui.theme.Green
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.theme.Purple40
import com.example.dormdeli.ui.theme.Red
import com.example.dormdeli.ui.theme.TextSecondary
import com.example.dormdeli.ui.viewmodels.admin.AdminUserManagementViewModel
import com.example.dormdeli.ui.viewmodels.admin.UserUIState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManagementScreen(
    viewModel: AdminUserManagementViewModel = viewModel()
) {
    val users by viewModel.displayUsers.collectAsState()
    val searchTerm by viewModel.searchTerm.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchTerm,
            onValueChange = { viewModel.onSearchTermChange(it) }, // Đã sửa tên hàm
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Tìm kiếm khách hàng...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangePrimary,
                unfocusedBorderColor = CardBorder,
                cursorColor = OrangePrimary,
                focusedLeadingIconColor = OrangePrimary
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(users) { userState ->
                UserItemCard(
                    userState = userState,
                    onToggleStatus = { viewModel.toggleUserStatus(userState) }
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }
}

@Composable
fun UserItemCard(
    userState: UserUIState,
    onToggleStatus: () -> Unit
) {
    val user = userState.user

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Placeholder
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Purple40.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Purple40)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.fullName,
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // Status Tag
                    StatusTag(isActive = user.active)
                }

                Text(text = user.email, color = TextSecondary, fontSize = 13.sp)

                Text(
                    text = "Khách hàng • ${userState.orderCount} đơn hàng",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Lock/Unlock Button
            IconButton(onClick = onToggleStatus) {
                Icon(
                    imageVector = if (user.active) Icons.Default.LockOpen else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (user.active) Green else Red
                )
            }
        }
    }
}

@Composable
fun StatusTag(isActive: Boolean) {
    val bgColor = if (isActive) Green.copy(alpha = 0.15f) else Red.copy(alpha = 0.15f)
    val textColor = if (isActive) Green else Red
    val label = if (isActive) "Hoạt động" else "Bị khóa"

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = TextStyle(color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        )
    }
}