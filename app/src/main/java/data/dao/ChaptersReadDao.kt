package data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import data.tables.ChaptersRead

@Dao
interface ChaptersReadDao
{
    @Upsert
    suspend fun addOrUpdateChaptersRead(chapterRead: ChaptersRead)

    @Query("UPDATE ChaptersRead SET page = :page, timeStamp = :timeStamp WHERE mangaId = :mangaId AND chapter = :chapter")
    suspend fun updatePage(mangaId: Int, chapter: Double, page: Int, timeStamp: Long)

    @Insert(onConflict = androidx.room.OnConflictStrategy.IGNORE)
    suspend fun insertIgnorePage(chapterRead: ChaptersRead)

    @Query("SELECT * FROM ChaptersRead WHERE mangaId = :mangaId AND chapter = :chapter")
    suspend fun getChapterRead(mangaId: Int, chapter: Double): ChaptersRead?

    @Query("SELECT totalPages FROM ChaptersRead WHERE chapterLink = :chapterLink")
    suspend fun getTotalPages(chapterLink: String): Int

}