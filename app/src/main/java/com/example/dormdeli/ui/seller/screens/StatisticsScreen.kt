package com.example.dormdeli.ui.seller.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dormdeli.ui.seller.viewmodels.SellerViewModel
import com.example.dormdeli.ui.theme.OrangeLight
import com.example.dormdeli.ui.theme.OrangePrimary
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatisticsScreen(viewModel: SellerViewModel) {
    val totalOrderCount by viewModel.totalOrderCount.collectAsState()
    val deliveredCount by viewModel.deliveredCount.collectAsState()
    val cancelledCount by viewModel.cancelledCount.collectAsState()
    val totalRevenue by viewModel.totalRevenue.collectAsState()
    val weeklyRevenue by viewModel.weeklyRevenue.collectAsState()

    val formattedRevenue = remember(totalRevenue) {
        NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(totalRevenue)
    }

    val currentDate = remember {
        SimpleDateFormat("EEEE, dd MMMM", Locale("vi", "VN")).format(Date())
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        // Bỏ topBar ở đây để nội dung đẩy lên cao hơn
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp) // Tăng khoảng cách giữa các phần một chút cho thoáng
        ) {
            // Phần 1: Header (Dashboard to & Ngày tháng)
            item {
                Column(
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 4.dp) // Padding top nhỏ để đẩy sát lên trên
                ) {
                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.headlineMedium.copy( // Chữ To Hơn hẳn
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp // Cố định size to
                        ),
                        color = Color(0xFF1F1F1F)
                    )
                    Text(
                        text = currentDate.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium, // Ngày tháng to hơn một chút cho dễ đọc
                        color = Color(0xFF757575),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Phần 2: Thẻ Doanh thu
            item {
                RevenueCard(revenue = formattedRevenue)
            }

            // Phần 3: Thống kê đơn hàng
            item {
                Text(
                    "Tổng quan đơn hàng",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF1F1F1F)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Tổng đơn",
                        value = totalOrderCount.toString(),
                        icon = Icons.Default.ReceiptLong,
                        iconColor = Color(0xFF4285F4),
                        iconBg = Color(0xFFE8F0FE),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Đã giao",
                        value = deliveredCount.toString(),
                        icon = Icons.Default.CheckCircle,
                        iconColor = Color(0xFF34A853),
                        iconBg = Color(0xFFE6F4EA),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Đã hủy",
                        value = cancelledCount.toString(),
                        icon = Icons.Default.Cancel,
                        iconColor = Color(0xFFEA4335),
                        iconBg = Color(0xFFFCE8E6),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Phần 4: Biểu đồ doanh thu
            if (weeklyRevenue.isNotEmpty()) {
                item {
                    ChartSection(
                        title = "Doanh thu tuần này",
                        subtitle = "Tổng: ${formatRevenue(weeklyRevenue.sum())} VNĐ",
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        LineChart(
                            data = weeklyRevenue,
                            labels = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN"),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }
            }

            // Phần 5: Hiệu suất
            item {
                QuickActionSection()
            }

            // Spacer cuối cùng để không bị cấn nút navigation bên dưới
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun RevenueCard(revenue: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp), // Tăng chiều cao
        shape = RoundedCornerShape(24.dp), // Bo góc mềm mại hơn
        elevation = CardDefaults.cardElevation(8.dp) // Đổ bóng sâu hơn
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            OrangePrimary, // Đổi màu cam
                            OrangeLight    // Đổi màu cam nhạt
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tổng doanh thu",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = revenue,
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            // Trang trí visual ẩn (hình tròn mờ)
            Icon(
                imageVector = Icons.Default.ShowChart,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.2f),
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 20.dp, y = 20.dp)
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp) // Đổ bóng nhẹ
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start // Canh trái nhìn pro hơn
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF1F1F1F)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF757575)
            )
        }
    }
}

@Composable
fun QuickActionSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Hiệu suất", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color.Green)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tăng trưởng tốt so với hôm qua", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun ChartSection(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFF1F1F1F),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = Color(0xFF757575),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun LineChart(
    data: List<Long>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val maxValue = (data.maxOrNull() ?: 0L).coerceAtLeast(1L)

    Row(modifier = modifier) {
        // Trục Oy
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(bottom = 24.dp)
                .width(44.dp)
                .padding(end = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            val steps = 4
            for (i in steps downTo 0) {
                val value = (maxValue * i) / steps
                Text(
                    text = formatRevenue(value),
                    fontSize = 10.sp,
                    color = Color(0xFF757575),
                    maxLines = 1
                )
            }
        }

        // Đường kẻ trục đứng
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(bottom = 24.dp)
                .width(1.dp)
                .background(Color(0xFF757575).copy(alpha = 0.3f))
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Vùng hiển thị Biểu đồ và Nhãn Ox
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            if (data.isEmpty() || data.all { it == 0L }) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Chưa có dữ liệu", color = Color(0xFF757575), fontSize = 14.sp)
                }
            } else {
                // Canvas vẽ đường biểu đồ
                Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    val xStep = size.width / (data.size - 1).coerceAtLeast(1)

                    // Tính toán tọa độ các điểm
                    val points = data.mapIndexed { index, value ->
                        val x = index * xStep
                        val y = size.height - (value.toFloat() / maxValue * size.height)
                        Offset(x, y)
                    }

                    if (points.isNotEmpty()) {
                        // Vẽ màu nền nhạt
                        val fillPath = Path().apply {
                            moveTo(points.first().x, size.height)
                            points.forEach { point ->
                                lineTo(point.x, point.y)
                            }
                            lineTo(points.last().x, size.height)
                            close()
                        }

                        drawPath(
                            path = fillPath,
                            color = OrangePrimary.copy(alpha = 0.3f)
                        )

                        // Vẽ các đoạn thẳng nối điểm
                        for (i in 0 until points.size - 1) {
                            drawLine(
                                color = OrangePrimary,
                                start = points[i],
                                end = points[i + 1],
                                strokeWidth = 3.dp.toPx()
                            )
                        }

                        // Vẽ các điểm tròn
                        points.forEach { point ->
                            drawCircle(
                                color = OrangePrimary,
                                radius = 4.dp.toPx(),
                                center = point
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Nhãn trục Ox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    labels.forEach { label ->
                        Text(
                            text = label,
                            color = Color(0xFF757575),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
private fun formatRevenue(revenue: Long): String {
    return when {
        revenue >= 1_000_000_000 -> String.format("%.1fB", revenue / 1_000_000_000.0)
        revenue >= 1_000_000 -> String.format("%.1fM", revenue / 1_000_000.0)
        revenue >= 1_000 -> String.format("%.1fK", revenue / 1_000.0)
        else -> revenue.toString()
    }
}
