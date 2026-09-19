package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.repository.AtharRepository
import com.example.ui.theme.AtharAmberSecondary
import com.example.ui.theme.AtharTealPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtharAssistantScreen() {
    val messages by AtharRepository.chatMessages.collectAsState()
    val appeals by AtharRepository.appeals.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val promptSuggestions = listOf(
        "خطط لمبادرة تعليم روبوتات للأطفال 🤖",
        "ما الذي بقي في مبادرتي الحالية؟ 📋",
        "كيف أصيغ درساً مستفاداً مؤثراً؟ 💡",
        "حل لمشكلة نقص الموارد الميدانية 🛠️"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = AtharTealPrimary.copy(alpha = 0.12f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = AtharTealPrimary,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "مساعد أثر الذكي",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "متصل",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "مستشارك المتخصص في تخطيط المبادرات وتوثيق الأثر",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages) { msg ->
                val isAi = msg.sender == "AI"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
                ) {
                    if (isAi) {
                        Surface(
                            shape = CircleShape,
                            color = AtharTealPrimary.copy(alpha = 0.12f),
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.Top)
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = AtharTealPrimary,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Card(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isAi) 4.dp else 16.dp,
                            bottomEnd = if (isAi) 16.dp else 4.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAi) MaterialTheme.colorScheme.surface
                            else AtharTealPrimary
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.widthIn(max = 310.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isAi) MaterialTheme.colorScheme.onSurface else Color.White,
                                lineHeight = 22.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.timestamp,
                                fontSize = 10.sp,
                                color = if (isAi) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xCCFFFFFF),
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }

                    if (!isAi) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = AtharAmberSecondary.copy(alpha = 0.15f),
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.Top)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = AtharAmberSecondary,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick Suggestion Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(promptSuggestions) { prompt ->
                SuggestionChip(
                    onClick = {
                        val currentAppeal = appeals.firstOrNull { it.id == "appeal_1" }
                        AtharRepository.sendChatMessage(prompt, currentAppeal)
                        coroutineScope.launch {
                            listState.animateScrollToItem(messages.size)
                        }
                    },
                    label = { Text(prompt, fontSize = 11.sp) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Bottom Input Row
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("اسأل مساعد أثر أو خطط لمبادرة...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val txt = inputText
                            inputText = ""
                            val currentAppeal = appeals.firstOrNull { it.id == "appeal_1" }
                            AtharRepository.sendChatMessage(txt, currentAppeal)
                            coroutineScope.launch {
                                listState.animateScrollToItem(messages.size)
                            }
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AtharTealPrimary)
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "إرسال",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
