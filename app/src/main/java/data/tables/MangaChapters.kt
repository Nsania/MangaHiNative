package data.tables

import androidx.room.Entity
import androidx.room.ForeignKey


@Entity(
    primaryKeys = ["mangaId", "chapter"],
    foreignKeys = [ForeignKey(
        entity = Mangas::class,
        parentColumns = arrayOf("mangaId"),
        childColumns = arrayOf("mangaId"),
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )]
)
data class MangaChapters(
    val mangaId: Int,
    val chapter: Double,
    val chapterTitle: String,
    val chapterLink: String,
    val uploadDate: String,
)