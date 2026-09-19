package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Appeal
import com.example.data.repository.AtharRepository
import com.example.ui.components.AppealCard
import com.example.ui.theme.AtharAmberSecondary
import com.example.ui.theme.AtharTealPrimary
import com.example.ui.theme.AtharUrgentRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppealsScreen(
    onAppealClick: (String) -> Unit,
    onNavigateToMap: () -> Unit = {},
    onNavigateToAssistant: () -> Unit = {}
) {
    val appeals by AtharRepository.appeals.collectAsState()
    val userProfile by AtharRepository.userProfile.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("الكل") }
    var selectedDistanceFilter by remember { mutableStateOf("الكل") }
    var selectedPriorityFilter by remember { mutableStateOf("الكل") }
    var selectedStatusFilter by remember { mutableStateOf("الكل") }

    // Instant Ask AI state
    var aiQueryText by remember { mutableStateOf("") }
    var aiAnswerText by remember { mutableStateOf<String?>(null) }
    var aiRecommendedAppealId by remember { mutableStateOf<String?>(null) }
    var isAiLoading by remember { mutableStateOf(false) }

    val typeOptions = listOf("الكل", "ورشة", "تطوع", "تقنية", "تعليم", "موارد")
    val distanceOptions = listOf("الكل", "5 كم", "10 كم", "25 كم", "50 كم")
    val priorityOptions = listOf("الكل", "عاجل 🔥", "عادي")
    val statusOptions = listOf("الكل", "يستقبل الردود", "نشط", "قيد التنفيذ")

    var showFiltersSheet by remember { mutableStateOf(false) }

    // Helper to answer AI prompts instantly
    fun handleAiAsk(prompt: String) {
        aiQueryText = prompt
        isAiLoading = true
        when {
            prompt.contains("عاجل", ignoreCase = true) || prompt.contains("فوري", ignoreCase = true) -> {
                aiAnswerText = "وجدت نداءين عاجلين بحاجة ماسة لمتطوعين: «برنامج دعم التحصيل الأكاديمي» (بقي مقعدين فقط) و«حملة تشجير المتنزه الطبيعي». أنصحك بالانضمام للتحصيل الأكاديمي فوراً!"
                aiRecommendedAppealId = "appeal_4"
                selectedPriorityFilter = "عاجل 🔥"
            }
            prompt.contains("قريب", ignoreCase = true) || prompt.contains("مسقط", ignoreCase = true) -> {
                aiAnswerText = "أقرب نداء لك في مسقط هو «ورشة بناء وتطوير الروبوتات للناشئين» على بعد 3.4 كم فقط في مركز الابتكار العلمي بالقرم."
                aiRecommendedAppealId = "appeal_1"
                selectedTypeFilter = "تقنية"
            }
            prompt.contains("تقني", ignoreCase = true) || prompt.contains("روبوت", ignoreCase = true) || prompt.contains("ذكاء", ignoreCase = true) -> {
                aiAnswerText = "بناءً على اهتماماتك التقنية، نرشح لك نداء «ورشة بناء وتطوير الروبوتات للناشئين» ونظام الأردوينو، بنسبة توافق 96% مع مهاراتك!"
                aiRecommendedAppealId = "appeal_1"
                selectedTypeFilter = "تقنية"
            }
            else -> {
                aiAnswerText = "بناءً على موقعك واهتماماتك: نرشح لك المشاركة في «ورشة الروبوتات» أو «دعم التحصيل الأكاديمي». يمكنك أيضاً استكشاف مواقعها ومساراتها مباشرة على الخريطة الأسطورية!"
                aiRecommendedAppealId = "appeal_1"
            }
        }
        isAiLoading = false
    }

    val filteredAppeals = remember(
        appeals, searchQuery, selectedTypeFilter, selectedDistanceFilter, selectedPriorityFilter, selectedStatusFilter
    ) {
        appeals.filter { appeal ->
            val matchQuery = searchQuery.isBlank() ||
                    appeal.title.contains(searchQuery, ignoreCase = true) ||
                    appeal.description.contains(searchQuery, ignoreCase = true) ||
                    appeal.locationName.contains(searchQuery, ignoreCase = true)

            val matchType = selectedTypeFilter == "الكل" || appeal.type == selectedTypeFilter
            val remainingSpots = appeal.requiredParticipants - appeal.participantsCount
            val matchPriority = when (selectedPriorityFilter) {
                "عاجل 🔥" -> appeal.priority == "عاجل" || appeal.priority == "مرتفع" || remainingSpots in 1..3
                "عادي" -> appeal.priority == "عادي" && remainingSpots > 3
                else -> true
            }
            val matchStatus = selectedStatusFilter == "الكل" || appeal.status == selectedStatusFilter

            val matchDistance = when (selectedDistanceFilter) {
                "5 كم" -> appeal.distanceKm <= 5.0
                "10 كم" -> appeal.distanceKm <= 10.0
                "25 كم" -> appeal.distanceKm <= 25.0
                "50 كم" -> appeal.distanceKm <= 50.0
                else -> true
            }

            matchQuery && matchType && matchPriority && matchStatus && matchDistance
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search & Filter Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("ابحث في النداءات أو المواقع...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "مسح")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    FilledIconButton(
                        onClick = { showFiltersSheet = true },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = AtharTealPrimary
                        )
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = "فلاتر متقدمة")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Type Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(typeOptions) { type ->
                        val isSelected = selectedTypeFilter == type
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTypeFilter = type },
                            label = { Text(type, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AtharTealPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }
        }

        // Active Filters Summary Bar
        val hasActiveFilters = selectedTypeFilter != "الكل" || selectedDistanceFilter != "الكل" ||
                selectedPriorityFilter != "الكل" || selectedStatusFilter != "الكل" || searchQuery.isNotEmpty()

        if (hasActiveFilters) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "النتائج المطابقة: ${filteredAppeals.size} نداء",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AtharTealPrimary
                    )

                    TextButton(
                        onClick = {
                            selectedTypeFilter = "الكل"
                            selectedDistanceFilter = "الكل"
                            selectedPriorityFilter = "الكل"
                            selectedStatusFilter = "الكل"
                            searchQuery = ""
                        }
                    ) {
                        Text("إعادة تعيين الفلاتر", fontSize = 11.sp)
                    }
                }
            }
        }

        // LazyColumn with Ask AI card, Legendary Map Hero Banner, and Appeals List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Ask AI Interactive Section ("اسأل من AI بعد الدخول")
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(AtharTealPrimary.copy(alpha = 0.6f), AtharAmberSecondary.copy(alpha = 0.5f))
                        ),
                        width = 1.5.dp
                    ),
                    shadowElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(AtharTealPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AutoAwesome,
                                        contentDescription = null,
                                        tint = AtharTealPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "اسأل المساعد الذكي (AI) ✨🤖",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "مرحباً ${userProfile.name} • اسأل ليقترح لك أفضل نداء",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Full Chat button
                            FilledTonalButton(
                                onClick = onNavigateToAssistant,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("المساعد 💬", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Ask AI Input Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = aiQueryText,
                                onValueChange = { aiQueryText = it },
                                placeholder = { Text("مثال: اقترح لي نداء عاجل في مسقط...", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                trailingIcon = {
                                    if (aiQueryText.isNotBlank()) {
                                        IconButton(onClick = { aiQueryText = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "مسح", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            )

                            Button(
                                onClick = {
                                    if (aiQueryText.isNotBlank()) {
                                        handleAiAsk(aiQueryText)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AtharTealPrimary),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "اسأل AI", modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick AI Prompt Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = AtharTealPrimary.copy(alpha = 0.1f),
                                    modifier = Modifier.clickable { handleAiAsk("أقرب نداء في مسقط") }
                                ) {
                                    Text(
                                        text = "📍 أقرب نداء إليّ",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AtharTealPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            item {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = AtharUrgentRed.copy(alpha = 0.12f),
                                    modifier = Modifier.clickable { handleAiAsk("نداءات عاجلة تحتاج متطوعين") }
                                ) {
                                    Text(
                                        text = "🔥 نداءات عاجلة فوراً",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AtharUrgentRed,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            item {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = AtharAmberSecondary.copy(alpha = 0.15f),
                                    modifier = Modifier.clickable { handleAiAsk("ورش التقنية والذكاء الاصطناعي") }
                                ) {
                                    Text(
                                        text = "🤖 ورش التقنية والروبوت",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AtharAmberSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // Instant AI Response Card
                        AnimatedVisibility(visible = aiAnswerText != null) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.horizontalGradient(listOf(AtharTealPrimary, AtharAmberSecondary)),
                                    width = 1.dp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("💡", fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "اقتراح الذكاء الاصطناعي المباشر:",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = AtharTealPrimary
                                            )
                                        }
                                        IconButton(
                                            onClick = { aiAnswerText = null },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "إغلاق", modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = aiAnswerText ?: "",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (aiRecommendedAppealId != null) {
                                            Button(
                                                onClick = { onAppealClick(aiRecommendedAppealId!!) },
                                                colors = ButtonDefaults.buttonColors(containerColor = AtharTealPrimary),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("عرض النداء المرشح 🚀", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        OutlinedButton(
                                            onClick = onNavigateToMap,
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("استكشف على الخريطة 🗺️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Legendary Interactive Map Banner ("خريطة تعرض كل شيء أسطوري")
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xF20B1322),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(Color(0xFF2DD4BF), Color(0xFFF59E0B))),
                        width = 1.2.dp
                    ),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToMap() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0x3310B981)
                                ) {
                                    Text(
                                        text = "رادار نشط 360° 📡",
                                        color = Color(0xFF10B981),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "خريطة أثر الأسطورية 🗺️⚡",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "تصفح كل النداءات برادار حي، ومسارات ملاحة ثلاثية الأبعاد وتحليلات الذكاء الاصطناعي في عُمان!",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = onNavigateToMap,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AtharTealPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text("فتح 🚀", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 3. Section Title
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "النداءات المتاحة (${filteredAppeals.size}) 📢",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "رُتّبت حسب الأقرب لك",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 4. Appeals list items or empty placeholder
            if (filteredAppeals.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "لا توجد نداءات مطابقة لمعايير البحث الحالية",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            } else {
                items(filteredAppeals) { appeal ->
                    AppealCard(
                        appeal = appeal,
                        onClick = { onAppealClick(appeal.id) },
                        onToggleSave = { AtharRepository.toggleSaveAppeal(appeal.id) }
                    )
                }
            }
        }
    }

    // Advanced Filters Bottom Sheet Dialog
    if (showFiltersSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFiltersSheet = false },
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("فلاتر تصفية النداءات ⚙️", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = { showFiltersSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Distance Filter
                Text("المسافة التقريبية:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    distanceOptions.forEach { opt ->
                        FilterChip(
                            selected = selectedDistanceFilter == opt,
                            onClick = { selectedDistanceFilter = opt },
                            label = { Text(opt, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Priority Filter
                Text("الأولوية:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    priorityOptions.forEach { opt ->
                        FilterChip(
                            selected = selectedPriorityFilter == opt,
                            onClick = { selectedPriorityFilter = opt },
                            label = { Text(opt, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Status Filter
                Text("حالة النداء:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    statusOptions.forEach { opt ->
                        FilterChip(
                            selected = selectedStatusFilter == opt,
                            onClick = { selectedStatusFilter = opt },
                            label = { Text(opt, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { showFiltersSheet = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AtharTealPrimary)
                ) {
                    Text("تطبيق الفلاتر", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
