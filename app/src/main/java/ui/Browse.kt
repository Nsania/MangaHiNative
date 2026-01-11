package ui

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mangahinative.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import data.dao.MangasDao
import data.tables.Mangas
import data.viewmodels.BrowseViewModel
import kotlinx.coroutines.launch
import scraper.getResults
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Browse(
    navController: NavController,
    mangasDao: MangasDao,
    browseViewModel: BrowseViewModel,
    paddingValues: PaddingValues // Includes height of Bottom Navigation Bar
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val searchValue by browseViewModel.searchValue.collectAsState()
    val searchResults by browseViewModel.searchResults.collectAsState()
    val errorMessage by browseViewModel.errorMessage.collectAsState()
    val topBarTitle by browseViewModel.topBarTitle.collectAsState()

    var isSearching by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val systemUi = rememberSystemUiController()

    LaunchedEffect(Unit) {
        systemUi.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = false,
        )
    }

    LaunchedEffect(isSearching) {
        if (isSearching) {
            focusRequester.requestFocus()
        }
    }

    val outsideTapModifier = Modifier.pointerInput(Unit) {
        detectTapGestures(onTap = {
            focusManager.clearFocus()
            isSearching = false
        })
    }

    Scaffold(
        modifier = outsideTapModifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                modifier = Modifier.animateContentSize(),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF160e1a)
                ),
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            AnimatedVisibility(
                                visible = isSearching,
                                enter = slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(durationMillis = 300, easing = EaseInOutCubic)
                                ) + fadeIn(animationSpec = tween(durationMillis = 200)),
                                exit = slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(durationMillis = 300, easing = EaseInOutCubic)
                                ) + fadeOut(animationSpec = tween(durationMillis = 200))
                            ) {
                                IconButton(
                                    modifier = Modifier.size(40.dp),
                                    onClick = { isSearching = false }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Default.ArrowBackIos,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = isSearching,
                                enter = fadeIn(animationSpec = tween(durationMillis = 500)) + expandHorizontally(
                                    animationSpec = tween(durationMillis = 300)
                                ),
                                exit = fadeOut(animationSpec = tween(durationMillis = 500)) + shrinkHorizontally(
                                    animationSpec = tween(durationMillis = 300)
                                )
                            ) {
                                TextField(
                                    modifier = Modifier
                                        .focusRequester(focusRequester)
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .padding(0.dp),
                                    value = searchValue,
                                    onValueChange = { newValue ->
                                        browseViewModel.updateSearchValue(
                                            TextFieldValue(
                                                text = newValue.text,
                                                selection = TextRange(newValue.text.length)
                                            )
                                        )
                                    },
                                    placeholder = {
                                        Text(
                                            text = "Search",
                                            style = TextStyle(
                                                fontSize = 18.sp,
                                                color = Color(0xFF9c9c9c),
                                                fontWeight = FontWeight.Normal
                                            )
                                        )
                                    },
                                    maxLines = 1,
                                    textStyle = TextStyle(fontSize = 18.sp, color = Color.White),
                                    shape = RoundedCornerShape(10),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFF4b434f),
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        unfocusedContainerColor = Color(0xFF4b434f)
                                    ),
                                    keyboardOptions = KeyboardOptions.Default.copy(
                                        imeAction = ImeAction.Search
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onSearch = {
                                            if (searchValue.text.isNotEmpty()) {
                                                browseViewModel.updateTopBarTitle("\"${searchValue.text}\" results")
                                                focusManager.clearFocus()
                                                isSearching = false
                                                coroutineScope.launch {
                                                    try {
                                                        browseViewModel.updateSearchResults(
                                                            getResults(context, searchValue.text)
                                                        )
                                                        browseViewModel.updateErrorMessage("")
                                                    } catch (e: Exception) {
                                                        Log.e("SimpleForm", "Error fetching results", e)
                                                        browseViewModel.updateErrorMessage("Failed to fetch search results")
                                                    }
                                                }
                                            }
                                        }
                                    ),
                                    trailingIcon = {
                                        if (searchValue.text.isNotEmpty()) {
                                            IconButton(
                                                onClick = {
                                                    browseViewModel.updateSearchValue(TextFieldValue())
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Clear,
                                                    contentDescription = null,
                                                    tint = Color.White
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                                .height(40.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AnimatedVisibility(
                                visible = !isSearching,
                                enter = slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(durationMillis = 100, easing = EaseInOutCubic)
                                ) + fadeIn(animationSpec = tween(durationMillis = 100)),
                                exit = slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = tween(durationMillis = 100, easing = EaseInOutCubic)
                                ) + fadeOut(animationSpec = tween(durationMillis = 100))
                            ) {
                                AnimatedContent(
                                    targetState = topBarTitle, label = "",
                                ) { targetTitle ->
                                    Text(
                                        text = targetTitle,
                                        color = Color.White
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = topBarTitle != "Browse" && !isSearching,
                                enter = fadeIn(animationSpec = tween(durationMillis = 100)),
                                exit = fadeOut(animationSpec = tween(durationMillis = 300))
                            ) {
                                IconButton(
                                    modifier = Modifier.size(40.dp),
                                    onClick = {
                                        browseViewModel.updateTopBarTitle("Browse")
                                        browseViewModel.updateSearchValue(TextFieldValue())
                                        browseViewModel.updateSearchResults(emptyList())
                                        isSearching = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isSearching) {
                                if (searchValue.text.isNotEmpty()) {
                                    browseViewModel.updateTopBarTitle("\"${searchValue.text}\" results")
                                    focusManager.clearFocus()
                                    isSearching = false
                                    coroutineScope.launch {
                                        try {
                                            browseViewModel.updateSearchResults(
                                                getResults(context, searchValue.text)
                                            )
                                            browseViewModel.updateErrorMessage("")
                                        } catch (e: Exception) {
                                            Log.e("SimpleForm", "Error fetching results", e)
                                            browseViewModel.updateErrorMessage("Failed to fetch search results")
                                        }
                                    }
                                }
                            } else {
                                isSearching = true
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF160e1a)),
        ) {
            if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.placeholderkuromiphone),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .alpha(0.7f),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            text = "Browse manga",
                            color = Color(0xFF6e6775),
                            fontSize = 20.sp
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (errorMessage.isNotEmpty()) {
                        Text(errorMessage, color = MaterialTheme.colorScheme.error)
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 120.dp),
                            modifier = Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(
                                top = innerPadding.calculateTopPadding() + 16.dp,
                                bottom = paddingValues.calculateBottomPadding() + 16.dp
                            )
                        ) {
                            items(searchResults) { result ->
                                Column(
                                    modifier = Modifier
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
                                                    val check = mangasDao.getManga(mangaLink = result.mangaLink)
                                                    if (check == null) {
                                                        mangasDao.addManga(
                                                            Mangas(
                                                                mangaLink = result.mangaLink,
                                                                mangaTitle = result.title,
                                                                mangaImageCover = result.imageCover,
                                                                mangaDescription = ""
                                                            )
                                                        )
                                                    }
                                                    navController.navigate(
                                                        Screen.ChaptersScreen.withArgs(
                                                            URLEncoder.encode(
                                                                result.mangaLink,
                                                                StandardCharsets.UTF_8.toString()
                                                            )
                                                        )
                                                    )
                                                }
                                            }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color(0xFF161317)),
                                        )

                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(result.imageCover)
                                                .addHeader("Referer", "https://mangakakalot.com/")
                                                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                                                .build(),
                                            contentDescription = "Image",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop,
                                        )
                                    }
                                    Text(
                                        text = result.title.take(20),
                                        modifier = Modifier.padding(8.dp),
                                        fontSize = 10.sp,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}