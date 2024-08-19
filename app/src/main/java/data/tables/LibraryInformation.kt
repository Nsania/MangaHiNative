package data.tables

import androidx.room.DatabaseView


@DatabaseView("SELECT Library.libraryId, Mangas.mangaId, Mangas.mangaTitle, Mangas.mangaImageCover, Mangas.mangaLink, Library.timeStamp FROM Library INNER JOIN Mangas ON Library.mangaId = Mangas.mangaId")
data class LibraryInformation(
    val libraryId: Int,
    val mangaId: Int,
    val mangaTitle: String,
    val mangaImageCover: String,
    val mangaLink: String,
    val timeStamp: Long
)