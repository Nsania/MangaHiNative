package ui

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import data.dao.ChaptersReadDao
import data.dao.ChaptersReadInformationDao
import data.dao.LibraryDao
import data.dao.MangaChaptersDao
import data.dao.MangasDao
import data.tables.ChaptersRead
import data.tables.Library
import data.tables.MangaChapters
import data.tables.Mangas
import data.viewmodels.ChaptersViewModel
import kotlinx.coroutines.launch
import scraper.getChapterNumber
import scraper.getChapters
import scraper.getMangaDescription
import scraper.getPageCount
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Chapters(
    mangaLink: String,
    navController: NavController,
    chaptersReadDao: ChaptersReadDao,
    libraryDao: LibraryDao,
    mangasDao: MangasDao,
    chaptersReadInformationDao: ChaptersReadInformationDao,
    mangaChaptersDao: MangaChaptersDao,
    chaptersViewModel: ChaptersViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val decodedMangaLink = remember(mangaLink) {
        try {
            URLDecoder.decode(mangaLink, StandardCharsets.UTF_8.toString())
        } catch (e: Exception) {
            mangaLink
        }
    }

    val chapters by chaptersViewModel.chapters.collectAsState()
    val mangaId by chaptersViewModel.mangaId.collectAsState()
    val inLibrary by chaptersViewModel.inLibrary.collectAsState()
    val libraryText by chaptersViewModel.libraryText.collectAsState()
    val mangaDescription by chaptersViewModel.mangaDescription.collectAsState()
    val title by chaptersViewModel.title.collectAsState()
    val imageCover by chaptersViewModel.imageCover.collectAsState()
    val readChapters by chaptersViewModel.readChapters.collectAsState()
    val readChaptersNumber by chaptersViewModel.readChaptersNumber.collectAsState()

    var expandedState by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val systemUi = rememberSystemUiController()
    var isOnTop by remember { mutableStateOf(true) }
    val density = LocalDensity.current.density
    var scrollOffset by remember { mutableFloatStateOf(0f) }

    var chapterRead by remember { mutableStateOf(false) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemScrollOffset }.collect { offset ->
            scrollOffset = offset / density
            isOnTop = listState.firstVisibleItemIndex == 0 && offset <= 0
        }
    }

    val topBarContainerColor by animateColorAsState(
        targetValue = if (isOnTop) Color.Transparent else Color(0xFF352e38),
        animationSpec = tween(durationMillis = 300),
        label = "TopBarColor"
    )

    val topBarTitleColor by animateColorAsState(
        targetValue = if (isOnTop) Color.Transparent else Color.White,
        animationSpec = tween(durationMillis = 300),
        label = "TopBarTitleColor"
    )

    LaunchedEffect(Unit) {
        systemUi.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = false,
        )
    }

    LaunchedEffect(decodedMangaLink) {
        coroutineScope.launch {
            chaptersViewModel.updateMangaDescription(getMangaDescription(context, decodedMangaLink))
        }
    }

    LaunchedEffect(decodedMangaLink) {
        coroutineScope.launch {
            chaptersReadInformationDao.getChaptersReadInformation(decodedMangaLink).collect { chaptersRead ->
                chaptersViewModel.updateReadChapters(chaptersRead)
            }
        }
    }

    LaunchedEffect(decodedMangaLink) {
        coroutineScope.launch {
            var manga = mangasDao.getManga(mangaLink = decodedMangaLink)

            if (manga == null) {
                mangasDao.addManga(
                    Mangas(
                        mangaLink = decodedMangaLink,
                        mangaTitle = "Loading...",
                        mangaImageCover = "",
                        mangaDescription = ""
                    )
                )
                manga = mangasDao.getManga(mangaLink = decodedMangaLink)
            }

            if (manga != null) {
                chaptersViewModel.updateMangaId(manga.mangaId)
                chaptersViewModel.updateTitle(manga.mangaTitle)
                chaptersViewModel.updateImageCover(manga.mangaImageCover)

                val check = libraryDao.getManga(mangaId = manga.mangaId)
                if (check != null) {
                    chaptersViewModel.updateInLibrary(true)
                    chaptersViewModel.updateLibraryText("In library")
                } else {
                    chaptersViewModel.updateInLibrary(false)
                    chaptersViewModel.updateLibraryText("Add to library")
                }

                if (mangaChaptersDao.checkManga(manga.mangaId)) {
                    Log.d("Chapters", "Loading from Database")
                    mangaChaptersDao.getMangaChapters(manga.mangaId).collect { chapters ->
                        chaptersViewModel.updateChapters(chapters)
                    }
                }
            }
        }
    }

    LaunchedEffect(mangaId) {
        if (mangaId != 0) {
            coroutineScope.launch {
                if (!mangaChaptersDao.checkManga(mangaId)) {
                    Log.d("Chapters", "Adding to Database")
                    val scraperChapters = getChapters(context, mangaId, decodedMangaLink)
                    val dbChapters = scraperChapters.map {
                        MangaChapters(
                            mangaId = it.mangaId,
                            chapter = it.chapter,
                            chapterTitle = it.chapterTitle,
                            chapterLink = it.chapterLink,
                            uploadDate = it.uploadDate
                        )
                    }
                    chaptersViewModel.updateChapters(dbChapters)
                    dbChapters.forEach { chapter ->
                        mangaChaptersDao.addMangaChapters(chapter)
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF160e1a))
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    bottom = innerPadding.calculateBottomPadding()
                )
            ) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        ) {
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imageCover)
                                    .addHeader("Referer", "https://mangakakalot.com/")
                                    .addHeader("User-Agent", "Mozilla/5.0")
                                    .diskCachePolicy(CachePolicy.ENABLED)
                                    .memoryCachePolicy(CachePolicy.ENABLED)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .blur(radius = 10.dp),
                                contentScale = ContentScale.Crop
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(alpha = 0.99f)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color(0xFF160e1a).copy(alpha = 1f)
                                            ),
                                            startY = 0f,
                                            endY = Float.POSITIVE_INFINITY
                                        )
                                    )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imageCover)
                                    .addHeader("Referer", "https://mangakakalot.com/")
                                    .addHeader("User-Agent", "Mozilla/5.0")
                                    .diskCachePolicy(CachePolicy.ENABLED)
                                    .memoryCachePolicy(CachePolicy.ENABLED)
                                    .build(),
                                contentDescription = "Image",
                                modifier = Modifier
                                    .width(135.dp)
                                    .height(190.dp)
                                    .padding(start = 10.dp)
                                    .padding(bottom = 10.dp)
                                    .clip(RoundedCornerShape(5)),
                                contentScale = ContentScale.Crop,
                            )
                            Column(
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier
                                    .background(Color.Transparent)
                                    .padding(bottom = 10.dp)
                                    .padding(start = 20.dp)
                                    .height(150.dp)
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 20.sp,
                                    color = Color.White,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(end = 20.dp)
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            if (!inLibrary) {
                                                if (mangaId != 0) {
                                                    libraryDao.addToLibrary(
                                                        Library(
                                                            mangaId = mangaId,
                                                            timeStamp = System.currentTimeMillis()
                                                        )
                                                    )
                                                    chaptersViewModel.updateInLibrary(true)
                                                    chaptersViewModel.updateLibraryText("In library")
                                                }
                                            } else {
                                                libraryDao.deleteLibraryEntry(mangaId = mangaId)
                                                chaptersViewModel.updateInLibrary(false)
                                                chaptersViewModel.updateLibraryText("Add to library")
                                            }
                                        }
                                    },
                                    modifier = Modifier.padding(top = 5.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (inLibrary) Color(0xFF6a4d75) else Color(
                                            0xFF4c8f45
                                        ),
                                    )
                                ) {
                                    Row {
                                        Text(
                                            text = libraryText,
                                            color = Color.White
                                        )
                                    }

                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(5.dp))
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = LinearOutSlowInEasing
                                )
                            ),
                        onClick = {
                            expandedState = !expandedState
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 20.dp, horizontal = 10.dp)
                            ) {
                                Text(
                                    text = "Description",
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Box {
                                    this@Row.AnimatedVisibility(
                                        visible = !expandedState,
                                        enter = fadeIn(),
                                        exit = fadeOut()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = Color.White
                                        )
                                    }

                                    this@Row.AnimatedVisibility(
                                        visible = expandedState,
                                        enter = fadeIn(),
                                        exit = fadeOut()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowUp,
                                            contentDescription = null,
                                            tint = Color.White
                                        )
                                    }
                                }


                            }
                            if (expandedState) {
                                Text(
                                    text = mangaDescription,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp)
                                        .padding(bottom = 10.dp),
                                    maxLines = 10,
                                    overflow = TextOverflow.Ellipsis,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(5.dp))
                }

                items(chapters) { chapter ->

                    chapterRead = chapter.chapter in readChaptersNumber

                    val temp = readChapters.find { it.chapter == chapter.chapter }

                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .padding(horizontal = 10.dp)
                            .clickable {
                                coroutineScope.launch {
                                    val chapterNumber = getChapterNumber(chapter.chapterLink)
                                    val existingChapterRead =
                                        chaptersReadDao.getChapterRead(mangaId, chapterNumber)
                                    val totalPages =
                                        getPageCount(context, chapterLink = chapter.chapterLink)
                                    if (existingChapterRead == null) {
                                        chaptersReadDao.addOrUpdateChaptersRead(
                                            chapterRead = ChaptersRead(
                                                mangaId = mangaId,
                                                chapterLink = chapter.chapterLink,
                                                chapterTitle = chapter.chapterTitle,
                                                chapter = chapterNumber,
                                                totalPages = totalPages,
                                                timeStamp = System.currentTimeMillis(),
                                            )
                                        )

                                        Log.d("Chapters", "chapter added: $chapterNumber")
                                    } else {
                                        Log.d(
                                            "Chapters",
                                            "chapter already exists: $chapterNumber"
                                        )
                                    }
                                }
                                navController.navigate(
                                    Screen.ReaderScreen.withArgs(
                                        URLEncoder.encode(
                                            chapter.chapterLink,
                                            StandardCharsets.UTF_8.toString(),
                                        ),
                                        mangaId.toString(),
                                        chapter.chapterTitle,
                                        URLEncoder.encode(
                                            decodedMangaLink,
                                            StandardCharsets.UTF_8.toString()
                                        ),
                                    )
                                )
                            }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${
                                    chapter.chapterTitle.lowercase()
                                        .substringAfterLast("chapter")
                                        .trim()
                                        .split(" ", limit = 2)
                                        .let { "Chapter ${it[0]} ${it.getOrNull(1)?.replaceFirstChar { char -> char.titlecase() } ?: ""}" }

                                }",
                                fontSize = 18.sp,
                                color = if (chapterRead) Color.Gray else Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = chapter.uploadDate,
                                textAlign = TextAlign.End,
                                fontSize = 14.sp,
                                color = if (chapterRead) Color.Gray else Color.White
                            )
                            Spacer(
                                modifier = Modifier.width(10.dp)
                            )
                            if (chapterRead) {
                                Text(
                                    text = "Pages left: ${temp?.pagesLeft}",
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Start,
                                    color = Color.Gray
                                )
                            }

                        }
                    }
                }
            }

            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Text(
                        text = title,
                        color = topBarTitleColor
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topBarContainerColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
            )
        }
    }
}