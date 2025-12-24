package com.example.dormdeli.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun RestaurantCard(
    name: String,
    tags: String,
    rating: Double,
    deliveryFee: String,
    deliveryTime: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(Color.Gray)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(verticalArrangement = Arrangement.SpaceBetween) {
                Text(text = name)
                Text(text = tags)
                Row {
                    Text(text = rating.toString())
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = deliveryFee)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = deliveryTime)
                }
            }
        }
    }
}
