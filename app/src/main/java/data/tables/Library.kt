package data.tables

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Library",
    foreignKeys = [ForeignKey(
        entity = Mangas::class,
        parentColumns = arrayOf("mangaId"),
        childColumns = arrayOf("mangaId"),
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )]
)
data class Library(
    @PrimaryKey(autoGenerate = true) val libraryId: Int = 0,
    val mangaId: Int,
    val timeStamp: Long = System.currentTimeMillis(),
)