package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AtharRepository
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppealDetailsScreen(
    appealId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val appeals by AtharRepository.appeals.collectAsState()
    val appeal = appeals.firstOrNull { it.id == appealId }

    var showResponseSheet by remember { mutableStateOf(false) }
    var selectedResponseType by remember { mutableStateOf("أستطيع المساعدة") }
    var optionalMessage by remember { mutableStateOf("") }
    var showReportDialog by remember { mutableStateOf(false) }

    if (appeal == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لم يتم العثور على النداء المطلوب")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تفاصيل النداء", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    // Save
                    IconButton(onClick = { AtharRepository.toggleSaveAppeal(appeal.id) }) {
                        Icon(
                            imageVector = if (appeal.isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "حفظ",
                            tint = if (appeal.isSaved) AtharAmberSecondary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Share
                    IconButton(onClick = {
                        Toast.makeText(context, "تم نسخ رابط النداء للمشاركة", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Outlined.Share, contentDescription = "مشاركة")
                    }
                    // Report
                    IconButton(onClick = { showReportDialog = true }) {
                        Icon(Icons.Default.Flag, contentDescription = "إبلاغ", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    if (appeal.userResponse != null) {
                        Column {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF047857)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "ردك: ${appeal.userResponse}",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF047857)
                                        )
                                        Text(
                                            text = "الحالة: ${appeal.responseStatus ?: "تم إرسال الرد بنجاح"}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF047857)
                                        )
                                    }
                                    TextButton(
                                        onClick = { AtharRepository.cancelResponse(appeal.id) }
                                    ) {
                                        Text("إلغاء الرد", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = { showResponseSheet = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AtharTealPrimary)
                        ) {
                            Icon(Icons.Default.VolunteerActivism, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("الرد والمشاركة في النداء", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Badges & Priority
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AtharTealPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = appeal.type,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AtharTealPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = appeal.status,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        val remainingSpots = appeal.requiredParticipants - appeal.participantsCount
                        val isUrgent = appeal.priority == "عاجل" || appeal.priority == "مرتفع" || remainingSpots in 1..3
                        if (isUrgent) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AtharUrgentContainer
                            ) {
                                Text(
                                    text = if (remainingSpots in 1..3) "عاجل 🔥 بقي $remainingSpots فقط" else "عاجل 🔥",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AtharUrgentOnContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Title
            item {
                Text(
                    text = appeal.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 32.sp
                )
            }

            // Creator & Time Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = AtharTealPrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("الجهة المنظمة / المنشئ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(appeal.creatorName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${appeal.date} • ${appeal.time}", fontSize = 13.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(appeal.duration, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Description
            item {
                Text(
                    text = "وصف النداء والهدف منه",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = appeal.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp
                )
            }

            // Location Box & Map Preview
            item {
                Text(
                    text = "الموقع الجغرافي للنداء 📍",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = AtharTealPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(appeal.locationName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${appeal.city} • تبعد عنك ${appeal.distanceKm} كم تقريبياً", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Mini Interactive Location Canvas (shows appeal location specifically)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E293B))
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val c = Offset(size.width / 2f, size.height / 2f)
                                drawCircle(Color(0x332DD4BF), radius = 40f, center = c)
                                drawCircle(Color(0xFF2DD4BF), radius = 10f, center = c)
                            }
                            Text(
                                text = "خريطة موقع النداء (لا تعرض موقع المستخدم)",
                                fontSize = 10.sp,
                                color = Color(0xAAFFFFFF),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(6.dp)
                            )
                        }
                    }
                }
            }

            // Required Skills & Resources
            item {
                Text(
                    text = "المهارات والقدرات المطلوبة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    appeal.requiredSkills.forEach { skill ->
                        AssistChip(
                            onClick = {},
                            label = { Text(skill) },
                            leadingIcon = { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Resources & Safety Conditions
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AtharAmberSecondary.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = AtharAmberSecondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إرشادات وشروط المشاركة والسلامة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Text(
                            text = "• الشروط: ${appeal.conditions}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "• السلامة: ${appeal.safetyInfo}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Participants Progress
            item {
                Text(
                    text = "المشاركون (${appeal.participantsCount}/${appeal.requiredParticipants})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (appeal.participantsCount.toFloat() / appeal.requiredParticipants).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = AtharTealPrimary
                )
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Response Bottom Sheet Dialog
    if (showResponseSheet) {
        ModalBottomSheet(
            onDismissRequest = { showResponseSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "الرد على النداء",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "اختر طبيعة مشاركتك لإبلاغ منشئ النداء وحفظها في مشاركاتك:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                val responseTypes = listOf(
                    "أستطيع المساعدة",
                    "أرغب في المشاركة",
                    "أحتاج إلى معلومات إضافية",
                    "أستطيع المساعدة لاحقاً"
                )

                responseTypes.forEach { type ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedResponseType == type) AtharTealPrimary.copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = if (selectedResponseType == type) CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(AtharTealPrimary),
                            width = 1.5.dp
                        ) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedResponseType == type,
                                onClick = { selectedResponseType = type }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(type, fontWeight = if (selectedResponseType == type) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = optionalMessage,
                    onValueChange = { optionalMessage = it },
                    label = { Text("رسالة أو تفاصيل إضافية (اختياري)") },
                    placeholder = { Text("مثلاً: متاح أيام نهاية الأسبوع، لدي خبرة في المجال...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        AtharRepository.submitResponse(appeal.id, selectedResponseType, optionalMessage)
                        showResponseSheet = false
                        Toast.makeText(context, "تم إرسال ردك بنجاح وحصلت على +50 نقطة أثر! ✨", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AtharTealPrimary)
                ) {
                    Text("تأكيد إرسال الرد", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Report Dialog
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("الإبلاغ عن النداء", fontWeight = FontWeight.Bold) },
            text = { Text("هل ترغب في الإبلاغ عن هذا النداء لوجود محتوى مخالف أو غير دقيق؟ سيتم مراجعته من قبل إدارة أثر.") },
            confirmButton = {
                Button(
                    onClick = {
                        showReportDialog = false
                        Toast.makeText(context, "شكرًا لك، تم إرسال البلاغ للمراجعة", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("إرسال البلاغ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) { Text("إلغاء") }
            }
        )
    }
}
