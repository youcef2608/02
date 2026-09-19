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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeaderboardEntry
import com.example.data.repository.AtharRepository
import com.example.ui.theme.AtharAmberSecondary
import com.example.ui.theme.AtharTealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(onBack: () -> Unit) {
    val currentLeaderboard by AtharRepository.currentLeaderboard.collectAsState()
    val userProfile by AtharRepository.userProfile.collectAsState()
    val archivedLeaderboards = AtharRepository.archivedLeaderboards

    var selectedMonth by remember { mutableStateOf("سبتمبر 2026 (الحالي)") }
    var showRulesDialog by remember { mutableStateOf(false) }
    var showAppearanceDialog by remember { mutableStateOf(false) }
    var customNickname by remember { mutableStateOf(userProfile.nickname) }

    val activeList = remember(selectedMonth, currentLeaderboard) {
        when (selectedMonth) {
            "أغسطس 2026" -> archivedLeaderboards["أغسطس 2026"] ?: emptyList()
            "يوليو 2026" -> archivedLeaderboards["يوليو 2026"] ?: emptyList()
            else -> currentLeaderboard
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("لوحة الصدارة الشهرية 🏆", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { showAppearanceDialog = true }) {
                        Icon(Icons.Default.Visibility, contentDescription = "طريقة الظهور", tint = AtharTealPrimary)
                    }
                    IconButton(onClick = { showRulesDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "معايير النقاط", tint = AtharAmberSecondary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Month Selector & Reset Notice
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "دورة الترتيب الشهرية",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = selectedMonth,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Month dropdown / archive selector
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(
                                onClick = { expanded = true },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("الأرشيف", fontSize = 12.sp)
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("سبتمبر 2026 (الحالي)") },
                                    onClick = {
                                        selectedMonth = "سبتمبر 2026 (الحالي)"
                                        expanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("أغسطس 2026 (الأرشيف)") },
                                    onClick = {
                                        selectedMonth = "أغسطس 2026"
                                        expanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("يوليو 2026 (الأرشيف)") },
                                    onClick = {
                                        selectedMonth = "يوليو 2026"
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Anonymity mode indicator bar
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AtharTealPrimary.copy(alpha = 0.08f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAppearanceDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = AtharTealPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ظهورك في القائمة: " + when (userProfile.displayNamePreference) {
                                        "ANONYMOUS" -> "مستخدم مجهول (${userProfile.anonymousId})"
                                        "NICKNAME" -> "اسم مستعار (${userProfile.nickname})"
                                        else -> "الاسم الحقيقي (${userProfile.name})"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AtharTealPrimary
                                )
                            }
                            Text(
                                text = "تعديل",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AtharTealPrimary
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top 3 Podium Cards
                if (activeList.size >= 3) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "المراكز الثلاثة الأولى 🎖️",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    // Rank 2 (Silver)
                                    PodiumPillar(
                                        entry = activeList[1],
                                        medal = "🥈",
                                        color = Color(0xFF94A3B8),
                                        height = 90.dp
                                    )

                                    // Rank 1 (Gold)
                                    PodiumPillar(
                                        entry = activeList[0],
                                        medal = "🥇",
                                        color = Color(0xFFF59E0B),
                                        height = 120.dp
                                    )

                                    // Rank 3 (Bronze)
                                    PodiumPillar(
                                        entry = activeList[2],
                                        medal = "🥉",
                                        color = Color(0xFFD97706),
                                        height = 70.dp
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "ترتيب المشاركين",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(activeList) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (entry.isCurrentUser) AtharTealPrimary.copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = if (entry.isCurrentUser) CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(AtharTealPrimary),
                            width = 1.5.dp
                        ) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rank number
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (entry.rank) {
                                            1 -> Color(0xFFFEF3C7)
                                            2 -> Color(0xFFF1F5F9)
                                            3 -> Color(0xFFFED7AA)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${entry.rank}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = when (entry.rank) {
                                        1 -> Color(0xFFB45309)
                                        2 -> Color(0xFF475569)
                                        3 -> Color(0xFF9A3412)
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Avatar Initial
                            Surface(
                                shape = CircleShape,
                                color = AtharTealPrimary.copy(alpha = 0.15f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = entry.avatarInitial,
                                        fontWeight = FontWeight.Bold,
                                        color = AtharTealPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Name & stats
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = entry.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    if (entry.isCurrentUser) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = AtharTealPrimary
                                        ) {
                                            Text(
                                                text = "أنت",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${entry.participationsCount} مشاركات • ${entry.lessonsCount} دروس مستفادة",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Points
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${entry.points}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = AtharTealPrimary
                                )
                                Text(
                                    text = "نقطة أثر",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Points calculation rules dialog
    if (showRulesDialog) {
        AlertDialog(
            onDismissRequest = { showRulesDialog = false },
            title = { Text("معايير احتساب نقاط أثر 🎯", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("يتم احتساب النقاط بناءً على المساهمات الحقيقية في المنظومة:")
                    ListItem(
                        headlineContent = { Text("المشاركة في نداء / مبادرة", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("الرد بالمساعدة أو المشاركة الميدانية") },
                        trailingContent = { Text("+50 نقطة", fontWeight = FontWeight.Bold, color = AtharTealPrimary) }
                    )
                    ListItem(
                        headlineContent = { Text("توثيق درس مستفاد", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("تحويل تجربة ميدانية إلى درس ذكي معتمد") },
                        trailingContent = { Text("+30 نقطة", fontWeight = FontWeight.Bold, color = AtharAmberSecondary) }
                    )
                    ListItem(
                        headlineContent = { Text("إكمال مهمة نشاط", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("إنجاز المهام المسندة بنجاح") },
                        trailingContent = { Text("+20 نقطة", fontWeight = FontWeight.Bold, color = Color(0xFF10B981)) }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showRulesDialog = false }) { Text("فهمت ذلك") }
            }
        )
    }

    // Appearance mode in leaderboard dialog
    if (showAppearanceDialog) {
        var selectedPref by remember { mutableStateOf(userProfile.displayNamePreference) }
        var tempNickname by remember { mutableStateOf(userProfile.nickname) }

        AlertDialog(
            onDismissRequest = { showAppearanceDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = AtharTealPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("طريقة الظهور في لوحة الصدارة", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "اختر كيف ترغب أن يظهر اسمك في الترتيب الشهري للجميع:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Option 1: Real name
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPref = "REAL_NAME" }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPref == "REAL_NAME",
                            onClick = { selectedPref = "REAL_NAME" }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("عرض الاسم الحقيقي", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(userProfile.name, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Option 2: Nickname
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPref = "NICKNAME" }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPref == "NICKNAME",
                            onClick = { selectedPref = "NICKNAME" }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("عرض اسم مستعار", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            if (selectedPref == "NICKNAME") {
                                OutlinedTextField(
                                    value = tempNickname,
                                    onValueChange = { tempNickname = it },
                                    label = { Text("الاسم المستعار") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                )
                            } else {
                                Text(if (tempNickname.isNotBlank()) tempNickname else "لم يُحدد بعد", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Option 3: Anonymous
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPref = "ANONYMOUS" }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPref == "ANONYMOUS",
                            onClick = { selectedPref = "ANONYMOUS" }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("الظهور كمستخدم مجهول (حجب الهوية)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                "سيظهر: \"${userProfile.anonymousId}\" ويتم إخفاء اسمك الحقيقي ومعلوماتك الشخصية بالكامل.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        AtharRepository.updateDisplayNamePreference(selectedPref, tempNickname)
                        showAppearanceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AtharTealPrimary)
                ) {
                    Text("حفظ الاختيار")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAppearanceDialog = false }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
fun PodiumPillar(
    entry: LeaderboardEntry,
    medal: String,
    color: Color,
    height: androidx.compose.ui.unit.Dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(text = medal, fontSize = 20.sp)
        Text(
            text = entry.name.split(" ").firstOrNull() ?: entry.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${entry.points} ن",
            fontSize = 11.sp,
            color = AtharTealPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(68.dp)
                .height(height)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(color.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${entry.rank}",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = color
            )
        }
    }
}
