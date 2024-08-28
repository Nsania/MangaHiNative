package data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import data.tables.Library
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao
{
    @Upsert
    suspend fun addToLibrary(library: Library)

    @Query("SELECT * FROM Library WHERE mangaId = :mangaId")
    suspend fun getManga(mangaId: Int): Library?

    @Query("SELECT * FROM Library ORDER BY timeStamp")
    fun getLibrary(): Flow<List<Library>>

    @Query("DELETE FROM Library WHERE mangaId = :mangaId")
    suspend fun deleteLibraryEntry(mangaId: Int)
}