package com.example.dormdeli.ui.seller.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.dormdeli.R
import com.example.dormdeli.model.Food
import com.example.dormdeli.ui.seller.viewmodels.SellerViewModel
import com.example.dormdeli.ui.theme.OrangePrimary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MenuManagementScreen(sellerViewModel: SellerViewModel = viewModel(), onNavigateToAddEdit: () -> Unit) {
    val foods by sellerViewModel.foods.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF8F9FA), // Nền xám hiện đại
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    sellerViewModel.onAddNewFoodClick()
                    onNavigateToAddEdit()
                },
                shape = RoundedCornerShape(16.dp), // Bo góc vuông mềm thay vì tròn hẳn
                containerColor = OrangePrimary, // Đổi màu cam
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add new item")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Text(
                    text = "Quản lý thực đơn",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                    color = Color(0xFF1F1F1F)
                )
            }

            items(foods, key = { it.id }) { item ->
                FoodItemRow(
                    item = item,
                    onEditClick = {
                        sellerViewModel.onEditFoodClick(item)
                        onNavigateToAddEdit()
                    },
                    onDeleteClick = { sellerViewModel.deleteFood(item) }
                )
            }

            item { Spacer(modifier = Modifier.height(60.dp)) } // Spacer để không bị FAB che mất item cuối
        }
    }
}

@Composable
fun FoodItemRow(item: Food, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp) // Flat design
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = item.imageUrl.ifEmpty { R.drawable.ic_launcher_background }, // Placeholder handling if empty
                    placeholder = painterResource(id = R.drawable.ic_launcher_background),
                    error = painterResource(id = R.drawable.ic_launcher_background)
                ),
                contentDescription = item.name,
                modifier = Modifier
                    .size(80.dp) // Hình to hơn xíu
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1F1F1F))
                Text(
                    text = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(item.price),
                    fontSize = 15.sp,
                    color = OrangePrimary, // Đổi màu cam
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Badge trạng thái
                Surface(
                    color = if (item.available) Color(0xFFE6F4EA) else Color(0xFFFCE8E6),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (item.available) "Đang bán" else "Hết hàng",
                        color = if (item.available) Color(0xFF34A853) else Color(0xFFEA4335),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Column {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray)
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEA4335))
                }
            }
        }
    }
}