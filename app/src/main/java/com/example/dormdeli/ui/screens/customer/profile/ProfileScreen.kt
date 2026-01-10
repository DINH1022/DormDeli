package com.example.dormdeli.ui.screens.customer.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.viewmodels.customer.ProfileViewModel
import com.example.dormdeli.R
import com.example.dormdeli.repository.image.CloudinaryHelper

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onLocationClick: () -> Unit, // Added for address navigation
    viewModel: ProfileViewModel = viewModel()
) {
    val user by viewModel.userState
    var currentSubScreen by remember { mutableStateOf("menu") } // "menu" or "personal_info"

    if (currentSubScreen == "menu") {
        MyProfileView(
            user = user,
            onBack = onNavigateBack,
            onPersonalInfoClick = { currentSubScreen = "personal_info" },
            onLocationClick = onLocationClick,
            onLogout = onLogout
        )
    } else {
        PersonalInfoView(
            viewModel = viewModel,
            onBack = { currentSubScreen = "menu" }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProfileView(
    user: com.example.dormdeli.model.User?,
    onBack: () -> Unit,
    onPersonalInfoClick: () -> Unit,
    onLocationClick: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = OrangePrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // Avatar with Progress Ring
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(140.dp)) {
                    drawArc(
                        color = OrangePrimary,
                        startAngle = -90f,
                        sweepAngle = 280f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                }
                
                ProfileAvatar(
                    avatarUrl = user?.avatarUrl ?: "",
                    size = 120.dp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(user?.fullName ?: "User Name", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(user?.role?.replaceFirstChar { it.uppercase() } ?: "Student", color = Color.Gray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(32.dp))

            // Menu Items
            ProfileMenuItem(icon = Icons.Default.Person, title = "Personal Info", onClick = onPersonalInfoClick)
            ProfileMenuItem(icon = Icons.Default.History, title = "Transaction History", onClick = {})
            ProfileMenuItem(icon = Icons.Default.LocationOn, title = "Delivery Addresses", onClick = onLocationClick)
            ProfileMenuItem(icon = Icons.Default.Settings, title = "Settings", onClick = {})
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Logout Item
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLogout() }
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = Color.Red)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Logout", color = Color.Red, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoView(
    viewModel: com.example.dormdeli.ui.viewmodels.customer.ProfileViewModel,
    onBack: () -> Unit
) {
    val user by viewModel.userState
    val isLoading by viewModel.isLoading
    val context = LocalContext.current

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dormBlock by remember { mutableStateOf("") }
    var roomNumber by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            isUploading = true
            CloudinaryHelper.uploadImage(uri, { url ->
                avatarUrl = url
                isUploading = false
            }, { isUploading = false })
        }
    }

    LaunchedEffect(user) {
        user?.let {
            fullName = it.fullName
            email = it.email
            dormBlock = it.dormBlock
            roomNumber = it.roomNumber
            avatarUrl = it.avatarUrl
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal Info", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            Box(contentAlignment = Alignment.BottomEnd) {
                ProfileAvatar(avatarUrl = avatarUrl, size = 100.dp)
                if (isUploading) {
                    Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.Black.copy(0.5f)), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(OrangePrimary)
                        .clickable { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            InfoTextField(label = "Name", value = fullName, onValueChange = { fullName = it })
            InfoTextField(label = "Email", value = email, onValueChange = { email = it }, enabled = false)
            InfoTextField(label = "Dorm Block", value = dormBlock, onValueChange = { dormBlock = it })
            InfoTextField(label = "Room Number", value = roomNumber, onValueChange = { roomNumber = it })

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    viewModel.updateUserProfile(fullName, email, dormBlock, roomNumber, avatarUrl)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White)
                else Text("Save", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileAvatar(avatarUrl: String, size: androidx.compose.ui.unit.Dp) {
    if (avatarUrl.startsWith("http")) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = null,
            modifier = Modifier.size(size).clip(CircleShape).border(2.dp, Color.White, CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.avatar_1),
            contentDescription = null,
            modifier = Modifier.size(size).clip(CircleShape).border(2.dp, Color.White, CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun InfoTextField(label: String, value: String, onValueChange: (String) -> Unit, enabled: Boolean = true) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(start = 4.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.LightGray,
                unfocusedIndicatorColor = Color.LightGray
            ),
            singleLine = true
        )
    }
}
