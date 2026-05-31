package com.example.madodict.wiki.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.madodict.wiki.data.repository.WikiItem
import com.example.madodict.wiki.data.repository.WikiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


// 搜索页 UI 状态
data class SearchUiState(
    val keyword: String = "",
    val isFullSearch: Boolean = false,
    val lastViewedItem: WikiItem? = null
)

// 列表页UI状态
sealed class ListUiState {
    data object Idle : ListUiState()
    data object Loading : ListUiState()
    data class Success(val items: List<WikiItem>) : ListUiState()
    data class Error(val message: String) : ListUiState()
}

// 详情页UI状态
sealed class DetailUiState {
    data object Idle : DetailUiState()
    data object Loading : DetailUiState()
    data class Success(val item: WikiItem) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}

class WikiViewModel(
    private val repository: WikiRepository
) : ViewModel() {

    // 搜索页状态
    private val _searchUiState = MutableStateFlow(SearchUiState())
    val searchUiState: StateFlow<SearchUiState> = _searchUiState.asStateFlow()

    //列表页状态
    private val _listUiState = MutableStateFlow<ListUiState>(ListUiState.Idle)
    val listUiState: StateFlow<ListUiState> = _listUiState.asStateFlow()

    // 详情页状态
    private val _detailUiState = MutableStateFlow<DetailUiState>(DetailUiState.Idle)
    val detailUiState: StateFlow<DetailUiState> = _detailUiState.asStateFlow()

    // 搜索页操作
    fun onKeywordChange(keyword: String) {
        _searchUiState.value = _searchUiState.value.copy(keyword = keyword)
    }

    fun onFullSearchToggle(enabled: Boolean) {
        _searchUiState.value = _searchUiState.value.copy(isFullSearch = enabled)
    }

    fun clearLastViewedItem() {
        _searchUiState.value = _searchUiState.value.copy(lastViewedItem = null)
    }

    fun setLastViewedItem(item: WikiItem) {
        _searchUiState.value = _searchUiState.value.copy(lastViewedItem = item)
    }

    fun search() {
        val state = _searchUiState.value
        val keyword = state.keyword.trim()
        if (keyword.isEmpty()) return
        viewModelScope.launch {
            _listUiState.value = ListUiState.Loading
            _listUiState.value = try {
                val results = if (state.isFullSearch) {
                    repository.searchFull(keyword)
                } else {
                    repository.search(keyword)
                }
                ListUiState.Success(results)
            } catch (e: Exception) {
                ListUiState.Error(e.message ?: "搜索失败")
            }
        }
    }

    fun loadAll() {
        viewModelScope.launch {
            _listUiState.value = ListUiState.Loading
            _listUiState.value = try {
                ListUiState.Success(repository.getAll())
            } catch (e: Exception) {
                ListUiState.Error(e.message ?: "加载失败")
            }
        }
    }

    // 随机跳转：直接返回条目，由调用方负责导航
    fun loadRandom(onResult: (WikiItem?) -> Unit) {
        viewModelScope.launch {
            onResult(
                try { repository.getRandom() }
                catch (e: Exception) { null }
            )
        }
    }

    // 列表页返回搜索页时重置列表状态
    fun resetListState() {
        _listUiState.value = ListUiState.Idle
    }
}
