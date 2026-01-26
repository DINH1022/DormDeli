package com.example.dormdeli.ui.screens.customer.auth

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.viewmodels.AuthViewModel

@Composable
fun StudentVerificationScreen(
    authViewModel: AuthViewModel,
    onVerificationSuccess: () -> Unit,
    onLogout: () -> Unit
) {
    var showScanner by remember { mutableStateOf(false) }
    val isLoading by authViewModel.isLoading
    val context = LocalContext.current

    // Launcher for Camera permission
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showScanner = true
        } else {
            Toast.makeText(context, "Camera permission is required to scan the card", Toast.LENGTH_LONG).show()
        }
    }

    if (showScanner) {
        StudentCardScannerScreen(
            onCardDetected = { info ->
                showScanner = false
                if (info.studentId != null) {
                    authViewModel.verifyStudentIdentity(info.studentId) {
                        Toast.makeText(context, "Verification successful!", Toast.LENGTH_SHORT).show()
                        onVerificationSuccess()
                    }
                } else {
                    Toast.makeText(context, "Could not recognize the card, please try again", Toast.LENGTH_LONG).show()
                }
            },
            onClose = { showScanner = false }
        )
    } else {
        Scaffold { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Badge,
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    authViewModel.verifyStudentIdentity("DEBUG_USER_123") {
                                        Toast.makeText(context, "Debug: Bypass verification successful", Toast.LENGTH_SHORT).show()
                                        onVerificationSuccess()
                                    }
                                }
                            )
                        },
                    tint = OrangePrimary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    "Student Verification",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "To ensure the safety of the DormDeli community, please scan your student card to activate your account.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                if (isLoading) {
                    CircularProgressIndicator(color = OrangePrimary)
                } else {
                    Button(
                        onClick = {
                            // Check permission before opening scanner
                            val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                showScanner = true
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Scanning", fontSize = 16.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TextButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Logout", color = Color.Gray)
                    }
                }
            }
        }
    }
}
