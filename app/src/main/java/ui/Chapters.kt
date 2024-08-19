package ui

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
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
import kotlinx.coroutines.flow.first
import scraper.getChapterNumber
import scraper.getMangaDescription
import scraper.getPageCount

@Composable
fun Chapters(
    mangaLink: String,
    navController: NavController,
    chaptersReadDao: ChaptersReadDao,
    libraryDao: LibraryDao,
    mangasDao: MangasDao,
    chaptersReadInformationDao: ChaptersReadInformationDao
) {
    val coroutineScope = rememberCoroutineScope()
    var chapters by remember { mutableStateOf<List<Chapter>>(emptyList()) }
    var mangaId by remember { mutableIntStateOf(0) }
    var inLibrary by remember { mutableStateOf(false) }
    var libraryText by remember { mutableStateOf("Add to Library") }
    val readChapters = remember { mutableStateListOf<Double>() }
    val readChaptersList = remember { mutableStateListOf<ChaptersReadInformation>()}
    var mangaDescription by remember {mutableStateOf("")}
    var expandedState by remember {mutableStateOf(false)}
    var title by remember {mutableStateOf("")}
    var imageCover by remember {mutableStateOf("")}

    LaunchedEffect(mangaLink) {
        coroutineScope.launch {
            mangaDescription = getMangaDescription(mangaLink)
        }
    }

    LaunchedEffect(mangaLink) {
        coroutineScope.launch {
            val temp = chaptersReadInformationDao.getChaptersReadInformation(mangaLink).first()
            readChapters.clear()
            readChapters.addAll(temp.map {it.chapter})
            readChaptersList.clear()
            readChaptersList.addAll(temp)
        }
    }

    LaunchedEffect(mangaLink) {
        coroutineScope.launch {
            chapters = getChapters(mangaLink)
        }
    }

    LaunchedEffect(mangaLink) {
        coroutineScope.launch {
            val manga = mangasDao.getManga(mangaLink = mangaLink)
            mangaId = manga?.mangaId ?: 0
            title = manga?.mangaTitle ?: ""
            imageCover = manga?.mangaImageCover ?: ""
            val check = libraryDao.getManga(mangaId = mangaId)
            if (check != null) {
                inLibrary = true
                libraryText = "In Library"
            }
        }
    }

    /*Column(*//*modifier = Modifier.verticalScroll(rememberScrollState())*//*){
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
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
                        .width(120.dp)
                        .height(180.dp)
                        .padding(start = 10.dp)
                        .padding(bottom = 10.dp),
                    contentScale = ContentScale.Crop,
                )
                // Add other Row content here if needed
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier
                    .background(Color.Transparent)
                    .padding(bottom = 10.dp)
                    .padding(start = 20.dp)
                    .height(150.dp)) {
                    Text(text = title, fontSize = 30.sp, color = Color.White)
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                if(!inLibrary)
                                {
                                    libraryDao.addToLibrary(Library(mangaId = mangaId, timeStamp = System.currentTimeMillis()))
                                    inLibrary = true
                                    libraryText = "In Library"
                                }
                                else
                                {
                                    libraryDao.deleteLibraryEntry(mangaId = mangaId)
                                    inLibrary = false
                                    libraryText = "Add to library"
                                }
                            }
                        },
                        modifier = Modifier.padding(top = 5.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (inLibrary) Color.Gray else Color.Green,
                            contentColor = Color.White
                        )
                    ) {
                        Text(libraryText)
                    }
                }
            }
        }


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

        ) {
            Column() {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 20.dp, horizontal = 5.dp)
                ) {
                    Text(text = "Description", fontSize = 20.sp)
                }
                if(expandedState)
                {
                    Text(
                        text = mangaDescription,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 5.dp)
                            .padding(bottom = 10.dp),
                        maxLines = 10,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }


        LazyColumn(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 50.dp)
            .wrapContentHeight()) {
            items(chapters) { chapter ->
                val color = if (chapter.chapter in readChapters) {
                    Color.Gray
                } else {
                    Color.Transparent
                }

                val temp = readChaptersList.find {it.chapter == chapter.chapter}

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
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
                                            chapter = chapterNumber,
                                            totalPages = totalPages,
                                            timeStamp = System.currentTimeMillis()
                                        )
                                    )
                                    readChapters.add(chapterNumber)
                                    Log.d("Chapters", "chapter added: $chapterNumber")
                                } else {
                                    Log.d("Chapters", "chapter already exists: $chapterNumber")
                                }
                            }
                            navController.navigate(
                                Screen.ReaderScreen.withArgs(
                                    URLEncoder.encode(
                                        chapter.readerLink,
                                        StandardCharsets.UTF_8.toString()
                                    ),
                                    mangaId.toString()
                                )
                            )
                        }
                        .background(color)
                ) {
                    Text(
                        text = chapter.title,
                        modifier = Modifier.weight(1f)
                    )

                    if(color == Color.Gray)
                    {
                        Text(
                            text = "Pages left: ${temp?.pagesLeft}"
                        )
                    }

                    Text(
                        text = chapter.uploadDate,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
                Spacer(Modifier.height(30.dp))
            }
        }

        *//*Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 50.dp)
        ) {
            chapters.forEach { chapter ->
                val color = if (chapter.chapter in readChapters) {
                    Color.Gray
                } else {
                    Color.Transparent
                }

                val temp = readChaptersList.find { it.chapter == chapter.chapter }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
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
                                            chapter = chapterNumber,
                                            totalPages = totalPages,
                                            timeStamp = System.currentTimeMillis()
                                        )
                                    )
                                    readChapters.add(chapterNumber)
                                    Log.d("Chapters", "chapter added: $chapterNumber")
                                } else {
                                    Log.d("Chapters", "chapter already exists: $chapterNumber")
                                }
                            }
                            navController.navigate(
                                Screen.ReaderScreen.withArgs(
                                    URLEncoder.encode(
                                        chapter.readerLink,
                                        StandardCharsets.UTF_8.toString()
                                    ),
                                    mangaId.toString()
                                )
                            )
                        }
                        .background(color)
                ) {
                    Text(
                        text = chapter.title,
                        modifier = Modifier.weight(1f)
                    )

                    if (color == Color.Gray) {
                        Text(
                            text = "Pages left: ${temp?.pagesLeft}"
                        )
                    }

                    Text(
                        text = chapter.uploadDate,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
                Spacer(Modifier.height(30.dp))
            }
        }*//*
    }*/
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 50.dp)
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
                        .height(250.dp)
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
                            .width(120.dp)
                            .height(180.dp)
                            .padding(start = 10.dp)
                            .padding(bottom = 10.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop,
                    )
                    Column(
                        horizontalAlignment = Alignment.Start,
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
                                        inLibrary = true
                                        libraryText = "In Library"
                                    } else {
                                        libraryDao.deleteLibraryEntry(mangaId = mangaId)
                                        inLibrary = false
                                        libraryText = "Add to library"
                                    }
                                }
                            },
                            modifier = Modifier.padding(top = 5.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (inLibrary) Color.Gray else Color.Green,
                                contentColor = Color.White
                            )
                        ) {
                            Text(libraryText)
                        }
                    }
                }
            }
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
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 20.dp, horizontal = 5.dp)
                    ) {
                        Text(text = "Description", fontSize = 20.sp)
                    }
                    if(expandedState) {
                        Text(
                            text = mangaDescription,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 5.dp)
                                .padding(bottom = 10.dp),
                            maxLines = 10,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }

        items(chapters) { chapter ->
            val color = if (chapter.chapter in readChapters) {
                Color.Gray
            } else {
                Color.Transparent
            }

            val temp = readChaptersList.find {it.chapter == chapter.chapter}

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
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
                                        chapter = chapterNumber,
                                        totalPages = totalPages,
                                        timeStamp = System.currentTimeMillis()
                                    )
                                )
                                readChapters.add(chapterNumber)
                                Log.d("Chapters", "chapter added: $chapterNumber")
                            } else {
                                Log.d("Chapters", "chapter already exists: $chapterNumber")
                            }
                        }
                        navController.navigate(
                            Screen.ReaderScreen.withArgs(
                                URLEncoder.encode(
                                    chapter.readerLink,
                                    StandardCharsets.UTF_8.toString()
                                ),
                                mangaId.toString()
                            )
                        )
                    }
                    .background(color)
            ) {
                Text(
                    text = chapter.title,
                    modifier = Modifier.weight(1f)
                )

                if(color == Color.Gray) {
                    Text(
                        text = "Pages left: ${temp?.pagesLeft}"
                    )
                }

                Text(
                    text = chapter.uploadDate,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}
