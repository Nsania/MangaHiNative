package ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.privacysandbox.tools.core.model.Type
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import data.UserRepository
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
import data.dao.MangaChaptersDao
import data.tables.ChaptersRead
import data.viewmodels.ReaderViewModel
import kotlinx.coroutines.Job
import scraper.downloadImage
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID
import kotlin.math.roundToInt



@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class
)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Reader(
    chapterLink: String,
    chaptersReadInformationDao: ChaptersReadInformationDao,
    chaptersReadDao: ChaptersReadDao,
    mangaId: Int,
    navController: NavController,
    viewModel: ReaderViewModel = viewModel(factory = ReaderViewModel.Factory),
    chapterTitle: String,
    mangaLink: String,
    mangaChaptersDao: MangaChaptersDao
) {
    val currentChapterLink by viewModel.currentChapterLink.collectAsState()
    val currentChapterTitle by viewModel.currentChapterTitle.collectAsState()
    val totalPages by viewModel.totalPages.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val imagePaths by viewModel.imagePaths.collectAsState()
    val previousChapterLink by viewModel.previousChapterLink.collectAsState()
    val chapter by viewModel.chapter.collectAsState()
    val previousChapter by viewModel.previousChapter.collectAsState()
    val nextChapter by viewModel.nextChapter.collectAsState()
    val previousChapterTotalPages by viewModel.previousChapterTotalPages.collectAsState()
    val nextChapterTotalPages by viewModel.nextChapterTotalPages.collectAsState()
    val imageFileNames by viewModel.imageFileNames.collectAsState()
    val readerModeSelected by viewModel.readerMode.collectAsState()

    var currentPage by remember { mutableIntStateOf(1) }
    var isTopBarVisible by remember { mutableStateOf(false) }
    var isReaderSettingsVisible by remember { mutableStateOf(false) }
    var fromExisting by remember { mutableStateOf(false) }
    var endOrStart by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var uniqueFolderName = UUID.nameUUIDFromBytes(chapterLink.toByteArray()).toString()
    val uniqueFolder = File(context.cacheDir, uniqueFolderName)
    val sheetState = rememberModalBottomSheetState()
    val systemUiController = rememberSystemUiController()
    val readerModeOptions  = listOf("Long Strip", "Paged(left to right)", "Paged(vertical)")
    val pagerState = rememberPagerState(pageCount = { totalPages + 2 }, initialPage = 1)

    if (!uniqueFolder.exists())
    {
        uniqueFolder.mkdirs()
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Main) {
            viewModel.updateCurrentChapterLink(chapterLink)
            viewModel.updateCurrentChapterTitle(chapterTitle)
        }
    }

    LaunchedEffect(isTopBarVisible) {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = false,
        )
    }


    LaunchedEffect(currentChapterLink) {
        if(currentChapterLink.isNotEmpty() && currentChapterLink != "")
        {
            uniqueFolderName = UUID.nameUUIDFromBytes(currentChapterLink.toByteArray()).toString()
            Log.d("Reader", "Chapter link: $currentChapterLink")
            coroutineScope.launch(Dispatchers.IO) {
                if (!uniqueFolder.exists())
                {
                    uniqueFolder.mkdirs()
                }
            }
            Log.d("Reader", "Current Chapter Link: $currentChapterLink")
            coroutineScope.launch(Dispatchers.IO) {
                val info = chaptersReadInformationDao.getMangaIdAndPage(currentChapterLink)
                val chapterNumber = getChapterNumber(currentChapterLink)
                withContext(Dispatchers.Main) {
                    if (info != null) {
                        currentPage = info.page
                        Log.d("Reader", "Loaded mangaId: $mangaId, currentPage: $currentPage")
                    }
                    viewModel.updateChapter(chapterNumber)
                    Log.d("Reader", "Loaded chapterNumber: $chapter")
                }
            }
            coroutineScope.launch(Dispatchers.IO) {
                val pages = getPageCount(currentChapterLink)
                withContext(Dispatchers.Main) {
                    viewModel.updateTotalPages(pages)
                    viewModel.updateImagePaths(List(pages) { "" })
                }
                val existingFiles = uniqueFolder.listFiles()?.sortedBy { it.name }?.map { it.absolutePath } ?: emptyList()
                viewModel.updateImageFileNames(existingFiles)

                if (existingFiles.size == totalPages)
                {
                    withContext(Dispatchers.Main) {
                        viewModel.updateImagePaths(existingFiles)
                    }
                }
                else
                {
                    viewModel.updateChapters(fetchChapterPageUrls(currentChapterLink))
                }
            }

            if (previousChapterLink != currentChapterLink)
            {
                withContext(Dispatchers.IO) {
                    clearCache(context, uniqueFolderName)
                }
                viewModel.updatePreviousChapterLink(currentChapterLink)
            }
        }
    }


    LaunchedEffect(chapter) {
        if(chapter != 0.0)
        {
            Log.d("Reader", "Current Chapter(257): $chapter")
            val previousChapterTemp = mangaChaptersDao.getPreviousChapter(mangaId, chapter)
            val nextChapterTemp = mangaChaptersDao.getNextChapter(mangaId, chapter)

            if(previousChapterTemp != null)
            {
                viewModel.updatePreviousChapter(previousChapterTemp)
            }
            else
            {
                viewModel.updatePreviousChapter(null)
            }

            if(nextChapterTemp != null)
            {
                viewModel.updateNextChapter(nextChapterTemp)
            }
            else
            {
                viewModel.updateNextChapter(null)
            }
        }
    }

    LaunchedEffect(previousChapter, nextChapter) {
        Log.d("Reader","Previous Chapter: ${previousChapter?.chapter}\nNext Chapter: ${nextChapter?.chapter}")
        Log.d("Reader", "Previous Chapter Link: ${previousChapter?.chapterLink}\nNext Chapter Link: ${nextChapter?.chapterLink}")

        if(previousChapter != null)
        {
            viewModel.updatePreviousChapterTotalPages(getPageCount(previousChapter!!.chapterLink))
        }
        else
        {
            viewModel.updatePreviousChapterTotalPages(0)
        }

        if(nextChapter != null)
        {
            viewModel.updateNextChapterTotalPages(getPageCount(nextChapter!!.chapterLink))
        }
        else
        {
            viewModel.updateNextChapterTotalPages(0)
        }
    }

    LaunchedEffect(chapters) {
        if (chapters.isNotEmpty() && imageFileNames.size != totalPages) {
            coroutineScope.launch {
                val downloadJobs = chapters.mapIndexed { index, imageURL ->
                    async(Dispatchers.IO) {
                        if (viewModel.getImagePathAt(index).isEmpty()) {
                            val imagePath = downloadImage(context, imageURL, uniqueFolder, index)
                            withContext(Dispatchers.Main) {
                                viewModel.updateImagePath(index, imagePath)
                            }
                        }
                    }
                }
                downloadJobs.awaitAll()
            }
        }
    }


    LaunchedEffect(totalPages) {
        if (totalPages > 0) {
            coroutineScope.launch {
                if(fromExisting && endOrStart)
                {
                    currentPage = 1
                }
                if(fromExisting && !endOrStart)
                {
                    currentPage = totalPages
                }
                listState.scrollToItem(currentPage + 1)
                pagerState.scrollToPage(currentPage)
            }
        }
    }


    LaunchedEffect(listState, pagerState) {
        // Observe changes in listState.firstVisibleItemIndex and pagerState.currentPage using snapshotFlow
        snapshotFlow {
            listState.firstVisibleItemIndex to pagerState.currentPage
        }.collect { (firstVisibleItemIndex, currentPageFromPager) ->

            // Determine the current page based on the selected reader mode
            currentPage = if (readerModeSelected == 0)
            {
                if(firstVisibleItemIndex in 2..totalPages)
                {
                    firstVisibleItemIndex - 1
                }
                else
                {
                    firstVisibleItemIndex
                }
            }
            else
            {
                currentPageFromPager
            }

            Log.d("Reader", "Current page updated to: $currentPage")

            // Ensure the page is within valid bounds before updating
            if (currentPage != totalPages + 1 && currentPage != 0) {
                // Only update if we're in the same chapter
                if (previousChapterLink == currentChapterLink) {
                    coroutineScope.launch {
                        try {
                            val existingChapterRead = chaptersReadDao.getChapterRead(mangaId, chapter)
                            if (existingChapterRead != null) {
                                Log.d("Reader", "Updating for chapter: $chapter")
                                chaptersReadDao.updatePage(
                                    mangaId = mangaId,
                                    chapter = chapter,
                                    page = currentPage,
                                    timeStamp = System.currentTimeMillis()
                                )
                                Log.d(
                                    "Reader",
                                    "Updated page: $currentPage for mangaId: $mangaId, chapter: $chapter"
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("Reader", "Error saving page: ${e.message}")
                        }
                    }
                }
            }

            // Handle scenario when at the start and need to navigate to the previous chapter
            if (readerModeSelected == 0 && currentPage == 0 && previousChapter != null) {
                fromExisting = true
                endOrStart = false
                Log.d("Reader", "Previous Chapter Link: ${previousChapter!!.chapterLink}")

                viewModel.updateCurrentChapterLink(previousChapter!!.chapterLink)
                viewModel.updateCurrentChapterTitle(previousChapter!!.chapterTitle)

                coroutineScope.launch {
                    val chapterNumber = getChapterNumber(previousChapter!!.chapterLink)
                    val existingChapterRead = chaptersReadDao.getChapterRead(mangaId, chapterNumber)
                    if (existingChapterRead == null) {
                        chaptersReadDao.addOrUpdateChaptersRead(
                            chapterRead = ChaptersRead(
                                mangaId = mangaId,
                                chapterLink = previousChapter!!.chapterLink,
                                chapterTitle = previousChapter!!.chapterTitle,
                                chapter = chapterNumber,
                                totalPages = previousChapterTotalPages,
                                timeStamp = System.currentTimeMillis()
                            )
                        )
                        Log.d("Chapters", "chapter added: $chapterNumber")
                    } else {
                        Log.d("Chapters", "chapter already exists: $chapterNumber")
                    }
                }
            }

            // Handle scenario when at the end and need to navigate to the next chapter
            if (readerModeSelected == 0 && currentPage == totalPages + 2 && nextChapter != null) {
                fromExisting = true
                endOrStart = true
                Log.d("Reader", "Next Chapter Link: ${nextChapter!!.chapterLink}")

                viewModel.updateCurrentChapterLink(nextChapter!!.chapterLink)
                viewModel.updateCurrentChapterTitle(nextChapter!!.chapterTitle)

                coroutineScope.launch {
                    val chapterNumber = getChapterNumber(nextChapter!!.chapterLink)
                    val existingChapterRead = chaptersReadDao.getChapterRead(mangaId, chapterNumber)
                    if (existingChapterRead == null) {
                        chaptersReadDao.addOrUpdateChaptersRead(
                            chapterRead = ChaptersRead(
                                mangaId = mangaId,
                                chapterLink = nextChapter!!.chapterLink,
                                chapterTitle = nextChapter!!.chapterTitle,
                                chapter = chapterNumber,
                                totalPages = nextChapterTotalPages,
                                timeStamp = System.currentTimeMillis()
                            )
                        )
                        Log.d("Chapters", "chapter added: $chapterNumber")
                    } else {
                        Log.d("Chapters", "chapter already exists: $chapterNumber")
                    }
                }
            }

            // Handle navigation to the previous chapter when not in reader mode 0
            if (currentPage == 0 && previousChapter != null && readerModeSelected != 0) {
                fromExisting = true
                endOrStart = false
                Log.d("Reader", "Previous Chapter Link: ${previousChapter!!.chapterLink}")

                viewModel.updateCurrentChapterLink(previousChapter!!.chapterLink)
                viewModel.updateCurrentChapterTitle(previousChapter!!.chapterTitle)

                coroutineScope.launch {
                    val chapterNumber = getChapterNumber(previousChapter!!.chapterLink)
                    val existingChapterRead = chaptersReadDao.getChapterRead(mangaId, chapterNumber)
                    if (existingChapterRead == null) {
                        chaptersReadDao.addOrUpdateChaptersRead(
                            chapterRead = ChaptersRead(
                                mangaId = mangaId,
                                chapterLink = previousChapter!!.chapterLink,
                                chapterTitle = previousChapter!!.chapterTitle,
                                chapter = chapterNumber,
                                totalPages = previousChapterTotalPages,
                                timeStamp = System.currentTimeMillis()
                            )
                        )
                        Log.d("Chapters", "chapter added: $chapterNumber")
                    } else {
                        Log.d("Chapters", "chapter already exists: $chapterNumber")
                    }
                }
            }

            // Handle navigation to the next chapter when not in reader mode 0
            if (currentPage == totalPages + 1 && nextChapter != null && readerModeSelected != 0) {
                fromExisting = true
                endOrStart = true
                Log.d("Reader", "Next Chapter Link: ${nextChapter!!.chapterLink}")

                viewModel.updateCurrentChapterLink(nextChapter!!.chapterLink)
                viewModel.updateCurrentChapterTitle(nextChapter!!.chapterTitle)

                coroutineScope.launch {
                    val chapterNumber = getChapterNumber(nextChapter!!.chapterLink)
                    val existingChapterRead = chaptersReadDao.getChapterRead(mangaId, chapterNumber)
                    if (existingChapterRead == null) {
                        chaptersReadDao.addOrUpdateChaptersRead(
                            chapterRead = ChaptersRead(
                                mangaId = mangaId,
                                chapterLink = nextChapter!!.chapterLink,
                                chapterTitle = nextChapter!!.chapterTitle,
                                chapter = chapterNumber,
                                totalPages = nextChapterTotalPages,
                                timeStamp = System.currentTimeMillis()
                            )
                        )
                        Log.d("Chapters", "chapter added: $chapterNumber")
                    } else {
                        Log.d("Chapters", "chapter already exists: $chapterNumber")
                    }
                }
            }
        }
    }



    /*LaunchedEffect(listState.firstVisibleItemIndex, pagerState.currentPage)
    {
        currentPage = if(readerModeSelected == 0)
        {
            listState.firstVisibleItemIndex
        }
        else
        {
            pagerState.currentPage
        }

        Log.d("Reader", "Current page updated to: $currentPage")

        if(currentPage != totalPages + 1 && currentPage != 0)
        {
            if(previousChapterLink == currentChapterLink)
            {
                coroutineScope.launch {
                    try {
                        val existingChapterRead = chaptersReadDao.getChapterRead(mangaId, chapter)
                        if (existingChapterRead != null)
                        {
                            Log.d("Reader", "Updating for chapter: $chapter")
                            chaptersReadDao.updatePage(
                                mangaId,
                                chapter,
                                currentPage,
                                System.currentTimeMillis()
                            )
                            Log.d(
                                "Reader",
                                "Updated page: $currentPage for mangaId: $mangaId, chapter: $chapter"
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("Reader", "Error saving page: ${e.message}")
                    }
                }
            }

        }


        if(readerModeSelected == 0 && currentPage == 0 && previousChapter != null)
        {
            fromExisting = true
            endOrStart = false
            Log.d("Reader", "Previous Chapter Link: ${previousChapter!!.chapterLink}")


            viewModel.updateCurrentChapterLink(previousChapter!!.chapterLink)
            viewModel.updateCurrentChapterTitle(previousChapter!!.chapterTitle)

            coroutineScope.launch {
                val chapterNumber = getChapterNumber(previousChapter!!.chapterLink)
                val existingChapterRead = chaptersReadDao.getChapterRead(mangaId, chapterNumber)
                if (existingChapterRead == null) {
                    chaptersReadDao.addOrUpdateChaptersRead(
                        chapterRead = ChaptersRead(
                            mangaId = mangaId,
                            chapterLink = previousChapter!!.chapterLink,
                            chapterTitle = previousChapter!!.chapterTitle,
                            chapter = chapterNumber,
                            totalPages = previousChapterTotalPages,
                            timeStamp = System.currentTimeMillis(),
                        )
                    )
                    Log.d("Chapters", "chapter added: $chapterNumber")
                }
                else
                {
                    Log.d("Chapters", "chapter already exists: $chapterNumber")
                }
            }
        }

        if(readerModeSelected == 0 && currentPage == totalPages + 2 && nextChapter != null)
        {
            fromExisting = true
            endOrStart = true
            Log.d("Reader", "Next Chapter Link: ${nextChapter!!.chapterLink}")
            viewModel.updateCurrentChapterLink(nextChapter!!.chapterLink)
            viewModel.updateCurrentChapterTitle(nextChapter!!.chapterTitle)

            coroutineScope.launch {
                Log.d("Reader", "HELLO PLEASE: $currentChapterLink")
                val chapterNumber = getChapterNumber(nextChapter!!.chapterLink)
                val existingChapterRead = chaptersReadDao.getChapterRead(mangaId, chapterNumber)
                if (existingChapterRead == null) {
                    chaptersReadDao.addOrUpdateChaptersRead(
                        chapterRead = ChaptersRead(
                            mangaId = mangaId,
                            chapterLink = nextChapter!!.chapterLink,
                            chapterTitle = nextChapter!!.chapterTitle,
                            chapter = chapterNumber,
                            totalPages = nextChapterTotalPages,
                            timeStamp = System.currentTimeMillis(),
                        )
                    )
                    Log.d("Chapters", "chapter added: $chapterNumber")
                } else {
                    Log.d("Chapters", "chapter already exists: $chapterNumber")
                }
            }
        }


        if(currentPage == 0 && previousChapter != null && readerModeSelected != 0)
        {
            fromExisting = true
            endOrStart = false
            Log.d("Reader", "Previous Chapter Link: ${previousChapter!!.chapterLink}")


            viewModel.updateCurrentChapterLink(previousChapter!!.chapterLink)
            viewModel.updateCurrentChapterTitle(previousChapter!!.chapterTitle)

            coroutineScope.launch {
                val chapterNumber = getChapterNumber(previousChapter!!.chapterLink)
                val existingChapterRead = chaptersReadDao.getChapterRead(mangaId, chapterNumber)
                if (existingChapterRead == null) {
                    chaptersReadDao.addOrUpdateChaptersRead(
                        chapterRead = ChaptersRead(
                            mangaId = mangaId,
                            chapterLink = previousChapter!!.chapterLink,
                            chapterTitle = previousChapter!!.chapterTitle,
                            chapter = chapterNumber,
                            totalPages = previousChapterTotalPages,
                            timeStamp = System.currentTimeMillis(),
                        )
                    )
                    Log.d("Chapters", "chapter added: $chapterNumber")
                }
                else
                {
                    Log.d("Chapters", "chapter already exists: $chapterNumber")
                }
            }
        }

        if(currentPage == totalPages + 1 && nextChapter != null && readerModeSelected != 0)
        {
            fromExisting = true
            endOrStart = true
            Log.d("Reader", "Next Chapter Link: ${nextChapter!!.chapterLink}")
            viewModel.updateCurrentChapterLink(nextChapter!!.chapterLink)
            viewModel.updateCurrentChapterTitle(nextChapter!!.chapterTitle)

            coroutineScope.launch {
                Log.d("Reader", "HELLO PLEASE: $currentChapterLink")
                val chapterNumber = getChapterNumber(nextChapter!!.chapterLink)
                val existingChapterRead = chaptersReadDao.getChapterRead(mangaId, chapterNumber)
                if (existingChapterRead == null) {
                    chaptersReadDao.addOrUpdateChaptersRead(
                        chapterRead = ChaptersRead(
                            mangaId = mangaId,
                            chapterLink = nextChapter!!.chapterLink,
                            chapterTitle = nextChapter!!.chapterTitle,
                            chapter = chapterNumber,
                            totalPages = nextChapterTotalPages,
                            timeStamp = System.currentTimeMillis(),
                        )
                    )
                    Log.d("Chapters", "chapter added: $chapterNumber")
                } else {
                    Log.d("Chapters", "chapter already exists: $chapterNumber")
                }
            }
        }
    }*/


    Scaffold(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        topBar = {
            AnimatedVisibility(
                visible = isTopBarVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.popBackStack()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Localized description",
                                tint = Color.White
                            )
                        }
                    },
                    title = {
                        Text("${
                            currentChapterTitle.lowercase()
                                .substringAfterLast("chapter")
                                .trim()
                                .split(" ", limit = 2)
                                .let {
                                    "Chapter ${it[0]} ${
                                        it.getOrNull(1)
                                            ?.replaceFirstChar { char -> char.titlecase() } ?: ""
                                    }"
                                }

                        }",
                            fontSize = (20.sp),
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate(
                                        Screen.ChaptersScreen.withArgs(
                                            URLEncoder.encode(
                                                mangaLink,
                                                StandardCharsets.UTF_8.toString()
                                            ),
                                        )
                                    )
                                }, maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }, actions = {
                        IconButton(onClick = {
                            isReaderSettingsVisible = !isReaderSettingsVisible
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Hello",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF352e38)
                    )
                )
            }

        },
        bottomBar = {
            AnimatedVisibility(
                visible = isTopBarVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                BottomAppBar(
                    containerColor = Color(0xFF352e38),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = {},
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(
                            modifier = Modifier.width(100.dp)
                        )

                        IconButton(
                            onClick = {}
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }

        }
    ) { innerPadding ->

        if(isReaderSettingsVisible)
        {
            ModalBottomSheet(
                onDismissRequest = { isReaderSettingsVisible = false },
                sheetState = sheetState,
                scrimColor = Color.Transparent,
                containerColor = Color(0xFF352e38),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                ){
                    Text(text = "Reader Mode", color = Color.White)
                    FlowRow() {
                        readerModeOptions.forEachIndexed { index, mode ->
                            FilterChip(
                                selected = readerModeSelected == index,
                                onClick = {
                                    viewModel.saveReaderMode(index)
                                },
                                label = { Text(text = mode, color = if(readerModeSelected == index) Color.Black else Color.White) },
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                    }
                }

            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    isTopBarVisible = !isTopBarVisible
                }
        ) {
            when (readerModeSelected)
            {
                0 -> {
                    LaunchedEffect(totalPages) {
                        if(totalPages > 0)
                        {
                            if(currentPage == 0)
                            {
                                currentPage = 2
                                coroutineScope.launch {
                                    Log.d("Reader", "Current Page: $currentPage")
                                    listState.scrollToItem(currentPage)
                                }
                            }
                            else
                            {
                                listState.scrollToItem(currentPage + 1)
                            }
                        }
                    }
                    val screenMaxHeight = maxHeight
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState
                    )
                    {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(screenMaxHeight/2).background(Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Previous chapter",
                                    color = Color.White,
                                    fontSize = 20.sp
                                )
                            }
                        }
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(screenMaxHeight/2).background(Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Start",
                                    color = Color.White,
                                    fontSize = 20.sp
                                )
                            }
                        }
                        itemsIndexed(imagePaths) { index, imagePath ->
                            var aspectRatio by remember { mutableStateOf(1f) }
                            var isImageLoaded by remember { mutableStateOf(false) }

                            if (imagePath.isNotEmpty()) {
                                LaunchedEffect(imagePath) {
                                    val request = ImageRequest.Builder(context)
                                        .data(imagePath)
                                        .allowHardware(false)
                                        .build()

                                    val result =
                                        (context.imageLoader.execute(request) as? SuccessResult)?.drawable
                                    result?.let { drawable ->
                                        aspectRatio =
                                            drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight.toFloat()
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
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f)
                                            .background(Color.Transparent)
                                    )
                                }
                            } else {
                                // Display a loading placeholder
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f) // Default aspect ratio for placeholders
                                        .background(Color.Transparent), // Placeholder color
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.width(64.dp),
                                        color = MaterialTheme.colorScheme.secondary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    )
                                }
                            }
                        }
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(screenMaxHeight/2).background(Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "End",
                                    color = Color.White,
                                    fontSize = 20.sp
                                )
                            }
                        }
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(screenMaxHeight/2).background(Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Next chapter",
                                    color = Color.White,
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }
                }

                1 -> {
                    LaunchedEffect(totalPages) {
                        if(totalPages > 0)
                        {
                            if(currentPage == 0)
                            {
                                currentPage = 1
                                coroutineScope.launch {
                                    Log.d("Reader", "Current Page: $currentPage")
                                    pagerState.scrollToPage(currentPage)
                                }
                            }
                            else
                            {
                                coroutineScope.launch {
                                    pagerState.scrollToPage(currentPage)
                                }
                            }
                        }
                    }
                    HorizontalPager(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                            .background(Color.Black),
                        state = pagerState,
                    ) { page ->
                        when (page) {
                            0 -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ){
                                    Text(text = "Start", color = Color.White)
                                }
                            }
                            totalPages + 1 -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ){
                                    Text(text = "End", color = Color.White)
                                }
                            }
                            else -> {
                                var aspectRatio by remember { mutableStateOf(1f) }
                                var isImageLoaded by remember { mutableStateOf(false) }

                                val imagePath = imagePaths[page - 1]
                                if (imagePath.isNotEmpty()) {
                                    LaunchedEffect(imagePath) {
                                        val request = ImageRequest.Builder(context)
                                            .data(imagePath)
                                            .allowHardware(false) // Avoid hardware bitmaps as they might cause issues with aspect ratio calculation
                                            .build()

                                        val result =
                                            (context.imageLoader.execute(request) as? SuccessResult)?.drawable
                                        result?.let { drawable ->
                                            aspectRatio =
                                                drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight.toFloat()
                                            isImageLoaded = true
                                        }
                                    }

                                    if (isImageLoaded) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Image(
                                                painter = rememberAsyncImagePainter(model = imagePath),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .aspectRatio(aspectRatio)
                                            )
                                        }

                                    } else {
                                        // Placeholder to maintain space while the image is loading
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(1f) // Default aspect ratio while loading
                                                .background(Color.Transparent) // Placeholder color
                                        )
                                    }
                                } else {
                                    // Display a loading placeholder
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .aspectRatio(1f)
                                            .background(Color.Transparent), // Placeholder color
                                        contentAlignment = Alignment.Center
                                    ) {
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
                }

                2 -> {
                    LaunchedEffect(totalPages) {
                        if(totalPages > 0)
                        {
                            if(currentPage == 0)
                            {
                                currentPage = 1
                                coroutineScope.launch {
                                    Log.d("Reader", "Current Page: $currentPage")
                                    pagerState.scrollToPage(currentPage)
                                }
                            }
                            else
                            {
                                coroutineScope.launch {
                                    pagerState.scrollToPage(currentPage)
                                }
                            }
                        }
                    }
                    VerticalPager(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                            .background(Color.Black),
                        state = pagerState,
                    ) { page ->
                        when (page) {
                            0 -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ){
                                    Text(text = "Start", color = Color.White)
                                }
                            }
                            totalPages + 1 -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ){
                                    Text(text = "End", color = Color.White)
                                }
                            }
                            else -> {
                                var aspectRatio by remember { mutableStateOf(1f) }
                                var isImageLoaded by remember { mutableStateOf(false) }

                                val imagePath = imagePaths[page - 1]
                                if (imagePath.isNotEmpty()) {
                                    LaunchedEffect(imagePath) {
                                        val request = ImageRequest.Builder(context)
                                            .data(imagePath)
                                            .allowHardware(false) // Avoid hardware bitmaps as they might cause issues with aspect ratio calculation
                                            .build()

                                        val result =
                                            (context.imageLoader.execute(request) as? SuccessResult)?.drawable
                                        result?.let { drawable ->
                                            aspectRatio =
                                                drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight.toFloat()
                                            isImageLoaded = true
                                        }
                                    }

                                    if (isImageLoaded) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Image(
                                                painter = rememberAsyncImagePainter(model = imagePath),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .aspectRatio(aspectRatio)
                                            )
                                        }

                                    } else {
                                        // Placeholder to maintain space while the image is loading
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(1f) // Default aspect ratio while loading
                                                .background(Color.Transparent) // Placeholder color
                                        )
                                    }
                                } else {
                                    // Display a loading placeholder
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .aspectRatio(1f)
                                            .background(Color.Transparent), // Placeholder color
                                        contentAlignment = Alignment.Center
                                    ) {
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
