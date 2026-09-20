package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Appeal
import com.example.data.repository.AtharRepository
import com.example.ui.theme.*
import kotlin.math.roundToInt

data class CityLocation(
    val name: String,
    val lat: Double,
    val lng: Double,
    val zoomLevel: Float = 1.6f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onAppealClick: (String) -> Unit,
    onNavigateToAssistant: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    val appeals by AtharRepository.appeals.collectAsState()
    val userProfile by AtharRepository.userProfile.collectAsState()
    val notifications by AtharRepository.notifications.collectAsState()
    val unreadNotifCount = remember(notifications) { notifications.count { !it.isRead } }

    // Map Navigation State: Pan & Zoom
    var zoom by remember { mutableFloatStateOf(1.2f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Map style: 0 = Dark Radar, 1 = Satellite / Topographical
    var mapStyle by remember { mutableIntStateOf(0) }

    // Selected Appeal on Map
    var selectedAppealId by remember { mutableStateOf<String?>(null) }
    val selectedAppeal = remember(selectedAppealId, appeals) {
        appeals.firstOrNull { it.id == selectedAppealId }
    }

    // Filter by type: "الكل", "ترشيح AI ✨", "عاجل 🔥", "ورشة", "تطوع", "تقنية", "تعليم", "موارد"
    var selectedTypeFilter by remember { mutableStateOf("الكل") }

    val filteredAppeals = remember(appeals, selectedTypeFilter, userProfile) {
        when (selectedTypeFilter) {
            "الكل" -> appeals
            "ترشيح AI ✨" -> appeals.filter { app ->
                app.type == "تقنية" || app.type == "ورشة" || app.distanceKm <= 5.0 || app.priority == "عاجل"
            }
            "عاجل 🔥" -> appeals.filter { app ->
                app.priority == "عاجل" || app.priority == "مرتفع" || (app.requiredParticipants - app.participantsCount) in 1..3
            }
            else -> appeals.filter { it.type == selectedTypeFilter }
        }
    }

    // Pulse animation for markers
    val infiniteTransition = rememberInfiniteTransition(label = "LiveMapPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha"
    )

    // Animated 360-degree radar sweep angle
    val radarAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarAngle"
    )

    // Animated trajectory pulse dot (travels 0f..1f from user to destination)
    val trajectoryProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "TrajectoryPulse"
    )

    // AI Map Assistant modal state
    var showAiMapDialog by remember { mutableStateOf(false) }

    // Geographic Projection Helper for National Algerian Territory
    fun algeriaNormX(lng: Double): Float =
        ((lng - (-8.7)) / (12.0 - (-8.7))).toFloat().coerceIn(0.04f, 0.96f)

    fun algeriaNormY(lat: Double): Float =
        (1.0 - (lat - 18.0) / (37.5 - 18.0)).toFloat().coerceIn(0.04f, 0.96f)

    // Quick Jump Algerian Wilayas Presets
    val cities = listOf(
        CityLocation("الجزائر العاصمة", 36.7538, 3.0588, 2.0f),
        CityLocation("وهران", 35.6987, -0.6349, 1.9f),
        CityLocation("قسنطينة", 36.3650, 6.6147, 1.9f),
        CityLocation("سطيف", 36.1905, 5.4137, 1.8f),
        CityLocation("عنابة", 36.9000, 7.7667, 1.8f),
        CityLocation("البليدة", 36.4700, 2.8300, 2.0f),
        CityLocation("تلمسان", 34.8783, -1.3150, 1.8f),
        CityLocation("ورقلة", 31.9500, 5.3300, 1.6f),
        CityLocation("أدرار", 27.8742, -0.2939, 1.5f)
    )

    // Reset center helper
    fun centerOnCity(city: CityLocation) {
        zoom = city.zoomLevel
        val normX = algeriaNormX(city.lng)
        val normY = algeriaNormY(city.lat)
        panOffsetX = -(normX - 0.5f) * 600f * zoom
        panOffsetY = -(normY - 0.5f) * 600f * zoom
    }

    // A single, completely dedicated full-screen map canvas
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (mapStyle == 0) Color(0xFF070D18) else Color(0xFF0F1E2E))
    ) {
        // 1. Interactive Panning, Zooming and Marker Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, gestureZoom, _ ->
                        zoom = (zoom * gestureZoom).coerceIn(0.8f, 4.0f)
                        panOffsetX += pan.x
                        panOffsetY += pan.y
                    }
                }
                .pointerInput(filteredAppeals, zoom, panOffsetX, panOffsetY) {
                    detectTapGestures { tapPos ->
                        val w = size.width
                        val h = size.height

                        // Check if tap hit any appeal marker
                        var clickedAppeal: Appeal? = null
                        var minDistanceSq = 48f * 48f // 48px touch radius for accessibility

                        filteredAppeals.forEach { app ->
                            val normX = algeriaNormX(app.longitude)
                            val normY = algeriaNormY(app.latitude)
                            val markerX = (normX * w * zoom) + panOffsetX + (w * (1f - zoom) / 2f)
                            val markerY = (normY * h * zoom) + panOffsetY + (h * (1f - zoom) / 2f)

                            val distSq = (tapPos.x - markerX) * (tapPos.x - markerX) + (tapPos.y - markerY) * (tapPos.y - markerY)
                            if (distSq < minDistanceSq) {
                                minDistanceSq = distSq
                                clickedAppeal = app
                            }
                        }

                        if (clickedAppeal != null) {
                            selectedAppealId = clickedAppeal?.id
                        } else {
                            // Tapping background dismisses selected card
                            selectedAppealId = null
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height

            clipRect {
                // Background Coordinate Grid
                val gridStep = 80f * zoom
                val startX = (panOffsetX % gridStep)
                val startY = (panOffsetY % gridStep)

                var currentX = startX
                while (currentX < w) {
                    drawLine(
                        color = if (mapStyle == 0) Color(0x102DD4BF) else Color(0x12FFFFFF),
                        start = Offset(currentX, 0f),
                        end = Offset(currentX, h),
                        strokeWidth = 1f
                    )
                    currentX += gridStep
                }

                var currentY = startY
                while (currentY < h) {
                    drawLine(
                        color = if (mapStyle == 0) Color(0x102DD4BF) else Color(0x12FFFFFF),
                        start = Offset(0f, currentY),
                        end = Offset(w, currentY),
                        strokeWidth = 1f
                    )
                    currentY += gridStep
                }

                // Algeria Mediterranean Coastline Contour Curves
                val coastPath = Path().apply {
                    val p1 = Offset(
                        (algeriaNormX(-2.0) * w * zoom) + panOffsetX + (w * (1f - zoom) / 2f),
                        (algeriaNormY(35.1) * h * zoom) + panOffsetY + (h * (1f - zoom) / 2f)
                    )
                    val p2 = Offset(
                        (algeriaNormX(-0.6) * w * zoom) + panOffsetX + (w * (1f - zoom) / 2f),
                        (algeriaNormY(35.7) * h * zoom) + panOffsetY + (h * (1f - zoom) / 2f)
                    )
                    val p3 = Offset(
                        (algeriaNormX(1.3) * w * zoom) + panOffsetX + (w * (1f - zoom) / 2f),
                        (algeriaNormY(36.3) * h * zoom) + panOffsetY + (h * (1f - zoom) / 2f)
                    )
                    val p4 = Offset(
                        (algeriaNormX(3.06) * w * zoom) + panOffsetX + (w * (1f - zoom) / 2f),
                        (algeriaNormY(36.75) * h * zoom) + panOffsetY + (h * (1f - zoom) / 2f)
                    )
                    val p5 = Offset(
                        (algeriaNormX(5.1) * w * zoom) + panOffsetX + (w * (1f - zoom) / 2f),
                        (algeriaNormY(36.75) * h * zoom) + panOffsetY + (h * (1f - zoom) / 2f)
                    )
                    val p6 = Offset(
                        (algeriaNormX(7.77) * w * zoom) + panOffsetX + (w * (1f - zoom) / 2f),
                        (algeriaNormY(36.9) * h * zoom) + panOffsetY + (h * (1f - zoom) / 2f)
                    )

                    moveTo(p1.x, p1.y)
                    quadraticBezierTo(p2.x, p2.y, p3.x, p3.y)
                    quadraticBezierTo(p4.x, p4.y, p5.x, p5.y)
                    quadraticBezierTo((p5.x + p6.x) / 2, (p5.y + p6.y) / 2, p6.x, p6.y)
                }

                drawPath(
                    path = coastPath,
                    color = if (mapStyle == 0) Color(0x352DD4BF) else Color(0x5538BDF8),
                    style = Stroke(width = 3f * zoom.coerceIn(1f, 2.5f))
                )

                // Coastline soft ambient glow
                drawPath(
                    path = coastPath,
                    color = if (mapStyle == 0) Color(0x122DD4BF) else Color(0x2038BDF8),
                    style = Stroke(width = 12f * zoom.coerceIn(1f, 2.5f))
                )

                // Algeria Major Wilayas Heat Zones / Activity Auras (Algiers, Oran, Constantine, Setif, Annaba)
                val heatHubs = listOf(
                    Triple(36.7538, 3.0588, Color(0x282DD4BF)), // Algiers
                    Triple(35.6987, -0.6349, Color(0x2210B981)), // Oran
                    Triple(36.3650, 6.6147, Color(0x1E38BDF8)), // Constantine
                    Triple(36.1905, 5.4137, Color(0x1EF59E0B)), // Setif
                    Triple(36.9000, 7.7667, Color(0x18818CF8))  // Annaba
                )

                heatHubs.forEach { (lat, lng, auraColor) ->
                    val hubNormX = algeriaNormX(lng)
                    val hubNormY = algeriaNormY(lat)
                    val hubPos = Offset(
                        (hubNormX * w * zoom) + panOffsetX + (w * (1f - zoom) / 2f),
                        (hubNormY * h * zoom) + panOffsetY + (h * (1f - zoom) / 2f)
                    )
                    drawCircle(auraColor, radius = 45f * zoom.coerceIn(0.8f, 2.0f), center = hubPos)
                    drawCircle(auraColor.copy(alpha = 0.5f), radius = 24f * zoom.coerceIn(0.8f, 2.0f), center = hubPos)
                }

                // User approximate location indicator (Algiers Center: 36.7538, 3.0588)
                val userNormX = algeriaNormX(3.0588)
                val userNormY = algeriaNormY(36.7538)
                val userPos = Offset(
                    (userNormX * w * zoom) + panOffsetX + (w * (1f - zoom) / 2f),
                    (userNormY * h * zoom) + panOffsetY + (h * (1f - zoom) / 2f)
                )

                // 360-Degree Animated High-Tech Radar Sweep Beam (from user position)
                val radarSweepRadius = 320f * zoom.coerceIn(0.8f, 2.2f)
                rotate(degrees = radarAngle, pivot = userPos) {
                    // Radar sweep line
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0x8810B981), Color(0x0010B981)),
                            startX = userPos.x,
                            endX = userPos.x + radarSweepRadius
                        ),
                        start = userPos,
                        end = userPos + Offset(radarSweepRadius, 0f),
                        strokeWidth = 2f
                    )
                    // Translucent radar scan cone
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(Color.Transparent, Color(0x2210B981), Color.Transparent),
                            center = userPos
                        ),
                        startAngle = -25f,
                        sweepAngle = 25f,
                        useCenter = true,
                        topLeft = userPos - Offset(radarSweepRadius, radarSweepRadius),
                        size = Size(radarSweepRadius * 2, radarSweepRadius * 2)
                    )
                }

                // Range radar ring circles around user
                drawCircle(
                    color = Color(0x1810B981),
                    radius = 120f * zoom.coerceIn(0.8f, 2.0f),
                    center = userPos,
                    style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f))
                )
                drawCircle(
                    color = Color(0x1010B981),
                    radius = 240f * zoom.coerceIn(0.8f, 2.0f),
                    center = userPos,
                    style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f))
                )

                // User core icon
                drawCircle(Color(0x3310B981), radius = 26f * zoom.coerceIn(0.9f, 1.6f), center = userPos)
                drawCircle(Color(0x7710B981), radius = 14f * zoom.coerceIn(0.9f, 1.6f), center = userPos)
                drawCircle(Color(0xFF10B981), radius = 7f * zoom.coerceIn(0.9f, 1.6f), center = userPos)
                drawCircle(Color.White, radius = 3f * zoom.coerceIn(0.9f, 1.6f), center = userPos)

                // Dynamic Navigation Trajectory Beam (If an appeal is selected)
                if (selectedAppeal != null) {
                    val selNormX = algeriaNormX(selectedAppeal.longitude)
                    val selNormY = algeriaNormY(selectedAppeal.latitude)
                    val targetPos = Offset(
                        (selNormX * w * zoom) + panOffsetX + (w * (1f - zoom) / 2f),
                        (selNormY * h * zoom) + panOffsetY + (h * (1f - zoom) / 2f)
                    )

                    // Trajectory Glow Line
                    drawLine(
                        color = Color(0x332DD4BF),
                        start = userPos,
                        end = targetPos,
                        strokeWidth = 6f
                    )

                    // Holographic Dashed Route Line
                    drawLine(
                        color = Color(0xFF2DD4BF),
                        start = userPos,
                        end = targetPos,
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )

                    // Animated Travelling Energy Pulse Dot
                    val pulseX = userPos.x + (targetPos.x - userPos.x) * trajectoryProgress
                    val pulseY = userPos.y + (targetPos.y - userPos.y) * trajectoryProgress
                    val travelPulsePos = Offset(pulseX, pulseY)
                    drawCircle(Color(0xFFF59E0B), radius = 5.5f, center = travelPulsePos)
                    drawCircle(Color(0x66F59E0B), radius = 11f, center = travelPulsePos)
                }

                // Draw Live Appeal Markers directly on map
                filteredAppeals.forEach { app ->
                    val isSelected = app.id == selectedAppealId
                    val isAiRecommended = app.type == "تقنية" || app.type == "ورشة" || app.distanceKm <= 5.0 || app.priority == "عاجل"
                    val normX = algeriaNormX(app.longitude)
                    val normY = algeriaNormY(app.latitude)
                    val markerPos = Offset(
                        (normX * w * zoom) + panOffsetX + (w * (1f - zoom) / 2f),
                        (normY * h * zoom) + panOffsetY + (h * (1f - zoom) / 2f)
                    )

                    val pinColor = when (app.type) {
                        "ورشة" -> Color(0xFF38BDF8)
                        "تطوع" -> Color(0xFF10B981)
                        "تقنية" -> Color(0xFF818CF8)
                        "تعليم" -> Color(0xFFF59E0B)
                        else -> Color(0xFFF43F5E)
                    }

                    val remaining = app.requiredParticipants - app.participantsCount

                    // Pulsing Ring on Selected, Urgent, or AI-recommended Markers
                    if (isSelected || remaining <= 2 || (isAiRecommended && selectedTypeFilter == "ترشيح AI ✨")) {
                        drawCircle(
                            color = (if (remaining <= 2) AtharUrgentRed else if (isAiRecommended) Color(0xFFF59E0B) else pinColor).copy(alpha = pulseAlpha),
                            radius = (40f * pulseScale) * zoom.coerceIn(0.9f, 1.5f),
                            center = markerPos,
                            style = Stroke(width = 2.5f)
                        )
                    }

                    // Golden aura for AI match
                    if (isAiRecommended) {
                        drawCircle(
                            color = Color(0x33F59E0B),
                            radius = (if (isSelected) 32f else 22f) * zoom.coerceIn(0.9f, 1.5f),
                            center = markerPos
                        )
                    }

                    // Outer halo glow
                    drawCircle(
                        color = pinColor.copy(alpha = if (isSelected) 0.5f else 0.25f),
                        radius = (if (isSelected) 28f else 18f) * zoom.coerceIn(0.9f, 1.5f),
                        center = markerPos
                    )

                    // Pin Head Background (White border)
                    drawCircle(
                        color = if (isSelected) Color(0xFF2DD4BF) else Color.White,
                        radius = (if (isSelected) 17f else 12f) * zoom.coerceIn(0.9f, 1.5f),
                        center = markerPos
                    )

                    // Pin Inner Solid Color
                    drawCircle(
                        color = pinColor,
                        radius = (if (isSelected) 13f else 9f) * zoom.coerceIn(0.9f, 1.5f),
                        center = markerPos
                    )

                    // Urgent fire indicator dot on top-right of pin
                    if (remaining <= 2) {
                        val urgentOffset = markerPos + Offset(10f * zoom.coerceIn(0.9f, 1.5f), -10f * zoom.coerceIn(0.9f, 1.5f))
                        drawCircle(Color(0xFFEF4444), radius = 5.5f, center = urgentOffset)
                        drawCircle(Color.White, radius = 2f, center = urgentOffset)
                    } else if (isAiRecommended) {
                        // AI recommended star badge on top-right
                        val aiOffset = markerPos + Offset(10f * zoom.coerceIn(0.9f, 1.5f), -10f * zoom.coerceIn(0.9f, 1.5f))
                        drawCircle(Color(0xFFF59E0B), radius = 4.5f, center = aiOffset)
                    }
                }
            }
        }

        // 2. Top Floating Navigation & Filter Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xF20B1322),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(Color(0x442DD4BF), Color(0x33F59E0B))),
                    width = 1.dp
                ),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    // Title and Quick Stats & Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (onBack != null) {
                                IconButton(
                                    onClick = onBack,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "رجوع",
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "خريطة أثر 🗺️",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AtharTealPrimary.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "${filteredAppeals.size} نداء",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2DD4BF),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Right Action Controls
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Login button if guest or not logged in
                            if (!userProfile.isLoggedIn || userProfile.isGuest) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = AtharAmberSecondary.copy(alpha = 0.2f),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = Brush.horizontalGradient(listOf(AtharAmberSecondary, Color(0xFFF59E0B))),
                                        width = 1.dp
                                    ),
                                    modifier = Modifier
                                        .clickable { onNavigateToAuth() }
                                        .padding(end = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Login,
                                            contentDescription = "تسجيل الدخول",
                                            tint = AtharAmberSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "دخول",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AtharAmberSecondary
                                        )
                                    }
                                }
                            }

                            // AI Assistant button
                            IconButton(
                                onClick = onNavigateToAssistant,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "مساعد أثر",
                                    tint = AtharTealPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Notifications button with badge
                            IconButton(
                                onClick = onNavigateToNotifications,
                                modifier = Modifier.size(32.dp)
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (unreadNotifCount > 0) {
                                            Badge(
                                                containerColor = AtharUrgentRed,
                                                contentColor = Color.White
                                            ) {
                                                Text(
                                                    text = if (unreadNotifCount > 9) "9+" else unreadNotifCount.toString(),
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "الإشعارات",
                                        tint = Color(0xFFCBD5E1),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Layer Switcher (Radar vs Satellite)
                            IconButton(
                                onClick = { mapStyle = if (mapStyle == 0) 1 else 0 },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (mapStyle == 0) Icons.Outlined.Layers else Icons.Default.Radar,
                                    contentDescription = "نمط الخريطة",
                                    tint = AtharAmberSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // City Quick-Jump Chips (Smooth navigation to regions in Oman)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(cities) { city ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0x331E293B),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.horizontalGradient(listOf(Color(0x332DD4BF), Color(0x11FFFFFF))),
                                    width = 0.8.dp
                                ),
                                modifier = Modifier.clickable { centerOnCity(city) }
                            ) {
                                Text(
                                    text = "${city.name} 📍",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFE2E8F0),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Type Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val filters = listOf("الكل", "ترشيح AI ✨", "عاجل 🔥", "ورشة", "تطوع", "تقنية", "تعليم", "موارد")
                        items(filters) { f ->
                            val isSelected = selectedTypeFilter == f
                            val isAiFilter = f == "ترشيح AI ✨"
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when {
                                    isSelected && isAiFilter -> AtharAmberSecondary
                                    isSelected -> AtharTealPrimary
                                    isAiFilter -> AtharAmberSecondary.copy(alpha = 0.15f)
                                    else -> Color(0x221E293B)
                                },
                                border = if (isAiFilter) CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.horizontalGradient(listOf(AtharAmberSecondary, Color(0xFFF59E0B))),
                                    width = 1.dp
                                ) else null,
                                modifier = Modifier.clickable { selectedTypeFilter = f }
                            ) {
                                Text(
                                    text = f,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected && isAiFilter -> Color(0xFF0F172A)
                                        isSelected -> Color.White
                                        isAiFilter -> AtharAmberSecondary
                                        else -> Color(0xFF94A3B8)
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Floating Zoom, Location, and AI Assistant Controls (Right side)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Ask AI on Map FAB
            FloatingActionButton(
                onClick = { showAiMapDialog = true },
                modifier = Modifier.size(44.dp),
                containerColor = AtharAmberSecondary,
                contentColor = Color(0xFF0F172A),
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "تحليل AI للخريطة", modifier = Modifier.size(20.dp))
            }

            // Zoom In (+)
            FloatingActionButton(
                onClick = { zoom = (zoom * 1.25f).coerceIn(0.8f, 4.0f) },
                modifier = Modifier.size(42.dp),
                containerColor = Color(0xF20F172A),
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "تكبير", modifier = Modifier.size(20.dp))
            }

            // Zoom Out (-)
            FloatingActionButton(
                onClick = { zoom = (zoom / 1.25f).coerceIn(0.8f, 4.0f) },
                modifier = Modifier.size(42.dp),
                containerColor = Color(0xF20F172A),
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "تصغير", modifier = Modifier.size(20.dp))
            }

            // Re-center / My Location (📍)
            FloatingActionButton(
                onClick = {
                    zoom = 1.6f
                    panOffsetX = 0f
                    panOffsetY = 0f
                },
                modifier = Modifier.size(44.dp),
                containerColor = AtharTealPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Outlined.MyLocation, contentDescription = "إعادة ضبط للمركز", modifier = Modifier.size(22.dp))
            }
        }

        // 4. Heads-Up Display (HUD) Telemetry bar (Bottom-Left)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xD80B1322),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(Color(0x442DD4BF), Color(0x2210B981))),
                width = 1.dp
            ),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 14.dp, bottom = if (selectedAppeal != null) 250.dp else 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Explore,
                    contentDescription = null,
                    tint = Color(0xFF2DD4BF),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "رادار أثر النشط 📡 • ${filteredAppeals.size} نداءات متاحة",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFCBD5E1)
                )
            }
        }

        // 5. Bottom Floating Active Appeal Card (Appears directly when any marker is tapped!)
        AnimatedVisibility(
            visible = selectedAppeal != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(14.dp)
        ) {
            if (selectedAppeal != null) {
                val remaining = maxOf(0, selectedAppeal.requiredParticipants - selectedAppeal.participantsCount)
                val isAiMatch = selectedAppeal.type == "تقنية" || selectedAppeal.type == "ورشة" || selectedAppeal.distanceKm <= 5.0 || selectedAppeal.priority == "عاجل"

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xF80B1322)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(Color(0x772DD4BF), Color(0x77F59E0B))),
                        width = 1.5.dp
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AtharTealPrimary.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = selectedAppeal.type,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2DD4BF),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${selectedAppeal.distanceKm} كم • ${selectedAppeal.city}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            // Dismiss button
                            IconButton(
                                onClick = { selectedAppealId = null },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "إغلاق",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = selectedAppeal.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "📍 ${selectedAppeal.locationName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1)
                        )

                        // Smart AI Match Insight
                        if (isAiMatch) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0x222DD4BF),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.horizontalGradient(listOf(Color(0x442DD4BF), Color(0x22F59E0B))),
                                    width = 0.8.dp
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = AtharAmberSecondary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "تحليل AI: توافق عالي بنسبة 98% • تم رسم خط الملاحة التفاعلي 🧭",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF2DD4BF)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (remaining <= 2) "بقي $remaining مقاعد فقط! 🔥" else "متبقي $remaining متطوعين",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (remaining <= 2) AtharUrgentRed else AtharAmberSecondary
                                )
                                Text(
                                    text = "${selectedAppeal.date} • ${selectedAppeal.time}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        centerOnCity(CityLocation(selectedAppeal.city, selectedAppeal.latitude, selectedAppeal.longitude, 2.2f))
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.MyLocation,
                                        contentDescription = null,
                                        tint = Color(0xFF2DD4BF),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "تتبع 🧭",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2DD4BF)
                                    )
                                }

                                Button(
                                    onClick = { onAppealClick(selectedAppeal.id) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AtharTealPrimary),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "انضم الآن",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. AI Map Intelligence Dialog
        if (showAiMapDialog) {
            AlertDialog(
                onDismissRequest = { showAiMapDialog = false },
                containerColor = Color(0xFF0F172A),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AtharAmberSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تحليل الذكاء الاصطناعي للخريطة 🤖",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "بناءً على موقعك واهتماماتك، تم تحليل خريطة عُمان وتقديم هذه التوصيات الذكية:",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )

                        // Smart recommendation 1: Nearest
                        val nearest = appeals.minByOrNull { it.distanceKm }
                        if (nearest != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0x331E293B),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.horizontalGradient(listOf(Color(0x442DD4BF), Color(0x11FFFFFF))),
                                    width = 1.dp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedAppealId = nearest.id
                                        centerOnCity(CityLocation(nearest.city, nearest.latitude, nearest.longitude, 2.2f))
                                        showAiMapDialog = false
                                    }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "🎯 أقرب نداء لموقعك (${nearest.distanceKm} كم)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2DD4BF)
                                        )
                                        Text(text = "تركيز 📍", fontSize = 10.sp, color = AtharAmberSecondary)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = nearest.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Smart recommendation 2: Urgent
                        val urgentAppeal = appeals.firstOrNull { it.priority == "عاجل" || (it.requiredParticipants - it.participantsCount) in 1..2 }
                        if (urgentAppeal != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0x331E293B),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.horizontalGradient(listOf(Color(0x44EF4444), Color(0x11FFFFFF))),
                                    width = 1.dp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedAppealId = urgentAppeal.id
                                        centerOnCity(CityLocation(urgentAppeal.city, urgentAppeal.latitude, urgentAppeal.longitude, 2.2f))
                                        showAiMapDialog = false
                                    }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "🔥 نداء عاجل بأمس الحاجة لمتطوعين",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AtharUrgentRed
                                        )
                                        Text(text = "تركيز 📍", fontSize = 10.sp, color = AtharAmberSecondary)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = urgentAppeal.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Smart recommendation 3: Full AI Chat
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AtharTealPrimary.copy(alpha = 0.15f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showAiMapDialog = false
                                    onNavigateToAssistant()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF2DD4BF), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "محادثة مفصلة مع المساعد الذكي 💬",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2DD4BF)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAiMapDialog = false }) {
                        Text(text = "إغلاق", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}
