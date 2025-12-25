package com.example.dormdeli.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.dormdeli.model.CartItem
import com.example.dormdeli.ui.viewmodels.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBasketScreen(
    cartViewModel: CartViewModel,
    onBackClick: () -> Unit
) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val totalPrice = cartItems.sumOf { it.food.price * it.quantity }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Basket") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format("Total: %.2f VND", totalPrice.toDouble()),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Button(onClick = { /* TODO: Implement place order */ }) {
                            Text("Place Order")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            if (cartItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Your basket is empty")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(cartItems) { cartItem ->
                        CartListItem(cartItem, cartViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun CartListItem(cartItem: CartItem, viewModel: CartViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(cartItem.food.imageUrl),
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(cartItem.food.name, fontWeight = FontWeight.Bold)
            Text(String.format("%.2f VND", cartItem.food.price.toDouble()))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.updateQuantity(cartItem, cartItem.quantity - 1) }) {
                    Text("-")
                }
                Text(cartItem.quantity.toString())
                IconButton(onClick = { viewModel.updateQuantity(cartItem, cartItem.quantity + 1) }) {
                    Text("+")
                }
            }
        }
        IconButton(onClick = { viewModel.removeFromCart(cartItem) }) {
            Icon(Icons.Default.Delete, contentDescription = "Remove")
        }
    }
}