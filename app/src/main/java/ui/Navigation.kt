package ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import data.dao.ChaptersReadDao
import data.dao.ChaptersReadInformationDao
import data.dao.LibraryDao
import data.dao.LibraryInformationDao
import data.dao.MangaChaptersDao
import data.dao.MangasDao
import data.viewmodels.BrowseViewModel
import data.viewmodels.LibraryViewModel
import data.viewmodels.RecentsViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(chaptersReadDao: ChaptersReadDao, libraryInformationDao: LibraryInformationDao, mangasDao: MangasDao, libraryDao: LibraryDao, chaptersReadInformationDao: ChaptersReadInformationDao
, mangaChaptersDao: MangaChaptersDao
)
{
    val navController = rememberNavController()
    //val libraryViewModel: LibraryViewModel = viewModel()
    val recentsViewModel: RecentsViewModel = viewModel()
    val browseViewModel: BrowseViewModel = viewModel()
    val libraryViewModel: LibraryViewModel = viewModel(factory = LibraryViewModel.Factory)

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
            BottomNavigationBar(navController) { innerPadding ->
                Library(libraryDao = libraryDao , libraryInformationDao = libraryInformationDao, navController = navController, paddingValues = innerPadding, libraryViewModel = libraryViewModel)
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
            BottomNavigationBar(navController) { innerPadding ->
                Browse(navController = navController, mangasDao, browseViewModel, innerPadding)
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
            Chapters(mangaLink = entry.arguments?.getString("mangaLink").orEmpty(), navController, chaptersReadDao, libraryDao, mangasDao, chaptersReadInformationDao, mangaChaptersDao)
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
                ?.toInt() ?: 0, navController, chapterTitle = entry.arguments?.getString("chapterTitle").toString(), mangaLink = entry.arguments?.getString("mangaLink").toString(), mangaChaptersDao = mangaChaptersDao)
        }

        composable(
            route = Screen.RecentsScreen.route,
            enterTransition = {EnterTransition.None},
            exitTransition =  {ExitTransition.None},
            popEnterTransition = {EnterTransition.None},
            popExitTransition = { ExitTransition.None},
        )
        {
            BottomNavigationBar(navController) { innerPadding ->
                Recents(navController, chaptersReadInformationDao, recentsViewModel, innerPadding)
            }
        }
    }
}
