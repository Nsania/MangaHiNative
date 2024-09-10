package data.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import scraper.Result

class BrowseViewModel: ViewModel() {
    private val _searchValue = MutableStateFlow<String>("")
    val searchValue: StateFlow<String> = _searchValue

    private val _searchResults = MutableStateFlow<List<Result>>(emptyList())
    val searchResults: StateFlow<List<Result>> = _searchResults

    private val _errorMessage = MutableStateFlow<String>("")
    val errorMessage: StateFlow<String> = _errorMessage

    private val _topBarTitle = MutableStateFlow<String>("Browse")
    val topBarTitle: StateFlow<String> = _topBarTitle


    fun updateSearchValue(searchValue: String)
    {
        _searchValue.value = searchValue
    }

    fun updateSearchResults(searchResults: List<Result>)
    {
        _searchResults.value = searchResults
    }

    fun updateErrorMessage(errorMessage: String)
    {
        _errorMessage.value = errorMessage
    }

    fun updateTopBarTitle(topBarTitle: String)
    {
        _topBarTitle.value = topBarTitle
    }

}