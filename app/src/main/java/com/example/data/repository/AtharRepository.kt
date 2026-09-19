package com.example.data.repository

import com.example.data.model.Appeal
import com.example.data.model.AtharNotification
import com.example.data.model.ChatMessage
import com.example.data.model.LeaderboardEntry
import com.example.data.model.NoteItem
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

object AtharRepository {

    private val _userProfile = MutableStateFlow(
        UserProfile(
            id = "user_1",
            name = "أحمد المنذري",
            email = "ahmed.almandhari@athar.om",
            city = "مسقط",
            governorate = "محافظة مسقط",
            interests = listOf("الروبوتات", "التكنولوجيا", "البيئة", "البرمجة"),
            goals = listOf("المشاركة في مبادرات", "التطوع", "التعلم"),
            experienceLevel = "متوسط",
            useLocation = true,
            searchRadiusKm = 10,
            userLat = 23.5880,
            userLng = 58.3829,
            notificationsEnabled = true,
            isOnboarded = true // default true for immediate rich exploration, user can re-trigger or test onboarding anytime
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

    fun login(identifier: String, pass: String): Boolean {
        _userProfile.update {
            it.copy(
                isLoggedIn = true,
                isGuest = false,
                email = if (identifier.contains("@")) identifier else it.email,
                name = if (!identifier.contains("@") && identifier.isNotBlank()) identifier else it.name
            )
        }
        return true
    }

    fun register(name: String, email: String, pass: String): Boolean {
        _userProfile.update {
            it.copy(
                name = name,
                email = email,
                isLoggedIn = true,
                isGuest = false,
                isOnboarded = false // Direct to AI Onboarding!
            )
        }
        return true
    }

    fun continueAsGuest() {
        _userProfile.update {
            it.copy(
                isLoggedIn = true,
                isGuest = true,
                name = "زائر أثر",
                email = "guest@athar.om",
                isOnboarded = true
            )
        }
    }

    fun logout() {
        _userProfile.update {
            it.copy(
                isLoggedIn = false,
                isGuest = false
            )
        }
    }

    fun deleteAccount() {
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

    fun setLocationPermission(allowed: Boolean, city: String = "مسقط", governorate: String = "محافظة مسقط") {
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
