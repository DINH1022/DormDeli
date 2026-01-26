package com.example.dormdeli.ui.screens.customer.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dormdeli.ui.screens.profile.LogoutRow
import com.example.dormdeli.ui.screens.profile.ProfileAvatar
import com.example.dormdeli.ui.screens.profile.ProfileMenuItem
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.viewmodels.AuthViewModel
import com.example.dormdeli.ui.viewmodels.customer.ProfileViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerProfileScreen(
    viewModel: ProfileViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onPersonalInfoClick: () -> Unit,
    onLocationClick: () -> Unit,
    onSwitchToShipper: () -> Unit,
    onLogout: () -> Unit
) {
    val user by viewModel.userState
    val shipperRequest by viewModel.shipperRequestState
    val registerShipperSuccess by viewModel.registerShipperSuccess
    val isGoogleLinked by authViewModel.isGoogleLinked
    val isLoading by authViewModel.isLoading
    val errorMessage by authViewModel.errorMessage
    val context = LocalContext.current
    
    val hasShipperRole = user?.roles?.contains("shipper") == true
    val isRequestPending = shipperRequest != null && !shipperRequest!!.approved
    
    var showRegisterDialog by remember { mutableStateOf(false) }

    // Google Link Logic
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        authViewModel.linkGoogleAccount(task) {
            Toast.makeText(context, "Google link successful!", Toast.LENGTH_SHORT).show()
        }
    }

    // Display error from AuthViewModel
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            authViewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(registerShipperSuccess) {
        if (registerShipperSuccess) {
            Toast.makeText(context, "Your request has been sent to Admin. Please wait for approval.", Toast.LENGTH_LONG).show()
            viewModel.resetUpdateSuccess()
        }
    }

    if (showRegisterDialog) {
        AlertDialog(
            onDismissRequest = { showRegisterDialog = false },
            title = { Text("Become a Shipper", fontWeight = FontWeight.Bold) },
            text = { Text("Your registration will be reviewed by the Admin. Once approved, you can start delivering orders.") },
            confirmButton = {
                Button(
                    onClick = {
                        showRegisterDialog = false
                        viewModel.registerAsShipper()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Send Request", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRegisterDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customer Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                
                Box(contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(140.dp)) {
                        drawArc(
                            color = OrangePrimary,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                    }
                    ProfileAvatar(avatarUrl = user?.avatarUrl ?: "", size = 120.dp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(user?.fullName ?: "User Name", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Student", color = OrangePrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

                Spacer(modifier = Modifier.height(32.dp))

                ProfileMenuItem(
                    icon = Icons.Default.Person,
                    title = "Personal Information",
                    onClick = onPersonalInfoClick
                )
                ProfileMenuItem(icon = Icons.Default.History, title = "Order History", onClick = {})
                ProfileMenuItem(
                    icon = Icons.Default.LocationOn,
                    title = "Delivery Address",
                    onClick = onLocationClick
                )

                // Google Link Status
                ProfileMenuItem(
                    icon = Icons.Default.Link,
                    title = if (isGoogleLinked) "Google Linked" else "Link Google Account",
                    onClick = {
                        if (!isGoogleLinked) {
                            googleLauncher.launch(authViewModel.getGoogleLinkIntent(context))
                        }
                    },
                    tint = if (isGoogleLinked) Color(0xFF4CAF50) else Color(0xFF4285F4),
                    trailingIcon = if (isGoogleLinked) Icons.Default.CheckCircle else null
                )
                
                when {
                    hasShipperRole -> {
                        ProfileMenuItem(
                            icon = Icons.Default.DirectionsRun,
                            title = "Switch to Shipper Mode",
                            onClick = {
                                viewModel.switchActiveRole("shipper") {
                                    onSwitchToShipper()
                                }
                            },
                            tint = Color(0xFF4CAF50)
                        )
                    }
                    isRequestPending -> {
                        ProfileMenuItem(
                            icon = Icons.Default.HourglassEmpty,
                            title = "Shipper Request Pending...",
                            onClick = { 
                                Toast.makeText(context, "Admin is reviewing your request.", Toast.LENGTH_SHORT).show()
                            },
                            tint = Color.Gray
                        )
                    }
                    else -> {
                        ProfileMenuItem(
                            icon = Icons.Default.Badge,
                            title = "Become a Shipper",
                            onClick = { showRegisterDialog = true }
                        )
                    }
                }

                ProfileMenuItem(icon = Icons.Default.Settings, title = "App Settings", onClick = {})
                
                Spacer(modifier = Modifier.height(16.dp))

                LogoutRow(onLogout = onLogout)
                Spacer(modifier = Modifier.height(32.dp))
            }

            if (isLoading) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.3f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = OrangePrimary)
                    }
                }
            }
        }
    }
}
