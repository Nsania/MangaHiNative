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
import androidx.core.view.WindowInsetsControllerCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import data.AppDatabase
import data.UserRepository


class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var windowInsetsController: WindowInsetsControllerCompat
    lateinit var userRepository: UserRepository

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            database = AppDatabase.getDatabase(applicationContext)
            Navigation(database.chaptersReadDao(), database.libraryInformationDao(), database.mangasDao(), database.libraryDao(), database.chaptersReadInformationDao())
        }

    }
}