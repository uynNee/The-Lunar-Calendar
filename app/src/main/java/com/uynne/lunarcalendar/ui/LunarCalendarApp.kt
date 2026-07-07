package com.uynne.lunarcalendar.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.uynne.lunarcalendar.ui.day.DayDetailScreen
import com.uynne.lunarcalendar.ui.event.EventEditorScreen
import com.uynne.lunarcalendar.ui.month.MonthScreen
import java.time.LocalDate

@Composable
fun LunarCalendarApp(today: LocalDate = LocalDate.now()) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "month") {
        composable("month") {
            MonthScreen(
                today = today,
                onDayClick = { date -> navController.navigate("day/${date.toEpochDay()}") },
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
