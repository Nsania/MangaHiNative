package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import data.dao.LibraryDao
import data.dao.LibraryInformationDao
import data.tables.LibraryInformation
import data.tables.Mangas
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import data.tables.Library
import data.viewmodels.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Library(libraryDao: LibraryDao, libraryInformationDao: LibraryInformationDao, navController: NavController, libraryViewModel: LibraryViewModel)
{
    //var mangasList by remember {mutableStateOf<List<LibraryInformation>>(emptyList())}
    val coroutineScope = rememberCoroutineScope()

    val mangasInLibraryList by libraryViewModel.mangasInLibraryList.collectAsState()

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
                //mangasList = mangas
                libraryViewModel.updateMangasInLibraryList(mangas)
            }
        }
    }
    Scaffold (
        modifier = Modifier.padding(bottom = 50.dp),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Library", color = Color.White)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF160e1a)
                )
            )
        }
    )
    { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF160e1a))
        ){
            LazyVerticalGrid( columns = GridCells.Adaptive(minSize = 128.dp), // Set the number of columns. You can change this to GridCells.Adaptive(minSize = 128.dp) if you want adaptive columns.
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
                                modifier = Modifier.fillMaxSize().background(Color(0xFF161317))
                            ) {

                            }

                            AsyncImage(
                                model = manga.mangaImageCover,
                                modifier = Modifier.fillMaxSize(),
                                contentDescription = "Image",
                                contentScale = ContentScale.Crop,
                            )
                        }

                        Text(
                            text = manga.mangaTitle.take(30),
                            modifier = Modifier.padding(8.dp),
                            fontSize = 12.sp,
                            color = Color.White
                        )

                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

    }

}