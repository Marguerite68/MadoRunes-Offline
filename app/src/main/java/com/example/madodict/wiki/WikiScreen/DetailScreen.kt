package com.example.madodict.wiki.WikiScreen

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.madodict.AppLanguage
import com.example.madodict.BottomBar
import com.example.madodict.R
import com.example.madodict.ui.theme.LightOutlineVariant
import com.example.madodict.ui.theme.PageBodyText
import com.example.madodict.wiki.data.repository.WikiItem



@Composable
fun DetailScreen(
    selectedTab: Int = 2,
    onTabSelected: (Int) -> Unit = {},
    sample: WikiItem = sampleItem,
    language: AppLanguage = AppLanguage.ZH,
) {
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
                        onClick = { /*返回上一页*/ }
                    ).size(27.dp)
                )
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = sample.name,
                    style = PageBodyText.copy(fontSize = 16.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 70.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AssetImage(assetPath = sample.imagePath)
                    }
                }
            }
        }
    }
}

@Composable
fun AssetImage(assetPath: String?) {
    val context = LocalContext.current
    val resolvedPath = remember(assetPath) {
        assetPath?.let {
            if (it.contains('/')) it else "wikiImg/$it"
        }
    }
    val bitmap = remember(resolvedPath) {
        resolvedPath?.let {
            context.assets.open(it).use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        }
    }

    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
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