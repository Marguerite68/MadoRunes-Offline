package com.example.madodict.wiki.wikiScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.madodict.AppLanguage
import com.example.madodict.BottomBar
import com.example.madodict.R
import com.example.madodict.appString
import com.example.madodict.ui.theme.InfoAndBottomBarLabelText
import com.example.madodict.ui.theme.PageTitle
import com.example.madodict.wiki.data.repository.WikiItem
import com.example.madodict.wiki.viewmodel.ListUiState

@Composable
fun ListScreen(
    selectedTab: Int = 2,
    onTabSelected: (Int) -> Unit = {},
    language: AppLanguage = AppLanguage.ZH,
    listUiState: ListUiState = ListUiState.Success(sampleItems),
    searchKeyword: String = "",
    isAllResults: Boolean = false,
    onBackToSearch: () -> Unit = {},
    onItemClick: (WikiItem) -> Unit = {},
    isFts: Boolean = false,
    listState: LazyListState = rememberLazyListState(),
    currentFilter: Int = 0,
    onFilterChange: (Int) -> Unit = {}
) {
    val context = LocalContext.current

    var selectedLanguage by rememberSaveable { mutableStateOf(language) }

    val finalSearchCondition: String = when {
        isAllResults || searchKeyword.isBlank() -> {
            val allText = appString(context, selectedLanguage, R.string.all)
            if (allText.length > 7) allText.substring(0, 7) + "..." else allText
        }

        else -> {
            if (searchKeyword.length > 7) {
                searchKeyword.substring(0, 7) + "..."
            } else {
                searchKeyword
            }
        }
    }

    

    val ftsFlag = if(isFts) {
        appString(context, selectedLanguage, R.string.on)
    }
    else {
        appString(context, selectedLanguage, R.string.off)
    }

    var filterMenuExpanded by remember { mutableStateOf(false) }

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
            Spacer(modifier = Modifier.height(40.dp) )
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Wiki",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = PageTitle,
                    letterSpacing = 3.sp
                )
                Icon(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .clickable { onBackToSearch() }
                        .size(27.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = appString(context, selectedLanguage, R.string.wiki_search_condition, finalSearchCondition,ftsFlag),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = InfoAndBottomBarLabelText.copy(fontSize = 12.sp)
                )

                Box {
                    Row(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { filterMenuExpanded = true }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.filter),
                            contentDescription = "Filter",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))

                        val filterLabel = when (currentFilter) {
                            1 -> "角色"
                            2 -> "魔女"
                            3 -> "词条"
                            else -> appString(context, selectedLanguage, R.string.filter)
                        }

                        Text(
                            text = filterLabel,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = InfoAndBottomBarLabelText.copy(fontSize = 12.sp)
                        )
                    }

                    DropdownMenu(
                        expanded = filterMenuExpanded,
                        onDismissRequest = { filterMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("关闭") },
                            onClick = {
                                onFilterChange(0)
                                filterMenuExpanded = false
                            }
                        )

                        DropdownMenuItem(
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color(0xFFFFB7C5), shape = CircleShape)
                                )
                            },
                            text = { Text("角色") },
                            onClick = {
                                onFilterChange(1)
                                filterMenuExpanded = false
                            }
                        )

                        DropdownMenuItem(
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color(0xFFD9D9D9), shape = CircleShape)
                                )
                            },
                            text = { Text("魔女") },
                            onClick = {
                                onFilterChange(2)
                                filterMenuExpanded = false
                            }
                        )

                        DropdownMenuItem(
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color(0xFF70838F), shape = CircleShape)
                                )
                            },
                            text = { Text("词条") },
                            onClick = {
                                onFilterChange(3)
                                filterMenuExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                thickness = 2.dp
            )

            when (val state = listUiState) {
                is ListUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = appString(context, selectedLanguage, R.string.loading),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = InfoAndBottomBarLabelText
                        )
                    }
                }
                is ListUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.error),
                            contentDescription = "Error",
                            tint = Color(0xFFAF1C08),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = InfoAndBottomBarLabelText
                        )
                    }
                }
                is ListUiState.Success -> {
                    val displayItems = remember(state.items, currentFilter) {
                        when (currentFilter) {
                            0 -> state.items
                            1 -> state.items.filter { it.category == 1 }
                            2 -> state.items.filter { it.category == 2 }
                            3 -> state.items.filter { it.category == 3 }
                            else -> state.items
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState
                    ) {
                        item() {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        items(displayItems.size) { index ->
                            ListItem(item = displayItems[index], onClick = onItemClick)
                            if (index < displayItems.size - 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                        if(displayItems.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(20.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    val filteredCount = displayItems.size
                                    val entryText = if(filteredCount == 1 && selectedLanguage == AppLanguage.EN) {
                                        appString(context, selectedLanguage, R.string.wiki_entry_num_one)
                                    }
                                    else {
                                        appString(context, selectedLanguage, R.string.wiki_entry_num, filteredCount)
                                    }

                                    Text(
                                        text = entryText,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                                        style = InfoAndBottomBarLabelText
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                        else {
                            item {
                                Spacer(modifier = Modifier.height(20.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = appString(context, selectedLanguage, R.string.wiki_no_results),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                                        style = InfoAndBottomBarLabelText
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }
                ListUiState.Idle -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.warning),
                            contentDescription = "Warning",
                            tint = Color(0xFFFAAD14),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = appString(context, selectedLanguage, R.string.searching_warning),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = InfoAndBottomBarLabelText
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ListItem(
    item: WikiItem,
    onClick: (WikiItem) -> Unit = {}
){
    val categoryColor = when (item.category) {
        1 -> Color(0xFFFFB7C5) // 角色
        2 -> Color(0xFFD9D9D9) // 魔女
        else -> {Color(0xFF70838F) // 一般词条
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(
                    bounded = true
                ),
                onClick = { onClick(item) },
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(4.dp))
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(categoryColor, shape = CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = item.name,
            style = InfoAndBottomBarLabelText.copy(fontSize = 14.sp),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

val sampleItems = listOf(
    WikiItem(
        "0001",
        2,
        "蔷薇的魔女 Gertrud",
        "Gertrud",
        null,
        "蔷薇的魔女，原名格特鲁德，是一位拥有强大力量的魔女。她以其独特的能力和神秘的背景而闻名于世。蔷薇的魔女在历史上留下了许多传说和故事，她的存在一直是人们津津乐道的话题。",
        externalLinks = emptyList(),
        version = 1
    ),
    WikiItem(
        "0002",
        1,
        "鹿目圆 Kaname Madoka",
        "Kaname Madoka",
        null,
        "鹿目圆香是《魔法少女小圆》中的主角之一，她是一个普通的初中生，后来成为了一名魔法少女。她以其善良、勇敢和坚定的性格赢得了许多粉丝的喜爱。鹿目圆香在故事中经历了许多挑战和冒险，她的成长和变化是整个故事的重要组成部分。",
        externalLinks = emptyList(),
        version = 1
    ),
    WikiItem(
        "0003",
        0,
        "魔法少女小圆 Puella Magi Madoka Magica",
        "Puella Magi Madoka Magica",
        null,
        "《魔法少女小圆》是一部由SHAFT制作的原创动画系列，讲述了一个普通的初中生鹿目圆香成为魔法少女后所经历的冒险和挑战。该系列以其独特的故事情节、深刻的主题和精美的动画风格而受到广泛赞誉。它探讨了希望、绝望、牺牲和人性的复杂关系，成为了现代动画中的经典之作。",
        externalLinks = emptyList(),
        version = 1
    )
)

@Preview
@Composable
fun ListScreenPreview() {
    ListScreen()
}