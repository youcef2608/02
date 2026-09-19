package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Appeal
import com.example.ui.theme.*

@Composable
fun AppealCard(
    appeal: Appeal,
    userInterests: List<String> = emptyList(),
    onClick: () -> Unit,
    onToggleSave: () -> Unit
) {
    val isSmartMatch = userInterests.any { it.equals(appeal.category, ignoreCase = true) }
    val remainingSpots = appeal.requiredParticipants - appeal.participantsCount
    val isUrgent = appeal.priority == "عاجل" || appeal.priority == "مرتفع" || remainingSpots in 1..3

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Smart Match Badge if relevant
            if (isSmartMatch) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AtharTealPrimary.copy(alpha = 0.1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "قد يهمك 🤖",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AtharTealPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "يتوافق مع اهتماماتك في ${appeal.category}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Top row: Type Tag, Urgency / Remaining Tag & Save Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Type Tag
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (appeal.type) {
                            "ورشة" -> AtharTealPrimary.copy(alpha = 0.12f)
                            "تطوع" -> Color(0xFF10B981).copy(alpha = 0.12f)
                            "تقنية" -> Color(0xFF3B82F6).copy(alpha = 0.12f)
                            "تعليم" -> Color(0xFF8B5CF6).copy(alpha = 0.12f)
                            else -> AtharAmberSecondary.copy(alpha = 0.12f)
                        }
                    ) {
                        Text(
                            text = appeal.type,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (appeal.type) {
                                "ورشة" -> AtharTealPrimary
                                "تطوع" -> Color(0xFF047857)
                                "تقنية" -> Color(0xFF1D4ED8)
                                "تعليم" -> Color(0xFF6D28D9)
                                else -> Color(0xFFB45309)
                            },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    // Urgency Badge with remaining slots (عاجل: بقي 2)
                    if (isUrgent) {
                        val urgentText = when {
                            remainingSpots == 2 -> "عاجل 🔥 بقي 2 فقط"
                            remainingSpots == 1 -> "عاجل ⚡ بقي مقعد 1"
                            remainingSpots > 0 -> "عاجل ⚡ بقي $remainingSpots مقاعد"
                            else -> "عاجل ⚡ مكتمل"
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AtharUrgentContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(AtharUrgentRed)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = urgentText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AtharUrgentOnContainer
                                )
                            }
                        }
                    }
                }

                // Save button
                IconButton(
                    onClick = onToggleSave,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (appeal.isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "حفظ",
                        tint = if (appeal.isSaved) AtharAmberSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = appeal.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Description summary
            Text(
                text = appeal.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Info row: Distance, Location & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Distance badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = null,
                        tint = AtharTealPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "تبعد ${appeal.distanceKm} كم",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AtharTealPrimary
                    )
                }

                // Date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = appeal.date,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Participants & Remaining spots
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        tint = if (remainingSpots in 1..2) AtharUrgentRed else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${appeal.participantsCount}/${appeal.requiredParticipants}",
                        fontSize = 12.sp,
                        fontWeight = if (remainingSpots in 1..2) FontWeight.Bold else FontWeight.Normal,
                        color = if (remainingSpots in 1..2) AtharUrgentRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom action & response status if any
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (appeal.userResponse != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF047857),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${appeal.userResponse} (${appeal.responseStatus ?: "مُرسل"})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f, fill = false)) {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = appeal.locationName,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AtharTealPrimary)
                ) {
                    Text("عرض التفاصيل", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
