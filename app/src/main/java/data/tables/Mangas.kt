package data.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Mangas")
data class Mangas(
    @PrimaryKey(autoGenerate = true)
    val mangaId: Int = 0,
    val mangaLink: String,
    val mangaTitle: String,
    val mangaImageCover: String,
    val mangaDescription: String,
)