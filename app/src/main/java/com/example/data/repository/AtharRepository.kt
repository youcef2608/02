package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.Appeal
import com.example.data.model.AtharNotification
import com.example.data.model.ChatMessage
import com.example.data.model.LeaderboardEntry
import com.example.data.model.NoteItem
import com.example.data.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

object AtharRepository {
    private const val DEFAULT_SERVER_URL = "http://10.0.2.2:3000"
    private const val SUPABASE_URL = "https://mrrnahcytpocnasnlijv.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1ycm5haGN5dHBvY25hc25saWp2Iiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc4OTg1MTQxMywiZXhwIjoyMTA1NDI3NDEzfQ.1UJ0RZpkTz_TP1_SzjWG2ELivvxBGww9VTxpyHmOez4"

    private var sharedPrefs: SharedPreferences? = null

    private val _serverSyncState = MutableStateFlow("متصل بقاعدة البيانات المركزية")
    val serverSyncState: StateFlow<String> = _serverSyncState.asStateFlow()

    fun initContext(context: Context) {
        val prefs = context.getSharedPreferences("athar_storage_prefs", Context.MODE_PRIVATE)
        sharedPrefs = prefs

        // Restore saved session from persistent storage
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        if (isLoggedIn) {
            val name = prefs.getString("user_name", "أحمد المنذري") ?: "أحمد المنذري"
            val email = prefs.getString("user_email", "ahmed@athar.om") ?: "ahmed@athar.om"
            val role = prefs.getString("user_role", "VOLUNTEER") ?: "VOLUNTEER"
            val roleTitle = prefs.getString("user_role_title", "متطوع ميداني نشط") ?: "متطوع ميداني نشط"
            val phone = prefs.getString("user_phone", "0550 12 34 56") ?: "0550 12 34 56"
            val wilaya = prefs.getString("user_wilaya", "16 - الجزائر العاصمة") ?: "16 - الجزائر العاصمة"
            val badge = prefs.getString("user_badge", "VOL-2026-018") ?: "VOL-2026-018"
            val assocName = prefs.getString("user_assoc_name", "") ?: ""
            val points = prefs.getInt("user_points", 980)
            val isOnboarded = prefs.getBoolean("is_onboarded", true)

            _userProfile.update {
                it.copy(
                    isLoggedIn = true,
                    isGuest = false,
                    name = name,
                    email = email,
                    role = role,
                    roleTitle = roleTitle,
                    phone = phone,
                    wilaya = wilaya,
                    badgeNumber = badge,
                    associationName = assocName,
                    impactScore = points,
                    isOnboarded = isOnboarded
                )
            }
        }
    }

    private fun persistUserSession(profile: UserProfile) {
        sharedPrefs?.edit()?.apply {
            putBoolean("is_logged_in", profile.isLoggedIn)
            putBoolean("is_guest", profile.isGuest)
            putString("user_name", profile.name)
            putString("user_email", profile.email)
            putString("user_role", profile.role)
            putString("user_role_title", profile.roleTitle)
            putString("user_phone", profile.phone)
            putString("user_wilaya", profile.wilaya)
            putString("user_badge", profile.badgeNumber)
            putString("user_assoc_name", profile.associationName)
            putInt("user_points", profile.impactScore)
            putBoolean("is_onboarded", profile.isOnboarded)
            apply()
        }
    }

