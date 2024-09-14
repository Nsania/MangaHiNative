package ui

import android.content.Context
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.*
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
import scraper.downloadImage
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import coil.compose.AsyncImage


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
    val totalPages by viewModel.totalPages.collectAsState()
    val currentChapterLink by viewModel.currentChapterLink.collectAsState()
    val currentChapterTitle by viewModel.currentChapterTitle.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val imagePaths by viewModel.imagePaths.collectAsState()
    val previousChapterLink by viewModel.previousChapterLink.collectAsState()
    val chapter by viewModel.chapter.collectAsState()
    val previousChapter by viewModel.previousChapter.collectAsState()
    val nextChapter by viewModel.nextChapter.collectAsState()
    val previousChapterTotalPages by viewModel.previousChapterTotalPages.collectAsState()
    val nextChapterTotalPages by viewModel.nextChapterTotalPages.collectAsState()
    val imageFileNames by viewModel.imageFileNames.collectAsState()

    var currentPage by remember { mutableIntStateOf(1) }
    var fromExisting by remember { mutableStateOf(false) }
    var endOrStart by remember { mutableStateOf(false) }
    var isTopBarVisible by remember { mutableStateOf(false) }
    var isReaderSettingsVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    //val uniqueFolderName = UUID.nameUUIDFromBytes(currentChapterLink.toByteArray()).toString()
    //val uniqueFolderName = "${chapterLink}"
    val uniqueFolder = File(context.cacheDir, chapterTitle)

    val sheetState = rememberModalBottomSheetState()

    val systemUiController = rememberSystemUiController()

    val readerModeOptions  = listOf("Long Strip", "Paged(left to right)", "Paged(vertical)")
    //var readerModeSelected by remember { mutableStateOf(readerModeOptions[0]) }
    val readerModeSelected by viewModel.readerMode.collectAsState()

    val pagerState = rememberPagerState(pageCount = { totalPages + 2 }, initialPage = 1)



    LaunchedEffect(Unit) {
        viewModel.updateChapterLink(chapterLink)
        viewModel.updateCurrentChapterTitle(chapterTitle)
    }

    //DON'T TOUCH
    LaunchedEffect(currentChapterLink) {
        if(currentChapterLink != "" && currentChapterLink != null)
        {
            Log.d("Reader", "Chapter link: $currentChapterLink")
            coroutineScope.launch(Dispatchers.IO) {
                if (!uniqueFolder.exists())
                {
                    uniqueFolder.mkdirs()
                }
            }
        }
    }


    LaunchedEffect(isTopBarVisible) {
        /*val color = if (isTopBarVisible) Color.Transparent else Color.Transparent*/
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = false,
        )
    }


    LaunchedEffect(currentChapterLink) {
        coroutineScope.launch(Dispatchers.IO) {
            val info = chaptersReadInformationDao.getMangaIdAndPage(currentChapterLink)
            val chapterNumber = getChapterNumber(currentChapterLink)
            withContext(Dispatchers.Main) {
                if (info != null) {
                    currentPage = info.page
                    Log.d("Reader", "Loaded mangaId: $mangaId, currentPage: $currentPage")
                }
                viewModel.updateChapter(chapterNumber)
            }
            Log.d("Reader", "Loaded chapterNumber: $chapter")
        }
    }


    LaunchedEffect(currentChapterLink) {
        if (previousChapterLink != currentChapterLink) {
            withContext(Dispatchers.IO) {
                clearCache(context, chapterTitle)
            }
            viewModel.updatePreviousChapterLink(currentChapterLink)

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
    }

    /*LaunchedEffect(currentChapterLink) {
        if (currentChapterLink.isEmpty()) {
            // Handle invalid or empty URL
            Log.e("Reader", "Error: currentChapterLink is empty or null")
            //viewModel.updateChapterLink(chapterLink)
            return@LaunchedEffect  // Exit early if the link is invalid
        }

        // Proceed with the rest of the logic...
        if (previousChapterLink != currentChapterLink) {
            withContext(Dispatchers.IO) {
                clearCache(context, chapterTitle)
            }
            //viewModel.updatePreviousChapterLink(currentChapterLink)
        }

        withContext(Dispatchers.IO) {
            val pages = getPageCount(currentChapterLink)

            withContext(Dispatchers.Main) {
                viewModel.updateTotalPages(pages)
                viewModel.updateImagePaths(List(pages) { "" })
            }

            val existingFiles = uniqueFolder.listFiles()?.sortedBy { it.name }?.map { it.absolutePath } ?: emptyList()
            withContext(Dispatchers.Main) {
                viewModel.updateImageFileNames(existingFiles)
            }

            if (existingFiles.size == pages)
            {
                withContext(Dispatchers.Main) {
                    viewModel.updateImagePaths(existingFiles)
                }
            }
            else
            {
                val chaptersTemp = fetchChapterPageUrls(currentChapterLink)
                withContext(Dispatchers.Main) {
                    viewModel.updateChapters(chaptersTemp)
                }
            }
        }
    }*/




    LaunchedEffect(currentChapterLink) {
        if(chapter != 0.0)
        {
            coroutineScope.launch(Dispatchers.IO){
                val nextChapterTemp = mangaChaptersDao.getNextChapter(mangaId, chapter)
                val previousChapterTemp = mangaChaptersDao.getPreviousChapter(mangaId, chapter)

                if(previousChapterTemp != null)
                {
                    viewModel.updatePreviousChapter(previousChapterTemp)
                }

                if(nextChapterTemp != null)
                {
                    viewModel.updateNextChapter(nextChapterTemp)
                }

            }
        }
    }

    LaunchedEffect(previousChapter, nextChapter) {
        Log.d("Reader","Previous Chapter: ${previousChapter?.chapter}\nNext Chapter: ${nextChapter?.chapter}")
        if(previousChapter != null)
        {
            viewModel.updatePreviousChapterTotalPages(getPageCount(previousChapter!!.chapterLink))
        }

        if(nextChapter != null)
        {
            viewModel.updateNextChapterTotalPages(getPageCount(nextChapter!!.chapterLink))
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


    /*LaunchedEffect(chapters) {
        if (chapters.isNotEmpty() && imageFileNames.size != totalPages) {
            // Launch all download tasks concurrently and wait for all of them to complete
            val downloadJobs = chapters.mapIndexed { index, imageURL ->
                async(Dispatchers.IO) {
                    if (viewModel.getImagePathAt(index).isEmpty()) {
                        // Download the image in IO thread
                        val imagePath = downloadImage(context, imageURL, uniqueFolder, index)
                        // Update the image path in the Main thread
                        withContext(Dispatchers.Main) {
                            viewModel.updateImagePath(index, imagePath)
                        }
                    }
                }
            }
            // Wait for all downloads to complete
            downloadJobs.awaitAll()
        }
    }*/


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
                listState.scrollToItem(currentPage)
                pagerState.scrollToPage(currentPage)
            }
        }
    }

    LaunchedEffect(currentPage) {
        if(totalPages > 0)
        {
            if(currentPage == totalPages + 1 && nextChapter != null)
            {
                fromExisting = true
                endOrStart = true
                Log.d("Reader", "Next Chapter Link: ${nextChapter!!.chapterLink}")

                withContext(Dispatchers.Main) {
                    viewModel.updateChapterLink(nextChapter!!.chapterLink)
                    viewModel.updateCurrentChapterTitle(nextChapter!!.chapterTitle)
                }

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
                                timeStamp = System.currentTimeMillis(),
                            )
                        )
                        Log.d("Chapters", "chapter added: $chapterNumber")
                    } else {
                        Log.d("Chapters", "chapter already exists: $chapterNumber")
                    }
                }
            }

            if(currentPage == 0 && previousChapter != null)
            {
                Log.d("Reader", "START")

                fromExisting = true
                endOrStart = false
                Log.d("Reader", "Previous Chapter Link: ${previousChapter!!.chapterLink}")
                viewModel.updateChapterLink(previousChapter!!.chapterLink)
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
                    } else {
                        Log.d("Chapters", "chapter already exists: $chapterNumber")
                    }
                }


            }
        }
    }

    LaunchedEffect(listState.firstVisibleItemIndex, pagerState.currentPage)
    {
        currentPage = if(readerModeSelected == 0) {
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
                coroutineScope.launch(Dispatchers.IO) {
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
                        coroutineScope.launch {
                            Log.d("Reader", "Current Page: $currentPage")
                            listState.scrollToItem(currentPage)
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                        , state = listState) {
                        itemsIndexed(imagePaths) { index, imagePath ->
                            var aspectRatio by remember { mutableStateOf(1f) }
                            var isImageLoaded by remember { mutableStateOf(false) }

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
                                            .background(Color.Transparent) // Placeholder color
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
                    }
                }

                1 -> {
                    LaunchedEffect(totalPages) {
                        if(totalPages > 0) {
                            if (currentPage == 0) {
                                currentPage = 1
                                coroutineScope.launch {
                                    Log.d("Reader", "Current Page: $currentPage")
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
                                        .background(Color.Blue),
                                    contentAlignment = Alignment.Center
                                ){
                                    Text("Start")
                                }
                            }
                            totalPages + 1 -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Blue),
                                    contentAlignment = Alignment.Center
                                ){
                                    Text("End")
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
                        if(totalPages > 0) {
                            if (currentPage == 0) {
                                currentPage = 1
                                coroutineScope.launch {
                                    Log.d("Reader", "Current Page: $currentPage")
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
                                        .background(Color.Blue),
                                    contentAlignment = Alignment.Center
                                ){
                                    Text("Start")
                                }
                            }
                            totalPages + 1 -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Blue),
                                    contentAlignment = Alignment.Center
                                ){
                                    Text("End")
                                }
                            }
                            else -> {
                                var aspectRatio by remember { mutableFloatStateOf(1f) }
                                var isImageLoaded by remember { mutableStateOf(false) }

                                val imagePath = imagePaths[page - 1]

                                if (imagePath.isNotEmpty())
                                {
                                    LaunchedEffect(imagePath) {
                                        Log.d("Reader", "Image Path: $imagePath")
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
                                        /*Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(aspectRatio) // Default aspect ratio while loading
                                                .background(Color.Transparent) // Placeholder color
                                        )*/

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
                                /*else {
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
                                }*/
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
