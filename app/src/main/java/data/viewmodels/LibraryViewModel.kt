package data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import data.MyApplication
import data.UserRepository
import data.tables.LibraryInformation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(private val userRepository: UserRepository): ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as MyApplication)
                LibraryViewModel(application.userRepository)
            }
        }
    }

    val viewMode: StateFlow<Int> =
        userRepository.currentViewMode.map { mode ->
            mode
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val _mangasInLibraryList = MutableStateFlow<List<LibraryInformation>>(emptyList())
    val mangasInLibraryList: StateFlow<List<LibraryInformation>> = _mangasInLibraryList

    fun saveViewMode(mode: Int) {
        viewModelScope.launch {
            userRepository.saveViewMode(mode)
        }
    }

    fun updateMangasInLibraryList(mangasInLibraryList: List<LibraryInformation>)
    {
        _mangasInLibraryList.value = mangasInLibraryList
    }
}