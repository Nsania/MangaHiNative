package ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mangahinative.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import data.dao.ChaptersReadInformationDao
import data.viewmodels.RecentsViewModel
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Recents(navController: NavController, chaptersReadInformationDao: ChaptersReadInformationDao, recentsViewModel: RecentsViewModel, paddingValues: PaddingValues)
{
    val coroutineScope = rememberCoroutineScope()
    val recentsList by recentsViewModel.recentsList.collectAsState()
    var settingsExpanded by remember { mutableStateOf(false) }
    var clearHistoryExpanded by remember { mutableStateOf(false) }
    var check by remember { mutableStateOf(true) }


    LaunchedEffect(Unit)
    {
        coroutineScope.launch {
            chaptersReadInformationDao.getRecents().collect { list ->
                check = list.isNotEmpty()
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
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Recents", color = Color.White)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF160e1a)
                ),
                actions = {
                    IconButton(
                        onClick = {
                            settingsExpanded = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = settingsExpanded,
                        onDismissRequest = { settingsExpanded = false },
                        modifier = Modifier.background(Color(0xFF352e38))
                    ) {
                       DropdownMenuItem(
                           text = {
                           Text(
                               text = "Clear History",
                               color = Color.White
                           )
                       },
                           onClick = {
                               clearHistoryExpanded = true
                               settingsExpanded = false
                           }
                       )
                    }

                    if(clearHistoryExpanded)
                    {
                        AlertDialog(
                            onDismissRequest = {
                                clearHistoryExpanded = false
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            chaptersReadInformationDao.clearData()
                                        }
                                        clearHistoryExpanded = false
                                    }
                                ) {
                                    Text(
                                        text = "Confirm",
                                        color = Color.White
                                    )
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        clearHistoryExpanded = false
                                    }
                                ) {
                                    Text(
                                        text = "Cancel",
                                        color = Color.White
                                    )
                                }
                            },
                            title = {
                                Text(
                                    text = "Clear History?",
                                    color = Color.White
                                )
                            },
                            text = {
                                Text(
                                    text = "All history will be lost",
                                    color = Color.White
                                )
                            },
                            containerColor = Color(0xFF352e38)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF160e1a))
        ) {
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
                            painter = painterResource(id = R.drawable.placeholderbaku),
                            contentDescription = null,
                            modifier = Modifier.size(100.dp).alpha(0.7f),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            text = "No recents",
                            color = Color(0xFF6e6775),
                            fontSize = 20.sp
                        )
                    }

                }
            }
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
                                .align(Alignment.CenterVertically)
                                .clickable {
                                    navController.navigate(
                                        Screen.ChaptersScreen.withArgs(
                                            URLEncoder.encode(
                                                recent.mangaLink,
                                                StandardCharsets.UTF_8.toString()
                                            )
                                        )
                                    )
                                },
                            contentScale = ContentScale.Crop,
                        )
                        Spacer(modifier = Modifier.width(8.dp))

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