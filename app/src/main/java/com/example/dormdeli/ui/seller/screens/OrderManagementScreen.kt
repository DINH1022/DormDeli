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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class Order(val id: String, val customerId: String, val total: Double, val status: String, val date: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderManagementScreen() {
    val orders = listOf(
        Order("#user12", "user12", 25.0, "Pending", "29/12/2025 21:03"),
        Order("#user45", "user45", 40.0, "Pending", "29/12/2025 21:03")
    )
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Pending", "In Progress", "Completed")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Management", fontWeight = FontWeight.Bold) }
            )
        }
    ) {
        Column(modifier = Modifier.padding(it)) {
            TabRow(selectedTabIndex = tabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(text = { Text(title) },
                        selected = tabIndex == index,
                        onClick = { tabIndex = index })
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(orders.filter { order ->
                    when (tabIndex) {
                        0 -> order.status == "Pending"
                        1 -> order.status == "In Progress"
                        2 -> order.status == "Completed"
                        else -> false
                    }
                }) { order ->
                    OrderCard(order = order)
                }
            }
        }
    }
}

@Composable
fun OrderCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Order ${order.id}", fontWeight = FontWeight.Bold)
            Text("Customer ID: ${order.customerId}")
            Text("Total: $${order.total}")
            Text("Status: ${order.status}", color = if(order.status == "Pending") Color.Red else Color.Green)
            Text("Ordered on: ${order.date}")
            Spacer(modifier = Modifier.padding(8.dp))
            Row {
                Button(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f)) {
                    Text("Accept")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f)) {
                    Text("Decline")
                }
            }
        }
    }
}

