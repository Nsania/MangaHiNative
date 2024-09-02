package data.tables

import androidx.room.Entity
import androidx.room.ForeignKey


@Entity(
    tableName = "ChaptersRead",
    primaryKeys = ["mangaId", "chapterLink"],
    foreignKeys = [ForeignKey(
        entity = Mangas::class,
        parentColumns = arrayOf("mangaId"),
        childColumns = arrayOf("mangaId"),
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )]
)
data class ChaptersRead(
    val mangaId: Int,
    val chapterLink: String,
    val chapterTitle: String,
    val chapter: Double,
    val page: Int = 0,
    val totalPages: Int,
    val timeStamp: Long = System.currentTimeMillis(),
)