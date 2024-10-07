package data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import data.MyApplication
import data.UserRepository
import data.tables.MangaChapters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class ReaderViewModel(private val userRepository: UserRepository) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as MyApplication)
                ReaderViewModel(application.userRepository)
            }
        }
    }

    val readerMode: StateFlow<Int> =
        userRepository.currentReaderMode.map { mode ->
            mode
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val _currentChapterLink = MutableStateFlow("")
    val currentChapterLink: StateFlow<String> = _currentChapterLink

    private val _currentChapterTitle = MutableStateFlow("")
    val currentChapterTitle: StateFlow<String> = _currentChapterTitle

    private val _totalPages = MutableStateFlow(0)
    val totalPages: StateFlow<Int> = _totalPages

    private val _chapters = MutableStateFlow<List<String>>(emptyList())
    val chapters: StateFlow<List<String>> = _chapters

    private val _imagePaths = MutableStateFlow<List<String>>(emptyList())
    val imagePaths: StateFlow<List<String>> = _imagePaths

    private val _previousChapterLink = MutableStateFlow<String?>(null)
    val previousChapterLink: StateFlow<String?> = _previousChapterLink

    private val _previousChapter = MutableStateFlow<MangaChapters?>(null)
    val previousChapter: StateFlow<MangaChapters?> = _previousChapter

    private val _nextChapter = MutableStateFlow<MangaChapters?>(null)
    val nextChapter: StateFlow<MangaChapters?> = _nextChapter

    private val _previousChapterTotalPages = MutableStateFlow<Int>(0)
    val previousChapterTotalPages: StateFlow<Int> = _previousChapterTotalPages
    
    private val _nextChapterTotalPages = MutableStateFlow<Int>(0)
    val nextChapterTotalPages: StateFlow<Int> = _nextChapterTotalPages

    private val _chapter = MutableStateFlow(0.0)
    val chapter: StateFlow<Double> = _chapter

    private val _imageFileNames = MutableStateFlow<List<String>>(emptyList())
    val imageFileNames: StateFlow<List<String>> = _imageFileNames

    private val _imageLinks = MutableStateFlow<List<String>>(emptyList())
    val imageLinks: StateFlow<List<String>> = _imageLinks

    fun saveReaderMode(mode: Int) {
        viewModelScope.launch {
            userRepository.saveReaderMode(mode)
        }
    }

    fun updateCurrentChapterLink(currentChapterLink: String)
    {
        _currentChapterLink.value = currentChapterLink
    }

    fun updateCurrentChapterTitle(currentChapterTitle: String)
    {
        _currentChapterTitle.value = currentChapterTitle
    }

    fun updateTotalPages(totalPages: Int) {
        _totalPages.value = totalPages
    }

    fun updateChapters(chapters: List<String>) {
        _chapters.value = chapters
    }

    fun updateImagePaths(imagePaths: List<String>) {
        _imagePaths.value = imagePaths
    }

    fun updatePreviousChapterLink(previousChapterLink: String?) {
        _previousChapterLink.value = previousChapterLink
    }

    fun updatePreviousChapter(previousChapter: MangaChapters?)
    {
        _previousChapter.value = previousChapter
    }

    fun updateNextChapter(nextChapter: MangaChapters?)
    {
        _nextChapter.value = nextChapter
    }
    
    fun updatePreviousChapterTotalPages(previousChapterTotalPages: Int)
    {
        _previousChapterTotalPages.value = previousChapterTotalPages
    }

    fun updateNextChapterTotalPages(nextChapterTotalPages: Int)
    {
        _nextChapterTotalPages.value = nextChapterTotalPages
    }

    fun updateChapter(chapter: Double) {
        _chapter.value = chapter
    }

    fun updateImageFileNames(imageFileNames: List<String>) {
        _imageFileNames.value = imageFileNames
    }

    fun getImagePathAt(index: Int): String {
        return _imagePaths.value.getOrElse(index) { "" }
    }

    fun updateImagePath(index: Int, newPath: String) {
        _imagePaths.update { paths ->
            paths.toMutableList().apply {
                if (index in indices) {
                    this[index] = newPath
                }
            }
        }
    }

    fun updateImageLinks(imageLinks: List<String>)
    {
        _imageLinks.value = imageLinks
    }

}
