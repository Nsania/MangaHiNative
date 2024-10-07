package ui

import org.jsoup.Jsoup

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