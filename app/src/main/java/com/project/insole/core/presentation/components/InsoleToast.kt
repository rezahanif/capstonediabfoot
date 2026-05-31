package com.project.insole.core.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ToastType {
    Success, Info, Warning, Error, Pairing
}

data class ToastData(
    val title: String,
    val description: String,
    val type: ToastType,
    val isVisible: Boolean = false
)

@Composable
fun InsoleToast(
    data: ToastData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, tintColor, bgColor) = when (data.type) {
        ToastType.Success -> Triple(Icons.Default.Check, Color(0xFF2EB88A), Color(0xFFECFDF5))
        ToastType.Info -> Triple(Icons.Default.Info, Color(0xFF2563EB), Color(0xFFEFF6FF))
        ToastType.Warning -> Triple(Icons.Default.Warning, Color(0xFFEAB308), Color(0xFFFEFCE8))
        ToastType.Error -> Triple(Icons.Default.Close, Color(0xFFEF4444), Color(0xFFFEF2F2))
        ToastType.Pairing -> Triple(Icons.Default.Link, Color(0xFF2563EB), Color(0xFFEFF6FF))
    }

    AnimatedVisibility(
        visible = data.isVisible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Icon Circle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tintColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.width(16.dp))

                // Content
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = data.title,
                        color = Color(0xFF112B4E),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = data.description,
                        color = Color(0xFF6B7280),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }

                // Right Action (Close or Spinner)
                if (data.type == ToastType.Pairing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = tintColor
                    )
                } else {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
