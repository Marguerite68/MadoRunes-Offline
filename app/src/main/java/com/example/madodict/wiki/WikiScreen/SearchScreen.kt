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
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.Shapes
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
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
import com.example.madodict.ui.theme.PageBodyText
import com.example.madodict.ui.theme.PageTitle
import androidx.compose.ui.res.painterResource

@Composable
fun SearchScreen(
    selectedTab: Int = 2,
    onTabSelected: (Int) -> Unit = {},
    language: AppLanguage = AppLanguage.ZH,
) {
    val context = LocalContext.current

    val queryState = remember { mutableStateOf("") }

    val interactionSource1 = remember { MutableInteractionSource() }
    val interactionSource2 = remember { MutableInteractionSource() }

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
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                BasicTextField(
                    value = queryState.value,
                    onValueChange = { queryState.value = it },
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
                                if (queryState.value.isEmpty()) {
                                    Text(
                                        text = appString(context, language, R.string.wiki_search_hint),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
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
                                    .size(18.dp)
                                    .clickable { }
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(20.dp))
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
                                onClick = { /* 随机文章 */ }
                            ),
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
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .height(30.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = { /* 全部文章 */ }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.all),
                            contentDescription = appString(context, language, R.string.wiki_all),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(25.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = appString(context, language, R.string.wiki_all),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun SearchScreenPreview() {
    SearchScreen()
}