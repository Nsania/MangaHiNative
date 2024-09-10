package ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import data.dao.ChaptersReadDao
import data.dao.ChaptersReadInformationDao
import data.dao.LibraryDao
import data.dao.LibraryInformationDao
import data.dao.MangasDao
import data.viewmodels.BrowseViewModel
import data.viewmodels.ChaptersViewModel
import data.viewmodels.LibraryViewModel
import data.viewmodels.RecentsViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(chaptersReadDao: ChaptersReadDao, libraryInformationDao: LibraryInformationDao, mangasDao: MangasDao, libraryDao: LibraryDao, chaptersReadInformationDao: ChaptersReadInformationDao
,
)
{
    val navController = rememberNavController()
    val libraryViewModel: LibraryViewModel = viewModel()
    val recentsViewModel: RecentsViewModel = viewModel()
    val browseViewModel: BrowseViewModel = viewModel()

    NavHost(
        navController,
        startDestination = Screen.LibraryScreen.route,
    )
    {
        composable(
            Screen.LibraryScreen.route,
            enterTransition = {EnterTransition.None},
            exitTransition =  {ExitTransition.None},
            popEnterTransition = {EnterTransition.None},
            popExitTransition = { ExitTransition.None},
            )
        {
            BottomNavigationBar(navController) {
                Library(libraryDao ,libraryInformationDao, navController, libraryViewModel)
            }
        }
        composable(
            Screen.BrowseScreen.route,
            enterTransition = {EnterTransition.None},
            exitTransition =  {ExitTransition.None},
            popEnterTransition = {EnterTransition.None},
            popExitTransition = { ExitTransition.None},
        )
        {
            BottomNavigationBar(navController) {
                Browse(navController = navController, mangasDao, browseViewModel)
            }
        }
        composable(
            route = Screen.ChaptersScreen.route + "/{mangaLink}",
            arguments = listOf(
                navArgument("mangaLink")
                {
                    type = NavType.StringType
                    nullable = false
                }
            ),
            popExitTransition = { ExitTransition.None},

        ) { entry ->
            Chapters(mangaLink = entry.arguments?.getString("mangaLink").orEmpty(), navController, chaptersReadDao, libraryDao, mangasDao, chaptersReadInformationDao)
        }
        composable(
            route = Screen.ReaderScreen.route + "/{chapterLink}/{mangaId}/{chapterTitle}/{mangaLink}",
            arguments = listOf(
                navArgument("chapterLink")
                {
                    type = NavType.StringType
                    nullable = false
                },
                navArgument("mangaId")
                {
                    type = NavType.StringType
                    nullable = false
                }
            ),
            exitTransition =  {ExitTransition.None},
            popExitTransition = { ExitTransition.None},
        )
        { entry ->
            Reader(chapterLink = entry.arguments?.getString("chapterLink").toString(), chaptersReadInformationDao, chaptersReadDao, mangaId = entry.arguments?.getString("mangaId")
                ?.toInt() ?: 0, navController, chapterTitle = entry.arguments?.getString("chapterTitle").toString(), mangaLink = entry.arguments?.getString("mangaLink").toString())
        }

        composable(
            route = Screen.RecentsScreen.route,
            enterTransition = {EnterTransition.None},
            exitTransition =  {ExitTransition.None},
            popEnterTransition = {EnterTransition.None},
            popExitTransition = { ExitTransition.None},
        )
        {
            BottomNavigationBar(navController)
            {
                Recents(navController, chaptersReadInformationDao, recentsViewModel)
            }
        }
    }
}
