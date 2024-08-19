package data.dao

import androidx.room.Dao
import androidx.room.DatabaseView
import androidx.room.Query
import data.tables.LibraryInformation
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryInformationDao
{
    @Query("SELECT * FROM LibraryInformation ORDER BY timeStamp DESC")
    fun getLibraryInformation(): Flow<List<LibraryInformation>>
}