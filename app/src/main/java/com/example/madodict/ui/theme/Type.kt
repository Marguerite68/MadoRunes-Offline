package com.example.madodict.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.madodict.R

private val Huitian = FontFamily(Font(R.font.huitian))
private val HanKengGroteskRegular = FontFamily(Font(R.font.hankengrotesk_regular))
private val FzLantingYuans = FontFamily(Font(R.font.fzlantingyuans_l_gb))
private val MadokaArchaic = FontFamily(Font(R.font.madoka_archaic))
private val MadokaModren = FontFamily(Font(R.font.madoka_modren))
private val MadokaMusical = FontFamily(Font(R.font.madoka_musical))
private val MagicumTexturae = FontFamily(Font(R.font.magicum_texturae))

val SplashTitleZh = TextStyle(
    fontFamily = Huitian,
    fontWeight = FontWeight.Normal,
    fontSize = 69.sp
)
val SplashTitleEn = TextStyle(
    fontFamily = Huitian,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp
)
val SplashFooter = TextStyle(
    fontFamily = HanKengGroteskRegular,
    fontWeight = FontWeight.Normal,
    fontSize = 6.sp
)

val PageTitle = TextStyle(
    fontFamily = Huitian,
    fontWeight = FontWeight.Normal,
    fontSize = 35.sp
)
val PageBodyText = TextStyle(
    fontFamily = FzLantingYuans,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp
)
val ContrastHanLatinText = TextStyle(
    fontFamily = FzLantingYuans,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp
)
val ContrastArchaicText = TextStyle(
    fontFamily = MadokaArchaic,
    fontWeight = FontWeight.Normal,
    fontSize = 20.sp
)
val ContrastModrenText = TextStyle(
    fontFamily = MadokaModren,
    fontWeight = FontWeight.Normal,
    fontSize = 20.sp
)
val ContrastMusicalText = TextStyle(
    fontFamily = MadokaMusical,
    fontWeight = FontWeight.Normal,
    fontSize = 20.sp
)
val ContrastGothicText = TextStyle(
    fontFamily = MagicumTexturae,
    fontWeight = FontWeight.Normal,
    fontSize = 20.sp
)
val SettingLabelText = TextStyle(
    fontFamily = FzLantingYuans,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp
)
val DropdownAndColorInputText = TextStyle(
    fontFamily = FzLantingYuans,
    fontWeight = FontWeight.Normal,
    fontSize = 11.sp
)
val LanguageNameText = TextStyle(
    fontFamily = HanKengGroteskRegular,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp
)
val InfoAndBottomBarLabelText = TextStyle(
    fontFamily = FzLantingYuans,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp
)

// Set of Material typography styles to start with
val Typography = Typography(
    titleLarge = PageTitle,
    bodyLarge = PageBodyText,
    bodyMedium = ContrastHanLatinText,
    bodySmall = PageBodyText,
    labelLarge = PageBodyText,
    labelMedium = SettingLabelText,
    labelSmall = DropdownAndColorInputText
)