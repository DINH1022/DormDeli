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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.dormdeli.ui.seller.viewmodels.SellerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFoodScreen(viewModel: SellerViewModel, onNavigateBack: () -> Unit) {
    val context = LocalContext.current

    // === ViewModel State ===
    val editingMenuItem by viewModel.editingMenuItem.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isAutofillLoading by viewModel.isAutofillLoading.collectAsState()
    val autofilledDescription by viewModel.autofilledDescription.collectAsState()
    val autofillError by viewModel.autofillError.collectAsState()

    // === Local UI State ===
    var name by remember(editingMenuItem) { mutableStateOf(editingMenuItem?.name ?: "") }
    var price by remember(editingMenuItem) { mutableStateOf(editingMenuItem?.price?.toString() ?: "") }
    var description by remember(editingMenuItem) { mutableStateOf(editingMenuItem?.description ?: "") }
    var isAvailable by remember(editingMenuItem) { mutableStateOf(editingMenuItem?.isAvailable ?: true) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Clean up autofill state when the screen is disposed
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearAutofill()
        }
    }

    // Update description field when autofill completes
    LaunchedEffect(autofilledDescription) {
        autofilledDescription?.let {
            description = it
        }
    }

    // Show general error as a Toast for simplicity
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, "Save Error: $it", Toast.LENGTH_LONG).show()
        }
    }

    val displayImage: Any? = imageUri ?: editingMenuItem?.imageUrl

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            imageUri = uri
            imageBitmap = uri?.toBitmap(context)
        }
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // Make screen scrollable
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

            // --- Description and AI Autofill Section ---
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.weight(1f),
                        maxLines = 5,
                        enabled = !isLoading && !isAutofillLoading
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            // Clear previous errors before trying again
                            viewModel.clearAutofill()
                            imageBitmap?.let {
                                viewModel.autofillDescription(name, it)
                            }
                        },
                        enabled = name.isNotBlank() && imageBitmap != null && !isAutofillLoading && !isLoading
                    ) {
                        if (isAutofillLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Autofill with AI")
                        }
                    }
                }
                // Display Autofill Error Text
                autofillError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }

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

// Helper function to convert Uri to Bitmap
private fun Uri.toBitmap(context: Context): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, this))
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, this)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
