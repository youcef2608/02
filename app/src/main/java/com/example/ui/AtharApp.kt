package com.example.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.data.repository.AtharRepository
import com.example.ui.components.AtharBottomNavigationBar
import com.example.ui.components.AtharTopAppBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.*

@Composable
fun AtharApp(navController: NavHostController = rememberNavController()) {
    val userProfile by AtharRepository.userProfile.collectAsState()
    val notifications by AtharRepository.notifications.collectAsState()
    val unreadCount = remember(notifications) { notifications.count { !it.isRead } }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Appeals.route

    // The 5 main bottom navigation bar destinations (النداءات أولاً ثم الخريطة)
    val isTopLevelDestination = currentRoute in listOf(
        Screen.Appeals.route,
        Screen.Map.route,
        Screen.Notes.route,
        Screen.Leaderboard.route,
        Screen.Profile.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (isTopLevelDestination && currentRoute != Screen.Map.route) {
                AtharTopAppBar(
                    title = when (currentRoute) {
                        Screen.Appeals.route -> "النداءات والمبادرات"
                        Screen.Map.route -> "خريطة الأثر"
                        Screen.Notes.route -> "ملاحظاتي والدروس"
                        Screen.Leaderboard.route -> "لوحة الصدارة"
                        Screen.Profile.route -> "حسابي وأثري"
                        else -> "أثر | Athar"
                    },
                    unreadNotifCount = unreadCount,
                    onAssistantClick = { navController.navigate(Screen.Assistant.route) },
                    onNotifClick = { navController.navigate(Screen.Notifications.route) },
                    onLeaderboardClick = { navController.navigate(Screen.Leaderboard.route) },
                    onSettingsClick = { navController.navigate(Screen.Settings.route) },
                    onAuthClick = { navController.navigate(Screen.Auth.route) }
                )
            }
        },
        bottomBar = {
            if (isTopLevelDestination) {
                AtharBottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Appeals.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (!userProfile.isLoggedIn) Screen.Auth.route else Screen.Appeals.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Auth Screen (Login / Register / Guest)
            composable(Screen.Auth.route) {
                AuthScreen(
                    onAuthSuccess = { isNewUser ->
                        if (isNewUser || !userProfile.isOnboarded) {
                            navController.navigate(Screen.Onboarding.route) {
                                popUpTo(Screen.Auth.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.Appeals.route) {
                                popUpTo(Screen.Auth.route) { inclusive = true }
                            }
                        }
                    }
                )
            }

            // Smart Conversational Onboarding Screen
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onFinish = {
                        navController.navigate(Screen.Appeals.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // Fallback for home route (directs immediately to Appeals)
            composable(Screen.Home.route) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Appeals.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            }

            // 1. Map Screen (Pure Full-Screen Dedicated Interactive Map)
            composable(Screen.Map.route) {
                MapScreen(
                    onAppealClick = { appealId ->
                        navController.navigate(Screen.AppealDetails.createRoute(appealId))
                    },
                    onNavigateToAssistant = { navController.navigate(Screen.Assistant.route) },
                    onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                    onNavigateToAuth = { navController.navigate(Screen.Auth.route) }
                )
            }

            // 2. Appeals Screen
            composable(Screen.Appeals.route) {
                AppealsScreen(
                    onAppealClick = { appealId ->
                        navController.navigate(Screen.AppealDetails.createRoute(appealId))
                    },
                    onNavigateToMap = { navController.navigate(Screen.Map.route) },
                    onNavigateToAssistant = { navController.navigate(Screen.Assistant.route) }
                )
            }

            // 4. Notes Screen
            composable(Screen.Notes.route) {
                NotesScreen(
                    onNavigateToAppeal = { appealId ->
                        navController.navigate(Screen.AppealDetails.createRoute(appealId))
                    }
                )
            }

            // 5. Leaderboard Screen
            composable(Screen.Leaderboard.route) {
                LeaderboardScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // 6. Profile & Impact Screen
            composable(Screen.Profile.route) {
                ProfileAndImpactScreen(
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToLeaderboard = { navController.navigate(Screen.Leaderboard.route) },
                    onAppealClick = { appealId ->
                        navController.navigate(Screen.AppealDetails.createRoute(appealId))
                    },
                    onNavigateToAuth = { navController.navigate(Screen.Auth.route) }
                )
            }

            // Detail & Utility Screens
            composable(Screen.Nearby.route) {
                NearbyAppealsScreen(
                    onAppealClick = { appealId ->
                        navController.navigate(Screen.AppealDetails.createRoute(appealId))
                    }
                )
            }

            composable(Screen.Assistant.route) {
                AtharAssistantScreen()
            }

            composable(
                route = Screen.AppealDetails.route,
                arguments = listOf(navArgument("appealId") { type = NavType.StringType })
            ) { backStackEntry ->
                val appealId = backStackEntry.arguments?.getString("appealId") ?: ""
                AppealDetailsScreen(
                    appealId = appealId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Notifications.route) {
                NotificationsScreen(
                    onBack = { navController.popBackStack() },
                    onAppealClick = { appealId ->
                        navController.navigate(Screen.AppealDetails.createRoute(appealId))
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onReopenOnboarding = {
                        navController.navigate(Screen.Onboarding.route)
                    },
                    onLogout = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
