package data.dao

import androidx.room.Dao
import androidx.room.Query
import data.tables.ChaptersReadInformation
import kotlinx.coroutines.flow.Flow

@Dao
interface ChaptersReadInformationDao
{
    @Query("SELECT * FROM ChaptersReadInformation WHERE mangaLink = :mangaLink")
    fun getChaptersReadInformation(mangaLink: String): Flow<List<ChaptersReadInformation>>

    @Query("SELECT chapter FROM ChaptersReadInformation WHERE mangaLink = :mangaLink")
    fun getChaptersReadInformationChaptersOnly(mangaLink: String): Flow<List<Double>>

    @Query("SELECT * FROM ChaptersReadInformation WHERE chapterLink = :chapterLink")
    fun getMangaIdAndPage(chapterLink: String): ChaptersReadInformation?

    @Query("SELECT t1.* FROM ChaptersReadInformation AS t1 INNER JOIN (SELECT mangaId, MAX(timeStamp) as timeStamp FROM ChaptersReadInformation GROUP BY mangaId) AS t2 ON t1.mangaId = t2.mangaId AND t1.timeStamp = t2.timeStamp ORDER BY t1.timeStamp DESC")
    fun getRecents(): Flow<List<ChaptersReadInformation>>

    @Query("DELETE FROM ChaptersRead")
    suspend fun clearData()
}