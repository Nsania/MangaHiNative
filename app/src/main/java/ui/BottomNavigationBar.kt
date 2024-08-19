package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun BottomNavigationBar(navController: NavController, content: @Composable (PaddingValues) -> Unit) {
    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(Color.Black),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = {
                    navigateTo(navController, Screen.LibraryScreen.route)
                }) {
                    Text("Library", color = Color.White)
                }

                TextButton(onClick = {
                    navigateTo(navController, Screen.BrowseScreen.route)
                }) {
                    Text("Browse", color = Color.White)
                }

                TextButton(onClick = {
                    navigateTo(navController, Screen.RecentsScreen.route)
                }) {
                    Text("Recents", color = Color.White)
                }
            }
        },
        content = { innerPadding -> content(innerPadding) }
    )
}

fun navigateTo(navController: NavController, route: String) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    if (currentRoute != route) {
        navController.navigate(route) {
            launchSingleTop = true
        }
    }
}