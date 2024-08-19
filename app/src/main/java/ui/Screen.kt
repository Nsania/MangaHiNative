package ui

sealed class Screen(val route: String)
{
    data object LibraryScreen: Screen("library")
    data object BrowseScreen: Screen("browse")
    data object ChaptersScreen: Screen("chapters")
    data object ReaderScreen: Screen("reader")
    data object RecentsScreen: Screen("recents")

    fun withArgs(vararg args: String): String
    {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}