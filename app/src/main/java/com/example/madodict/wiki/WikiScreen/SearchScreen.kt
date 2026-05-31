package com.example.madodict.wiki.WikiScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.madodict.AppLanguage
import com.example.madodict.BottomBar
import com.example.madodict.R
import com.example.madodict.appString
import com.example.madodict.ui.theme.PageTitle
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import com.example.madodict.ui.theme.InfoAndBottomBarLabelText
import com.example.madodict.ui.theme.SettingLabelText
import com.example.madodict.wiki.data.db.VersionRecordsEntity
import com.example.madodict.wiki.data.db.WikiDao
import com.example.madodict.wiki.data.db.WikiItemEntity
import com.example.madodict.wiki.data.repository.WikiRepository
import com.example.madodict.wiki.viewmodel.WikiViewModel

@Composable
fun SearchScreen(
    selectedTab: Int = 2,
    onTabSelected: (Int) -> Unit = {},
    language: AppLanguage = AppLanguage.ZH,
    viewModel: WikiViewModel,
    onShowList: (String, Boolean) -> Unit = { _, _ -> },
    onShowDetail: (com.example.madodict.wiki.data.repository.WikiItem) -> Unit = {}
) {
    val context = LocalContext.current

    val searchUiState by viewModel.searchUiState.collectAsState()

    val hasLastRead = searchUiState.lastViewedItem != null

    val letterSpacingValue = if (language == AppLanguage.ZH) 2.sp else 0.sp

    Scaffold(
        bottomBar = {
            BottomBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                language = language
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 30.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "Wiki",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = PageTitle,
                letterSpacing = 3.sp
            )
            Spacer(modifier = Modifier.height(120.dp))
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = appString(context, language, R.string.wiki_greeting),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = SettingLabelText.copy(fontSize = 18.sp),
                    letterSpacing = letterSpacingValue
                )
                Spacer(modifier = Modifier.height(15.dp))

                BasicTextField(
                    value = searchUiState.keyword,
                    onValueChange = { viewModel.onKeywordChange(it) },
                    singleLine = true,
                    modifier = Modifier
                        .width(300.dp)
                        .height(50.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(50.dp)
                        )
                        .padding(horizontal = 12.dp),
                    decorationBox = { innerTextField ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchUiState.keyword.isEmpty()) {
                                    Text(
                                        text = appString(context, language, R.string.wiki_search_hint),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
                                    )
                                }
                                innerTextField()
                            }
                            Icon(
                                painter = painterResource(id = R.drawable.search),
                                contentDescription = appString(
                                    context,
                                    language,
                                    R.string.wiki_search_hint
                                ),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .size(22.dp)
                                    .clickable {
                                        val keyword = searchUiState.keyword.trim()
                                        if (keyword.isNotEmpty()) {
                                            viewModel.search()
                                            onShowList(keyword, false)
                                        }
                                    }
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(15.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 50.dp),

                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        modifier = Modifier
                            .height(30.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(
                                    bounded = true
                                ),
                                onClick = {
                                    viewModel.loadRandom { item ->
                                        if (item != null) {
                                            onShowDetail(item)
                                        }
                                    }
                                }
                            )
                            .padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.random),
                            contentDescription = appString(context, language, R.string.wiki_random),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(25.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = appString(context, language, R.string.wiki_random),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = InfoAndBottomBarLabelText.copy(fontSize = 14.sp)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .height(30.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = {
                                    viewModel.loadAll()
                                    onShowList("", true)
                                }
                            )
                            .padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.all),
                            contentDescription = appString(context, language, R.string.wiki_all),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(27.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = appString(context, language, R.string.wiki_all),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = InfoAndBottomBarLabelText.copy(fontSize = 14.sp)
                        )
                    }
                }

                if(hasLastRead){
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = appString(context, language, R.string.continue_last_reading),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = InfoAndBottomBarLabelText.copy(fontSize = 12.sp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = searchUiState.lastViewedItem?.name ?: "",
                            color = MaterialTheme.colorScheme.primary,
                            style = InfoAndBottomBarLabelText.copy(fontSize = 12.sp),
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable(
                                onClick = { /* TODO: 直接跳转至上次打开的条目 */ },
                            )
                        )
                    }
                }
            }
        }
    }
}

private class PreviewWikiDao : WikiDao {
    override suspend fun insertEntry(entry: WikiItemEntity) = Unit
    override suspend fun updateEntry(entry: WikiItemEntity) = Unit
    override suspend fun deleteEntry(entryId: String) = Unit
    override suspend fun searchEntries(keyword: String): List<WikiItemEntity> = emptyList()
    override suspend fun getAllEntries(): List<WikiItemEntity> = emptyList()
    override suspend fun getRandomEntry(): WikiItemEntity? = null
    override suspend fun getEntryById(entryId: String): WikiItemEntity? = null
    override suspend fun insertVersionRecord(record: VersionRecordsEntity) = Unit
    override suspend fun updateVersionRecord(record: VersionRecordsEntity) = Unit
    override suspend fun deleteVersionRecord(entryId: String) = Unit
    override suspend fun getAllVersionRecords(): List<VersionRecordsEntity> = emptyList()
    override suspend fun getVersionRecord(entryId: String): VersionRecordsEntity? = null
    override suspend fun rebuildFts() = Unit
}

@Composable
@Preview
fun SearchScreenPreview() {
    val previewViewModel = remember { WikiViewModel(WikiRepository(PreviewWikiDao())) }
    SearchScreen(viewModel = previewViewModel)
}