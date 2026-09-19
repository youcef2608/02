package com.example.data.model

data class UserProfile(
    val id: String = "user_1",
    val name: String = "أحمد المنذري",
    val nickname: String = "أبو وسام",
    val anonymousId: String = "مستخدم أثر #4829",
    val displayNamePreference: String = "REAL_NAME", // "REAL_NAME", "NICKNAME", "ANONYMOUS"
    val email: String = "ahmed.almandhari@athar.om",
    val city: String = "مسقط",
    val governorate: String = "محافظة مسقط",
    val interests: List<String> = listOf("الروبوتات", "التكنولوجيا", "البيئة", "البرمجة"),
    val skills: List<String> = listOf("برمجة بايثون", "تنظيم فعاليات", "صيانة حواسيب"),
    val goals: List<String> = listOf("المشاركة في مبادرات", "التطوع", "التعلم"),
    val experienceLevel: String = "متوسط",
    val willingToVolunteer: Boolean = true,
    val willingToHelp: Boolean = true,
    val availableTimes: List<String> = listOf("عطلة نهاية الأسبوع", "الفترة المسائية"),
    val preferredAppealTypes: List<String> = listOf("ورشة", "تطوع", "تقنية", "تعليم"),
    val maxTravelDistanceKm: Int = 15,
    val useLocation: Boolean = true,
    val searchRadiusKm: Int = 10,
    val userLat: Double = 23.5880,
    val userLng: Double = 58.3829,
    val notificationsEnabled: Boolean = true,
    val notificationTypes: List<String> = listOf("NEARBY", "MATCHING", "REMINDER", "AI"),
    val impactScore: Int = 980,
    val isOnboarded: Boolean = true,
    val isLoggedIn: Boolean = false,
    val isGuest: Boolean = false
)

data class Appeal(
    val id: String,
    val title: String,
    val description: String,
    val type: String, // ورشة، تطوع، مساعدة، مبادرة، تقنية، تعليم، موارد
    val category: String, // الروبوتات، البيئة، التعليم، البرمجة، التطوع
    val distanceKm: Double,
    val latitude: Double,
    val longitude: Double,
    val city: String,
    val locationName: String,
    val date: String,
    val time: String,
    val duration: String,
    val creatorName: String,
    val participantsCount: Int,
    val requiredParticipants: Int,
    val priority: String, // عاجل، مرتفع، عادي
    val status: String, // نشط، يستقبل الردود، قيد التنفيذ، مكتمل
    val isSaved: Boolean = false,
    val requiredSkills: List<String> = emptyList(),
    val requiredResources: String = "",
    val conditions: String = "",
    val safetyInfo: String = "",
    val userResponse: String? = null, // "أستطيع المساعدة", "أرغب في المشاركة", etc.
    val responseStatus: String? = null // "تم إرسال الرد", "قيد المراجعة", "تم القبول"
)

data class NoteItem(
    val id: String,
    val title: String,
    val content: String,
    val type: String, // ملاحظة، فكرة، قرار، تذكير، تجربة، درس مستفاد
    val linkedAppealId: String? = null,
    val linkedAppealTitle: String? = null,
    val isLesson: Boolean = false,
    val lessonExtractedText: String? = null,
    val createdAt: String
)

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val points: Int,
    val participationsCount: Int,
    val lessonsCount: Int,
    val completedTasksCount: Int,
    val isCurrentUser: Boolean = false,
    val avatarInitial: String = "أ"
)

data class AtharNotification(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val type: String, // NEARBY, REMINDER, TEAM, AI, APPEAL_UPDATE
    val isRead: Boolean = false,
    val targetAppealId: String? = null
)

data class ChatMessage(
    val id: String,
    val sender: String, // "AI" or "USER"
    val text: String,
    val timestamp: String,
    val actionSuggestion: String? = null
)
