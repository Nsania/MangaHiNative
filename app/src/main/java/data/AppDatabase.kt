package data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import data.dao.ChaptersReadDao
import data.dao.ChaptersReadInformationDao
import data.dao.LibraryDao
import data.dao.LibraryInformationDao
import data.dao.MangasDao
import data.tables.ChaptersRead
import data.tables.ChaptersReadInformation
import data.tables.Library
import data.tables.LibraryInformation
import data.tables.Mangas

@Database(entities = [Mangas::class, Library::class, ChaptersRead::class], views = [ChaptersReadInformation::class, LibraryInformation::class], version = 1)
abstract class AppDatabase: RoomDatabase()
{
    abstract fun mangasDao(): MangasDao
    abstract fun libraryDao(): LibraryDao
    abstract fun chaptersReadDao(): ChaptersReadDao
    abstract fun chaptersReadInformationDao(): ChaptersReadInformationDao
    abstract fun libraryInformationDao(): LibraryInformationDao

    companion object
    {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "user_data"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}