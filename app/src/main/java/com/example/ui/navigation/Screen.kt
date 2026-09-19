package com.example.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Auth : Screen("auth", "البداية والتسجيل")
    object Onboarding : Screen("onboarding", "التهيئة الذكية")

    // 6 Main Bottom Bar Tabs
    object Home : Screen("home", "الرئيسية")
    object Appeals : Screen("appeals", "النداءات")
    object Map : Screen("map", "الخريطة")
    object Notes : Screen("notes", "ملاحظاتي")
    object Leaderboard : Screen("leaderboard", "لوحة الصدارة")
    object Profile : Screen("profile", "حسابي")

    // Additional Detail & Service screens
    object Nearby : Screen("nearby", "قريب منك")
    object Assistant : Screen("assistant", "مساعد أثر")
    object AppealDetails : Screen("appeal_details/{appealId}", "تفاصيل النداء") {
        fun createRoute(appealId: String) = "appeal_details/$appealId"
    }
    object Notifications : Screen("notifications", "الإشعارات")
    object Settings : Screen("settings", "الإعدادات")
}
