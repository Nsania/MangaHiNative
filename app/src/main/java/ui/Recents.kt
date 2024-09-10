package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import data.dao.ChaptersReadInformationDao
import data.tables.ChaptersReadInformation
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import data.viewmodels.RecentsViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Recents(navController: NavController, chaptersReadInformationDao: ChaptersReadInformationDao, recentsViewModel: RecentsViewModel)
{
    val coroutineScope = rememberCoroutineScope()
    val recentsList by recentsViewModel.recentsList.collectAsState()


    LaunchedEffect(Unit)
    {
        coroutineScope.launch {
            chaptersReadInformationDao.getRecents().collect { list ->
                recentsViewModel.updateRecentsList(list)
            }
        }
    }

    val systemUi = rememberSystemUiController()
    LaunchedEffect(Unit)
    {
        systemUi.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = false,
        )
    }

    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Recents", color = Color.White)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF160e1a)
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF160e1a))
        ) {
            LazyColumn(
                modifier = Modifier.padding(innerPadding)
            ) {
                items(recentsList) { recent ->
                    Row(
                        modifier = Modifier
                            .height(120.dp)
                            .fillMaxWidth()
                            .background(color = Color.Transparent)
                            .padding(start = 20.dp, end = 20.dp)
                            .clickable {
                                navController.navigate(
                                    Screen.ReaderScreen.withArgs(
                                        URLEncoder.encode(
                                            recent.chapterLink,
                                            StandardCharsets.UTF_8.toString()
                                        ),
                                        recent.mangaId.toString(),
                                        recent.chapterTitle,
                                        URLEncoder.encode(
                                            recent.mangaLink,
                                            StandardCharsets.UTF_8.toString()
                                        ),
                                    )
                                )
                            }
                    ) {
                        AsyncImage(
                            model = recent.mangaImageCover,
                            contentDescription = "Image",
                            modifier = Modifier
                                .width(80.dp)
                                .height(110.dp)
                                .align(Alignment.CenterVertically),
                            contentScale = ContentScale.Crop,
                        )
                        Spacer(modifier = Modifier.width(8.dp)) // Add space between the image and text

                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .background(color = Color.Transparent)
                        ) {
                            Text(text = recent.mangaTitle, fontSize = 20.sp, color = Color.White)
                            Text(text = "Chapter: ${recent.chapter} | Pages left: ${recent.pagesLeft}", fontSize = 16.sp, color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                }
            }
        }

    }
}