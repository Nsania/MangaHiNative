package ui

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.BottomAppBar
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import data.dao.ChaptersReadDao
import data.dao.ChaptersReadInformationDao
import data.dao.MangaChaptersDao
import data.tables.ChaptersRead
import data.viewmodels.ReaderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.engawapg.lib.zoomable.ScrollGesturePropagation
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import scraper.fetchChapterPageUrls
import scraper.getChapterNumber
import scraper.getPageCount
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


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
    val imageLinks by viewModel.imageLinks.collectAsState()

    var currentPage by remember { mutableIntStateOf(0) }
    var isTopBarVisible by remember { mutableStateOf(false) }
    var isReaderSettingsVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val sheetState = rememberModalBottomSheetState()
    val systemUiController = rememberSystemUiController()
    val readerModeOptions  = listOf("Long Strip", "Paged(left to right)", "Paged(vertical)")
    val pagerState = rememberPagerState(pageCount = { totalPages })
    var continuing by remember { mutableStateOf(false) }

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
        if (currentChapterLink.isNotEmpty() && currentChapterLink != "") {
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
                }
                viewModel.updateImageLinks(fetchChapterPageUrls(currentChapterLink))

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

    LaunchedEffect(totalPages) {
        if (totalPages > 0) {
            coroutineScope.launch {
                listState.scrollToItem(currentPage)
                pagerState.scrollToPage(currentPage)
            }
        }
    }


    LaunchedEffect(listState, pagerState) {
        snapshotFlow {
            listState.firstVisibleItemIndex to pagerState.currentPage
        }.collect { (firstVisibleItemIndex, currentPageFromPager) ->

            currentPage = if (readerModeSelected == 0)
            {
                firstVisibleItemIndex
            }
            else
            {
                currentPageFromPager
            }

            Log.d("Reader", "Current page updated to: $currentPage")

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
                            onClick = {
                                if (previousChapter != null) {
                                    continuing = true
                                    viewModel.updateImageLinks(emptyList())
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
                                            Log.d("Chapters", "Previous chapter added: $chapterNumber")
                                        } else {
                                            Log.d("Chapters", "Previous chapter already exists: $chapterNumber")
                                        }
                                    }
                                }
                            },
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
                            onClick = {
                                if (nextChapter != null) {
                                    continuing = true
                                    viewModel.updateImageLinks(emptyList())
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
                                            Log.d("Chapters", "Next chapter added: $chapterNumber")
                                        } else {
                                            Log.d("Chapters", "Next chapter already exists: $chapterNumber")
                                        }
                                    }
                                }
                            }
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
                    FlowRow {
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

        Box(
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
                            if(continuing)
                            {
                                currentPage = 0
                            }
                            coroutineScope.launch {
                                listState.scrollToItem(currentPage)
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize().zoomable(
                            zoomState = rememberZoomState(),
                            onTap = {
                                isTopBarVisible = !isTopBarVisible
                            }
                        ),
                        state = listState
                    ) {
                        if (imageLinks.isNotEmpty())
                        {
                            itemsIndexed(imageLinks) { index, imageLink ->
                                var aspectRatio by remember { mutableStateOf(1f) }
                                var isImageLoaded by remember { mutableStateOf(false) }

                                if (imageLink.isNotEmpty())
                                {
                                    LaunchedEffect(imageLink) {
                                        val request = ImageRequest.Builder(context)
                                            .data(imageLink)
                                            .addHeader("Referer", "https://mangakakalot.com")
                                            .allowHardware(false)
                                            .build()

                                        val result = (context.imageLoader.execute(request) as? SuccessResult)?.drawable

                                        result?.let { drawable ->
                                            aspectRatio = drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight.toFloat()
                                            isImageLoaded = true
                                        }
                                    }

                                    if (isImageLoaded)
                                    {
                                        Image(
                                            painter = rememberAsyncImagePainter(
                                                model = ImageRequest.Builder(context)
                                                    .data(imageLink)
                                                    .addHeader("Referer", "https://mangakakalot.com") // Set Referer header
                                                    .build()
                                            ),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(aspectRatio)
                                        )
                                    }
                                    else
                                    {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(aspectRatio)
                                                .background(Color.Black),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(64.dp),
                                                color = MaterialTheme.colorScheme.secondary,
                                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    LaunchedEffect(totalPages) {
                        if(totalPages > 0)
                        {
                            if(continuing)
                            {
                                currentPage = 0
                            }
                            coroutineScope.launch {
                                pagerState.scrollToPage(currentPage)
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

                        if(imageLinks.isNotEmpty())
                        {
                            var aspectRatio by remember { mutableStateOf(1f) }
                            var isImageLoaded by remember { mutableStateOf(false) }


                            val imageLink = imageLinks[page]
                            if (imageLink.isNotEmpty())
                            {
                                LaunchedEffect(imageLink) {
                                    val request = ImageRequest.Builder(context)
                                        .data(imageLink)
                                        .addHeader("Referer", "https://mangakakalot.com")
                                        .allowHardware(false)
                                        .build()

                                    val result = (context.imageLoader.execute(request) as? SuccessResult)?.drawable

                                    result?.let { drawable ->
                                        aspectRatio = drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight.toFloat()
                                        isImageLoaded = true
                                    }
                                }

                                if (isImageLoaded)
                                {
                                    Image(
                                        painter = rememberAsyncImagePainter(
                                            model = ImageRequest.Builder(context)
                                                .data(imageLink)
                                                .addHeader("Referer", "https://mangakakalot.com") // Set Referer header
                                                .build()
                                        ),
                                        contentDescription = null,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(aspectRatio)
                                            .zoomable(
                                                zoomState = rememberZoomState(),
                                                scrollGesturePropagation = ScrollGesturePropagation.ContentEdge,
                                                onTap = {
                                                    isTopBarVisible = !isTopBarVisible
                                                }
                                                )
                                    )
                                }
                                else
                                {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(aspectRatio)
                                            .background(Color.Black),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(64.dp),
                                            color = MaterialTheme.colorScheme.secondary,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant
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
                            if(continuing)
                            {
                                currentPage = 0
                            }
                            coroutineScope.launch {
                                pagerState.scrollToPage(currentPage)
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

                        if(imageLinks.isNotEmpty())
                        {
                            var aspectRatio by remember { mutableStateOf(1f) }
                            var isImageLoaded by remember { mutableStateOf(false) }

                            val imageLink = imageLinks[page]
                            if (imageLink.isNotEmpty())
                            {
                                LaunchedEffect(imageLink) {
                                    val request = ImageRequest.Builder(context)
                                        .data(imageLink)
                                        .addHeader("Referer", "https://mangakakalot.com")
                                        .allowHardware(false)
                                        .build()

                                    val result = (context.imageLoader.execute(request) as? SuccessResult)?.drawable

                                    result?.let { drawable ->
                                        aspectRatio = drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight.toFloat()
                                        isImageLoaded = true
                                    }
                                }

                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isImageLoaded)
                                    {
                                        Image(
                                            painter = rememberAsyncImagePainter(
                                                model = ImageRequest.Builder(context)
                                                    .data(imageLink)
                                                    .addHeader("Referer", "https://mangakakalot.com") // Set Referer header
                                                    .build()
                                            ),
                                            contentDescription = null,
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(aspectRatio)
                                                .zoomable(
                                                    zoomState = rememberZoomState(),
                                                    scrollGesturePropagation = ScrollGesturePropagation.ContentEdge,
                                                    onTap = {
                                                        isTopBarVisible = !isTopBarVisible
                                                    }
                                                )
                                        )
                                    }
                                    else
                                    {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(aspectRatio)
                                                .background(Color.Black),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(64.dp),
                                                color = MaterialTheme.colorScheme.secondary,
                                                trackColor = MaterialTheme.colorScheme.surfaceVariant
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
}
