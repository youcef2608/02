package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.repository.AtharRepository
import com.example.ui.theme.AtharAmberSecondary
import com.example.ui.theme.AtharTealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtharTopAppBar(
    title: String = "أثر | Athar",
    unreadNotifCount: Int = 2,
    onAssistantClick: () -> Unit = {},
    onNotifClick: () -> Unit = {},
    onLeaderboardClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onAuthClick: () -> Unit = {}
) {
    val userProfile by AtharRepository.userProfile.collectAsState()
    val serverStatus by AtharRepository.serverSyncState.collectAsState()

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Athar App Icon Badge
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(AtharTealPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_icon_1789845024443),
                        contentDescription = "Athar Logo",
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                    )
                }

                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (userProfile.isLoggedIn) "${userProfile.name} • ${if (userProfile.role == "ASSOCIATION") "جمعية معتمدة" else "متطوع"}" else "متصل بالموقع والسيرفر",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        actions = {
            // Athar AI Assistant Sparkle Icon
            IconButton(onClick = onAssistantClick) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "مساعد أثر الذكي",
                    tint = AtharAmberSecondary
                )
            }

            // Notification Bell with Badge
            IconButton(onClick = onNotifClick) {
                BadgedBox(
                    badge = {
                        if (unreadNotifCount > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = Color.White
                            ) {
                                Text("$unreadNotifCount")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "الإشعارات"
                    )
                }
            }

            // Login / Auth Icon Button ("تسجيل الدخول")
            IconButton(onClick = onAuthClick) {
                Icon(
                    imageVector = Icons.Default.Login,
                    contentDescription = "تسجيل الدخول",
                    tint = AtharTealPrimary
                )
            }

            // Settings
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "الإعدادات"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun AtharBottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        // 1. النداءات
        NavigationBarItem(
            selected = currentRoute == "appeals",
            onClick = { onNavigate("appeals") },
            icon = { Icon(Icons.Default.Campaign, contentDescription = "النداءات") },
            label = { Text("النداءات", fontSize = 10.sp, fontWeight = if (currentRoute == "appeals") FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AtharTealPrimary,
                indicatorColor = AtharTealPrimary.copy(alpha = 0.15f)
            )
        )

        // 2. الخريطة
        NavigationBarItem(
            selected = currentRoute == "map",
            onClick = { onNavigate("map") },
            icon = { Icon(Icons.Default.Map, contentDescription = "الخريطة") },
            label = { Text("الخريطة", fontSize = 10.sp, fontWeight = if (currentRoute == "map") FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AtharTealPrimary,
                indicatorColor = AtharTealPrimary.copy(alpha = 0.15f)
            )
        )

        // 3. الملاحظات
        NavigationBarItem(
            selected = currentRoute == "notes",
            onClick = { onNavigate("notes") },
            icon = { Icon(Icons.Default.EditNote, contentDescription = "الملاحظات") },
            label = { Text("الملاحظات", fontSize = 10.sp, fontWeight = if (currentRoute == "notes") FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AtharTealPrimary,
                indicatorColor = AtharTealPrimary.copy(alpha = 0.15f)
            )
        )

        // 4. لوحة الصدارة
        NavigationBarItem(
            selected = currentRoute == "leaderboard",
            onClick = { onNavigate("leaderboard") },
            icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "لوحة الصدارة") },
            label = { Text("الصدارة", fontSize = 10.sp, fontWeight = if (currentRoute == "leaderboard") FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AtharTealPrimary,
                indicatorColor = AtharTealPrimary.copy(alpha = 0.15f)
            )
        )

        // 5. حسابي
        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = { onNavigate("profile") },
            icon = { Icon(Icons.Default.Person, contentDescription = "حسابي") },
            label = { Text("حسابي", fontSize = 10.sp, fontWeight = if (currentRoute == "profile") FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AtharTealPrimary,
                indicatorColor = AtharTealPrimary.copy(alpha = 0.15f)
            )
        )
    }
}