    private fun clearUserSession() {
        sharedPrefs?.edit()?.clear()?.apply()
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            syncWithServer()
        }
    }

    suspend fun syncWithServer(serverUrl: String = DEFAULT_SERVER_URL) {
        var synced = false
        // 1. Try local node server first
        try {
            val url = URL("$serverUrl/api/sync")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 2500
            conn.readTimeout = 2500
            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val responseText = reader.readText()
                reader.close()
                val json = JSONObject(responseText)
                if (json.optBoolean("success")) {
                    val callsArray = json.optJSONArray("calls")
                    if (callsArray != null && callsArray.length() > 0) {
                        parseAndApplyAppeals(callsArray)
                        synced = true
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback to direct Supabase
        }

        // 2. Direct Supabase Fallback if server is offline or unreachable
        if (!synced) {
            try {
                val supaUrl = URL("$SUPABASE_URL/rest/v1/calls?select=*")
                val conn = supaUrl.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("apikey", SUPABASE_KEY)
                conn.setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                conn.connectTimeout = 3500
                conn.readTimeout = 3500
                if (conn.responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val responseText = reader.readText()
                    reader.close()
                    val callsArray = JSONArray(responseText)
                    if (callsArray.length() > 0) {
                        parseAndApplyAppeals(callsArray)
                    }
                }
            } catch (e: Exception) {
                // Keep default local data if both are unreachable
            }
        }
    }

    private fun parseAndApplyAppeals(callsArray: JSONArray) {
        val syncedAppeals = mutableListOf<Appeal>()
        for (i in 0 until callsArray.length()) {
            val c = callsArray.getJSONObject(i)
            val loc = c.optJSONObject("location")
            syncedAppeals.add(
                Appeal(
                    id = c.optString("id", "call_$i"),
                    title = c.optString("title", "نداء ميداني"),
                    description = c.optString("description", ""),
                    type = if (c.optString("category") == "help") "مساعدة" else "تطوع",
                    category = when (c.optString("category")) {
                        "help" -> "الإغاثة"
                        "volunteer" -> "البيئة"
                        "tech" -> "البرمجة"
                        "education" -> "التعليم"
                        else -> "التطوع"
                    },
                    distanceKm = 3.5,
                    latitude = loc?.optDouble("latitude", 36.7538) ?: 36.7538,
                    longitude = loc?.optDouble("longitude", 3.0588) ?: 3.0588,
                    city = loc?.optString("city", "الجزائر") ?: "الجزائر",
                    locationName = loc?.optString("placeName", "الجزائر") ?: "موقع ميداني",
                    date = c.optString("startTime", "2026-09-20").take(10),
                    time = "04:30 مساءً",
                    duration = "3 ساعات",
                    creatorName = c.optString("creatorOrg", c.optString("creatorName", "جمعية ناس الخير")),
                    participantsCount = c.optInt("confirmedCount", 0),
                    requiredParticipants = c.optInt("requiredCount", 10),
                    priority = if (c.optString("priority") == "urgent") "عاجل" else "عادي",
                    status = if (c.optString("status") == "active") "يستقبل الردود" else "نشط"
                )
            )
        }
        if (syncedAppeals.isNotEmpty()) {
            _appeals.value = syncedAppeals
        }
    }

    private val _userProfile = MutableStateFlow(
        UserProfile(
            id = "user_1",
            name = "متطوع أثر الجزائري",
            email = "volunteer@athar.dz",
            city = "الجزائر العاصمة",
            governorate = "ولاية الجزائر",
            interests = listOf("الإغاثة", "التكنولوجيا", "البيئة", "الإسعاف"),
            goals = listOf("المشاركة في مبادرات", "التطوع", "التعلم"),
            experienceLevel = "متوسط",
            useLocation = true,
            searchRadiusKm = 10,
            userLat = 36.7538,
            userLng = 3.0588,
            notificationsEnabled = true,
            isOnboarded = true
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _appeals = MutableStateFlow<List<Appeal>>(
        listOf(
            Appeal(
                id = "appeal_1",
                title = "ورشة بناء وتطوير الروبوتات للناشئين",
                description = "مبادرة تطوعية تهدف لتدريب طلاب المدارس على أساسيات برمجة وتجميع روبوتات تعليمية لتمكينهم من خوض مسابقات الروبوت الوطنية. نحتاج مدربين ومهتمين بالروبوتات للمساعدة في توجيه الفرق.",
                type = "ورشة",
                category = "الروبوتات",
                distanceKm = 3.4,
                latitude = 23.5930,
                longitude = 58.4010,
                city = "مسقط",
                locationName = "مركز الابتكار العلمي - القرم",
                date = "24 سبتمبر 2026",
                time = "04:30 مساءً",
                duration = "3 ساعات",
                creatorName = "نادي الروبوت والذكاء الاصطناعي",
                participantsCount = 18,
                requiredParticipants = 20,
                priority = "عاجل",
                status = "يستقبل الردود",
                isSaved = true,
                requiredSkills = listOf("برمجة بايثون", "تركيب دوائر إلكترونية", "توجيه الفرق"),
                requiredResources = "أجهزة حاسوب محمولة، حقائب أردوينو، مجسات حساسة",
                conditions = "العمر فوق 18 سنة للمدربين، شغف بمشاركة المعرفة",
                safetyInfo = "توفر حقيبة إسعافات أولية، بيئة عمل مجهزة بالكامل ومكيفة",
                userResponse = "أستطيع المساعدة",
                responseStatus = "تم قبول المشاركة"
            ),
            Appeal(
                id = "appeal_2",
                title = "حملة تشجير وتأهيل المتنزه الطبيعي",
                description = "دعوة مفتوحة لأهالي المنطقة والمتطوعين للمساهمة في غرس 500 شتلة برية محلية وترميم مسارات المشي لدعم التنوع البيئي ومكافحة التصحر.",
                type = "تطوع",
                category = "البيئة",
                distanceKm = 5.2,
                latitude = 23.6120,
                longitude = 58.4150,
                city = "مسقط",
                locationName = "متنزه الوادي الكبير الطبيعي",
                date = "26 سبتمبر 2026",
                time = "06:30 صباحاً",
                duration = "4 ساعات",
                creatorName = "جمعية حماة البيئة العمانية",
                participantsCount = 48,
                requiredParticipants = 50,
                priority = "عاجل",
                status = "نشط",
                isSaved = false,
                requiredSkills = listOf("العمل الميداني", "الغرس", "تنظيم المتطوعين"),
                requiredResources = "أدوات حفر، قفازات، مياه شرب",
                conditions = "ارتداء أحذية مريحة وقبعة شمسية",
                safetyInfo = "نقطة رعاية طبية متواجدة في الموقع طوال فترة الفعالية"
            ),
            Appeal(
                id = "appeal_3",
                title = "ملتقى تسريع وتطوير التطبيقات للمجتمع",
                description = "هاكاثون تقني لمدة يومين لحل تحديات لوجستية وتطوعية حقيقية وتحويلها إلى برمجيات مفتوحة المصدر تخدم الجمعيات الخيرية.",
                type = "تقنية",
                category = "البرمجة",
                distanceKm = 8.1,
                latitude = 23.5700,
                longitude = 58.3500,
                city = "مسقط",
                locationName = "واحة المعرفة مسقط - المبنى الرابع",
                date = "28 سبتمبر 2026",
                time = "09:00 صباحاً",
                duration = "يومان",
                creatorName = "مجتمع المطورين التقني",
                participantsCount = 45,
                requiredParticipants = 60,
                priority = "عادي",
                status = "يستقبل الردود",
                isSaved = true,
                requiredSkills = listOf("Kotlin", "Flutter", "تصميم واجهات UI/UX", "Backend"),
                requiredResources = "إنترنت عالي السرعة متوفر، غرف نقاش مجهزة",
                conditions = "الالتزام بالحضور طوال فترة التحدي",
                safetyInfo = "التزام بإرشادات السلامة العامة في المبنى"
            ),
            Appeal(
                id = "appeal_4",
                title = "برنامج دعم التحصيل الأكاديمي لطلاب الأسر المنتجة",
                description = "جلسات تعليمية تفاعلية لمساعدة طلاب المرحلة المتوسطة في مادتي الرياضيات والعلوم، وتقديم الإرشاد الدراسي والمهني.",
                type = "تعليم",
                category = "التعليم",
                distanceKm = 2.8,
                latitude = 23.5800,
                longitude = 58.3900,
                city = "مسقط",
                locationName = "مركز التنمية الاجتماعية - روي",
                date = "30 سبتمبر 2026",
                time = "05:00 مساءً",
                duration = "ساعتان",
                creatorName = "مبادرة علمني لأبني",
                participantsCount = 10,
                requiredParticipants = 12,
                priority = "عاجل",
                status = "نشط",
                isSaved = false,
                requiredSkills = listOf("مهارات تدريس", "شرح مادة الرياضيات والعلوم"),
                requiredResources = "سبورات بيضاء، دفاتر وأقلام للطلاب",
                conditions = "خبرة أو خلفية في التدريس أو التخصص العلمي",
                safetyInfo = "بيئة صفية آمنة ومراقبة"
            ),
            Appeal(
                id = "appeal_5",
                title = "توفير وتجهيز أجهزة حاسوب لمعمل قرائي",
                description = "نداء مساندة لتجميع وإعادة تهيئة وصيانة 10 أجهزة حاسوب مستعملة لتوزيعها على مكتبة ناشئة في منطقة قروية.",
                type = "موارد",
                category = "التكنولوجيا",
                distanceKm = 12.0,
                latitude = 23.5400,
                longitude = 58.3100,
                city = "السيب",
                locationName = "مكتبة المعرفة المجتمعية - الموالح",
                date = "02 أكتوبر 2026",
                time = "10:00 صباحاً",
                duration = "أسبوع",
                creatorName = "فريق العطاء الرقمي",
                participantsCount = 6,
                requiredParticipants = 8,
                priority = "عاجل",
                status = "يستقبل الردود",
                isSaved = false,
                requiredSkills = listOf("صيانة حواسيب", "تثبيت أنظمة تشغيل لينكس/ويندوز"),
                requiredResources = "أقراص تخزين SSD، شاشات، كابلات كهربائية",
                conditions = "تسليم الأجهزة مفحوصة وصالحة للاستخدام المكتبي",
                safetyInfo = "اتباع إجراءات السلامة الكهربائية أثناء الصيانة"
            )
        )
    )
    val appeals: StateFlow<List<Appeal>> = _appeals.asStateFlow()

    private val _notes = MutableStateFlow<List<NoteItem>>(
        listOf(
            NoteItem(
                id = "note_1",
                title = "تجهيز حقائب الأردوينو قبل ورشة الروبوتات",
                content = "واجهنا مشكلة في توفير الأدوات والأسلاك الكافية قبل بداية النشاط بنصف ساعة مما أخر انطلاق الورشة.",
                type = "درس مستفاد",
                linkedAppealId = "appeal_1",
                linkedAppealTitle = "ورشة بناء وتطوير الروبوتات للناشئين",
                isLesson = true,
                lessonExtractedText = "يجب التأكد من فحص وتوفر كافة الأدوات والمستهلكات قبل موعد النشاط بـ 48 ساعة على الأقل وحصر النواقص مسبقاً.",
                createdAt = "18 سبتمبر 2026"
            ),
            NoteItem(
                id = "note_2",
                title = "فكرة لتوزيع شتلات المتنزه",
                content = "تقسيم المتطوعين إلى 4 فرق صغيرة حسب ألوان الشارات يزيد من سرعة إنجاز غرس الأشجار بنسبة 40%.",
                type = "فكرة",
                linkedAppealId = "appeal_2",
                linkedAppealTitle = "حملة تشجير وتأهيل المتنزه الطبيعي",
                isLesson = false,
                lessonExtractedText = null,
                createdAt = "16 سبتمبر 2026"
            ),
            NoteItem(
                id = "note_3",
                title = "تنظيم وقت استراحة الطلاب",
                content = "جلسات الشرح الأكاديمي التي تزيد عن 45 دقيقة متواصلة تقلل من تركيز الطلاب؛ من الأفضل تطبيق أسلوب 25 دقيقة عمل و5 دقائق راحة.",
                type = "درس مستفاد",
                linkedAppealId = "appeal_4",
                linkedAppealTitle = "برنامج دعم التحصيل الأكاديمي",
                isLesson = true,
                lessonExtractedText = "اعتماد تقنية بومودورو التعليمية (25 دقيقة تركيز تليها 5 دقائق استراحة) لرفع استيعاب الطلاب ومشاركتهم النشطة.",
                createdAt = "12 سبتمبر 2026"
            )
        )
    )
    val notes: StateFlow<List<NoteItem>> = _notes.asStateFlow()

    private val _notifications = MutableStateFlow<List<AtharNotification>>(
        listOf(
            AtharNotification(
                id = "notif_1",
                title = "نشاط قريب يطابق اهتماماتك 🤖",
                message = "تمت إضافة ورشة روبوتات تبعد عنك 3.4 كم فقط في مركز الابتكار العلمي بالقرم.",
                time = "منذ 15 دقيقة",
                type = "NEARBY",
                isRead = false,
                targetAppealId = "appeal_1"
            ),
            AtharNotification(
                id = "notif_2",
                title = "تم قبول مشاركتك في المبادرة ✨",
                message = "رحب نادي الروبوت بانضمامك كمدرب مساعد في ورشة الناشئين القادمة.",
                time = "منذ ساعتين",
                type = "TEAM",
                isRead = false,
                targetAppealId = "appeal_1"
            ),
            AtharNotification(
                id = "notif_3",
                title = "اقتراح ذكي من أثر 💡",
                message = "وجدنا تجربة ودرساً مستفاداً سابقاً حول تجهيز حقائب الأردوينو قد يفيدك في نشاطك القادم.",
                time = "أمس",
                type = "AI",
                isRead = true,
                targetAppealId = "appeal_1"
            ),
            AtharNotification(
                id = "notif_4",
                title = "تذكير بموعد قريب ⏰",
                message = "حملة تشجير المتنزه الطبيعي تنطلق صباح الغد في تمام 06:30 صباحاً.",
                time = "منذ يومين",
                type = "REMINDER",
                isRead = true,
                targetAppealId = "appeal_2"
            )
        )
    )
    val notifications: StateFlow<List<AtharNotification>> = _notifications.asStateFlow()

    // Monthly Leaderboard - Current Month (September 2026)
    private val _currentLeaderboard = MutableStateFlow<List<LeaderboardEntry>>(
        listOf(
            LeaderboardEntry(rank = 1, name = "سارة المعمرية", points = 1250, participationsCount = 8, lessonsCount = 6, completedTasksCount = 14, isCurrentUser = false, avatarInitial = "س"),
            LeaderboardEntry(rank = 2, name = "أحمد المنذري", points = 980, participationsCount = 6, lessonsCount = 4, completedTasksCount = 11, isCurrentUser = true, avatarInitial = "أ"),
            LeaderboardEntry(rank = 3, name = "خالد الهنائي", points = 890, participationsCount = 5, lessonsCount = 3, completedTasksCount = 9, isCurrentUser = false, avatarInitial = "خ"),
            LeaderboardEntry(rank = 4, name = "مريم البلوشية", points = 760, participationsCount = 4, lessonsCount = 4, completedTasksCount = 8, isCurrentUser = false, avatarInitial = "م"),
            LeaderboardEntry(rank = 5, name = "فيصل العامري", points = 640, participationsCount = 4, lessonsCount = 2, completedTasksCount = 7, isCurrentUser = false, avatarInitial = "ف"),
            LeaderboardEntry(rank = 6, name = "أسماء الشيبانية", points = 530, participationsCount = 3, lessonsCount = 3, completedTasksCount = 6, isCurrentUser = false, avatarInitial = "أ"),
            LeaderboardEntry(rank = 7, name = "طارق الكندي", points = 420, participationsCount = 2, lessonsCount = 1, completedTasksCount = 4, isCurrentUser = false, avatarInitial = "ط")
        )
    )
    val currentLeaderboard: StateFlow<List<LeaderboardEntry>> = _currentLeaderboard.asStateFlow()

    // Archived Leaderboards for previous months (August 2026, July 2026)
    val archivedLeaderboards: Map<String, List<LeaderboardEntry>> = mapOf(
        "أغسطس 2026" to listOf(
            LeaderboardEntry(rank = 1, name = "خالد الهنائي", points = 1420, participationsCount = 9, lessonsCount = 7, completedTasksCount = 16, avatarInitial = "خ"),
            LeaderboardEntry(rank = 2, name = "سارة المعمرية", points = 1180, participationsCount = 7, lessonsCount = 5, completedTasksCount = 12, avatarInitial = "س"),
            LeaderboardEntry(rank = 3, name = "أحمد المنذري", points = 1040, participationsCount = 6, lessonsCount = 5, completedTasksCount = 11, isCurrentUser = true, avatarInitial = "أ"),
            LeaderboardEntry(rank = 4, name = "عبدالله الرواحي", points = 870, participationsCount = 5, lessonsCount = 3, completedTasksCount = 9, avatarInitial = "ع")
        ),
        "يوليو 2026" to listOf(
            LeaderboardEntry(rank = 1, name = "أحمد المنذري", points = 1350, participationsCount = 8, lessonsCount = 6, completedTasksCount = 15, isCurrentUser = true, avatarInitial = "أ"),
            LeaderboardEntry(rank = 2, name = "مريم البلوشية", points = 1210, participationsCount = 7, lessonsCount = 6, completedTasksCount = 13, avatarInitial = "م"),
            LeaderboardEntry(rank = 3, name = "خالد الهنائي", points = 990, participationsCount = 5, lessonsCount = 4, completedTasksCount = 10, avatarInitial = "خ")
        )
    )

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "msg_1",
                sender = "AI",
                text = "مرحبًا بك يا أحمد في مساعد أثر الذكي 👋\nأنا هنا لمساعدتك في تخطيط المبادرات، تنظيم المهام، وحل أي تحديات ميدانية. ما الذي تود إنجازه اليوم؟",
                timestamp = "الآن",
                actionSuggestion = "خطط لمبادرة جديدة"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // Actions
    fun toggleSaveAppeal(appealId: String) {
        _appeals.update { list ->
            list.map { if (it.id == appealId) it.copy(isSaved = !it.isSaved) else it }
        }
    }

    fun submitResponse(appealId: String, responseType: String, message: String) {
        _appeals.update { list ->
            list.map {
                if (it.id == appealId) {
                    it.copy(
                        userResponse = responseType,
                        responseStatus = "تم إرسال الرد",
                        participantsCount = it.participantsCount + 1
                    )
                } else it
            }
        }
        // Add points to current user in leaderboard (+50 points)
        _currentLeaderboard.update { list ->
            list.map {
                if (it.isCurrentUser) {
                    it.copy(
                        points = it.points + 50,
                        participationsCount = it.participationsCount + 1
                    )
                } else it
            }.sortedByDescending { it.points }.mapIndexed { index, item -> item.copy(rank = index + 1) }
        }

        // Send to Web Server & Supabase Database
        CoroutineScope(Dispatchers.IO).launch {
            var sent = false
            try {
                val url = URL("$DEFAULT_SERVER_URL/api/responses")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.connectTimeout = 2500
                conn.readTimeout = 2500
                conn.doOutput = true
                val payload = JSONObject().apply {
                    put("callId", appealId)
                    put("userName", _userProfile.value.name)
                    put("userPhone", "0550123456")
                    put("responseType", "can_help")
                    put("message", message)
                    put("status", "accepted")
                }
                conn.outputStream.write(payload.toString().toByteArray())
                if (conn.responseCode in 200..299) sent = true
            } catch (e: Exception) {}

            if (!sent) {
                try {
                    val supaUrl = URL("$SUPABASE_URL/rest/v1/responses")
                    val supaConn = supaUrl.openConnection() as HttpURLConnection
                    supaConn.requestMethod = "POST"
                    supaConn.setRequestProperty("Content-Type", "application/json")
                    supaConn.setRequestProperty("apikey", SUPABASE_KEY)
                    supaConn.setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                    supaConn.connectTimeout = 3500
                    supaConn.readTimeout = 3500
                    supaConn.doOutput = true
                    val payload = JSONObject().apply {
                        put("callId", appealId)
                        put("userName", _userProfile.value.name)
                        put("userPhone", "0550123456")
                        put("responseType", "can_help")
                        put("message", message)
                        put("status", "accepted")
                    }
                    supaConn.outputStream.write(payload.toString().toByteArray())
                    supaConn.responseCode
                } catch (se: Exception) {}
            }
        }
    }

    fun cancelResponse(appealId: String) {
        _appeals.update { list ->
            list.map {
                if (it.id == appealId) {
                    it.copy(
                        userResponse = null,
                        responseStatus = null,
                        participantsCount = maxOf(0, it.participantsCount - 1)
                    )
                } else it
            }
        }
    }

    fun addNote(
        title: String,
        content: String,
        type: String,
        linkedAppealId: String?,
        linkedAppealTitle: String?,
        isLesson: Boolean,
        lessonText: String?
    ) {
        val newNote = NoteItem(
            id = "note_${UUID.randomUUID()}",
            title = title,
            content = content,
            type = type,
            linkedAppealId = linkedAppealId,
            linkedAppealTitle = linkedAppealTitle,
            isLesson = isLesson,
            lessonExtractedText = lessonText,
            createdAt = "اليوم"
        )
        _notes.update { listOf(newNote) + it }

        if (isLesson) {
            // Add points for documenting lesson (+30 points)
            _currentLeaderboard.update { list ->
                list.map {
                    if (it.isCurrentUser) {
                        it.copy(
                            points = it.points + 30,
                            lessonsCount = it.lessonsCount + 1
                        )
                    } else it
                }.sortedByDescending { it.points }.mapIndexed { index, item -> item.copy(rank = index + 1) }
            }
        }

        // Send to Web Server Database (Notes) with Supabase Fallback
        CoroutineScope(Dispatchers.IO).launch {
            var noteSent = false
            try {
                val url = URL("$DEFAULT_SERVER_URL/api/notes")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.connectTimeout = 2500
                conn.readTimeout = 2500
                conn.doOutput = true
                val payload = JSONObject().apply {
                    put("senderName", _userProfile.value.name)
                    put("message", "$title: $content")
                    put("type", "field_observation")
                    put("status", "new")
                }
                conn.outputStream.write(payload.toString().toByteArray())
                if (conn.responseCode in 200..299) noteSent = true
            } catch (e: Exception) {}

            if (!noteSent) {
                try {
                    val supaUrl = URL("$SUPABASE_URL/rest/v1/notes")
                    val supaConn = supaUrl.openConnection() as HttpURLConnection
                    supaConn.requestMethod = "POST"
                    supaConn.setRequestProperty("Content-Type", "application/json")
                    supaConn.setRequestProperty("apikey", SUPABASE_KEY)
                    supaConn.setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                    supaConn.connectTimeout = 3500
                    supaConn.readTimeout = 3500
                    supaConn.doOutput = true
                    val payload = JSONObject().apply {
                        put("senderName", _userProfile.value.name)
                        put("message", "$title: $content")
                        put("type", "field_observation")
                        put("status", "new")
                    }
                    supaConn.outputStream.write(payload.toString().toByteArray())
                    supaConn.responseCode
                } catch (se: Exception) {}
            }
        }
    }

    fun convertNoteToLesson(noteId: String, lessonText: String) {
        _notes.update { list ->
            list.map {
                if (it.id == noteId) {
                    it.copy(
                        type = "درس مستفاد",
                        isLesson = true,
                        lessonExtractedText = lessonText
                    )
                } else it
            }
        }
        _currentLeaderboard.update { list ->
            list.map {
                if (it.isCurrentUser) {
                    it.copy(
                        points = it.points + 30,
                        lessonsCount = it.lessonsCount + 1
                    )
                } else it
            }.sortedByDescending { it.points }.mapIndexed { index, item -> item.copy(rank = index + 1) }
        }
    }

    fun deleteNote(noteId: String) {
        _notes.update { list -> list.filter { it.id != noteId } }
    }

    fun markNotificationRead(notifId: String) {
        _notifications.update { list ->
            list.map { if (it.id == notifId) it.copy(isRead = true) else it }
        }
    }

    fun markAllNotificationsRead() {
        _notifications.update { list -> list.map { it.copy(isRead = true) } }
    }

    fun updateProfile(
        interests: List<String>,
        goals: List<String>,
        experienceLevel: String,
        useLocation: Boolean,
        searchRadiusKm: Int,
        city: String,
        governorate: String,
        skills: List<String> = emptyList(),
        availableTimes: List<String> = emptyList(),
        preferredAppealTypes: List<String> = emptyList(),
        willingToVolunteer: Boolean = true,
        willingToHelp: Boolean = true,
        maxTravelDistanceKm: Int = 15
    ) {
        _userProfile.update {
            it.copy(
                interests = if (interests.isNotEmpty()) interests else it.interests,
                goals = if (goals.isNotEmpty()) goals else it.goals,
                experienceLevel = experienceLevel,
                useLocation = useLocation,
                searchRadiusKm = searchRadiusKm,
                city = city,
                governorate = governorate,
                skills = if (skills.isNotEmpty()) skills else it.skills,
                availableTimes = if (availableTimes.isNotEmpty()) availableTimes else it.availableTimes,
                preferredAppealTypes = if (preferredAppealTypes.isNotEmpty()) preferredAppealTypes else it.preferredAppealTypes,
                willingToVolunteer = willingToVolunteer,
                willingToHelp = willingToHelp,
                maxTravelDistanceKm = maxTravelDistanceKm,
                isOnboarded = true
            )
        }
    }

    fun updateDisplayNamePreference(pref: String, nickname: String = "") {
        _userProfile.update {
            it.copy(
                displayNamePreference = pref,
                nickname = if (nickname.isNotBlank()) nickname else it.nickname
            )
        }
        val current = _userProfile.value
        val displayedName = when (pref) {
            "ANONYMOUS" -> current.anonymousId
            "NICKNAME" -> if (current.nickname.isNotBlank()) current.nickname else current.name
            else -> current.name
        }
        val initial = if (pref == "ANONYMOUS") "?" else displayedName.firstOrNull()?.toString() ?: "أ"
        _currentLeaderboard.update { list ->
            list.map {
                if (it.isCurrentUser) {
                    it.copy(name = displayedName, avatarInitial = initial)
                } else it
            }
        }
    }

    suspend fun loginUser(identifier: String, pass: String): Boolean = withContext(Dispatchers.IO) {
        var userAuthenticated = false
        var fetchedName = if (!identifier.contains("@") && identifier.isNotBlank()) identifier else "متطوع أثر الميداني"
        var fetchedEmail = if (identifier.contains("@")) identifier else "${identifier.replace(" ", "")}@athar.dz"
        var fetchedRole = "VOLUNTEER"
        var fetchedRoleTitle = "متطوع ميداني نشط"
        var fetchedWilaya = "16 - الجزائر العاصمة"
        var fetchedPhone = "0550 12 34 56"
        var fetchedBadge = "VOL-2026-018"
        var fetchedAssoc = ""
        var fetchedPoints = 980

        // 1. Authenticate with local node backend
        try {
            val url = URL("$DEFAULT_SERVER_URL/api/auth/login")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            conn.doOutput = true
            val payload = JSONObject().apply {
                put("identifier", identifier)
                put("password", pass)
            }
            conn.outputStream.write(payload.toString().toByteArray())
            if (conn.responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val resp = JSONObject(reader.readText())
                reader.close()
                if (resp.optBoolean("success")) {
                    val u = resp.optJSONObject("user")
                    if (u != null) {
                        fetchedName = u.optString("name", fetchedName)
                        fetchedEmail = u.optString("email", fetchedEmail)
                        fetchedRole = u.optString("role", fetchedRole).uppercase()
                        fetchedRoleTitle = u.optString("roleTitle", fetchedRoleTitle)
                        fetchedWilaya = u.optString("wilaya", fetchedWilaya)
                        fetchedPhone = u.optString("phone", fetchedPhone)
                        fetchedBadge = u.optString("badgeNumber", fetchedBadge)
                        fetchedAssoc = u.optString("associationName", fetchedAssoc)
                        fetchedPoints = u.optInt("points", fetchedPoints)
                        userAuthenticated = true
                        _serverSyncState.value = "متصل بقاعدة البيانات المركزية"
                    }
                }
            }
        } catch (e: Exception) {
            // Local server offline, will fallback to local session or Supabase
        }

        // 2. Direct Supabase Query fallback if node server was offline
        if (!userAuthenticated) {
            try {
                val supaUrl = URL("$SUPABASE_URL/rest/v1/users?email=eq.$fetchedEmail&select=*")
                val conn = supaUrl.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("apikey", SUPABASE_KEY)
                conn.setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                conn.connectTimeout = 3500
                conn.readTimeout = 3500
                if (conn.responseCode in 200..299) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val array = JSONArray(reader.readText())
                    reader.close()
                    if (array.length() > 0) {
                        val u = array.getJSONObject(0)
                        fetchedName = u.optString("name", fetchedName)
                        fetchedRole = u.optString("role", fetchedRole).uppercase()
                        fetchedRoleTitle = u.optString("roleTitle", fetchedRoleTitle)
                        fetchedWilaya = u.optString("wilaya", fetchedWilaya)
                        fetchedPhone = u.optString("phone", fetchedPhone)
                        fetchedBadge = u.optString("badgeNumber", fetchedBadge)
                        userAuthenticated = true
                    }
                }
            } catch (e: Exception) {}
        }

        val updatedProfile = _userProfile.value.copy(
            isLoggedIn = true,
            isGuest = false,
            name = fetchedName,
            email = fetchedEmail,
            role = fetchedRole,
            roleTitle = fetchedRoleTitle,
            wilaya = fetchedWilaya,
            phone = fetchedPhone,
            badgeNumber = fetchedBadge,
            associationName = fetchedAssoc,
            impactScore = fetchedPoints
        )
        _userProfile.value = updatedProfile
        persistUserSession(updatedProfile)

        // Sync appeals and notes
        syncWithServer()
        true
    }

    fun login(identifier: String, pass: String): Boolean {
        CoroutineScope(Dispatchers.IO).launch {
            loginUser(identifier, pass)
        }
        return true
    }

    suspend fun registerUser(
        name: String,
        email: String,
        pass: String,
        role: String = "VOLUNTEER",
        phone: String = "",
        wilaya: String = "16 - الجزائر العاصمة",
        associationName: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val isAssoc = role.equals("ASSOCIATION", ignoreCase = true)

        var registeredOnServer = false
        // 1. Post to Central Server
        try {
            val url = URL("$DEFAULT_SERVER_URL/api/auth/register")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            conn.doOutput = true
            val payload = JSONObject().apply {
                put("name", name.trim())
                put("email", cleanEmail)
                put("password", pass)
                put("role", if (isAssoc) "association" else "volunteer")
                put("phone", phone)
                put("wilaya", wilaya)
                put("associationName", associationName)
            }
            conn.outputStream.write(payload.toString().toByteArray())
            if (conn.responseCode in 200..299) {
                registeredOnServer = true
                _serverSyncState.value = "تم الحفظ في قاعدة البيانات المركزية"
            }
        } catch (e: Exception) {}

        // 2. Direct Supabase Registration fallback
        try {
            val supaUrl = URL("$SUPABASE_URL/rest/v1/users")
            val supaConn = supaUrl.openConnection() as HttpURLConnection
            supaConn.requestMethod = "POST"
            supaConn.setRequestProperty("Content-Type", "application/json")
            supaConn.setRequestProperty("apikey", SUPABASE_KEY)
            supaConn.setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
            supaConn.connectTimeout = 3500
            supaConn.readTimeout = 3500
            supaConn.doOutput = true
            val supaPayload = JSONObject().apply {
                put("id", "usr_${UUID.randomUUID()}")
                put("name", name.trim())
                put("email", cleanEmail)
                put("role", if (isAssoc) "association" else "volunteer")
                put("phone", phone)
                put("wilaya", wilaya)
            }
            supaConn.outputStream.write(supaPayload.toString().toByteArray())
            supaConn.responseCode
        } catch (e: Exception) {}

        val newProfile = _userProfile.value.copy(
            isLoggedIn = true,
            isGuest = false,
            name = name.trim(),
            email = cleanEmail,
            role = if (isAssoc) "ASSOCIATION" else "VOLUNTEER",
            roleTitle = if (isAssoc) "جمعية معتمدة" else "متطوع ميداني نشط",
            phone = phone,
            wilaya = wilaya,
            associationName = associationName,
            badgeNumber = if (isAssoc) "DZ-ASSOC-${(1000..9999).random()}" else "VOL-${(1000..9999).random()}",
            isOnboarded = false // Direct to onboarding
        )
        _userProfile.value = newProfile
        persistUserSession(newProfile)

        syncWithServer()
        true
    }

    fun register(
        name: String,
        email: String,
        pass: String,
        role: String = "VOLUNTEER",
        phone: String = "",
        wilaya: String = "16 - الجزائر العاصمة"
    ): Boolean {
        CoroutineScope(Dispatchers.IO).launch {
            registerUser(name, email, pass, role, phone, wilaya)
        }
        return true
    }

    fun continueAsGuest() {
        val guestProfile = _userProfile.value.copy(
            isLoggedIn = true,
            isGuest = true,
            name = "زائر أثر",
            email = "guest@athar.om",
            isOnboarded = true
        )
        _userProfile.value = guestProfile
        persistUserSession(guestProfile)
    }

    fun logout() {
        clearUserSession()
        _userProfile.update {
            it.copy(
                isLoggedIn = false,
                isGuest = false
            )
        }
    }

    fun deleteAccount() {
        clearUserSession()
        _userProfile.update {
            it.copy(
                isLoggedIn = false,
                isGuest = false,
                name = "مستخدم جديد",
                email = "",
                isOnboarded = false
            )
        }
    }

    fun setLocationPermission(allowed: Boolean, city: String = "الجزائر العاصمة", governorate: String = "ولاية الجزائر") {
        _userProfile.update {
            it.copy(
                useLocation = allowed,
                city = city,
                governorate = governorate
            )
        }
    }

    fun resetOnboarding() {
        _userProfile.update { it.copy(isOnboarded = false) }
    }

    fun sendChatMessage(userText: String, currentAppeal: Appeal? = null) {
        val userMsg = ChatMessage(
            id = "msg_${UUID.randomUUID()}",
            sender = "USER",
            text = userText,
            timestamp = "الآن"
        )
        _chatMessages.update { it + userMsg }

        // Generate intelligent contextual response
        val responseText = when {
            userText.contains("روبوت", ignoreCase = true) || userText.contains("أطفال", ignoreCase = true) -> {
                "ممتاز! لإنشاء مبادرة تعليم روبوتات للأطفال، إليك مقترح خطة عمل متكاملة:\n\n" +
                        "1. المشكلة: قلة الفرص التطبيقية للناشئين في الذكاء الاصطناعي والروبوتات.\n" +
                        "2. الهدف: تدريب 25 طفلاً على تركيب دوائر بسيطة وبرمجة حساس الحركة.\n" +
                        "3. الفئة المستهدفة: الأعمار من 10 إلى 15 سنة.\n" +
                        "4. الموارد المطلوبة: حقائب أردوينو تعليمية، قاعة مجهزة بحواسيب، 3 متطوعين تقنيين.\n" +
                        "5. المخاطر: ضيق الوقت لنهاية المشروع -> الحل: توزيع العمل على مجموعات ثنائية.\n" +
                        "6. مؤشر النجاح: إتمام كل فريق لروبوت متتبع للضوء بنهاية اليوم."
            }
            userText.contains("بقي", ignoreCase = true) || userText.contains("حالة", ignoreCase = true) -> {
                if (currentAppeal != null) {
                    "حسب بيانات نداء «${currentAppeal.title}»:\n" +
                            "- عدد المشاركين الحاليين: ${currentAppeal.participantsCount} من أصل ${currentAppeal.requiredParticipants} مطلوبين.\n" +
                            "- المتبقي لاكتمال الفريق: ${maxOf(0, currentAppeal.requiredParticipants - currentAppeal.participantsCount)} مشاركين.\n" +
                            "- الموعد: ${currentAppeal.date} الساعة ${currentAppeal.time}.\n" +
                            "- الحالة الحالية: ${currentAppeal.status}."
                } else {
                    "لديك مشاركة مقبولة في «ورشة بناء وتطوير الروبوتات للناشئين» وموعدها يوم 24 سبتمبر 2026. هل ترغب في مراجعة قائمة الموارد أو المهام؟"
                }
            }
            userText.contains("درس", ignoreCase = true) || userText.contains("ملاحظة", ignoreCase = true) -> {
                "بإمكانك تدوين أي ملاحظة وسأقوم بصياغتها كدرس مستفاد موجز يضاف إلى أرشيف ذاكرة أثر ويكسبك 30 نقطة في لوحة الصدارة!"
            }
            else -> {
                "أفهمك تماماً. في أثر نركز على تحويل كل مبادرة إلى أثر حقيقي ومستدام. هل تود أن نحدد أهداف هذه الخطوة، أم تبحث عن نداءات قريبة للمشاركة فيها؟"
            }
        }

        val aiMsg = ChatMessage(
            id = "msg_${UUID.randomUUID()}",
            sender = "AI",
            text = responseText,
            timestamp = "الآن"
        )
        _chatMessages.update { it + aiMsg }
    }
}
