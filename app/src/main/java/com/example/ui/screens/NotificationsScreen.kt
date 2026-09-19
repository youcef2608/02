package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AtharNotification
import com.example.data.repository.AtharRepository
import com.example.ui.theme.AtharAmberSecondary
import com.example.ui.theme.AtharTealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onAppealClick: (String) -> Unit
) {
    val notifications by AtharRepository.notifications.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الإشعارات والتنبيهات", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    TextButton(onClick = { AtharRepository.markAllNotificationsRead() }) {
                        Text("قراءة الكل", color = AtharTealPrimary)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (notifications.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxSize().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("لا توجد إشعارات جديدة", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(notifications) { notif ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                AtharRepository.markNotificationRead(notif.id)
                                if (notif.targetAppealId != null) {
                                    onAppealClick(notif.targetAppealId)
                                }
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (!notif.isRead) AtharTealPrimary.copy(alpha = 0.08f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = when (notif.type) {
                                    "NEARBY" -> AtharTealPrimary.copy(alpha = 0.15f)
                                    "TEAM" -> Color(0xFF10B981).copy(alpha = 0.15f)
                                    "AI" -> AtharAmberSecondary.copy(alpha = 0.15f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = when (notif.type) {
                                        "NEARBY" -> Icons.Default.NearMe
                                        "TEAM" -> Icons.Default.CheckCircle
                                        "AI" -> Icons.Default.AutoAwesome
                                        else -> Icons.Default.Notifications
                                    },
                                    contentDescription = null,
                                    tint = when (notif.type) {
                                        "NEARBY" -> AtharTealPrimary
                                        "TEAM" -> Color(0xFF047857)
                                        "AI" -> AtharAmberSecondary
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.padding(8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = notif.title,
                                    fontWeight = if (!notif.isRead) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = notif.message,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = notif.time,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (!notif.isRead) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(AtharTealPrimary)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
