package data.tables

import androidx.room.DatabaseView


@DatabaseView("SELECT Mangas.mangaId, Mangas.mangaTitle, Mangas.mangaLink, Mangas.mangaImageCover, ChaptersRead.chapterLink, ChaptersRead.chapterTitle, ChaptersRead.chapter, ChaptersRead.page, ChaptersRead.totalPages, ChaptersRead.totalPages - ChaptersRead.page AS pagesLeft, ChaptersRead.timeStamp FROM Mangas INNER JOIN ChaptersRead ON Mangas.mangaId = ChaptersRead.mangaId")
data class ChaptersReadInformation(
    val mangaId: Int,
    val mangaTitle: String,
    val mangaLink: String,
    val mangaImageCover: String,
    val chapterLink: String,
    val chapterTitle: String,
    val chapter: Double,
    val page: Int,
    val totalPages: Int,
    val pagesLeft: Int,
    val timeStamp: Long,
)