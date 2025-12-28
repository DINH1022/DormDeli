package com.example.dormdeli.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dormdeli.model.Food
import com.example.dormdeli.ui.components.FoodItem
import com.example.dormdeli.ui.components.HomeSearchBar
import com.example.dormdeli.ui.viewmodels.customer.FavoriteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    favoriteViewModel: FavoriteViewModel,
    onBackClick: () -> Unit,
    onFoodClick: (String) -> Unit
) {
    val favoriteItems by favoriteViewModel.favoriteItems.collectAsState()
    var searchText by remember { mutableStateOf("") }

    val filteredFavorites by remember(searchText, favoriteItems) {
        derivedStateOf {
            if (searchText.isBlank()) {
                favoriteItems
            } else {
                favoriteItems.filter {
                    it.name.contains(searchText, ignoreCase = true) ||
                    it.description.contains(searchText, ignoreCase = true)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Liked") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                HomeSearchBar(
                    value = searchText,
                    onValueChange = { searchText = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredFavorites.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (searchText.isBlank()) "Empty" else "Not Found")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredFavorites) { food ->
                        FoodItem(
                            food = food,
                            onImageClick = onFoodClick
                        )
                    }
                }
            }
        }
    }
}