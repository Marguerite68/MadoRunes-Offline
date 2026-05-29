package com.example.madodict

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.madodict.ui.theme.LightTitleEn
import com.example.madodict.ui.theme.LightTitleZh
import com.example.madodict.ui.theme.SplashTitleEn
import com.example.madodict.ui.theme.SplashTitleZh
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.madodict.ui.theme.LightBottomText
import com.example.madodict.ui.theme.SplashFooter

@Composable
fun BootScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ){
        Image(
            painter = painterResource(id = R.drawable.main_kv02_modified),
            contentDescription = "boot screen background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.main_kv_chara),
                contentDescription = "boot screen mian visual",
                modifier = Modifier.height(550.dp)
            )
            val titleShadow = Shadow(
                color = Color.Black.copy(alpha = 0.35f),
                offset = Offset(3f, 3f),
                blurRadius = 5f
            )
            Text(
                text = "魔法字典",
                style = SplashTitleZh.copy(shadow = titleShadow),
                color = LightTitleZh,
                letterSpacing = 6.sp
            )
            Text(
                text = "Madoka Runes Dictionary",
                style = SplashTitleEn.copy(shadow = titleShadow),
                color = LightTitleEn,
                letterSpacing = 1.1.sp
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            Text(
                text = "Version 1.1.0a / This application follows the CC BY-NC 4.0 license",
                style = SplashFooter,
                color = LightBottomText,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "For learning and research purposes only",
                style = SplashFooter,
                color = LightBottomText,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview
@Composable
fun BootScreenPreview() {
    BootScreen()
}