package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Appeal
import com.example.data.repository.AtharRepository
import com.example.ui.components.AppealCard
import com.example.ui.theme.AtharAmberSecondary
import com.example.ui.theme.AtharTealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyAppealsScreen(
    onAppealClick: (String) -> Unit
) {
    val appeals by AtharRepository.appeals.collectAsState()
    val userProfile by AtharRepository.userProfile.collectAsState()

    var isMapView by remember { mutableStateOf(false) }
    var selectedRadiusKm by remember { mutableIntStateOf(10) }
    val radiusOptions = listOf(1, 5, 10, 25, 50, 100)

    var selectedPinAppealId by remember { mutableStateOf<String?>(null) }

    val filteredAppeals = remember(appeals, selectedRadiusKm) {
        if (selectedRadiusKm >= 100) appeals
        else appeals.filter { it.distanceKm <= selectedRadiusKm }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header & View Toggle
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = AtharTealPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "قريب منك 📍",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "نطاق بحثك: ${userProfile.city} • ${if (selectedRadiusKm >= 100) "جميع المناطق" else "$selectedRadiusKm كم"}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Toggle View Buttons (List vs Map)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(2.dp)
                    ) {
                        IconButton(
                            onClick = { isMapView = false },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!isMapView) AtharTealPrimary else Color.Transparent)
                        ) {
                            Icon(
                                Icons.Default.ViewList,
                                contentDescription = "قائمة",
                                tint = if (!isMapView) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { isMapView = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isMapView) AtharTealPrimary else Color.Transparent)
                        ) {
                            Icon(
                                Icons.Default.Map,
                                contentDescription = "خريطة",
                                tint = if (isMapView) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Radius selection chips
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "نصف القطر:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(radiusOptions) { radius ->
                            val isSelected = selectedRadiusKm == radius
                            val label = if (radius >= 100) "الكل" else "$radius كم"
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedRadiusKm = radius },
                                label = { Text(label, fontSize = 11.sp) },
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
        }

        if (isMapView) {
            // Interactive Map View with radar concentric circles and pins
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF0F172A))
            ) {
                // Interactive Radar Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    val centerOffset = Offset(size.width / 2f, size.height / 2f)

                    // Concentric radius circles
                    drawCircle(
                        color = Color(0x332DD4BF),
                        radius = size.minDimension * 0.42f,
                        center = centerOffset,
                        style = Stroke(width = 2f)
                    )
                    drawCircle(
                        color = Color(0x222DD4BF),
                        radius = size.minDimension * 0.28f,
                        center = centerOffset,
                        style = Stroke(width = 1.5f)
                    )
                    drawCircle(
                        color = Color(0x152DD4BF),
                        radius = size.minDimension * 0.14f,
                        center = centerOffset,
                        style = Stroke(width = 1f)
                    )

                    // Grid cross lines
                    drawLine(
                        color = Color(0x15FFFFFF),
                        start = Offset(centerOffset.x, 0f),
                        end = Offset(centerOffset.x, size.height),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = Color(0x15FFFFFF),
                        start = Offset(0f, centerOffset.y),
                        end = Offset(size.width, centerOffset.y),
                        strokeWidth = 1f
                    )

                    // User approximate location in center
                    drawCircle(
                        color = Color(0x442DD4BF),
                        radius = 24f,
                        center = centerOffset
                    )
                    drawCircle(
                        color = Color(0xFF2DD4BF),
                        radius = 10f,
                        center = centerOffset
                    )
                }

                // Map Pins Overlay for Nearby Appeals
                val offsets = listOf(
                    Offset(0.35f, 0.38f),
                    Offset(0.68f, 0.44f),
                    Offset(0.28f, 0.65f),
                    Offset(0.72f, 0.28f),
                    Offset(0.55f, 0.72f)
                )

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val w = maxWidth
                    val h = maxHeight

                    filteredAppeals.take(offsets.size).forEachIndexed { idx, appeal ->
                        val pos = offsets[idx]
                        val isSelected = selectedPinAppealId == appeal.id

                        Box(
                            modifier = Modifier
                                .offset(x = w * pos.x - 20.dp, y = h * pos.y - 20.dp)
                                .clickable { selectedPinAppealId = appeal.id }
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) AtharAmberSecondary else AtharTealPrimary,
                                tonalElevation = 6.dp,
                                modifier = Modifier.wrapContentSize()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when (appeal.type) {
                                            "ورشة" -> Icons.Default.PrecisionManufacturing
                                            "تطوع" -> Icons.Default.VolunteerActivism
                                            "تقنية" -> Icons.Default.Code
                                            "تعليم" -> Icons.Default.School
                                            else -> Icons.Default.Handshake
                                        },
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${appeal.distanceKm} كم",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom floating preview of selected or first appeal in map
                val activeAppeal = appeals.firstOrNull { it.id == selectedPinAppealId } ?: filteredAppeals.firstOrNull()
                if (activeAppeal != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAppealClick(activeAppeal.id) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = AtharTealPrimary.copy(alpha = 0.12f),
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Place,
                                        contentDescription = null,
                                        tint = AtharTealPrimary,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = activeAppeal.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "📍 ${activeAppeal.locationName} • تبعد ${activeAppeal.distanceKm} كم",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Button(
                                    onClick = { onAppealClick(activeAppeal.id) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AtharTealPrimary)
                                ) {
                                    Text("عرض", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Standard List View
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredAppeals.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "لا توجد نداءات ضمن مسافة $selectedRadiusKm كم",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "جرّب توسيع نطاق البحث إلى 25 كم أو 50 كم لاكتشاف مبادرات إضافية.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { selectedRadiusKm = 50 },
                                    colors = ButtonDefaults.buttonColors(containerColor = AtharTealPrimary)
                                ) {
                                    Text("توسيع النطاق إلى 50 كم")
                                }
                            }
                        }
                    }
                } else {
                    items(filteredAppeals) { appeal ->
                        AppealCard(
                            appeal = appeal,
                            userInterests = userProfile.interests,
                            onClick = { onAppealClick(appeal.id) },
                            onToggleSave = { AtharRepository.toggleSaveAppeal(appeal.id) }
                        )
                    }
                }
            }
        }
    }
}
