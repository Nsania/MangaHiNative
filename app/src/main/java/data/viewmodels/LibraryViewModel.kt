package data.viewmodels

import androidx.lifecycle.ViewModel
import data.tables.LibraryInformation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LibraryViewModel: ViewModel() {
    private val _mangasInLibraryList = MutableStateFlow<List<LibraryInformation>>(emptyList())
    val mangasInLibraryList: StateFlow<List<LibraryInformation>> = _mangasInLibraryList

    fun updateMangasInLibraryList(mangasInLibraryList: List<LibraryInformation>)
    {
        _mangasInLibraryList.value = mangasInLibraryList
    }
}