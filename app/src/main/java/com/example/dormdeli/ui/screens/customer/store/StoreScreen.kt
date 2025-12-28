package com.example.dormdeli.ui.screens.customer.store

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.dormdeli.ui.components.*
import com.example.dormdeli.ui.theme.CardBackground
import com.example.dormdeli.ui.theme.CardBorder
import com.example.dormdeli.ui.theme.Green
import com.example.dormdeli.ui.theme.Red
import com.example.dormdeli.ui.theme.TextSecondary
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dormdeli.model.Food
import com.example.dormdeli.ui.viewmodels.customer.StoreViewModel


@Composable
fun StoreScreen(
    storeId: String,
    viewModel: StoreViewModel = viewModel(),
    onBack: () -> Unit, onMenuClick: () -> Unit,
    onFoodClick: (String) -> Unit
) {
    val store by viewModel.store
    val categories = viewModel.categories()
    val selectedCategory by viewModel.selectedCategory
    val foods by viewModel.filteredFoods
    val isLoading by viewModel.isLoading

    LaunchedEffect(storeId) {
        if (store == null && storeId.isNotBlank()) {
            viewModel.loadStore(storeId)
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (store != null) {
        val storeData = store!!
        Column(modifier = Modifier.fillMaxSize()) {
            StoreNavBar(onBack = onBack, onMenuClick = onMenuClick)
            
            // Store Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Color(0xFFFFE5D0))
            ) {
                Image(
                    painter = rememberAsyncImagePainter(storeData.imageUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = CardBorder
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    // Tên nhà hàng
                    Text(
                        text = storeData.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Open time",
                                tint = Green,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = storeData.openTime)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Close time",
                                tint = Red,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = storeData.closeTime)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = storeData.description,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }


            Spacer(modifier = Modifier.height(4.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(categories.size) { index ->
                    CategoryChip (
                        text = categories[index],
                        isSelected = categories[index] == selectedCategory,
                        onClick = {
                            viewModel.selectCategory(categories[index])
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(foods.size) { index ->
                    FoodItem(
                        food = foods[index],
                        onImageClick = {food ->
                            onFoodClick(food)
                        }
                    )
                }

            }
        }
    } else {
        // Show empty state or error message
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Store not found")
                Spacer(modifier = Modifier.height(16.dp))
                Text("Store ID: $storeId", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}