package com.example.dormdeli.ui.screens.admin.features.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.dormdeli.ui.theme.OrangeDark
import com.example.dormdeli.ui.theme.OrangeLight
import com.example.dormdeli.ui.theme.OrangePrimary

@Composable
fun AvatarRender(
    url: String?,
    fullName: String,
    size: Int = 52
) {
    val avatarSize = size.dp

    if (!url.isNullOrEmpty()) {
        SubcomposeAsyncImage(
            model = url,
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    modifier = Modifier.background(OrangeLight),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(size.dp / 2),
                        color = OrangePrimary,
                        strokeWidth = 2.dp
                    )
                }
            },
            error = {
                FallbackAvatar(fullName, avatarSize)
            }
        )
    } else {
        FallbackAvatar(fullName, avatarSize)
    }
}

@Composable
private fun FallbackAvatar(text: String, size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(OrangeLight),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (text.isNotEmpty()) text.take(1).uppercase() else "?",
            color = OrangeDark,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.35).sp
        )
    }
}