package com.example.dormdeli.ui.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dormdeli.model.Review
import java.text.SimpleDateFormat
import java.util.*

// Định nghĩa màu giống trong ảnh (Màu cam đỏ)
val PrimaryOrange = Color(0xFFFF6347)
val StarYellow = Color(0xFFFFC107)
val BgGray = Color(0xFFF5F5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    foodId: String?,
    onBackClick: () -> Unit
) {
    val originalReviews = remember { getDummyReviews() }

    var selectedFilter by remember { mutableStateOf("All") }

    val filteredReviews by remember(selectedFilter) {
        derivedStateOf {
            when (selectedFilter) {
                "All" -> originalReviews
                "Positive" -> originalReviews.filter { it.rating >= 4 } // 4, 5 sao
                "Negative" -> originalReviews.filter { it.rating <= 3 } // 1, 2, 3 sao
                "5 ★" -> originalReviews.filter { it.rating == 5 }
                "4 ★" -> originalReviews.filter { it.rating == 4 }
                "3 ★" -> originalReviews.filter { it.rating == 3 }
                "2 ★" -> originalReviews.filter { it.rating == 2 }
                "1 ★" -> originalReviews.filter { it.rating == 1 }
                else -> originalReviews
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Reviews", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Chicken Burger", fontSize = 14.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 1. HEADER
                item { RatingOverviewSection(originalReviews) } // Truyền list gốc để tính điểm trung bình

                // 2. FILTER CHIPS
                item {
                    FilterSection(
                        currentFilter = selectedFilter,
                        onFilterSelected = { newFilter -> selectedFilter = newFilter }
                    )
                }

                // 3. DANH SÁCH REVIEW (Dùng list đã lọc)
                if (filteredReviews.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                            Text("No reviews found for this filter", color = Color.Gray)
                        }
                    }
                } else {
                    items(filteredReviews) { review ->
                        ReviewItem(review)
                        HorizontalDivider(
                            modifier = Modifier.padding(top = 16.dp),
                            thickness = 0.5.dp,
                            color = Color.LightGray.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

// --- COMPONENT: Tổng quan điểm số ---
@Composable
fun RatingOverviewSection(reviews: List<Review>) {
    // Tính toán số liệu thực tế từ danh sách
    val totalReviews = reviews.size
    val averageRating = if (totalReviews > 0) reviews.map { it.rating }.average() else 0.0

    // Đếm số lượng từng sao để vẽ thanh progress
    val count5 = reviews.count { it.rating == 5 }.toFloat()
    val count4 = reviews.count { it.rating == 4 }.toFloat()
    val count3 = reviews.count { it.rating == 3 }.toFloat()
    val count2 = reviews.count { it.rating == 2 }.toFloat()
    val count1 = reviews.count { it.rating == 1 }.toFloat()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cột điểm số to
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(0.8f)
        ) {
            Text(
                text = String.format("%.1f", averageRating), // Làm tròn 1 số thập phân
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            Row {
                repeat(5) { index ->
                    // Logic tô màu sao dựa trên điểm trung bình
                    val color = if (index < averageRating.toInt()) StarYellow else Color.LightGray
                    Icon(Icons.Default.Star, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                }
            }
            Text(text = "($totalReviews Reviews)", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Cột các thanh Progress (Tính % dựa trên tổng số review)
        Column(
            modifier = Modifier.weight(1.2f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val safeTotal = if (totalReviews == 0) 1f else totalReviews.toFloat()
            RatingBarRow(label = "5", progress = count5 / safeTotal)
            RatingBarRow(label = "4", progress = count4 / safeTotal)
            RatingBarRow(label = "3", progress = count3 / safeTotal)
            RatingBarRow(label = "2", progress = count2 / safeTotal)
            RatingBarRow(label = "1", progress = count1 / safeTotal)
        }
    }
}

@Composable
fun RatingBarRow(label: String, progress: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.width(12.dp))
        Spacer(modifier = Modifier.width(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = PrimaryOrange,
            trackColor = BgGray,
            strokeCap = StrokeCap.Round,
        )
    }
}

// --- COMPONENT: Bộ lọc ---
@Composable
fun FilterSection(
    currentFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val filters = listOf("All", "Positive", "Negative", "5 ★", "4 ★", "3 ★", "2 ★", "1 ★")

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(filters) { filter ->
            val isSelected = filter == currentFilter
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) PrimaryOrange else BgGray,
                onClick = { onFilterSelected(filter) } // Gửi sự kiện chọn filter ngược lên trên
            ) {
                Text(
                    text = filter,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = if (isSelected) Color.White else Color.Black,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// --- COMPONENT: Item Review chi tiết ---
@Composable
fun ReviewItem(review: Review) {
    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Avatar User
            AsyncImage(
                model = review.userAvatarUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Hàng Tên + Rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = review.userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    // Rating 5 sao nhỏ
                    Row {
                        repeat(5) { index ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (index < review.rating) StarYellow else Color.LightGray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Ngày tháng
                Text(
                    text = formatDate(review.createdAt),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Nội dung Comment
        Text(
            text = review.comment,
            color = Color.DarkGray,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 20.sp
        )
    }
}

// --- Helper: Format ngày tháng ---
fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// --- Data Giả lập để test ---
fun getDummyReviews(): List<Review> {
    return listOf(
        Review(
            id = "1", userName = "John Doe", rating = 5,
            userAvatarUrl = "https://i.pravatar.cc/150?u=1",
            comment = "Delicious chicken burger! Loved the crispy chicken and the bun was perfectly toasted. Definitely a new favorite!",
            createdAt = System.currentTimeMillis()
        ),
        Review(
            id = "2", userName = "James", rating = 4,
            userAvatarUrl = "https://i.pravatar.cc/150?u=2",
            comment = "The chicken burger was okay, but it was a bit overcooked for my liking. The toppings were fresh, though.",
            createdAt = System.currentTimeMillis() - 86400000 // Trừ 1 ngày
        ),
        Review(
            id = "3", userName = "David", rating = 5,
            userAvatarUrl = "https://i.pravatar.cc/150?u=3",
            comment = "Absolutely delicious! The chicken burger was juicy and flavorful. Highly recommend!",
            createdAt = System.currentTimeMillis() - 172800000 // Trừ 2 ngày
        )
    )
}