package com.example.dormdeli.ui.seller.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.dormdeli.enums.FoodCategory
import com.example.dormdeli.ui.seller.viewmodels.SellerViewModel
import com.example.dormdeli.ui.seller.components.CustomTextField
import com.example.dormdeli.ui.theme.OrangeLight
import com.example.dormdeli.ui.theme.OrangePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFoodScreen(viewModel: SellerViewModel, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val editingFood by viewModel.editingFood.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isAutofillLoading by viewModel.isAutofillLoading.collectAsState()
    val autofilledDescription by viewModel.autofilledDescription.collectAsState()
    val autofillError by viewModel.autofillError.collectAsState()

    var name by remember(editingFood) { mutableStateOf(editingFood?.name ?: "") }
    var price by remember(editingFood) { mutableStateOf(editingFood?.price?.toString() ?: "") }
    var description by remember(editingFood) { mutableStateOf(editingFood?.description ?: "") }
    var category by remember(editingFood) { mutableStateOf(editingFood?.category ?: FoodCategory.RICE.value) }
    var keyAvailable by remember(editingFood) { mutableStateOf(editingFood?.available ?: true) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var expanded by remember { mutableStateOf(false) }

    DisposableEffect(Unit) { onDispose { viewModel.clearAutofill() } }
    LaunchedEffect(autofilledDescription) { autofilledDescription?.let { description = it } }
    LaunchedEffect(error) { error?.let { Toast.makeText(context, "Lỗi: $it", Toast.LENGTH_LONG).show() } }

    val displayImage: Any? = imageUri ?: editingFood?.imageUrl
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            imageUri = uri
            imageBitmap = uri?.toBitmap(context)
        }
    )

    Scaffold(
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Title
            Text(
                text = if (editingFood == null) "Thêm món mới" else "Chỉnh sửa món",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(16.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Image Picker - Style bìa chữ nhật bo góc
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .clickable(enabled = !isLoading) { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (displayImage != null && displayImage.toString().isNotBlank()) {
                        Image(
                            painter = rememberAsyncImagePainter(model = displayImage),
                            contentDescription = "Food Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Thêm hình ảnh", color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Form Inputs - White Background
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        CustomTextField(value = name, onValueChange = { name = it }, label = "Tên món ăn")
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Category Dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = when(category) {
                                    "Rice" -> "Cơm"
                                    "Noodle" -> "Mì/Phở/Bún"
                                    "Fast food" -> "Đồ ăn nhanh"
                                    "Drink" -> "Đồ uống"
                                    "Dessert" -> "Tráng miệng"
                                    "Sandwich" -> "Bánh mì"
                                    else -> "Khác"
                                },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Phân loại") },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    Icon(Icons.Default.ArrowDropDown, "dropdown", Modifier.clickable { expanded = true })
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                FoodCategory.values().forEach { cat ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(when(cat) {
                                                FoodCategory.RICE -> "Cơm"
                                                FoodCategory.NOODLE -> "Mì/Phở/Bún"
                                                FoodCategory.FAST_FOOD -> "Đồ ăn nhanh"
                                                FoodCategory.DRINK -> "Đồ uống"
                                                FoodCategory.DESSERT -> "Tráng miệng"
                                                FoodCategory.SANDWICH -> "Bánh mì"
                                                else -> "Khác"
                                            })
                                        },
                                        onClick = {
                                            category = cat.value
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        CustomTextField(value = price, onValueChange = { price = it }, label = "Giá bán", keyboardType = KeyboardType.Number)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Description + AI
                        Row(verticalAlignment = Alignment.Top) {
                            CustomTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = "Mô tả",
                                maxLines = 5,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    viewModel.clearAutofill()
                                    imageBitmap?.let { viewModel.autofillDescription(name, it) }
                                },
                                enabled = name.isNotBlank() && imageBitmap != null && !isAutofillLoading && !isLoading,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                if (isAutofillLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = OrangePrimary)
                                else Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = OrangePrimary)
                            }
                        }
                        if (autofillError != null) {
                            Text(text = autofillError!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Trạng thái: ${if(keyAvailable) "Đang bán" else "Hết hàng"}", fontWeight = FontWeight.SemiBold)
                            Switch(
                                checked = keyAvailable,
                                onCheckedChange = { keyAvailable = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = OrangePrimary, checkedTrackColor = OrangeLight)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Save Button Gradient
                Button(
                    onClick = {
                        viewModel.saveFood(
                            name = name,
                            description = description,
                            price = price.toDoubleOrNull() ?: 0.0,
                            category = category,
                            isAvailable = keyAvailable,
                            imageUri = imageUri
                        ) { onNavigateBack() }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isLoading
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(colors = listOf(OrangePrimary, OrangeLight))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.White)
                        else Text("Lưu thay đổi", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// Helper Uri to Bitmap
private fun Uri.toBitmap(context: Context): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, this))
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, this)
        }
    } catch (e: Exception) { e.printStackTrace(); null }
}
