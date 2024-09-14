package data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import data.tables.MangaChapters
import kotlinx.coroutines.flow.Flow


@Dao
interface MangaChaptersDao
{
    @Upsert
    suspend fun addMangaChapters(mangaChapter: MangaChapters)

    @Query("SELECT * FROM MangaChapters WHERE mangaId = :mangaId AND chapter < :currentChapter ORDER BY CHAPTER DESC LIMIT 1")
    suspend fun getPreviousChapter(mangaId: Int, currentChapter: Double): MangaChapters

    @Query("SELECT * FROM MangaChapters WHERE mangaId = :mangaId AND chapter > :currentChapter ORDER BY chapter ASC LIMIT 1")
    suspend fun getNextChapter(mangaId: Int, currentChapter: Double): MangaChapters

    @Query("SELECT EXISTS(SELECT 1 FROM MangaChapters WHERE mangaId = :mangaId)")
    suspend fun checkManga(mangaId: Int): Boolean

    @Query("SELECT * FROM MangaChapters WHERE mangaId = :mangaId ORDER BY chapter DESC")
    fun getMangaChapters(mangaId: Int): Flow<List<MangaChapters>>
}