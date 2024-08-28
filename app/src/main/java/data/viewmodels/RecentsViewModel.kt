package data.viewmodels

import androidx.lifecycle.ViewModel
import data.tables.ChaptersReadInformation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RecentsViewModel: ViewModel() {

    private val _recentsList = MutableStateFlow<List<ChaptersReadInformation>>(emptyList())
    val recentsList: StateFlow<List<ChaptersReadInformation>> = _recentsList

    fun updateRecentsList(recentsList: List<ChaptersReadInformation>)
    {
        _recentsList.value = recentsList
    }
}