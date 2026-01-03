package com.example.dormdeli.ui.seller.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dormdeli.ui.seller.model.MenuItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuManagementScreen() {
    // Sample data
    val menuItems = listOf(
        MenuItem("1", "Classic Burger", 10.0, true),
        MenuItem("2", "Pizza Margherita", 12.0, true),
        MenuItem("3", "Caesar Salad", 8.0, false),
        MenuItem("4", "French Fries", 5.0, true)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menu Management", fontWeight = FontWeight.Bold) },
                actions = {
                    Button(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Autofill")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AI Autofill")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO */ },
                shape = CircleShape,
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(menuItems) { item ->
                MenuItemRow(item = item)
            }
        }
    }
}

@Composable
fun MenuItemRow(item: MenuItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("$${item.price}", fontSize = 14.sp, color = Color.Gray)
                Text(
                    text = if (item.isAvailable) "Available" else "Unavailable",
                    color = if (item.isAvailable) Color(0xFF34A853) else Color.Red,
                    fontSize = 12.sp
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(onClick = { /* TODO: Edit item */ }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit item")
                }
                IconButton(onClick = { /* TODO: Delete item */ }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete item")
                }
            }
        }
    }
}
