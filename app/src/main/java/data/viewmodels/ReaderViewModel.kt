package data.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class ReaderViewModel : ViewModel() {

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
