package data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ui.MainActivity
import ui.MyApplication

/*data class CurrentReaderMode (
    val mode: Int
)*/

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

    private val _totalPages = MutableStateFlow(0)
    val totalPages: StateFlow<Int> = _totalPages

    private val _chapters = MutableStateFlow<List<String>>(emptyList())
    val chapters: StateFlow<List<String>> = _chapters

    private val _imagePaths = MutableStateFlow<List<String>>(emptyList())
    val imagePaths: StateFlow<List<String>> = _imagePaths

    private val _previousChapterLink = MutableStateFlow<String?>(null)
    val previousChapterLink: StateFlow<String?> = _previousChapterLink

    private val _chapter = MutableStateFlow(0.0)
    val chapter: StateFlow<Double> = _chapter

    private val _imageFileNames = MutableStateFlow<List<String>>(emptyList())
    val imageFileNames: StateFlow<List<String>> = _imageFileNames

    fun saveReaderMode(mode: Int) {
        viewModelScope.launch {
            userRepository.saveReaderMode(mode)
        }
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
}
