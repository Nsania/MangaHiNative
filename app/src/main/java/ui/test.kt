package ui

import android.os.Build
import androidx.annotation.RequiresApi
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import scraper.Chapter
import java.time.Instant

fun main()
{
    //val client = OkHttpClient()
    val mangaLink = "https://chapmanganato.to/manga-kr954974"
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
    print(description)
}