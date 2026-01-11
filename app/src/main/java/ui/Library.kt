package ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mangahinative.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import data.dao.LibraryDao
import data.dao.LibraryInformationDao
import data.tables.Library
import data.viewmodels.LibraryViewModel
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun Library(
    libraryDao: LibraryDao,
    libraryInformationDao: LibraryInformationDao,
    navController: NavController,
    libraryViewModel: LibraryViewModel,
    paddingValues: PaddingValues,
)
{
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val mangasInLibraryList by libraryViewModel.mangasInLibraryList.collectAsState()
    val viewModeSelected by libraryViewModel.viewMode.collectAsState()

    var toggleViewSettings by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val viewOptions = listOf("Grid", "List")
    var check by remember { mutableStateOf(true) }

    val systemUi = rememberSystemUiController()
    LaunchedEffect(Unit)
    {
        systemUi.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = false,
        )
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            libraryInformationDao.getLibraryInformation().collect { mangas ->
                check = mangas.isNotEmpty()
                libraryViewModel.updateMangasInLibraryList(mangas)
            }
        }
    }
    Scaffold (
        modifier = Modifier.padding(paddingValues),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Library", color = Color.White)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF160e1a)
                ),
                actions = {
                    IconButton(
                        onClick = {
                            toggleViewSettings = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = null,
                            tint = Color.White,
                        )
                    }
                }
            )
        }
    )
    { innerPadding ->


        if(toggleViewSettings)
        {
            ModalBottomSheet(
                onDismissRequest = {
                    toggleViewSettings = false
                },
                sheetState = sheetState,
                scrimColor = Color.Transparent,
                containerColor = Color(0xFF352e38),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(10.dp)
                ) {
                    Text(text = "View", color = Color.White)
                    FlowRow {
                        viewOptions.forEachIndexed { index, mode ->
                            FilterChip(
                                selected = viewModeSelected == index,
                                onClick = {
                                    libraryViewModel.saveViewMode(index)
                                },
                                label = { Text(text = mode, color = if(viewModeSelected == index) Color.Black else Color.White) },
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
                .background(Color(0xFF160e1a))
        ){

            if(!check)
            {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.placeholderkuromiskull),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .alpha(0.7f),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            text = "Your library is empty",
                            color = Color(0xFF6e6775),
                            fontSize = 20.sp
                        )
                    }

                }
            }

            when(viewModeSelected) {
                0 -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 128.dp),
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(horizontal = 15.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp) )
                    {
                        items(mangasInLibraryList) { manga ->
                            Column(modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(180.dp)
                                        .height(250.dp)
                                        .clip(RoundedCornerShape(1))
                                        .clickable {
                                            coroutineScope.launch {
                                                libraryDao.addToLibrary(
                                                    Library(
                                                        manga.libraryId,
                                                        manga.mangaId,
                                                        System.currentTimeMillis()
                                                    )
                                                )
                                            }
                                            navController.navigate(
                                                Screen.ChaptersScreen.withArgs(
                                                    URLEncoder.encode(
                                                        manga.mangaLink,
                                                        StandardCharsets.UTF_8.toString()
                                                    )
                                                )
                                            )
                                        }
                                ){
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFF161317))
                                    ) {

                                    }

                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(manga.mangaImageCover)
                                            .addHeader("Referer", "https://mangakakalot.com/")
                                            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                                            .build(),
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(5)),
                                        contentDescription = "Image",
                                        contentScale = ContentScale.Crop,
                                    )
                                }

                                Text(
                                    text = manga.mangaTitle.take(30),
                                    maxLines = 1,
                                    modifier = Modifier.padding(8.dp),
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    overflow = TextOverflow.Ellipsis
                                )

                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }

                1 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 15.dp)
                    ) {
                        items(mangasInLibraryList) { manga ->
                            Row(
                                modifier = Modifier.height(100.dp).fillMaxWidth()
                                    .clickable {
                                        coroutineScope.launch {
                                            libraryDao.addToLibrary(
                                                Library(
                                                    manga.libraryId,
                                                    manga.mangaId,
                                                    System.currentTimeMillis()
                                                )
                                            )
                                        }
                                        navController.navigate(
                                            Screen.ChaptersScreen.withArgs(
                                                URLEncoder.encode(
                                                    manga.mangaLink,
                                                    StandardCharsets.UTF_8.toString()
                                                )
                                            )
                                        )
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(manga.mangaImageCover)
                                        .addHeader("Referer", "https://mangakakalot.com/")
                                        .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                                        .build(),
                                    modifier = Modifier.size(90.dp).clip(RoundedCornerShape(5)),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                )

                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = manga.mangaTitle,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                )
                            }
                        }
                    }
                }

            }


        }
    }
}