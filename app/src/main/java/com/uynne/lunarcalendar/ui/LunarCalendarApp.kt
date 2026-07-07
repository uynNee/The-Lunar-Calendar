package com.uynne.lunarcalendar.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.uynne.lunarcalendar.ui.day.DayDetailScreen
import com.uynne.lunarcalendar.ui.event.EventEditorScreen
import com.uynne.lunarcalendar.ui.month.MonthScreen
import com.uynne.lunarcalendar.ui.settings.SettingsScreen
import com.uynne.lunarcalendar.ui.theme.AppearanceMode
import java.time.LocalDate

@Composable
fun LunarCalendarApp(
    today: LocalDate = LocalDate.now(),
    initialEpochDay: Long? = null,
    appearanceMode: AppearanceMode = AppearanceMode.SYSTEM,
    onAppearanceModeChange: (AppearanceMode) -> Unit = {},
) {
    val navController = rememberNavController()
    LaunchedEffect(Unit) {
        initialEpochDay?.let { navController.navigate("day/$it") }
    }
    NavHost(
        navController = navController,
        startDestination = "month",
        enterTransition = {
            slideInHorizontally(
                animationSpec = tween(240),
                initialOffsetX = { it / 4 },
            )
        },
        exitTransition = {
            slideOutHorizontally(
                animationSpec = tween(220),
                targetOffsetX = { -it / 5 },
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                animationSpec = tween(220),
                initialOffsetX = { -it / 5 },
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                animationSpec = tween(240),
                targetOffsetX = { it / 3 },
            )
        },
    ) {
        composable("month") {
            MonthScreen(
                today = today,
                onOpenDayDetail = { date -> navController.navigate("day/${date.toEpochDay()}") },
                onAddEvent = { date -> navController.navigate("event/${date.toEpochDay()}") },
                onEditEvent = { eventId, date ->
                    navController.navigate("event/${date.toEpochDay()}?eventId=$eventId")
                },
                onOpenSettings = { navController.navigate("settings") },
            )
        }
        composable(
            route = "day/{epochDay}",
            arguments = listOf(navArgument("epochDay") { type = NavType.LongType }),
        ) { backStackEntry ->
            val epochDay = backStackEntry.arguments?.getLong("epochDay") ?: today.toEpochDay()
            DayDetailScreen(
                date = LocalDate.ofEpochDay(epochDay),
                onBack = { navController.popBackStack() },
                onAddEvent = { date -> navController.navigate("event/${date.toEpochDay()}") },
                onEditEvent = { eventId ->
                    navController.navigate("event/${epochDay}?eventId=$eventId")
                },
            )
        }
        composable("settings") {
            SettingsScreen(
                appearanceMode = appearanceMode,
                onAppearanceModeChange = onAppearanceModeChange,
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = "event/{epochDay}?eventId={eventId}",
            arguments = listOf(
                navArgument("epochDay") { type = NavType.LongType },
                navArgument("eventId") {
                    type = NavType.LongType
                    defaultValue = -1L
                },
            ),
        ) { backStackEntry ->
            val epochDay = backStackEntry.arguments?.getLong("epochDay") ?: today.toEpochDay()
            val eventId = backStackEntry.arguments?.getLong("eventId") ?: -1L
            EventEditorScreen(
                date = LocalDate.ofEpochDay(epochDay),
                eventId = eventId,
                onClose = { navController.popBackStack() },
            )
        }
    }
}
