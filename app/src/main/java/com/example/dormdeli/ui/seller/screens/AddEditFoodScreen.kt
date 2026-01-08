package com.example.dormdeli.ui.seller.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.dormdeli.ui.seller.viewmodels.SellerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFoodScreen(viewModel: SellerViewModel, onNavigateBack: () -> Unit) {
    val editingMenuItem by viewModel.editingMenuItem.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var name by remember(editingMenuItem) { mutableStateOf(editingMenuItem?.name ?: "") }
    var price by remember(editingMenuItem) { mutableStateOf(editingMenuItem?.price?.toString() ?: "") }
    var description by remember(editingMenuItem) { mutableStateOf(editingMenuItem?.description ?: "") }
    var isAvailable by remember(editingMenuItem) { mutableStateOf(editingMenuItem?.isAvailable ?: true) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val displayImage: Any? = imageUri ?: editingMenuItem?.imageUrl

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> imageUri = uri }
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (editingMenuItem == null) "Add Food" else "Edit Food") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
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
                    Text("Add Image")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Food Name") }, modifier = Modifier.fillMaxWidth(), enabled = !isLoading)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), maxLines = 3, enabled = !isLoading)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Available")
                Switch(checked = isAvailable, onCheckedChange = { isAvailable = it }, enabled = !isLoading)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.saveMenuItem(
                        name = name,
                        description = description,
                        price = price.toDoubleOrNull() ?: 0.0,
                        isAvailable = isAvailable,
                        imageUri = imageUri
                    ) {
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Food Item")
                }
            }
        }
    }
}
