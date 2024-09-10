package ui

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import data.AppDatabase
import data.UserRepository


class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //WindowCompat.setDecorFitsSystemWindows(window, false)

        // Enter immersive mode
        //enterImmersiveMode()

        window.setBackgroundDrawableResource(android.R.color.black)
        setContent {
            database = AppDatabase.getDatabase(applicationContext)
            Navigation(database.chaptersReadDao(), database.libraryInformationDao(), database.mangasDao(), database.libraryDao(), database.chaptersReadInformationDao())
        }

    }

    private fun enterImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)

        // Hide system bars
        insetsController.hide(WindowInsetsCompat.Type.systemBars())

        // Optionally: Set the system bars behavior
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}