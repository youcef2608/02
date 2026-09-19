package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NoteItem
import com.example.data.repository.AtharRepository
import com.example.ui.theme.AtharAmberSecondary
import com.example.ui.theme.AtharTealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onNavigateToAppeal: (String) -> Unit
) {
    val context = LocalContext.current
    val notes by AtharRepository.notes.collectAsState()
    val appeals by AtharRepository.appeals.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("الكل") }
    val typeFilters = listOf("الكل", "درس مستفاد", "ملاحظة", "فكرة", "قرار", "تجربة", "تذكير")

    var showAddNoteDialog by remember { mutableStateOf(false) }

    // State for creating new note
    var newTitle by remember { mutableStateOf("") }
    var newContent by remember { mutableStateOf("") }
    var newType by remember { mutableStateOf("ملاحظة") }
    var selectedLinkedAppealId by remember { mutableStateOf<String?>(null) }

    // AI Lesson Suggestion trigger in dialog
    var aiSuggestedLesson by remember { mutableStateOf<String?>(null) }
    var showAiSuggestionInDialog by remember { mutableStateOf(false) }

    val filteredNotes = remember(notes, searchQuery, selectedTypeFilter) {
        notes.filter { note ->
            val matchesSearch = note.title.contains(searchQuery, ignoreCase = true) ||
                    note.content.contains(searchQuery, ignoreCase = true) ||
                    (note.lessonExtractedText?.contains(searchQuery, ignoreCase = true) == true)
            val matchesType = if (selectedTypeFilter == "الكل") true else note.type == selectedTypeFilter
            matchesSearch && matchesType
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    newTitle = ""
                    newContent = ""
                    newType = "ملاحظة"
                    selectedLinkedAppealId = null
                    aiSuggestedLesson = null
                    showAiSuggestionInDialog = false
                    showAddNoteDialog = true
                },
                containerColor = AtharTealPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "ملاحظة جديدة")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header & Title
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.EditNote,
                                contentDescription = null,
                                tint = AtharTealPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ملاحظاتي والدروس المستفادة 📝",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AtharAmberSecondary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${notes.count { it.isLesson }} دروس مسجلة",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("ابحث في ملاحظاتك والدروس المستفادة...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "مسح")
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filter Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(typeFilters) { filter ->
                            val isSelected = selectedTypeFilter == filter
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedTypeFilter = filter },
                                label = { Text(filter, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AtharTealPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )
                        }
                    }
                }
            }

            // Notes List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredNotes.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NoteAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "لم تدون أي ملاحظات بعد",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "سجل ملاحظاتك السريعة وتجاربك الميدانية وحولها إلى دروس مستفادة بضغطة زر.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { showAddNoteDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = AtharTealPrimary)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("أضف أول ملاحظة")
                                }
                            }
                        }
                    }
                } else {
                    items(filteredNotes) { note ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (note.isLesson) AtharAmberSecondary.copy(alpha = 0.06f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Top row: Tag & Date & Delete
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (note.isLesson) Color(0xFFF59E0B).copy(alpha = 0.15f)
                                        else AtharTealPrimary.copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (note.isLesson) {
                                                Icon(
                                                    Icons.Default.School,
                                                    contentDescription = null,
                                                    tint = Color(0xFFB45309),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                            Text(
                                                text = note.type,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (note.isLesson) Color(0xFFB45309) else AtharTealPrimary
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = note.createdAt,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        IconButton(
                                            onClick = {
                                                AtharRepository.deleteNote(note.id)
                                                Toast.makeText(context, "تم حذف الملاحظة", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.DeleteOutline,
                                                contentDescription = "حذف",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Title
                                Text(
                                    text = note.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Content
                                Text(
                                    text = note.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 20.sp
                                )

                                // Formatted Lesson Box if converted
                                if (note.isLesson && note.lessonExtractedText != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFFEF3C7),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.AutoAwesome,
                                                    contentDescription = null,
                                                    tint = Color(0xFFB45309),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "الدرس المستفاد المصاغ:",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF78350F)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = note.lessonExtractedText,
                                                fontSize = 12.sp,
                                                color = Color(0xFF78350F),
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }
                                }

                                // Linked Appeal if available
                                if (note.linkedAppealTitle != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Link,
                                            contentDescription = null,
                                            tint = AtharTealPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "مرتبطة بـ: ${note.linkedAppealTitle}",
                                            fontSize = 11.sp,
                                            color = AtharTealPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                // If not converted to lesson, offer AI suggestion button
                                if (!note.isLesson) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    TextButton(
                                        onClick = {
                                            val generated = when {
                                                note.content.contains("أدوات", ignoreCase = true) || note.content.contains("مشكلة", ignoreCase = true) ->
                                                    "يجب التأكد من توفر وفحص الأدوات والمعدات قبل موعد النشاط بـ 48 ساعة على الأقل لتفادي التأخير."
                                                note.content.contains("طلاب", ignoreCase = true) || note.content.contains("وقت", ignoreCase = true) ->
                                                    "تطبيق أسلوب الفترات التعليمية القصيرة (25 دقيقة تركيز + 5 دقائق استراحة) لرفع التفاعل."
                                                else ->
                                                    "توثيق آلية العمل مسبقاً وتوزيع المسؤوليات على أعضاء الفريق بشكل واضح يضمن النجاح المستدام."
                                            }
                                            AtharRepository.convertNoteToLesson(note.id, generated)
                                            Toast.makeText(context, "تم تحويل الملاحظة إلى درس مستفاد (+30 نقطة أثر) ✨", Toast.LENGTH_LONG).show()
                                        },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AtharAmberSecondary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("تحويل إلى درس مستفاد عبر AI 💡", fontSize = 11.sp, color = AtharAmberSecondary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Fast Add Note Dialog with Real-time AI Lesson Conversion Suggestion
    if (showAddNoteDialog) {
        val linkedOptions = listOf("بدون ربط") + appeals.map { it.title }
        var linkedSelection by remember { mutableStateOf("بدون ربط") }

        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text("تدوين ملاحظة جديدة", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("عنوان الملاحظة") },
                        placeholder = { Text("مثلاً: توفير أدوات الورشة") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newContent,
                        onValueChange = {
                            newContent = it
                            // Smart detection of lessons / challenges
                            if (it.contains("مشكلة", ignoreCase = true) ||
                                it.contains("أدوات", ignoreCase = true) ||
                                it.contains("تأخر", ignoreCase = true) ||
                                it.contains("تجربة", ignoreCase = true)
                            ) {
                                aiSuggestedLesson = "يجب التأكد من توفر الأدوات والموارد قبل النشاط بوقت كافٍ وفحصها مسبقاً."
                                showAiSuggestionInDialog = true
                            }
                        },
                        label = { Text("نص الملاحظة أو التجربة") },
                        placeholder = { Text("واجهنا مشكلة في توفير الأدوات قبل بداية النشاط...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // AI Suggestion Box inside Dialog
                    AnimatedVisibility(visible = showAiSuggestionInDialog && aiSuggestedLesson != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEF3C7),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFFB45309),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "اقتراح AI: هل تريد تحويلها إلى درس مستفاد؟",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF78350F)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = aiSuggestedLesson ?: "",
                                    fontSize = 11.sp,
                                    color = Color(0xFF78350F)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            newType = "درس مستفاد"
                                            Toast.makeText(context, "تم اعتماد صياغة الدرس المستفاد", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Text("قبول الصياغة ✓", fontSize = 11.sp, color = Color(0xFF047857), fontWeight = FontWeight.Bold)
                                    }
                                    TextButton(onClick = { showAiSuggestionInDialog = false }) {
                                        Text("تجاهل", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Type Picker
                    Text("نوع المحتوى:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    val types = listOf("ملاحظة", "فكرة", "قرار", "درس مستفاد", "تجربة")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        items(types) { t ->
                            FilterChip(
                                selected = newType == t,
                                onClick = { newType = t },
                                label = { Text(t, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isBlank() && newContent.isBlank()) {
                            Toast.makeText(context, "يرجى كتابة نص الملاحظة", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val titleToUse = if (newTitle.isNotBlank()) newTitle else newContent.take(25) + "..."
                        val isLessonFinal = newType == "درس مستفاد" || showAiSuggestionInDialog
                        val lessonContent = if (isLessonFinal) aiSuggestedLesson else null

                        val linkedAppeal = appeals.firstOrNull { it.title == linkedSelection }

                        AtharRepository.addNote(
                            title = titleToUse,
                            content = newContent,
                            type = if (isLessonFinal) "درس مستفاد" else newType,
                            linkedAppealId = linkedAppeal?.id,
                            linkedAppealTitle = linkedAppeal?.title,
                            isLesson = isLessonFinal,
                            lessonText = lessonContent
                        )
                        showAddNoteDialog = false
                        Toast.makeText(context, "تم حفظ الملاحظة بنجاح", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AtharTealPrimary)
                ) {
                    Text("حفظ الملاحظة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) { Text("إلغاء") }
            }
        )
    }
}
