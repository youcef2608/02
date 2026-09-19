package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AtharRepository
import com.example.ui.theme.AtharAmberSecondary
import com.example.ui.theme.AtharTealPrimary

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onReopenOnboarding: () -> Unit,
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val userProfile by AtharRepository.userProfile.collectAsState()

    var useLocation by remember { mutableStateOf(userProfile.useLocation) }
    var selectedRadius by remember { mutableIntStateOf(userProfile.searchRadiusKm) }
    var notifNearby by remember { mutableStateOf(true) }
    var notifMatching by remember { mutableStateOf(true) }
    var notifReminders by remember { mutableStateOf(true) }
    var notifAi by remember { mutableStateOf(true) }

    var showCityDialog by remember { mutableStateOf(false) }
    var currentCity by remember { mutableStateOf(userProfile.city) }

    // Edit Name Dialog
    var showEditNameDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(userProfile.name) }
    var newNickname by remember { mutableStateOf(userProfile.nickname) }

    // Edit Interests & Skills Dialog
    var showInterestsSkillsDialog by remember { mutableStateOf(false) }
    var editInterests by remember { mutableStateOf(userProfile.interests.toSet()) }
    var editSkills by remember { mutableStateOf(userProfile.skills.toSet()) }

    // Privacy & Appearance Dialogs
    var showAppearanceDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الإعدادات وحسابي ⚙️", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Account Profile & Identity
            item {
                Text(
                    text = "الحساب والبيانات الشخصية 👤",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Edit Name Item
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    newName = userProfile.name
                                    newNickname = userProfile.nickname
                                    showEditNameDialog = true
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("الاسم والاسم المستعار", fontWeight = FontWeight.Bold)
                                Text(
                                    text = "${userProfile.name} (المستعار: ${userProfile.nickname.ifEmpty { "غير محدد" }})",
                                    fontSize = 12.sp,
                                    color = AtharTealPrimary
                                )
                            }
                            Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        // Edit Interests & Skills Item
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editInterests = userProfile.interests.toSet()
                                    editSkills = userProfile.skills.toSet()
                                    showInterestsSkillsDialog = true
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("الاهتمامات والمهارات", fontWeight = FontWeight.Bold)
                                Text(
                                    text = "${userProfile.interests.size} اهتمامات • ${userProfile.skills.size} مهارات",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        // Leaderboard Anonymity Setting
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAppearanceDialog = true }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("طريقة الظهور في لوحة الصدارة", fontWeight = FontWeight.Bold)
                                Text(
                                    text = when (userProfile.displayNamePreference) {
                                        "ANONYMOUS" -> "مستخدم مجهول (${userProfile.anonymousId})"
                                        "NICKNAME" -> "اسم مستعار (${userProfile.nickname})"
                                        else -> "الاسم الحقيقي (${userProfile.name})"
                                    },
                                    fontSize = 12.sp,
                                    color = AtharTealPrimary
                                )
                            }
                            Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Section 2: Location & Search Settings
            item {
                Text(
                    text = "الموقع الجغرافي ونطاق البحث 📍",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("استخدام الموقع لاكتشاف الأنشطة", fontWeight = FontWeight.Bold)
                                Text(
                                    text = "يساعد في ترشيح النداءات القريبة منك بدقة",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = useLocation,
                                onCheckedChange = {
                                    useLocation = it
                                    AtharRepository.setLocationPermission(it, currentCity)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = AtharTealPrimary)
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        // Manual City Select
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCityDialog = true }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("المدينة الحالية", fontWeight = FontWeight.Bold)
                                Text(
                                    text = "$currentCity • انقر للتغيير يدويًا",
                                    fontSize = 12.sp,
                                    color = AtharTealPrimary
                                )
                            }
                            Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        // Radius selector
                        Text("نصف قطر البحث الافتراضي: $selectedRadius كم", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val radii = listOf(5, 10, 15, 25, 50)
                            radii.forEach { r ->
                                FilterChip(
                                    selected = selectedRadius == r,
                                    onClick = {
                                        selectedRadius = r
                                        AtharRepository.updateProfile(
                                            interests = userProfile.interests,
                                            goals = userProfile.goals,
                                            experienceLevel = userProfile.experienceLevel,
                                            useLocation = useLocation,
                                            searchRadiusKm = r,
                                            city = currentCity,
                                            governorate = userProfile.governorate
                                        )
                                    },
                                    label = { Text("$r كم", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AtharTealPrimary,
                                        selectedLabelColor = androidx.compose.ui.graphics.Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Section 3: Notifications Settings
            item {
                Text(
                    text = "الإشعارات والتنبيهات 🔔",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("أنشطة قريبة جديدة", fontSize = 14.sp)
                            Switch(checked = notifNearby, onCheckedChange = { notifNearby = it })
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("مبادرات تطابق اهتماماتك الذكية", fontSize = 14.sp)
                            Switch(checked = notifMatching, onCheckedChange = { notifMatching = it })
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("تذكيرات المواعيد والأنشطة المشترك بها", fontSize = 14.sp)
                            Switch(checked = notifReminders, onCheckedChange = { notifReminders = it })
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("اقتراحات وتوجيهات مساعد أثر الذكي", fontSize = 14.sp)
                            Switch(checked = notifAi, onCheckedChange = { notifAi = it })
                        }
                    }
                }
            }

            // Section 4: Privacy & Smart Onboarding
            item {
                Text(
                    text = "الخصوصية والتهيئة 🛡️",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Re-run Smart Onboarding
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onReopenOnboarding)
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AtharTealPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("إعادة تشغيل حوار التهيئة الذكي", fontWeight = FontWeight.Bold)
                                Text("تحديث أسئلة التعارف والقدرات مع المساعد", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        // Privacy Policy Dialog
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showPrivacyDialog = true }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = AtharTealPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("سياسة الخصوصية وحماية البيانات", fontWeight = FontWeight.Bold)
                                Text("الشفافية في استخدام الموقع والبيانات", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Section 5: Account Actions (Logout & Delete)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Logout
                        OutlinedButton(
                            onClick = { showLogoutDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تسجيل الخروج من الحساب")
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Delete Account
                        TextButton(
                            onClick = { showDeleteAccountDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("حذف الحساب نهائيًا وفق النظام", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // About Platform
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AtharTealPrimary.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("منصة أثر | Athar Mobile", fontWeight = FontWeight.Bold, color = AtharTealPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "«أثر يحول الأفكار والتجارب إلى معرفة وأثر مستقبلي»",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("الإصدار 1.0.0 (رسمي متصل بالمنظومة) • سلطنة عُمان", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("تعديل الاسم والاسم المستعار ✏️", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("الاسم الكامل") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newNickname,
                        onValueChange = { newNickname = it },
                        label = { Text("الاسم المستعار (اختياري)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            AtharRepository.updateDisplayNamePreference(userProfile.displayNamePreference, newNickname.trim())
                            AtharRepository.updateProfile(
                                interests = userProfile.interests,
                                goals = userProfile.goals,
                                experienceLevel = userProfile.experienceLevel,
                                useLocation = userProfile.useLocation,
                                searchRadiusKm = userProfile.searchRadiusKm,
                                city = userProfile.city,
                                governorate = userProfile.governorate,
                                skills = userProfile.skills
                            )
                            Toast.makeText(context, "تم تحديث البيانات بنجاح", Toast.LENGTH_SHORT).show()
                            showEditNameDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AtharTealPrimary)
                ) {
                    Text("حفظ التعديل")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Edit Interests & Skills Dialog
    if (showInterestsSkillsDialog) {
        val allInterests = listOf("الروبوتات", "التكنولوجيا", "البيئة", "البرمجة", "التعليم والتدريب", "المبادرات المجتمعية")
        val allSkills = listOf("برمجة وتطوير تقني", "إدارة وتنظيم فعاليات", "صيانة وإلكترونيات", "تصميم وجرافيك", "تعليم وتدريس")

        AlertDialog(
            onDismissRequest = { showInterestsSkillsDialog = false },
            title = { Text("تعديل الاهتمامات والمهارات 🎯", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("الاهتمامات:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allInterests.forEach { item ->
                            val isSelected = editInterests.contains(item)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    editInterests = if (isSelected) editInterests - item else editInterests + item
                                },
                                label = { Text(item, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AtharTealPrimary,
                                    selectedLabelColor = androidx.compose.ui.graphics.Color.White
                                )
                            )
                        }
                    }

                    Text("المهارات:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allSkills.forEach { skill ->
                            val isSelected = editSkills.contains(skill)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    editSkills = if (isSelected) editSkills - skill else editSkills + skill
                                },
                                label = { Text(skill, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AtharTealPrimary,
                                    selectedLabelColor = androidx.compose.ui.graphics.Color.White
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        AtharRepository.updateProfile(
                            interests = editInterests.toList(),
                            goals = userProfile.goals,
                            experienceLevel = userProfile.experienceLevel,
                            useLocation = userProfile.useLocation,
                            searchRadiusKm = userProfile.searchRadiusKm,
                            city = userProfile.city,
                            governorate = userProfile.governorate,
                            skills = editSkills.toList()
                        )
                        Toast.makeText(context, "تم حفظ الاهتمامات والمهارات", Toast.LENGTH_SHORT).show()
                        showInterestsSkillsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AtharTealPrimary)
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInterestsSkillsDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Leaderboard Appearance Dialog
    if (showAppearanceDialog) {
        var selectedPref by remember { mutableStateOf(userProfile.displayNamePreference) }
        var tempNickname by remember { mutableStateOf(userProfile.nickname) }

        AlertDialog(
            onDismissRequest = { showAppearanceDialog = false },
            title = { Text("طريقة الظهور في لوحة الصدارة", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPref = "REAL_NAME" }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedPref == "REAL_NAME", onClick = { selectedPref = "REAL_NAME" })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("الاسم الحقيقي (${userProfile.name})")
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPref = "NICKNAME" }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedPref == "NICKNAME", onClick = { selectedPref = "NICKNAME" })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("اسم مستعار (${tempNickname.ifEmpty { "غير محدد" }})")
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPref = "ANONYMOUS" }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedPref == "ANONYMOUS", onClick = { selectedPref = "ANONYMOUS" })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("مستخدم مجهول (${userProfile.anonymousId})")
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
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAppearanceDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // City Selection Dialog
    if (showCityDialog) {
        val cities = listOf("مسقط", "صلالة", "صحار", "نزوى", "صور", "السيب", "بوشر", "المطرح", "البريمي")
        AlertDialog(
            onDismissRequest = { showCityDialog = false },
            title = { Text("اختر مدينتك", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    cities.forEach { city ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentCity = city
                                    AtharRepository.setLocationPermission(useLocation, city)
                                    showCityDialog = false
                                    Toast.makeText(context, "تم تغيير المدينة إلى $city", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentCity == city,
                                onClick = {
                                    currentCity = city
                                    AtharRepository.setLocationPermission(useLocation, city)
                                    showCityDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(city)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCityDialog = false }) { Text("إغلاق") }
            }
        )
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("سياسة الخصوصية وحماية البيانات 🛡️", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "• نلتزم بأعلى معايير حماية البيانات الشخصية وفق اللوائح المعتمدة في سلطنة عُمان.\n" +
                            "• موقعك الجغرافي يستخدم فقط لتحديد النداءات القريبة ولا يتم عرضه بدقة لأي طرف آخر على الخريطة.\n" +
                            "• يمكنك في أي وقت استخدام الاسم المستعار أو وضع التخفي في لوحة الصدارة.\n" +
                            "• تملك الحق الكامل في تعديل بياناتك أو حذف حسابك ومسح سجلاتك متى شئت.",
                    fontSize = 13.sp,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) { Text("إغلاق") }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("تسجيل الخروج", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من رغبتك في تسجيل الخروج من تطبيق أثر؟") },
            confirmButton = {
                Button(
                    onClick = {
                        AtharRepository.logout()
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("نعم، تسجيل الخروج")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Delete Account Confirmation Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("حذف الحساب نهائيًا ⚠️", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
            text = {
                Text("تنبيه: سيؤدي حذف الحساب إلى مسح جميع مشاركاتك، وملاحظاتك، ونقاطك في لوحة الصدارة وفق النظام. هل ترغب في المتابعة؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        AtharRepository.deleteAccount()
                        showDeleteAccountDialog = false
                        Toast.makeText(context, "تم حذف الحساب بنجاح", Toast.LENGTH_SHORT).show()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حذف الحساب")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) { Text("إلغاء") }
            }
        )
    }
}
