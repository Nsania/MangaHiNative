package ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.imageLoader
import coil.request.ImageRequest
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
    val previousChapter by viewModel.previousChapter.collectAsState()
    val nextChapter by viewModel.nextChapter.collectAsState()
    val previousChapterTotalPages by viewModel.previousChapterTotalPages.collectAsState()
    val nextChapterTotalPages by viewModel.nextChapterTotalPages.collectAsState()
    val readerModeSelected by viewModel.readerMode.collectAsState()
    val imageLinks by viewModel.imageLinks.collectAsState()
    val chapter by viewModel.chapter.collectAsState()

    var currentPage by remember { mutableIntStateOf(-1) }
    var isDraggingSlider by remember { mutableStateOf(false) }
    var isTopBarVisible by remember { mutableStateOf(false) }
    var isReaderSettingsVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val systemUiController = rememberSystemUiController()
    val readerModeOptions = listOf("Long Strip", "Paged(left to right)", "Paged(vertical)")

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    DisposableEffect(Unit) {
        onDispose {
            if (currentPage >= 0) {
                viewModel.savePageProgress(mangaId, chapter, currentPage)
            }
        }
    }

    val onPreviousClick: () -> Unit = {
        if (previousChapter != null) {
            currentPage = -1
            viewModel.updateImageLinks(emptyList())
            viewModel.updateCurrentChapterLink(previousChapter!!.chapterLink)
            viewModel.updateCurrentChapterTitle(previousChapter!!.chapterTitle)

            coroutineScope.launch {
                val chapterNumber = getChapterNumber(previousChapter!!.chapterLink)
                val existingChapterRead = chaptersReadDao.getChapterRead(mangaId, chapterNumber)
                if (existingChapterRead == null) {
                    chaptersReadDao.addOrUpdateChaptersRead(
                        ChaptersRead(
                            mangaId = mangaId,
                            chapterLink = previousChapter!!.chapterLink,
                            chapterTitle = previousChapter!!.chapterTitle,
                            chapter = chapterNumber,
                            page = 0, // Explicitly start at page 0
                            totalPages = previousChapterTotalPages,
                            timeStamp = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    val onNextClick: () -> Unit = {
        if (nextChapter != null) {
            currentPage = -1
            viewModel.updateImageLinks(emptyList())
            viewModel.updateCurrentChapterLink(nextChapter!!.chapterLink)
            viewModel.updateCurrentChapterTitle(nextChapter!!.chapterTitle)

            coroutineScope.launch {
                val chapterNumber = getChapterNumber(nextChapter!!.chapterLink)
                val existingChapterRead = chaptersReadDao.getChapterRead(mangaId, chapterNumber)
                if (existingChapterRead == null) {
                    // FIX: Using Named Parameters to avoid Type Mismatch
                    chaptersReadDao.addOrUpdateChaptersRead(
                        ChaptersRead(
                            mangaId = mangaId,
                            chapterLink = nextChapter!!.chapterLink,
                            chapterTitle = nextChapter!!.chapterTitle,
                            chapter = chapterNumber,
                            page = 0,
                            totalPages = nextChapterTotalPages,
                            timeStamp = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Main) {
            viewModel.updateCurrentChapterLink(chapterLink)
            viewModel.updateCurrentChapterTitle(chapterTitle)
        }
    }

    LaunchedEffect(isTopBarVisible) {
        systemUiController.setSystemBarsColor(color = Color.Transparent, darkIcons = false)
    }

    LaunchedEffect(currentChapterLink) {
        if (currentChapterLink.isNotEmpty()) {
            currentPage = -1

            coroutineScope.launch(Dispatchers.IO) {
                val info = chaptersReadInformationDao.getMangaIdAndPage(currentChapterLink)
                val chapterNumber = getChapterNumber(currentChapterLink)

                withContext(Dispatchers.Main) {
                    currentPage = info?.page ?: 0
                    viewModel.updateChapter(chapterNumber)
                }
            }
            coroutineScope.launch(Dispatchers.IO) {
                val pages = getPageCount(context, currentChapterLink)
                withContext(Dispatchers.Main) { viewModel.updateTotalPages(pages) }
                viewModel.updateImageLinks(fetchChapterPageUrls(context, currentChapterLink))
            }
        }
    }

    LaunchedEffect(imageLinks) {
        if (imageLinks.isNotEmpty()) {
            coroutineScope.launch(Dispatchers.IO) {
                imageLinks.forEach { link ->
                    val request = ImageRequest.Builder(context).data(link).addHeader("Referer", "https://mangakakalot.com/").addHeader("User-Agent", "Mozilla/5.0").build()
                    context.imageLoader.enqueue(request)
                }
            }
        }
    }

    LaunchedEffect(chapter) {
        if(chapter != 0.0) {
            viewModel.updatePreviousChapter(mangaChaptersDao.getPreviousChapter(mangaId, chapter))
            viewModel.updateNextChapter(mangaChaptersDao.getNextChapter(mangaId, chapter))
        }
    }

    LaunchedEffect(previousChapter, nextChapter) {
        viewModel.updatePreviousChapterTotalPages(if (previousChapter != null) getPageCount(context, previousChapter!!.chapterLink) else 0)
        viewModel.updateNextChapterTotalPages(if (nextChapter != null) getPageCount(context, nextChapter!!.chapterLink) else 0)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AnimatedVisibility(visible = isTopBarVisible, enter = fadeIn(), exit = fadeOut()) {
                TopAppBar(
                    navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } },
                    title = { Text(currentChapterTitle, fontSize = 20.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    actions = { IconButton(onClick = { isReaderSettingsVisible = !isReaderSettingsVisible }) { Icon(Icons.Filled.Settings, null, tint = Color.White) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF352e38))
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(visible = isTopBarVisible, enter = fadeIn(), exit = fadeOut()) {
                Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF352e38))) {
                    if (totalPages > 0) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "${(currentPage + 1).coerceAtMost(totalPages)}", color = Color.White, fontSize = 14.sp)

                            Slider(
                                value = currentPage.coerceAtMost(totalPages - 1).coerceAtLeast(0).toFloat(),
                                onValueChange = {
                                    isDraggingSlider = true
                                    currentPage = it.toInt()
                                },
                                onValueChangeFinished = { isDraggingSlider = false },
                                valueRange = 0f..(totalPages - 1).coerceAtLeast(0).toFloat(),
                                colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White, inactiveTrackColor = Color.Gray),
                                modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                            )
                            Text(text = "$totalPages", color = Color.White, fontSize = 14.sp)
                        }
                    }

                    BottomAppBar(containerColor = Color.Transparent) {
                        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                            IconButton(onClick = onPreviousClick) { Icon(Icons.Default.SkipPrevious, null, tint = Color.White, modifier = Modifier.size(36.dp)) }
                            Spacer(modifier = Modifier.width(100.dp))
                            IconButton(onClick = onNextClick) { Icon(Icons.Default.SkipNext, null, tint = Color.White, modifier = Modifier.size(36.dp)) }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->

        if (isReaderSettingsVisible) {
            ModalBottomSheet(
                onDismissRequest = { isReaderSettingsVisible = false },
                containerColor = Color(0xFF352e38)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                    Text("Reader Mode", color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        readerModeOptions.forEachIndexed { index, mode ->
                            FilterChip(
                                selected = readerModeSelected == index,
                                onClick = { viewModel.saveReaderMode(index) },
                                label = { Text(mode) }
                            )
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize().background(Color.Black).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { isTopBarVisible = !isTopBarVisible }) {

            if (currentPage != -1 && imageLinks.isNotEmpty()) {

                key(currentChapterLink) {
                    when (readerModeSelected) {
                        0 -> {
                            val listState = rememberLazyListState(initialFirstVisibleItemIndex = currentPage)

                            LaunchedEffect(currentPage) {
                                if (isDraggingSlider && !listState.isScrollInProgress) {
                                    listState.scrollToItem(currentPage)
                                }
                            }

                            LaunchedEffect(listState) {
                                snapshotFlow {
                                    val layoutInfo = listState.layoutInfo
                                    if (layoutInfo.visibleItemsInfo.isEmpty()) return@snapshotFlow null
                                    val center = layoutInfo.viewportSize.height / 2
                                    layoutInfo.visibleItemsInfo.find { (it.offset <= center) && ((it.offset + it.size) >= center) }?.index
                                }.collect { index ->
                                    if (index != null && !isDraggingSlider && currentPage != index) {
                                        currentPage = index
                                        viewModel.savePageProgress(mangaId, chapter, currentPage)
                                    }
                                }
                            }

                            LazyColumn(modifier = Modifier.fillMaxSize().zoomable(rememberZoomState(), onTap = { _ -> isTopBarVisible = !isTopBarVisible }), state = listState) {
                                itemsIndexed(imageLinks) { _, imageLink ->
                                    SubcomposeAsyncImage(
                                        model = ImageRequest.Builder(context).data(imageLink).addHeader("Referer", "https://mangakakalot.com/").addHeader("User-Agent", "Mozilla/5.0").build(),
                                        contentDescription = null, contentScale = ContentScale.FillWidth, modifier = Modifier.fillMaxWidth(),
                                        loading = { Box(modifier = Modifier.fillMaxWidth().height(screenHeight), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) } }
                                    )
                                }
                            }
                        }
                        1 -> {
                            val pagerState = rememberPagerState(initialPage = currentPage, pageCount = { imageLinks.size })

                            LaunchedEffect(currentPage) {
                                if (isDraggingSlider && pagerState.currentPage != currentPage) {
                                    pagerState.scrollToPage(currentPage)
                                }
                            }

                            LaunchedEffect(pagerState) {
                                snapshotFlow { pagerState.currentPage }.collect { index ->
                                    if (!isDraggingSlider && currentPage != index) {
                                        currentPage = index
                                        viewModel.savePageProgress(mangaId, chapter, currentPage)
                                    }
                                }
                            }

                            HorizontalPager(modifier = Modifier.fillMaxSize().align(Alignment.Center).background(Color.Black), state = pagerState) { page ->
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(context).data(imageLinks[page]).addHeader("Referer", "https://mangakakalot.com/").addHeader("User-Agent", "Mozilla/5.0").build(),
                                    contentDescription = null, contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize().zoomable(rememberZoomState(), scrollGesturePropagation = ScrollGesturePropagation.ContentEdge, onTap = { _ -> isTopBarVisible = !isTopBarVisible }),
                                    loading = { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) } }
                                )
                            }
                        }
                        2 -> {
                            val pagerState = rememberPagerState(initialPage = currentPage, pageCount = { imageLinks.size })

                            LaunchedEffect(currentPage) {
                                if (isDraggingSlider && pagerState.currentPage != currentPage) {
                                    pagerState.scrollToPage(currentPage)
                                }
                            }

                            LaunchedEffect(pagerState) {
                                snapshotFlow { pagerState.currentPage }.collect { index ->
                                    if (!isDraggingSlider && currentPage != index) {
                                        currentPage = index
                                        viewModel.savePageProgress(mangaId, chapter, currentPage)
                                    }
                                }
                            }

                            VerticalPager(modifier = Modifier.fillMaxSize().align(Alignment.Center).background(Color.Black), state = pagerState) { page ->
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(context).data(imageLinks[page]).addHeader("Referer", "https://mangakakalot.com/").addHeader("User-Agent", "Mozilla/5.0").build(),
                                    contentDescription = null, contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize().zoomable(rememberZoomState(), scrollGesturePropagation = ScrollGesturePropagation.ContentEdge, onTap = { _ -> isTopBarVisible = !isTopBarVisible }),
                                    loading = { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) } }
                                )
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            AnimatedVisibility(visible = !isTopBarVisible && totalPages > 0, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp)) {
                Box(modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp)).padding(horizontal = 14.dp, vertical = 6.dp)) {
                    Text(text = "${(currentPage + 1).coerceAtMost(totalPages)} / $totalPages", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}