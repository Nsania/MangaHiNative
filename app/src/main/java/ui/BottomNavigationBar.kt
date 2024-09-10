package ui

import android.graphics.drawable.Icon
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WatchLater
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mangahinative.R

@Composable
fun BottomNavigationBar(navController: NavController, content: @Composable (PaddingValues) -> Unit) {

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .background(Color(0xFF352e38)),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {

                BottomNavigationItem(
                    isSelected = currentRoute == Screen.LibraryScreen.route,
                    onClick = {
                        navigateTo(navController, Screen.LibraryScreen.route)
                    },
                    icon = Icons.Outlined.CollectionsBookmark,
                    label = ""
                )

                BottomNavigationItem(
                    isSelected = currentRoute == Screen.BrowseScreen.route,
                    onClick = {
                        navigateTo(navController, Screen.BrowseScreen.route)
                    },
                    icon = Icons.Outlined.Search,
                    label = ""
                )

                BottomNavigationItem(
                    isSelected = currentRoute == Screen.RecentsScreen.route,
                    onClick = {
                        navigateTo(navController, Screen.RecentsScreen.route)
                    },
                    icon = Icons.Outlined.History,
                    label = ""
                )

            }
        },
        content = { innerPadding -> content(innerPadding) }
    )
}

@Composable
fun BottomNavigationItem(
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String
)
{
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Color.Transparent else Color.Transparent,
        animationSpec = tween(durationMillis = 0),
        label = ""
    )

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
        ),
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White
            )
        }
    }
}


fun navigateTo(navController: NavController, route: String) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    if (currentRoute != route) {
        navController.navigate(route) {
            launchSingleTop = true
        }
    }
}