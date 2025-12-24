package com.example.dormdeli.ui.profile

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.model.ProfileView


// Import resource R to access drawables
// Since I don't know the exact package name of R, I will assume it is com.example.dormdeli.R based on file structure
// If it fails, I will fix it.
import com.example.dormdeli.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileView = viewModel()
) {
    val user by viewModel.userState
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val updateSuccess by viewModel.updateSuccess
    val context = LocalContext.current

    // Local state for editing fields
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var dormBlock by remember { mutableStateOf("") }
    var roomNumber by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf("") }

    // Avatar Selection Dialog State
    var showAvatarDialog by remember { mutableStateOf(false) }

    // List of available avatars in res/drawable/Avatar (mapped to resource IDs)
    // Assuming the resource names are avatar_1, avatar_2, etc.
    // In Android resources, we access them via R.drawable.avatar_1
    val avatarList = listOf(
        R.drawable.avatar_1,
        R.drawable.avatar_2,
        R.drawable.avatar_3,
        R.drawable.avatar_4,
        R.drawable.avatar_5,
        R.drawable.avatar_6
    )

    // Function to map resource ID to a string identifier for saving in DB
    fun getAvatarName(resId: Int): String {
        return when (resId) {
            R.drawable.avatar_1 -> "avatar_1"
            R.drawable.avatar_2 -> "avatar_2"
            R.drawable.avatar_3 -> "avatar_3"
            R.drawable.avatar_4 -> "avatar_4"
            R.drawable.avatar_5 -> "avatar_5"
            R.drawable.avatar_6 -> "avatar_6"
            else -> ""
        }
    }

    // Function to map string identifier back to resource ID
    fun getAvatarResId(name: String): Int? {
        return when (name) {
            "avatar_1" -> R.drawable.avatar_1
            "avatar_2" -> R.drawable.avatar_2
            "avatar_3" -> R.drawable.avatar_3
            "avatar_4" -> R.drawable.avatar_4
            "avatar_5" -> R.drawable.avatar_5
            "avatar_6" -> R.drawable.avatar_6
            else -> null
        }
    }

    // Initialize fields when user data is loaded
    LaunchedEffect(user) {
        user?.let {
            fullName = it.fullName
            email = it.email
            phone = it.phone
            dormBlock = it.dormBlock
            roomNumber = it.roomNumber
            avatarUrl = it.avatarUrl
        }
    }

    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            viewModel.resetUpdateSuccess()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    if (showAvatarDialog) {
        AlertDialog(
            onDismissRequest = { showAvatarDialog = false },
            title = { Text("Choose Avatar") },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(avatarList) { resId ->
                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = "Avatar Option",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .clickable {
                                    avatarUrl = getAvatarName(resId)
                                    showAvatarDialog = false
                                }
                                .background(if (getAvatarName(resId) == avatarUrl) OrangePrimary.copy(alpha = 0.3f) else Color.Transparent)
                                .padding(4.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAvatarDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Your Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (isLoading && user == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = OrangePrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Profile Image
                Box(
                    contentAlignment = Alignment.BottomEnd
                ) {
                    val resId = getAvatarResId(avatarUrl)
                    if (resId != null) {
                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )
                    } else if (avatarUrl.isNotEmpty() && avatarUrl.startsWith("http")) {
                        // Fallback for online URLs if any
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Placeholder
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEEEEEE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_camera),
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    // Edit Icon
                    Box(
                        modifier = Modifier
                            .offset(x = 4.dp, y = 4.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(OrangePrimary)
                            .clickable { showAvatarDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile Picture",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Phone
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Phone Number",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            disabledContainerColor = Color(0xFFF5F5F5),
                            disabledTextColor = Color.Gray
                        ),
                        enabled = false,
                        leadingIcon = {
                             Text("🇻🇳", fontSize = 20.sp, modifier = Modifier.padding(start = 8.dp))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Email
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Email",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Email", color = Color.Gray) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            disabledContainerColor = Color(0xFFF5F5F5),
                            disabledTextColor = Color.Gray
                        ),
                        enabled = false,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Full Name
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Full Name",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Full Name", color = Color.Gray) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5)
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dorm Block
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Dorm Block",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = dormBlock,
                        onValueChange = { dormBlock = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Dorm Block", color = Color.Gray) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5)
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Room Number
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Room Number",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = roomNumber,
                        onValueChange = { roomNumber = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Room Number", color = Color.Gray) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5)
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Continue / Save Button
                Button(
                    onClick = {
                        viewModel.updateUserProfile(
                            fullName = fullName,
                            email = email,
                            dormBlock = dormBlock,
                            roomNumber = roomNumber,
                            avatarUrl = avatarUrl
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Save",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(onClick = {
                    // Reset to current cached user data first
                    user?.let {
                        fullName = it.fullName
                        email = it.email
                        phone = it.phone
                        dormBlock = it.dormBlock
                        roomNumber = it.roomNumber
                        avatarUrl = it.avatarUrl
                    }
                    // Reload from server to be sure
                    viewModel.loadUserProfile()
                }) {
                     Text(
                        text = "Skip",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
