package com.example.dormdeli.ui.screens.admin.features.dashboard

import android.annotation.SuppressLint
import com.example.dormdeli.ui.theme.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dormdeli.ui.viewmodels.admin.dashboard.AdminDashboardViewModel
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.ui.graphics.Path
import com.example.dormdeli.repository.admin.dataclass.TopStoreRevenue
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tổng quan") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardBackground
                )
            )
        },
        containerColor = CardBackground
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = OrangePrimary)
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Lỗi: ${uiState.error}",
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Kéo xuống để tải lại",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(bottom = 16.dp)
            ) {
                // Stats Cards - Horizontal Scroll
                // Stats Cards Section
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(vertical = 8.dp) // Giảm padding dọc một chút
                ) {
                    // Card Đơn hàng
                    item {
                        val calendar = Calendar.getInstance()
                        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                        val todayIndex = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
                        val todayOrders = uiState.weeklyOrders.getOrNull(todayIndex) ?: 0
                        StatCard(
                            title = "Đơn hàng tuần này",
                            value = formatNumber(uiState.weeklyOrders.sum()),
                            change = "$todayOrders đơn hôm nay",
                            icon = Icons.Default.Receipt,
                            iconColor = OrangePrimary,
                            isPositive = true
                        )
                    }

                    // Card Doanh thu
                    item {
                        val todayRevenue = uiState.weeklyRevenue.lastOrNull() ?: 0L
                        StatCard(
                            title = "Doanh thu tuần",
                            value = formatRevenue(uiState.weeklyRevenue.sum()),
                            change = "${formatRevenue(todayRevenue)} hôm nay",
                            icon = Icons.Default.Store,
                            iconColor = Green,
                            isPositive = true
                        )
                    }

                    // Card Người dùng
                    item {
                        StatCard(
                            title = "Người dùng mới",
                            value = uiState.newUsersLast7Days.toString(),
                            change = "Trong 7 ngày qua",
                            icon = Icons.Default.People,
                            iconColor = OrangeDark,
                            isPositive = true
                        )
                    }

                    // Card Quán ăn
                    item {
                        StatCard(
                            title = "Quán ăn",
                            value = uiState.pendingStores.toString(),
                            change = if (uiState.pendingStores > 0) "Cần xét duyệt ngay" else "Đã duyệt hết",
                            icon = Icons.Default.Restaurant,
                            iconColor = OrangePrimary,
                            isPositive = uiState.pendingStores == 0
                        )
                    }

                    // Card Shipper
                    item {
                        StatCard(
                            title = "Shipper",
                            value = uiState.pendingShippers.toString(),
                            change = if (uiState.pendingShippers > 0) "Đang chờ hồ sơ" else "Đã ổn định",
                            icon = Icons.Default.LocalShipping,
                            iconColor = OrangeDark,
                            isPositive = uiState.pendingShippers == 0
                        )
                    }
                }

                // Weekly Revenue Chart
                if (uiState.weeklyRevenue.isNotEmpty()) {
                    ChartSection(
                        title = "Doanh thu tuần này",
                        subtitle = "Tổng: ${formatRevenue(uiState.weeklyRevenue.sum())} VNĐ",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        LineChart(
                            data = uiState.weeklyRevenue,
                            labels = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN"),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Weekly Orders Chart
                if (uiState.weeklyOrders.isNotEmpty()) {
                    ChartSection(
                        title = "Đơn hàng tuần này",
                        subtitle = "Tổng: ${uiState.weeklyOrders.sum()} đơn hàng",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        BarChart(
                            data = uiState.weeklyOrders,
                            labels = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN"),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Top Stores
                if (uiState.topStores.isNotEmpty()) {
                    TopStoresSection(
                        stores = uiState.topStores,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                } else {
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CardBackground
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Chưa có dữ liệu quán ăn",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}


private fun formatNumber(number: Int): String {
    return NumberFormat.getNumberInstance(Locale("vi", "VN")).format(number)
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

@Composable
fun StatCard(
    title: String,
    value: String,
    change: String,
    icon: ImageVector,
    iconColor: Color,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(240.dp) // Giảm xuống 240dp để thấy được nhiều card hơn, gợi ý scroll
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Thêm chút bóng cho nổi bật
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f), // Ép title không lấn chỗ icon
                    maxLines = 1
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(
                    text = value,
                    color = Black,
                    fontSize = 24.sp, // Giảm từ 32sp xuống 24sp để an toàn với số tiền lớn
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = change,
                    color = if (isPositive) Green else OrangeDark,
                    fontSize = 12.sp,
                    maxLines = 1
                )
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
            containerColor = CardBackground
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                color = Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = TextSecondary,
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

        /* ===== 1. Trục Oy (Giữ nguyên như trước) ===== */
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
                    color = TextSecondary,
                    maxLines = 1
                )
            }
        }

        /* ===== 2. Đường kẻ trục đứng (Giữ nguyên) ===== */
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(bottom = 24.dp)
                .width(1.dp)
                .background(TextSecondary.copy(alpha = 0.3f))
        )

        Spacer(modifier = Modifier.width(8.dp))

        /* ===== 3. Vùng hiển thị Biểu đồ và Nhãn Ox ===== */
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            if (data.isEmpty() || data.all { it == 0L }) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Chưa có dữ liệu", color = TextSecondary, fontSize = 14.sp)
                }
            } else {
                // Canvas vẽ đường biểu đồ
                Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    val xStep = size.width / (data.size - 1).coerceAtLeast(1)

                    // Tính toán tọa độ các điểm
                    val points = data.mapIndexed { index, value ->
                        val x = index * xStep
                        // y = 0 là đỉnh trên cùng, y = size.height là đáy dưới cùng
                        val y = size.height - (value.toFloat() / maxValue * size.height)
                        Offset(x, y)
                    }

                    if (points.isNotEmpty()) {
                        /* =========== PHẦN MỚI THÊM: VẼ MÀU NỀN NHẠT =========== */
                        // 1. Tạo Path cho vùng cần tô màu
                        val fillPath = Path().apply {
                            // Bắt đầu từ góc dưới cùng bên trái (tương ứng với điểm đầu tiên)
                            moveTo(points.first().x, size.height)

                            // Vẽ đường đi qua tất cả các điểm dữ liệu trên cao
                            points.forEach { point ->
                                lineTo(point.x, point.y)
                            }

                            // Vẽ đường xuống góc dưới cùng bên phải (tương ứng với điểm cuối cùng)
                            lineTo(points.last().x, size.height)

                            // Đóng path lại (nối về điểm bắt đầu)
                            close()
                        }

                        // 2. Tô màu path đó bằng màu cam nhạt (alpha = 0.3f)
                        // QUAN TRỌNG: Vẽ cái này TRƯỚC khi vẽ đường line chính
                        drawPath(
                            path = fillPath,
                            color = OrangePrimary.copy(alpha = 0.3f)
                        )
                        /* ========================================================== */


                        // Vẽ các đoạn thẳng nối điểm (Đường chính đậm hơn)
                        for (i in 0 until points.size - 1) {
                            drawLine(
                                color = OrangePrimary,
                                start = points[i],
                                end = points[i + 1],
                                strokeWidth = 3.dp.toPx()
                            )
                        }

                        // Vẽ các điểm tròn (Nodes)
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

                /* ===== 4. Nhãn trục Ox (Giữ nguyên) ===== */
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    labels.forEach { label ->
                        Text(
                            text = label,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BarChart(
    data: List<Int>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val maxValue = (data.maxOrNull() ?: 0).coerceAtLeast(1)

    Box(modifier = modifier) {
        if (data.isEmpty() || data.all { it == 0 }) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Chưa có dữ liệu", color = TextSecondary, fontSize = 14.sp)
            }
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                /* ===== Trục Oy ===== */
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(bottom = 24.dp) // Trừ hao phần nhãn Ox bên dưới
                        .width(40.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    val steps = 4
                    for (i in steps downTo 0) {
                        Text(
                            text = ((maxValue * i) / steps).toString(),
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                /* ===== Chart area ===== */
                Row(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    data.forEachIndexed { index, value ->
                        // Gom Cột và Nhãn vào 1 Column để đảm bảo luôn thẳng hàng
                        Column(
                            modifier = Modifier.fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            // Cột
                            Box(
                                modifier = Modifier
                                    .width(24.dp) // Giảm width một chút để tránh chật chội
                                    .weight(1f, fill = false)
                                    .fillMaxHeight(fraction = (value.toFloat() / maxValue).coerceAtLeast(0.05f))
                                    .background(
                                        color = OrangePrimary,
                                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Nhãn (T2, T3...)
                            Text(
                                text = labels.getOrNull(index) ?: "",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun TopStoresSection(
    stores: List<TopStoreRevenue>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Top quán ăn",
                color = Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Theo doanh thu",
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            stores.take(5).forEachIndexed { index, store ->
                TopStoreItem(
                    rank = index + 1,
                    name = store.storeName,
                    orders = store.totalOrders,
                    revenue = store.totalRevenue
                )
                if (index < stores.size - 1 && index < 4) {
                    Divider(
                        color = CardBorder,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TopStoreItem(
    rank: Int,
    name: String,
    orders: Int,
    revenue: Long
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "#$rank",
                color = OrangePrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(40.dp)
            )
            Column {
                Text(
                    text = name,
                    color = Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$orders đơn hàng",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatRevenue(revenue),
                color = Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = " VNĐ",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}