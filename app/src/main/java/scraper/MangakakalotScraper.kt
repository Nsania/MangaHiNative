package scraper

import data.tables.MangaChapters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException

data class Result(val title: String, val imageCover: String, val mangaLink: String)
{
    override fun toString(): String {
        return "{Title: $title, ImageCover: $imageCover, MangaLink: $mangaLink}"
    }
}

private val client = OkHttpClient()

suspend fun fetchChapterPageUrls(chapterUrl: String): List<String> = withContext(Dispatchers.IO) {
    val pageUrls = mutableListOf<String>()
    try {
        val request = Request.Builder()
            .url(chapterUrl)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
            .header("Referer", "https://mangakakalot.com/")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val document: Document = Jsoup.parse(response.body!!.string())
            val images = document.select(".container-chapter-reader > img")
            for (item in images) {
                val imgUrl = item.absUrl("src")
                pageUrls.add(imgUrl)
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    pageUrls
}


suspend fun getResults(search: String): List<Result> = withContext(Dispatchers.IO) {
    val url = "https://mangakakalot.com/search/story/${search.replace(" ", "_")}"
    val document = Jsoup.connect(url).get()
    val titles = document.select(".story_name")
    val imageCovers = document.select(".story_item>a>img")
    val mangaLinks = document.select(".story_item>a")

    titles.mapIndexed { index, element ->
        Result(
            title = element.text(),
            imageCover = imageCovers.getOrNull(index)?.attr("src") ?: "",
            mangaLink = mangaLinks.getOrNull(index)?.attr("href") ?: ""
        )
    }
}

suspend fun getChapters(mangaId: Int, mangaLink: String): List<MangaChapters> = withContext(Dispatchers.IO) {
    when {
        mangaLink.contains("chapmanganato") -> {
            val document = Jsoup.connect(mangaLink).get()
            val chapters = document.select(".a-h>a")
            val dates = document.select(".a-h>.chapter-time")

            chapters.mapIndexed { index, element ->
                MangaChapters(
                    mangaId = mangaId,
                    chapter = element.attr("href").let {
                        val regex = "chapter-(\\d+(\\.\\d+)?)".toRegex()
                        val matchResult = regex.find(it)
                        matchResult?.groupValues?.get(1)
                    }?.toDoubleOrNull() ?: -1.0,
                    chapterTitle = element.attr("title"),
                    chapterLink = element.attr("href"),
                    uploadDate = dates.getOrNull(index)?.text() ?: ""
                )
            }
        }
        mangaLink.contains("mangakakalot") -> {
            val document = Jsoup.connect(mangaLink).get()
            val chapters = document.select(".row>span>a")
            val dates = document.select(".row>span[title]")
            val chapterNumbers = chapters.map {
                val href = it.attr("href")
                val regex = "chapter_(\\d+(\\.\\d+)?)".toRegex()
                regex.find(href)?.groupValues?.get(1)?.toDoubleOrNull() ?: -1.0
            }

            chapters.mapIndexed { index, element ->
                MangaChapters(
                    mangaId = mangaId,
                    chapter = chapterNumbers.getOrNull(index) ?: -1.0,
                    chapterTitle = element.attr("title"),
                    chapterLink = element.attr("href"),
                    uploadDate = dates.getOrNull(index + 1)?.attr("title") ?: ""
                )
            }
        }
        else -> emptyList()
    }
}

suspend fun getChapterNumber(chapterLink: String): Double = withContext(Dispatchers.IO) {
    val regex = when {
        chapterLink.contains("mangakakalot") -> "chapter_(\\d+(\\.\\d+)?)".toRegex()
        chapterLink.contains("chapmanganato") -> "chapter-(\\d+(\\.\\d+)?)".toRegex()
        else -> return@withContext -1.0
    }
    regex.find(chapterLink)?.groupValues?.get(1)?.toDoubleOrNull() ?: -1.0
}

suspend fun getPageCount(chapterLink: String): Int = withContext(Dispatchers.IO) {
    val document = Jsoup.connect(chapterLink).get()
    when {
        chapterLink.contains("mangakakalot") || chapterLink.contains("chapmanganato") -> {
            document.select(".container-chapter-reader>img").size
        }
        else -> -1
    }
}

suspend fun getMangaDescription(mangaLink: String): String = withContext(Dispatchers.IO) {
    val document = Jsoup.connect(mangaLink).get()
    when {
        mangaLink.contains("mangakakalot") -> {
            document.select("#noidungm").text().substringAfter(": ")
        }
        mangaLink.contains("chapmanganato") -> {
            document.select(".panel-story-info-description").text().substringAfter(": ")
        }
        else -> ""
    }
}

