package data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import data.tables.Mangas

@Dao
interface MangasDao
{
    @Insert
    suspend fun addManga(manga: Mangas)

    @Query("SELECT * FROM Mangas WHERE mangaLink = :mangaLink")
    suspend fun getManga(mangaLink: String): Mangas?
}