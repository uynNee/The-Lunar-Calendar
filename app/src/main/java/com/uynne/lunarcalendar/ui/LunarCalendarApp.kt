package com.uynne.lunarcalendar.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.uynne.lunarcalendar.ui.day.DayDetailScreen
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
            )
        }
    }
}
