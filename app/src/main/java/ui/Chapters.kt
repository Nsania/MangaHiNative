package ui

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch
import scraper.Chapter
import scraper.getChapters
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import data.dao.*
import data.tables.ChaptersRead
import data.tables.ChaptersReadInformation
import data.tables.Library
import data.tables.Mangas
import data.viewmodels.ChaptersViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import scraper.getChapterNumber
import scraper.getMangaDescription
import scraper.getPageCount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Chapters(
    mangaLink: String,
    navController: NavController,
    chaptersReadDao: ChaptersReadDao,
    libraryDao: LibraryDao,
    mangasDao: MangasDao,
    chaptersReadInformationDao: ChaptersReadInformationDao,
    chaptersViewModel: ChaptersViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()

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
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val listState = rememberLazyListState()
    val systemUi = rememberSystemUiController()
    var isOnTop by remember { mutableStateOf(true) }
    val density = LocalDensity.current.density
    var scrollOffset by remember { mutableStateOf(0f) }

    var chapterRead by remember { mutableStateOf(false)}

    LaunchedEffect(Unit)
    {
        systemUi.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = false,
        )
    }


    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemScrollOffset
        }.collect { offset ->
            scrollOffset = offset / density // Convert pixels to dp
            if (scrollOffset > 0) {
                // Trigger action when scrolled away from the top
                isOnTop = false
            }
            else
            {
                isOnTop = true
            }
        }
    }


    LaunchedEffect(mangaLink) {
        coroutineScope.launch {
            chaptersViewModel.updateMangaDescription(getMangaDescription(mangaLink))
        }
    }

    LaunchedEffect(mangaLink) {
        coroutineScope.launch {

            chaptersReadInformationDao.getChaptersReadInformation(mangaLink).collect { chaptersRead ->
                chaptersViewModel.updateReadChapters(chaptersRead)
            }
        }
    }

    LaunchedEffect(mangaLink) {
        coroutineScope.launch {
            chaptersViewModel.updateChapters(getChapters(mangaLink))
        }
    }

    LaunchedEffect(mangaLink) {
        coroutineScope.launch {
            val manga = mangasDao.getManga(mangaLink = mangaLink)
            chaptersViewModel.updateMangaId(manga?.mangaId ?: 0)
            chaptersViewModel.updateTitle(manga?.mangaTitle ?: "")
            chaptersViewModel.updateImageCover(manga?.mangaImageCover ?: "")

            val check = libraryDao.getManga(mangaId = manga?.mangaId ?: 0)
            if (check != null)
            {
                chaptersViewModel.updateInLibrary(true)
                chaptersViewModel.updateLibraryText("In library")
            }
            else
            {
                chaptersViewModel.updateInLibrary(false)
                chaptersViewModel.updateLibraryText("Add to library")
            }
        }
    }


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        navigateTo(navController, Screen.LibraryScreen.route)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Text(title)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if(isOnTop) Color.Transparent else Color(0xFF352e38),
                    titleContentColor = if(isOnTop) Color.Transparent else Color.White,
                ),
            )
        }
    )
    { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF160e1a))
        ){

        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = listState
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                    ) {
                        AsyncImage(
                            model = imageCover,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .blur(radius = 10.dp),
                            contentScale = ContentScale.Crop
                        )

                        // Gradient overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(alpha = 0.99f)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.9f)
                                        ),
                                        startY = 0f,
                                        endY = Float.POSITIVE_INFINITY
                                    )
                                )
                        )
                    }

                    // Original Row content
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        AsyncImage(
                            model = imageCover,
                            contentDescription = "Image",
                            modifier = Modifier
                                .width(140.dp)
                                .height(190.dp)
                                .padding(start = 10.dp)
                                .padding(bottom = 10.dp)
                                .clip(RoundedCornerShape(1)),
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
                            Text(text = title, fontSize = 30.sp, color = Color.White)
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        if(!inLibrary) {
                                            libraryDao.addToLibrary(Library(mangaId = mangaId, timeStamp = System.currentTimeMillis()))
                                            chaptersViewModel.updateInLibrary(true)
                                            chaptersViewModel.updateLibraryText("In library")
                                        } else {
                                            libraryDao.deleteLibraryEntry(mangaId = mangaId)
                                            chaptersViewModel.updateInLibrary(false)
                                            chaptersViewModel.updateLibraryText("Add to library")
                                        }
                                    }
                                },
                                modifier = Modifier.padding(top = 5.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (inLibrary) Color(0xFF6a4d75) else Color(0xFF4c8f45),
                                )
                            ) {
                                Row(){
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
                            modifier = Modifier.padding(vertical = 20.dp, horizontal = 5.dp)
                        ) {
                            Text(text = "Description", fontSize = 20.sp, color = Color.White)
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
                        if(expandedState) {
                            Text(
                                text = mangaDescription,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 5.dp)
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
                /*val color = if (chapter.chapter in readChaptersNumber) {
                    Color.Gray
                } else {
                    Color.Transparent
                }*/

                if(chapter.chapter in readChaptersNumber)
                {
                    chapterRead = true
                }
                else
                {
                    chapterRead = false
                }

                val temp = readChapters.find {it.chapter == chapter.chapter}

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .padding(horizontal = 10.dp)
                        .clickable {
                            coroutineScope.launch {
                                val chapterNumber = getChapterNumber(chapter.readerLink)
                                val existingChapterRead =
                                    chaptersReadDao.getChapterRead(mangaId, chapterNumber)
                                val totalPages = getPageCount(chapterLink = chapter.readerLink)
                                if (existingChapterRead == null) {
                                    chaptersReadDao.addOrUpdateChaptersRead(
                                        chapterRead = ChaptersRead(
                                            mangaId = mangaId,
                                            chapterLink = chapter.readerLink,
                                            chapterTitle = chapter.title,
                                            chapter = chapterNumber,
                                            totalPages = totalPages,
                                            timeStamp = System.currentTimeMillis(),
                                        )
                                    )

                                    Log.d("Chapters", "chapter added: $chapterNumber")
                                } else {
                                    Log.d("Chapters", "chapter already exists: $chapterNumber")
                                }
                            }
                            navController.navigate(
                                Screen.ReaderScreen.withArgs(
                                    URLEncoder.encode(
                                        chapter.readerLink,
                                        StandardCharsets.UTF_8.toString(),
                                    ),
                                    mangaId.toString(),
                                    chapter.title,
                                    URLEncoder.encode(
                                        mangaLink,
                                        StandardCharsets.UTF_8.toString()
                                    ),
                                )
                            )
                        }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ){
                        Text(
                            text = "${
                                chapter.title.lowercase()
                                    .substringAfterLast("chapter")
                                    .trim()
                                    .split(" ", limit = 2)
                                    .let { "Chapter ${it[0]} ${it.getOrNull(1)?.replaceFirstChar { char -> char.titlecase() } ?: ""}" }

                            }",
                            fontSize = 18.sp,
                            color = if(chapterRead) Color.Gray else Color.White,
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
                            color = if(chapterRead) Color.Gray else Color.White
                        )
                        Spacer(
                            modifier = Modifier.width(10.dp)
                        )
                        if(chapterRead) {
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
    }
}
