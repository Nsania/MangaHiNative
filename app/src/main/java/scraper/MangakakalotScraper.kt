package scraper

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import data.tables.MangaChapters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths


data class Result(val title: String, val imageCover: String, val mangaLink: String)
{
    override fun toString(): String
    {
        return "{Title: $title, ImageCover: $imageCover, MangaLink: $mangaLink}"
    }
}

data class Chapter(val title: String, val readerLink: String, val uploadDate: String, val chapter: Double)
{
    override fun toString(): String
    {
        return "{Title: $title, ReaderLink: $readerLink, UploadDate: $uploadDate, Chapter: $chapter}"
    }
}

private val client = OkHttpClient()

suspend fun fetchChapterPageUrls(chapterUrl: String): List<String> {

    return withContext(Dispatchers.IO) {
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
}


@RequiresApi(Build.VERSION_CODES.O)
suspend fun downloadImage(context: Context, imageUrl: String, folder: File, index: Int): String {
    return withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(imageUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
                .header("Referer", "https://mangakakalot.com/")
                .build()
            val client = OkHttpClient()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val fileName = String.format("%04d.jpg", index) // Zero-padded filename
                val outputPath = File(folder, fileName).toPath()

                response.body!!.byteStream().use { input ->
                    Files.copy(input, outputPath)
                }
                println("Downloaded: $fileName")
                outputPath.toString()  // Return the output path
            }
        } catch (e: IOException) {
            println("Error downloading image: ${e.message}")
            ""
        }
    }
}




suspend fun getResults(search: String): List<Result> {

    return withContext(Dispatchers.IO) {
        val url = "https://mangakakalot.com/search/story/${search.replace(" ", "_")}"
        val document = Jsoup.connect(url).get()
        val titles = document.select(".story_name")
        val imageCovers = document.select(".story_item>a>img")
        val mangaLinks = document.select(".story_item>a")

        val results = titles.mapIndexed { index, element ->
            Result(
                title = element.text(),
                imageCover = imageCovers.getOrNull(index)?.attr("src") ?: "",
                mangaLink = mangaLinks.getOrNull(index)?.attr("href") ?: ""
            )
        }

        results
    }
}


suspend fun getChapters(mangaId: Int, mangaLink: String): List<MangaChapters> {
    return withContext(Dispatchers.IO) {
        if (mangaLink.contains("chapmanganato")) {
            val document = Jsoup.connect(mangaLink).get()
            val chapters = document.select(".a-h>a")
            val dates = document.select(".a-h>.chapter-time")

            val results = chapters.mapIndexed { index, element ->
                /*Chapter(
                    title = element.attr("title"),
                    readerLink = element.attr("href"),
                    uploadDate = dates.getOrNull(index)?.text() ?: "",
                    chapter = element.attr("href").let {
                        val regex = "chapter-(\\d+(\\.\\d+)?)".toRegex()
                        val matchResult = regex.find(it)
                        matchResult?.groupValues?.get(1)
                    }?.toDoubleOrNull() ?: -1.0
                )*/
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

            results
        } else if (mangaLink.contains("mangakakalot")) {
            val document = Jsoup.connect(mangaLink).get()
            val chapters = document.select(".row>span>a")
            val temp = document.select(".row")
            val chaptersUploadDates = mutableListOf<String>()
            temp.forEach { row ->
                val spans = row.select("span")
                val date = spans[2].attr("title")
                chaptersUploadDates.add(date)
            }
            val chapterNumberTemp = document.select(".chapter-list>.row")
            val chapterNumberList = mutableListOf<Double>()
            chapterNumberTemp.forEach { row ->
                val spans = row.select("span")
                val href = spans[0].select("a").attr("href")
                val chapterNumber = href.let {
                    val regex = "chapter_(\\d+(\\.\\d+)?)".toRegex()
                    val matchResult = regex.find(it)
                    matchResult?.groupValues?.get(1)?.toDoubleOrNull() ?: -1.0
                }
                chapterNumberList.add(chapterNumber)
            }


            val results = chapters.mapIndexed { index, element ->
                /*Chapter(
                    title = element.attr("title"),
                    readerLink = element.attr("href"),
                    uploadDate = chaptersUploadDates.getOrNull(index + 1).toString(),
                    chapter = chapterNumberList.getOrNull(index)?.toDouble() ?: -1.0
                )*/
                MangaChapters(
                    mangaId = mangaId,
                    chapter = chapterNumberList.getOrNull(index)?.toDouble() ?: -1.0,
                    chapterTitle = element.attr("title"),
                    chapterLink = element.attr("href"),
                    uploadDate = chaptersUploadDates.getOrNull(index + 1).toString()
                )
            }

            results
        } else {
            emptyList()
        }
    }
}

suspend fun getChapterNumber(chapterLink: String): Double {
    return withContext(Dispatchers.IO) {

        val chapterNumber = when {
            chapterLink.contains("mangakakalot") -> {
                chapterLink.let {
                    val regex = "chapter_(\\d+(\\.\\d+)?)".toRegex()
                    val result = regex.find(it)
                    result?.groupValues?.get(1)?.toDoubleOrNull()
                } ?: -1.0
            }
            chapterLink.contains("chapmanganato") -> {
                chapterLink.let {
                    val regex = "chapter-(\\d+(\\.\\d+)?)".toRegex()
                    val result = regex.find(it)
                    result?.groupValues?.get(1)?.toDoubleOrNull()
                } ?: -1.0
            }
            else -> -1.0
        }

        chapterNumber
    }
}

suspend fun getPageCount(chapterLink: String): Int {
    return withContext(Dispatchers.IO) {
        val document = Jsoup.connect(chapterLink).get()
        val pageCount = when {
            chapterLink.contains("mangakakalot") -> {
                document.select(".container-chapter-reader>img").count()
            }
            chapterLink.contains("chapmanganato") -> {
                document.select(".container-chapter-reader>img").count()
            }
            else -> -1
        }

        pageCount
    }
}
suspend fun getMangaDescription(mangaLink: String): String
{
    return withContext(Dispatchers.IO) {
        val document = Jsoup.connect(mangaLink).get()
        val description = when {
            mangaLink.contains("mangakakalot") -> {
                document.select("#noidungm").text().substringAfter(": ")
            }
            mangaLink.contains("chapmanganato") -> {
                document.select(".panel-story-info-description").text().substringAfter(": ")
            }
            else -> ""
        }
        description
    }
}


