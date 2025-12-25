package com.example.dormdeli.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.dormdeli.model.Food
import com.example.dormdeli.ui.theme.OrangePrimary

@Composable
fun FoodDetailScreen(
    food: Food,
    onBackClick: () -> Unit,
    onAddToCart: (Int) -> Unit,
    onSeeReviewsClick: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    // Unused parameters kept for nav consistency
    foodId: String,
    onBack: () -> Unit,
    onSeeReviews: () -> Unit
) {
    var quantity by remember { mutableStateOf(1) }
    val context = LocalContext.current

    // State for additional options
    val options = remember {
        listOf(
            SelectableOption("Add Cheese", 0.50, OptionType.CHECKBOX),
            SelectableOption("Add Bacon", 1.00, OptionType.CHECKBOX),
            SelectableOption("Add Meat", 1.50, OptionType.CHECKBOX)
        )
    }
    val selectedOptions = remember { mutableStateListOf<SelectableOption>() }

    val totalOptionPrice = selectedOptions.sumOf { it.price }
    val finalPrice = (food.price + totalOptionPrice) * quantity

    Scaffold(
        bottomBar = {
            Surface(
                modifier = Modifier.padding(8.dp),
                shape = RoundedCornerShape(32.dp),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (quantity > 1) quantity-- }) {
                           Text("-", fontSize = 24.sp)
                        }
                        Text(quantity.toString(), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = { quantity++ }) {
                            Text("+", fontSize = 24.sp)
                        }
                    }
                    Button(
                        onClick = { onAddToCart(quantity) },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.ShoppingBasket, contentDescription = "Basket")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add to Basket")
                    }
                }
            }
        }
    ) {
        LazyColumn(modifier = Modifier.padding(it)) {
            item {
                Box(contentAlignment = Alignment.TopStart) {
                    Image(
                        painter = rememberAsyncImagePainter(food.imageUrl),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)),
                        contentScale = ContentScale.Crop
                    )
                    // Overlay Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        IconButton(
                            onClick = {
                                onToggleFavorite()
                                val message = if (!isFavorite) "Added to favorites" else "Removed from favorites"
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.background(Color.White, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Color.Red else Color.Gray
                            )
                        }
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(food.name, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    PriceRow(originalPrice = food.price * 1.2, newPrice = food.price)

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFC107))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${food.ratingAvg}", fontWeight = FontWeight.Bold)
                        Text(" (1.205)", color = Color.Gray, modifier = Modifier.padding(horizontal = 4.dp))
                        Text("·", color = Color.Gray)
                        Text("See all review", color = OrangePrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { onSeeReviewsClick() }.padding(start=8.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("A delicious chicken burger served on a toasted bun with fresh lettuce, tomato slices, and mayonnaise. Juicy grilled chicken patty seasone...",
                        lineHeight = 24.sp, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                    Text("See more", color = OrangePrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { /* TODO */ })

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("Additional Options:", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    options.forEach { option ->
                        OptionItem(option = option, isSelected = selectedOptions.contains(option)) {
                            if (selectedOptions.contains(option)) {
                                selectedOptions.remove(option)
                            } else {
                                selectedOptions.add(option)
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class OptionType { CHECKBOX, RADIO }
data class SelectableOption(val name: String, val price: Double, val type: OptionType)

@Composable
fun OptionItem(option: SelectableOption, isSelected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            when (option.type) {
                OptionType.CHECKBOX -> {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onSelect() },
                        colors = CheckboxDefaults.colors(checkedColor = OrangePrimary)
                    )
                    Text(option.name, fontSize = 16.sp)
                }
                OptionType.RADIO -> {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelect() },
                        colors = RadioButtonDefaults.colors(selectedColor = OrangePrimary)
                    )
                    Text(option.name, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
        Text("+ £${String.format("%.2f", option.price)}", color = Color.Gray, fontSize = 16.sp)
    }
}

@Composable
fun PriceRow(originalPrice: Double, newPrice: Long) {
    Text(
        buildAnnotatedString {
            withStyle(style = SpanStyle(textDecoration = TextDecoration.LineThrough, color = Color.Gray, fontSize = 18.sp)) {
                append("£${String.format("%.2f", originalPrice)}")
            }
            append(" ")
            withStyle(style = SpanStyle(color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 24.sp)) {
                append("£${newPrice}.00")
            }
        }
    )
}