package com.example.madodict
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.madodict.ui.theme.PageTitle

@Composable
fun ConverterScreen(
    selectedTab: Int = 1,
    onTabSelected: (Int) -> Unit = {},
    language: AppLanguage = AppLanguage.ZH
) {
    val context = LocalContext.current

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
                .padding(horizontal = 30.dp, vertical = 40.dp)
        ) {
            Text(
                text = "Converter", //固定显示
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = PageTitle,
                letterSpacing = 3.sp
            )
        }
    }
}

@Preview
@Composable
fun ConverterScreenPreview() {
    ConverterScreen()
}