package com.example.dormdeli.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dormdeli.model.UserAddress
import com.example.dormdeli.ui.theme.OrangePrimary
import com.example.dormdeli.ui.viewmodels.LocationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(
    viewModel: LocationViewModel,
    onBackClick: () -> Unit,
    onAddNewLocation: () -> Unit,
    onEditLocation: (String) -> Unit,
    onDeleteLocation: (String) -> Unit
) {
    val addresses by viewModel.addresses.collectAsState()
    val selectedAddress by viewModel.selectedAddress.collectAsState()
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }
    var addressToDelete by remember { mutableStateOf<UserAddress?>(null) }

    if (showDeleteDialog && addressToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Location") },
            text = { Text("Are you sure you want to delete '${addressToDelete?.label}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        addressToDelete?.let { onDeleteLocation(it.id) }
                        showDeleteDialog = false
                        addressToDelete = null
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Locations", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    selectedAddress?.let { 
                        viewModel.selectAddress(it)
                        Toast.makeText(context, "Location selected: ${it.label}", Toast.LENGTH_SHORT).show()
                        onBackClick()
                    } ?: run {
                         Toast.makeText(context, "Please select a location", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Apply", fontSize = 16.sp, color = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "My Locations",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(addresses) { address ->
                    LocationItem(
                        address = address,
                        isSelected = address.id == selectedAddress?.id,
                        onClick = { viewModel.selectAddress(address) },
                        onEditClick = { onEditLocation(address.id) },
                        onDeleteClick = {
                            addressToDelete = address
                            showDeleteDialog = true
                        }
                    )
                }

                item {
                    Button(
                        onClick = onAddNewLocation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary.copy(alpha = 0.1f),
                            contentColor = OrangePrimary
                        ),
                        shape = MaterialTheme.shapes.medium,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add New Location")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LocationItem(
    address: UserAddress,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = address.label,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Edit",
                        color = OrangePrimary,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { onEditClick() }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "|",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delete",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { onDeleteClick() }
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = address.address,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    maxLines = 2
                )
            }
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = OrangePrimary)
            )
        }
    }
}
