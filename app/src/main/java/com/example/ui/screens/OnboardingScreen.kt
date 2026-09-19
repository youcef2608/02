package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AtharRepository
import com.example.ui.theme.AtharAmberSecondary
import com.example.ui.theme.AtharTealPrimary

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    var step by remember { mutableIntStateOf(1) }
    val totalSteps = 5

    // State for answers
    val availableSkills = listOf(
        "برمجة وتطوير تقني", "تصميم جرافيك وتجربة مستخدم", "إدارة وتنظيم فعاليات",
        "تعليم وتدريس", "صيانة وإلكترونيات", "تصوير ومونتاج",
        "إرشاد وتوجيه", "عمل ميداني وزراعة", "كتابة وصناعة محتوى", "إسعافات أولية"
    )
    var selectedSkills by remember { mutableStateOf(setOf("برمجة وتطوير تقني", "إدارة وتنظيم فعاليات")) }
    var customSkillText by remember { mutableStateOf("") }

    val availableInterests = listOf(
        "التقنية والبرمجة", "الروبوتات والذكاء الاصطناعي", "البيئة والاستدامة",
        "التعليم والتدريب", "المبادرات المجتمعية", "الريادة والابتكار",
        "الفنون والثقافة", "الصحة والرياضة"
    )
    var selectedInterests by remember { mutableStateOf(setOf("التقنية والبرمجة", "البيئة والاستدامة")) }

    var willingToVolunteer by remember { mutableStateOf(true) }
    var willingToHelp by remember { mutableStateOf(true) }

    val availableAppealTypes = listOf("ورش عمل", "مبادرات تطوعية", "مساندة تقنية", "دعم تعليمي", "توفير موارد")
    var selectedAppealTypes by remember { mutableStateOf(setOf("ورش عمل", "مبادرات تطوعية", "مساندة تقنية")) }

    val availableTimes = listOf("عطلة نهاية الأسبوع", "الفترة المسائية", "الفترة الصباحية", "حسب التنسيق المسبق")
    var selectedTimes by remember { mutableStateOf(setOf("عطلة نهاية الأسبوع", "الفترة المسائية")) }

    var selectedDistanceKm by remember { mutableIntStateOf(15) }

    // Manual location state
    var selectedCity by remember { mutableStateOf("مسقط") }
    var selectedGov by remember { mutableStateOf("محافظة مسقط") }
    var showManualLocationDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header Progress & Step Count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (i in 1..totalSteps) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (i <= step) AtharTealPrimary else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "$step من $totalSteps",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Chat AI Avatar and Context Bubble
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = AtharTealPrimary.copy(alpha = 0.15f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = AtharTealPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "مساعد أثر الذكي",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "يتعرف على قدراتك واهتماماتك لتخصيص النداءات بدقة",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Main Content Area by Step
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (step) {
                    1 -> {
                        // Question 1: What can you do? Skills
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "مرحبًا بك! ماذا تستطيع أن تفعل؟ وما مهاراتك؟ 🛠️",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "اختر المهارات التي تتقنها أو ترغب في توظيفها لمساعدة المبادرات والنداءات المجتمعية:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            item {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    availableSkills.forEach { skill ->
                                        val isSelected = selectedSkills.contains(skill)
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                selectedSkills = if (isSelected) selectedSkills - skill else selectedSkills + skill
                                            },
                                            label = { Text(skill, fontSize = 13.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = AtharTealPrimary,
                                                selectedLabelColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                    }
                                }
                            }

                            item {
                                OutlinedTextField(
                                    value = customSkillText,
                                    onValueChange = { customSkillText = it },
                                    label = { Text("أو اكتب مهارة أو قدرة إضافية...") },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }

                    2 -> {
                        // Question 2: Interests & Fields
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "ما المجالات التي تحبها وتهتم بها؟ 💡",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "هل تهتم بالتعليم أو التقنية أو البيئة أو المبادرات المجتمعية؟ سنرشح لك النداءات الأكثر توافقاً.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            item {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    availableInterests.forEach { interest ->
                                        val isSelected = selectedInterests.contains(interest)
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                selectedInterests = if (isSelected) selectedInterests - interest else selectedInterests + interest
                                            },
                                            label = { Text(interest, fontSize = 13.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = AtharTealPrimary,
                                                selectedLabelColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // Question 3: Volunteering & Helping
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "هل تحب التطوع وتقديم المساعدة؟ 🤝",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "اختر ما يناسب رغبتك وظروفك الحالية، ويمكنك تغيير ذلك في أي وقت.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            item {
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { willingToVolunteer = !willingToVolunteer }
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = willingToVolunteer,
                                            onCheckedChange = { willingToVolunteer = it },
                                            colors = CheckboxDefaults.colors(checkedColor = AtharTealPrimary)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("نعم، أرغب في المشاركة في الأعمال التطوعية", fontWeight = FontWeight.Bold)
                                            Text(
                                                "استقبال إشعارات النداءات التي تتطلب متطوعين ميدانيين",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { willingToHelp = !willingToHelp }
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = willingToHelp,
                                            onCheckedChange = { willingToHelp = it },
                                            colors = CheckboxDefaults.colors(checkedColor = AtharTealPrimary)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("أستطيع تقديم مساعدة تخصصية أو استشارية", fontWeight = FontWeight.Bold)
                                            Text(
                                                "حل تحديات تقنية، تدريب، أو تقديم مشورة للفرق والمبادرات",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    4 -> {
                        // Question 4: Preferred Appeals, Times, and Distance
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "نوع النداءات، والأوقات، والمسافة 🎯",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "حدد تفضيلاتك الزمنية والجغرافية لعرض الأنشطة الأنسب لجدولك:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            item {
                                Text("نوع النداءات المرغوبة:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    availableAppealTypes.forEach { type ->
                                        val isSelected = selectedAppealTypes.contains(type)
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                selectedAppealTypes = if (isSelected) selectedAppealTypes - type else selectedAppealTypes + type
                                            },
                                            label = { Text(type, fontSize = 12.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = AtharTealPrimary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }

                            item {
                                Text("الأوقات المناسبة لك:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    availableTimes.forEach { time ->
                                        val isSelected = selectedTimes.contains(time)
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                selectedTimes = if (isSelected) selectedTimes - time else selectedTimes + time
                                            },
                                            label = { Text(time, fontSize = 12.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = AtharTealPrimary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }

                            item {
                                Text("أقصى مسافة تستطيع الوصول إليها: $selectedDistanceKm كم", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Slider(
                                    value = selectedDistanceKm.toFloat(),
                                    onValueChange = { selectedDistanceKm = it.toInt() },
                                    valueRange = 5f..100f,
                                    steps = 18,
                                    colors = SliderDefaults.colors(
                                        thumbColor = AtharTealPrimary,
                                        activeTrackColor = AtharTealPrimary
                                    )
                                )
                            }
                        }
                    }

                    5 -> {
                        // Question 5 / Phase 3: Location Permission Once with Privacy & Transparency
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = AtharTealPrimary.copy(alpha = 0.12f),
                                modifier = Modifier.size(80.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = AtharTealPrimary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "تحديد موقعك الجغرافي مرة واحدة 📍",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Transparency explanation card
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = null,
                                            tint = AtharTealPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "الخصوصية والشفافية التامة:",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "• نطلب الإذن مرة واحدة لحساب المسافة وعرض النداءات القريبة منك وإرسال إشعارات جغرافية مفيدة.\n" +
                                                "• لن يُطلب الإذن مجددًا في كل فتح للتطبيق، ويمكنك تعديله من الإعدادات في أي لحظة.\n" +
                                                "• موقعك شخصي ولن يتم عرضه أو مشاركته مع أي مستخدم آخر على الخريطة.\n" +
                                                "• مواقع النداءات يحددها أصحابها من خريطة المنصة العامة.",
                                        style = MaterialTheme.typography.bodySmall,
                                        lineHeight = 20.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Allow Button
                            Button(
                                onClick = {
                                    val finalSkills = selectedSkills.toMutableList()
                                    if (customSkillText.isNotBlank()) finalSkills.add(customSkillText.trim())

                                    AtharRepository.updateProfile(
                                        interests = selectedInterests.toList(),
                                        goals = listOf("المشاركة في مبادرات", "التطوع"),
                                        experienceLevel = "متوسط",
                                        useLocation = true,
                                        searchRadiusKm = selectedDistanceKm,
                                        city = "مسقط",
                                        governorate = "محافظة مسقط",
                                        skills = finalSkills,
                                        availableTimes = selectedTimes.toList(),
                                        preferredAppealTypes = selectedAppealTypes.toList(),
                                        willingToVolunteer = willingToVolunteer,
                                        willingToHelp = willingToHelp,
                                        maxTravelDistanceKm = selectedDistanceKm
                                    )
                                    onFinish()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AtharTealPrimary)
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("السماح باستخدام موقعي", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Manual City Pick
                            OutlinedButton(
                                onClick = { showManualLocationDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.EditLocation, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("تحديد المدينة يدويًا ($selectedCity)")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Skip location
                            TextButton(
                                onClick = {
                                    val finalSkills = selectedSkills.toMutableList()
                                    if (customSkillText.isNotBlank()) finalSkills.add(customSkillText.trim())

                                    AtharRepository.updateProfile(
                                        interests = selectedInterests.toList(),
                                        goals = listOf("المشاركة في مبادرات", "التطوع"),
                                        experienceLevel = "متوسط",
                                        useLocation = false,
                                        searchRadiusKm = selectedDistanceKm,
                                        city = selectedCity,
                                        governorate = selectedGov,
                                        skills = finalSkills,
                                        availableTimes = selectedTimes.toList(),
                                        preferredAppealTypes = selectedAppealTypes.toList(),
                                        willingToVolunteer = willingToVolunteer,
                                        willingToHelp = willingToHelp,
                                        maxTravelDistanceKm = selectedDistanceKm
                                    )
                                    onFinish()
                                }
                            ) {
                                Text("المتابعة بدون موقع (تصفح النداءات العامة)")
                            }
                        }
                    }
                }
            }

            // Bottom Navigation Buttons (Previous, Next, Skip)
            if (step < totalSteps) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (step > 1) {
                        OutlinedButton(
                            onClick = { step-- },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("السابق")
                        }
                    }

                    // Skip question button
                    TextButton(
                        onClick = { step++ },
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text("تخطي")
                    }

                    Button(
                        onClick = { step++ },
                        modifier = Modifier
                            .weight(if (step > 1) 1.5f else 2.5f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AtharTealPrimary)
                    ) {
                        Text("التالي", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Manual Location Picker Dialog
    if (showManualLocationDialog) {
        val cities = listOf("مسقط", "صلالة", "صحار", "نزوى", "صور", "السيب", "بوشر", "المطرح", "البريمي")
        AlertDialog(
            onDismissRequest = { showManualLocationDialog = false },
            title = { Text("اختر مدينتك يدويًا 📍", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "اختر منطقتك لتصفح النداءات والمبادرات الخاصة بها:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    cities.forEach { city ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCity = city
                                    selectedGov = "محافظة $city"
                                    showManualLocationDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCity == city,
                                onClick = {
                                    selectedCity = city
                                    selectedGov = "محافظة $city"
                                    showManualLocationDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(city, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showManualLocationDialog = false }) {
                    Text("تم", fontWeight = FontWeight.Bold, color = AtharTealPrimary)
                }
            }
        )
    }
}
