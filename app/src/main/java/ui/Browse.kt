package ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import data.dao.MangasDao
import data.tables.Mangas
import kotlinx.coroutines.launch
import scraper.Result
import scraper.getResults
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun Browse(navController: NavController, mangasDao: MangasDao) {
    var value: String by remember { mutableStateOf("") }
    var results by remember { mutableStateOf(emptyList<Result>()) }
    var errorMessage: String by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Column {
        TextField(
            value = value,
            onValueChange = { value = it },
            label = { Text("Search Manga") },
            maxLines = 1,
            modifier = Modifier.fillMaxWidth().padding(top = 60.dp)
        )

        Button(onClick = {
            coroutineScope.launch {
                try {
                    results = getResults(value)
                    errorMessage = "" // Clear error message on successful result
                } catch (e: Exception) {
                    Log.e("SimpleForm", "Error fetching results", e)
                    errorMessage = "Failed to fetch results: ${e.message}"
                }
            }
        }) {
            Text("Search")
        }


        if (errorMessage.isNotEmpty())
        {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }
        else
        {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp), // Set the number of columns. You can change this to GridCells.Adaptive(minSize = 128.dp) if you want adaptive columns.
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(results) { result ->
                    Column(modifier = Modifier.fillMaxHeight().fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        AsyncImage(
                            model = result.imageCover,
                            contentDescription = "Image",
                            modifier = Modifier.width(120.dp).height(180.dp).clickable{
                                coroutineScope.launch {
                                    val check = mangasDao.getManga(mangaLink = result.mangaLink)
                                    if(check == null)
                                    {
                                        mangasDao.addManga(Mangas(mangaLink = result.mangaLink, mangaTitle = result.title, mangaImageCover = result.imageCover, mangaDescription = ""))
                                    }
                                    navController.navigate(Screen.ChaptersScreen.withArgs(URLEncoder.encode(result.mangaLink, StandardCharsets.UTF_8.toString())))
                                }
                            },
                            contentScale = ContentScale.Crop,
                        )
                        Text(
                            text = result.title.take(20),
                            modifier = Modifier.padding(8.dp),
                            fontSize = 10.sp,
                        )

                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}