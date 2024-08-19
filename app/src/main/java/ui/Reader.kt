package ui

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import scraper.fetchChapterPageUrls
import scraper.getPageCount
import scraper.getChapterNumber
import data.dao.ChaptersReadDao
import data.dao.ChaptersReadInformationDao
import scraper.downloadImage
import java.io.File
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Reader(chapterLink: String, chaptersReadInformationDao: ChaptersReadInformationDao, chaptersReadDao: ChaptersReadDao, mangaId: Int) {
    var totalPages by remember { mutableStateOf(0) }
    var chapters by remember { mutableStateOf(emptyList<String>()) }
    val imagePaths = remember { mutableStateListOf<String>() }
    var previousChapterLink by remember { mutableStateOf<String?>(null) }
    var currentPage by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val uniqueFolderName = UUID.nameUUIDFromBytes(chapterLink.toByteArray()).toString()
    val uniqueFolder = File(context.cacheDir, uniqueFolderName)
    val imageFileNames = remember { mutableStateOf(emptyList<String>()) }
    //var mangaId by remember { mutableStateOf(0) }
    var chapter by remember { mutableStateOf(0.0) }

    if (!uniqueFolder.exists()) {
        uniqueFolder.mkdirs()
    }

    LaunchedEffect(chapterLink) {
        coroutineScope.launch(Dispatchers.IO) {
            val info = chaptersReadInformationDao.getMangaIdAndPage(chapterLink)
            val chapterNumber = getChapterNumber(chapterLink)  // Fetch chapter number
            withContext(Dispatchers.Main) {
                if (info != null) {
                    currentPage = info.page
                    Log.d("Reader", "Loaded mangaId: $mangaId, currentPage: $currentPage")
                }
                else
                {
                    /*val chapterRead = ChaptersRead(mangaId, chapterLink, chapterNumber, timeStamp = System.currentTimeMillis())
                    chaptersReadDao.addOrUpdateChaptersRead(chapterRead)*/
                }
                chapter = chapterNumber
                Log.d("Reader", "Loaded chapterNumber: $chapter")
            }
        }
    }

    LaunchedEffect(chapterLink) {
        // Clear the cache if we are switching to a different chapter
        if (previousChapterLink != chapterLink) {
            withContext(Dispatchers.IO) {
                clearCache(context, uniqueFolderName)
            }
            previousChapterLink = chapterLink
        }

        coroutineScope.launch(Dispatchers.IO) {
            val pages = getPageCount(chapterLink)
            withContext(Dispatchers.Main) {
                totalPages = pages
                imagePaths.clear()
                imagePaths.addAll(List(totalPages) { "" })
            }

            // Check for existing files
            val existingFiles = uniqueFolder.listFiles()?.sortedBy { it.name }?.map { it.absolutePath } ?: emptyList()
            imageFileNames.value = existingFiles

            if (existingFiles.size == totalPages) {
                // Load images from cache if all files are already downloaded
                withContext(Dispatchers.Main) {
                    imagePaths.clear()
                    imagePaths.addAll(existingFiles)
                }
            } else {
                // Fetch chapter URLs if not all images are cached
                chapters = fetchChapterPageUrls(chapterLink)
            }
        }
    }

    // Scroll to the initial page placeholder immediately
    LaunchedEffect(totalPages) {
        if (totalPages > 0) {
            coroutineScope.launch {
                listState.scrollToItem(currentPage)
            }
        }
    }

    LaunchedEffect(chapters) {
        if (chapters.isNotEmpty() && imageFileNames.value.size != totalPages) {
            coroutineScope.launch {
                val downloadJobs = chapters.mapIndexed { index, imageURL ->
                    async(Dispatchers.IO) {
                        if (imagePaths[index].isEmpty()) {
                            val imagePath = downloadImage(context, imageURL, uniqueFolder, index)
                            withContext(Dispatchers.Main) {
                                imagePaths[index] = imagePath
                            }
                        }
                    }
                }
                downloadJobs.awaitAll()
            }
        }
    }

    // Track the current page based on the list state and update the database
    LaunchedEffect(listState.firstVisibleItemIndex) {
        currentPage = listState.firstVisibleItemIndex
        Log.d("Reader", "Current page updated to: $currentPage")

        // Update the database with the current page
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val existingChapterRead = chaptersReadDao.getChapterRead(mangaId, chapter)
                if (existingChapterRead == null) {
                    /*val chapterRead = ChaptersRead(mangaId, chapterLink, chapter, currentPage)
                    chaptersReadDao.addOrUpdateChaptersRead(chapterRead)
                    Log.d("Reader", "Inserted page: $currentPage for mangaId: $mangaId, chapter: $chapter")*/
                } else {
                    chaptersReadDao.updatePage(mangaId, chapter, currentPage, System.currentTimeMillis())
                    Log.d("Reader", "Updated page: $currentPage for mangaId: $mangaId, chapter: $chapter")
                }
            } catch (e: Exception) {
                Log.e("Reader", "Error saving page: ${e.message}")
            }
        }
    }

    LazyColumn(modifier = Modifier.fillMaxWidth(), state = listState) {
        itemsIndexed(imagePaths) { index, imagePath ->
            var aspectRatio by remember { mutableStateOf(1f) }
            var isImageLoaded by remember { mutableStateOf(false) }

            if (imagePath.isNotEmpty()) {
                LaunchedEffect(imagePath) {
                    val request = ImageRequest.Builder(context)
                        .data(imagePath)
                        .allowHardware(false) // Avoid hardware bitmaps as they might cause issues with aspect ratio calculation
                        .build()

                    val result = (context.imageLoader.execute(request) as? SuccessResult)?.drawable
                    result?.let { drawable ->
                        aspectRatio = drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight.toFloat()
                        isImageLoaded = true
                    }
                }

                if (isImageLoaded) {
                    Image(
                        painter = rememberAsyncImagePainter(model = imagePath),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(aspectRatio)
                    )
                } else {
                    // Placeholder to maintain space while image is loading
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f) // Default aspect ratio while loading
                            .background(Color.DarkGray) // Placeholder color
                    )
                }
            }
            else {
                // Display a loading placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f) // Default aspect ratio for placeholders
                        .background(Color.DarkGray) // Placeholder color
                    ,
                    contentAlignment = Alignment.Center
                )
                {
                    CircularProgressIndicator(
                        modifier = Modifier.width(64.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }
        }
    }
}

fun clearCache(context: Context, currentFolderName: String) {
    val cacheDir = context.cacheDir
    cacheDir.listFiles()?.forEach { file ->
        if (file.isDirectory && file.name != currentFolderName) {
            file.deleteRecursively()
        }
    }
}
