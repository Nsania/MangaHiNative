package data.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.tables.ChaptersReadInformation
import data.tables.MangaChapters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import scraper.Chapter

class ChaptersViewModel: ViewModel() {
    private val _chapters = MutableStateFlow<List<MangaChapters>>(emptyList())
    val chapters: StateFlow<List<MangaChapters>> = _chapters

    private val _mangaId = MutableStateFlow<Int>(0)
    val mangaId: StateFlow<Int> = _mangaId

    private val _inLibrary = MutableStateFlow<Boolean>(false)
    val inLibrary: StateFlow<Boolean> = _inLibrary

    private val _libraryText = MutableStateFlow<String>("Add to Library")
    val libraryText: StateFlow<String> = _libraryText

    private val _mangaDescription = MutableStateFlow<String>("")
    val mangaDescription: StateFlow<String> = _mangaDescription

    private val _expandedState = MutableStateFlow<Boolean>(false)
    val expandedState: StateFlow<Boolean> = _expandedState

    private val _title = MutableStateFlow<String>("")
    val title: StateFlow<String> = _title

    private val _imageCover = MutableStateFlow<String>("")
    val imageCover: StateFlow<String> = _imageCover

    private val _readChapters = MutableStateFlow<List<ChaptersReadInformation>>(emptyList())
    val readChapters: StateFlow<List<ChaptersReadInformation>> = _readChapters

    val readChaptersNumber: StateFlow<List<Double>> = _readChapters.map { chapters -> chapters.map { it.chapter } }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun updateChapters(chapters: List<MangaChapters>)
    {
        _chapters.value = chapters
    }

    fun updateMangaId(mangaId: Int)
    {
        _mangaId.value = mangaId
    }

    fun updateInLibrary(inLibrary: Boolean)
    {
        _inLibrary.value = inLibrary
    }

    fun updateLibraryText(libraryText: String)
    {
        _libraryText.value = libraryText
    }

    fun updateMangaDescription(mangaDescription: String)
    {
        _mangaDescription.value = mangaDescription
    }

    fun updateTitle(title: String)
    {
        _title.value = title
    }

    fun updateImageCover(imageCover: String)
    {
        _imageCover.value = imageCover
    }

    fun updateReadChapters(readChapters: List<ChaptersReadInformation>)
    {
        _readChapters.value = readChapters
    }

    fun updateExpandedState(expandedState: Boolean)
    {
        _expandedState.value = expandedState
    }
}