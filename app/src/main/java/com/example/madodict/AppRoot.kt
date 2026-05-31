package com.example.madodict

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.madodict.wiki.WikiScreen.DetailScreen
import com.example.madodict.wiki.WikiScreen.ListScreen
import com.example.madodict.wiki.WikiScreen.SearchScreen
import com.example.madodict.wiki.data.db.WikiDatabase
import com.example.madodict.wiki.data.repository.WikiItem
import com.example.madodict.wiki.data.repository.WikiRepository
import kotlinx.coroutines.delay

@Composable
fun AppRoot(
    language: AppLanguage,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
) {
    val context = LocalContext.current

    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var showBootScreen by rememberSaveable { mutableStateOf(true) }
    var converterInputText by remember { mutableStateOf("") }
    var converterFont by remember { mutableStateOf(WitchFontType.ANCIENT) }
    var converterSize by remember { mutableStateOf(15) }
    var converterColorHex by remember { mutableStateOf("6E6488") }
    var converterSpacingMultiplier by remember { mutableStateOf(1.0f) }

    var wikiScreen by rememberSaveable { mutableStateOf(WikiScreenType.Search) }
    var listSearchKeyword by rememberSaveable { mutableStateOf("") }
    var listIsAll by rememberSaveable { mutableStateOf(false) }
    var detailBackTarget by rememberSaveable { mutableStateOf(WikiScreenType.Search) }
    var selectedWikiItem by remember { mutableStateOf<WikiItem?>(null) }

    val wikiDao = remember { WikiDatabase.getInstance(context).encyclopediaDao() }
    val wikiRepository = remember { WikiRepository(wikiDao) }
    val wikiViewModel = remember { com.example.madodict.wiki.viewmodel.WikiViewModel(wikiRepository) }
    val listUiState by wikiViewModel.listUiState.collectAsState()

    val onTabSelectedHandler: (Int) -> Unit = { tab ->
        selectedTab = tab
        if (tab == 2) {
            wikiScreen = WikiScreenType.Search
            wikiViewModel.resetListState()
        }
    }

    LaunchedEffect(Unit) {
        delay(400L)
        showBootScreen = false
        selectedTab = 0
    }

    if (showBootScreen) {
        BootScreen()
        return
    }

    when (selectedTab) {
        0 -> DictionaryScreen(
            runeEntries = defaultRuneEntries,
            selectedTab = 0,
            onTabSelected = onTabSelectedHandler,
            language = language
        )
        1 -> ConverterScreen(
            selectedTab = 1,
            onTabSelected = onTabSelectedHandler,
            language = language,
            inputText = converterInputText,
            onInputTextChange = { converterInputText = it },
            selectedFont = converterFont,
            onSelectedFontChange = { converterFont = it },
            selectedSize = converterSize,
            onSelectedSizeChange = { converterSize = it },
            colorHex = converterColorHex,
            onColorHexChange = { converterColorHex = it },
            spacingMultiplier = converterSpacingMultiplier,
            onSpacingMultiplierChange = { converterSpacingMultiplier = it }
        )
        2 -> {
            when (wikiScreen) {
                WikiScreenType.List -> {
                    ListScreen(
                        selectedTab = 2,
                        onTabSelected = onTabSelectedHandler,
                        language = language,
                        listUiState = listUiState,
                        searchKeyword = listSearchKeyword,
                        isAllResults = listIsAll,
                        onBackToSearch = {
                            wikiScreen = WikiScreenType.Search
                            wikiViewModel.resetListState()
                        },
                        onItemClick = { item ->
                            selectedWikiItem = item
                            detailBackTarget = WikiScreenType.List
                            wikiScreen = WikiScreenType.Detail
                        }
                    )
                }
                WikiScreenType.Detail -> {
                    val detailItem = selectedWikiItem
                    if (detailItem != null) {
                        DetailScreen(
                            selectedTab = 2,
                            onTabSelected = onTabSelectedHandler,
                            item = detailItem,
                            language = language,
                            onBack = { wikiScreen = detailBackTarget }
                        )
                    } else {
                        SearchScreen(
                            selectedTab = 2,
                            onTabSelected = onTabSelectedHandler,
                            language = language,
                            viewModel = wikiViewModel,
                            onShowList = { keyword, isAll ->
                                listSearchKeyword = keyword
                                listIsAll = isAll
                                wikiScreen = WikiScreenType.List
                            },
                            onShowDetail = { item ->
                                selectedWikiItem = item
                                detailBackTarget = WikiScreenType.Search
                                wikiScreen = WikiScreenType.Detail
                            }
                        )
                    }
                }
                WikiScreenType.Search -> {
                    SearchScreen(
                        selectedTab = 2,
                        onTabSelected = onTabSelectedHandler,
                        language = language,
                        viewModel = wikiViewModel,
                        onShowList = { keyword, isAll ->
                            listSearchKeyword = keyword
                            listIsAll = isAll
                            wikiScreen = WikiScreenType.List
                        },
                        onShowDetail = { item ->
                            selectedWikiItem = item
                            detailBackTarget = WikiScreenType.Search
                            wikiScreen = WikiScreenType.Detail
                        }
                    )
                }
            }
        }
        3 -> SettingsScreen(
            selectedTab = 3,
            onTabSelected = onTabSelectedHandler,
            switchSelected = isDarkTheme,
            onThemeChange = onThemeChange,
            language = language,
            onLanguageChange = onLanguageChange
        )
    }
}

private enum class WikiScreenType {
    Search,
    List,
    Detail
}
