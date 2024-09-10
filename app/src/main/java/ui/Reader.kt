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
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.AssistChip
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
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
import data.viewmodels.ReaderViewModel
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
) {
    val totalPages by viewModel.totalPages.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val imagePaths by viewModel.imagePaths.collectAsState()
    val previousChapterLink by viewModel.previousChapterLink.collectAsState()
    val chapter by viewModel.chapter.collectAsState()
    val imageFileNames by viewModel.imageFileNames.collectAsState()

    var currentPage by remember { mutableIntStateOf(0) }
    var isTopBarVisible by remember { mutableStateOf(false) }
    var isReaderSettingsVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val uniqueFolderName = UUID.nameUUIDFromBytes(chapterLink.toByteArray()).toString()
    val uniqueFolder = File(context.cacheDir, uniqueFolderName)

    val sheetState = rememberModalBottomSheetState()

    val systemUiController = rememberSystemUiController()

    val readerModeOptions  = listOf("Long Strip", "Paged(left to right)", "Paged(vertical)")
    //var readerModeSelected by remember { mutableStateOf(readerModeOptions[0]) }
    val readerModeSelected by viewModel.readerMode.collectAsState()

    val pagerState = rememberPagerState(pageCount = { totalPages })

    LaunchedEffect(isTopBarVisible) {
        /*val color = if (isTopBarVisible) Color.Transparent else Color.Transparent*/
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = false,
        )
    }

    //DON'T TOUCH
    if (!uniqueFolder.exists()) {
        uniqueFolder.mkdirs()
    }

    LaunchedEffect(chapterLink) {
        coroutineScope.launch(Dispatchers.IO) {
            val info = chaptersReadInformationDao.getMangaIdAndPage(chapterLink)
            val chapterNumber = getChapterNumber(chapterLink)
            withContext(Dispatchers.Main) {
                if (info != null) {
                    currentPage = info.page
                    Log.d("Reader", "Loaded mangaId: $mangaId, currentPage: $currentPage")
                }
                viewModel.updateChapter(chapterNumber)
                Log.d("Reader", "Loaded chapterNumber: $chapter")
            }
        }
    }

    LaunchedEffect(chapterLink) {
        if (previousChapterLink != chapterLink) {
            withContext(Dispatchers.IO) {
                clearCache(context, uniqueFolderName)
            }
            viewModel.updatePreviousChapterLink(chapterLink)
        }

        coroutineScope.launch(Dispatchers.IO) {
            val pages = getPageCount(chapterLink)
            withContext(Dispatchers.Main) {
                viewModel.updateTotalPages(pages)
                viewModel.updateImagePaths(List(pages) { "" })
            }

            val existingFiles = uniqueFolder.listFiles()?.sortedBy { it.name }?.map { it.absolutePath } ?: emptyList()
            viewModel.updateImageFileNames(existingFiles)

            if (existingFiles.size == totalPages) {
                withContext(Dispatchers.Main) {
                    viewModel.updateImagePaths(existingFiles)
                }
            } else {
                viewModel.updateChapters(fetchChapterPageUrls(chapterLink))
            }
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
                listState.scrollToItem(currentPage)
                pagerState.scrollToPage(currentPage)
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

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val existingChapterRead = chaptersReadDao.getChapterRead(mangaId, chapter)
                if (existingChapterRead != null) {
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
                            chapterTitle.lowercase()
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
                    modifier = Modifier.fillMaxSize().padding(10.dp)
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
                        if(totalPages > 0)
                        {
                            coroutineScope.launch {
                                Log.d("Reader", "Current Page: $currentPage")
                                pagerState.scrollToPage(currentPage)
                            }
                        }

                    }
                    HorizontalPager(
                        modifier = Modifier.fillMaxSize(),
                        state = pagerState,
                    ) { page ->
                        var aspectRatio by remember { mutableStateOf(1f) }
                        var isImageLoaded by remember { mutableStateOf(false) }

                        val imagePath = imagePaths[page]

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

                2 -> {
                    LaunchedEffect(totalPages) {
                        if(totalPages > 0)
                        {
                            coroutineScope.launch {
                                Log.d("Reader", "Current Page: $currentPage")
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
                        var aspectRatio by remember { mutableStateOf(1f) }
                        var isImageLoaded by remember { mutableStateOf(false) }

                        val imagePath = imagePaths[page]

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



fun clearCache(context: Context, currentFolderName: String) {
    val cacheDir = context.cacheDir
    cacheDir.listFiles()?.forEach { file ->
        if (file.isDirectory && file.name != currentFolderName) {
            file.deleteRecursively()
        }
    }
}
