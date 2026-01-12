package scraper

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

data class Result(val title: String, val imageCover: String, val mangaLink: String) {
    override fun toString(): String = "{Title: $title, ImageCover: $imageCover, MangaLink: $mangaLink}"
}

data class MangaChapters(
    val mangaId: Int,
    val chapter: Double,
    val chapterTitle: String,
    val chapterLink: String,
    val uploadDate: String
)

private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .followRedirects(true)
    .build()

private fun fetchHtml(url: String): String {
    val request = Request.Builder()
        .url(url)
        .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
        .header("Referer", "https://mangakakalot.gg/")
        .header("Accept-Language", "en-US,en;q=0.9")
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw Exception("Failed to load: ${response.code}")
        return response.body?.string() ?: ""
    }
}

fun getChapterNumber(chapterLink: String): Double {
    val regex = "(?:chapter[-_])(\\d+(\\.\\d+)?)".toRegex()
    return regex.find(chapterLink)?.groupValues?.get(1)?.toDoubleOrNull() ?: -1.0
}

suspend fun fetchChapterPageUrls(context: Context, chapterUrl: String): List<String> = withContext(Dispatchers.IO) {
    val pageUrls = mutableListOf<String>()
    try {
        val html = fetchHtml(chapterUrl)
        val document = Jsoup.parse(html, chapterUrl)
        val images = document.select(".container-chapter-reader > img")

        for (item in images) {
            val imgUrl = if (item.hasAttr("data-src")) item.absUrl("data-src") else item.absUrl("src")
            if (imgUrl.isNotEmpty()) {
                pageUrls.add(imgUrl)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    pageUrls
}

suspend fun getResults(context: Context, search: String): List<Result> = withContext(Dispatchers.IO) {
    val url = "https://mangakakalot.gg/search/story/${search.replace(" ", "_")}"
    try {
        val html = fetchHtml(url)
        val document = Jsoup.parse(html, url)
        val items = document.select(".story_item")

        items.map { element ->
            val title = element.select(".story_name").text()

            val cover = element.selectFirst("a > img")?.absUrl("src") ?: ""
            val link = element.selectFirst("a")?.absUrl("href") ?: ""

            Result(title, cover, link)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

suspend fun getChapters(context: Context, mangaId: Int, mangaLink: String): List<MangaChapters> = withContext(Dispatchers.IO) {
    try {
        val html = fetchHtml(mangaLink)
        val document = Jsoup.parse(html, mangaLink)

        if (mangaLink.contains("chapmanganato")) {
            val chapters = document.select(".a-h")
            chapters.map { element ->
                val linkTag = element.selectFirst("a")
                val dateTag = element.selectFirst(".chapter-time")
                val href = linkTag?.absUrl("href") ?: ""

                MangaChapters(
                    mangaId = mangaId,
                    chapter = getChapterNumber(href),
                    chapterTitle = linkTag?.text() ?: "",
                    chapterLink = href,
                    uploadDate = dateTag?.text() ?: ""
                )
            }
        } else {
            val rows = document.select(".chapter-list > .row")
            rows.map { row ->
                val linkTag = row.selectFirst("span > a")
                val dateTag = row.selectFirst("span:nth-child(3)")
                val href = linkTag?.absUrl("href") ?: ""

                MangaChapters(
                    mangaId = mangaId,
                    chapter = getChapterNumber(href),
                    chapterTitle = linkTag?.text() ?: "",
                    chapterLink = href,
                    uploadDate = dateTag?.attr("title") ?: dateTag?.text() ?: ""
                )
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

suspend fun getPageCount(context: Context, chapterLink: String): Int = withContext(Dispatchers.IO) {
    try {
        val html = fetchHtml(chapterLink)
        val document = Jsoup.parse(html)
        document.select(".container-chapter-reader > img, .vung-doc > img").size
    } catch (e: Exception) {
        -1
    }
}

suspend fun getMangaDescription(context: Context, mangaLink: String): String = withContext(Dispatchers.IO) {
    try {
        val html = fetchHtml(mangaLink)
        val document = Jsoup.parse(html)
        when {
            mangaLink.contains("mangakakalot") -> document.select("#contentBox").text().substringAfter(": ")
            mangaLink.contains("chapmanganato") -> document.select(".panel-story-info-description").text().substringAfter(": ")
            else -> ""
        }
    } catch (e: Exception) {
        ""
    }
}