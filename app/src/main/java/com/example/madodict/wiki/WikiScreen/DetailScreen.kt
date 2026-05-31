package com.example.madodict.wiki.WikiScreen

import android.content.Intent
import android.net.Uri
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.madodict.AppLanguage
import com.example.madodict.BottomBar
import com.example.madodict.R
import com.example.madodict.ui.theme.ContrastArchaicText
import com.example.madodict.ui.theme.PageBodyText
import com.example.madodict.wiki.data.repository.WikiItem
import androidx.compose.ui.text.style.TextDecoration
import com.example.madodict.appString


@Composable
fun DetailScreen(
    selectedTab: Int = 2,
    onTabSelected: (Int) -> Unit = {},
    item: WikiItem = sampleItem,
    language: AppLanguage = AppLanguage.ZH,
    onBack: () -> Unit = {}
) {
    val categoryColor = when (item.category) {
        1 -> Color(0xFFFFB7C5) // 角色
        2 -> Color(0xFFD9D9D9) // 魔女
        else -> {Color(0xFF70838F) // 一般词条
        }
    }

    val context = LocalContext.current

    var selectedLanguage by remember { mutableStateOf(language) }

    Scaffold(
        bottomBar = {
            BottomBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                language = language
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 30.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Row (
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ){
                Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Back to previous screen",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable(
                        onClick = onBack
                    ).size(27.dp)
                )
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = item.name,
                    style = PageBodyText.copy(fontSize = 16.sp, letterSpacing = 1.5.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 70.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AssetImage(assetPath = item.imagePath,
                            selectedLanguage)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.name,
                            style = PageBodyText.copy(fontSize = 16.sp, letterSpacing = 1.5.sp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(categoryColor, shape = CircleShape)
                            )

                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (item.category) {
                                    1 -> "角色"
                                    2 -> "魔女"
                                    else -> "一般词条"
                                },
                                style = PageBodyText.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                if(item.category==2 && item.enName!=null){
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.enName,
                                style = ContrastArchaicText.copy(fontSize = 18.sp, letterSpacing = 2.sp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                if(item.enName!=null){
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "英文名： " + item.enName,
                                style = PageBodyText.copy(
                                    fontSize = 14.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                item{
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f),
                        thickness = 2.dp
                    )
                }
                item{
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ){
                        Text(
                            text = item.content,
                            style = PageBodyText.copy(fontSize = 14.sp, letterSpacing = 1.5.sp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                if(item.externalLinks.isNotEmpty()){
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f),
                            thickness = 2.dp
                        )
                    }
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "外部链接：",
                                style = PageBodyText.copy(fontSize = 14.sp, letterSpacing = 1.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            item.externalLinks.forEach { link ->
                                Text(
                                    text = link.label,
                                    style = PageBodyText.copy(
                                        fontSize = 14.sp,
                                        letterSpacing = 1.5.sp,
                                        textDecoration = TextDecoration.Underline
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable {
                                        openExternalLink(context, link.url)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun openExternalLink(context: android.content.Context, url: String) {
    if (url.isBlank()) return
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}

@Composable
fun AssetImage(assetPath: String?,
               language: AppLanguage) {
    val context = LocalContext.current
    val resolvedPath = remember(assetPath) {
        assetPath?.let {
            if (it.contains('/')) it else "wikiImg/$it"
        }
    }
    val bitmap = remember(resolvedPath) {
        resolvedPath?.let {
            runCatching {
                context.assets.open(it).use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }.getOrNull()
        }
    }

    if (bitmap == null) {
        Text(
            text = appString(context,language, R.string.wiki_img_loading_error),
            style = PageBodyText.copy(fontSize = 12.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(225.dp)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(8.dp)
        )
        return
    }

    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "Wiki Item Image",
        modifier = Modifier
            .fillMaxWidth()
            .height(225.dp)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(8.dp)
            .clip(RoundedCornerShape(20.dp))
    )
}

val sampleItem =
    WikiItem(
        "0000",
        2,
        "蔷薇的魔女 Gertrud",
        "Gertrud",
        "0000_Gertrud.png",
        "蔷薇的魔女，原名格特鲁德，是一位拥有强大力量的魔女。她以其独特的能力和神秘的背景而闻名于世。蔷薇的魔女在历史上留下了许多传说和故事，她的存在一直是人们津津乐道的话题。",
        externalLinks = emptyList(),
        version = 1
    )

@Preview
@Composable
fun DetailScreenPreview() {
    DetailScreen()
}