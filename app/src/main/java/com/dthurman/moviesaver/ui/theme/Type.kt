package com.dthurman.moviesaver.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.dthurman.moviesaver.R

@Stable
val lexendFontFamily = FontFamily(
    Font(R.font.lexend_thin, FontWeight.Thin),
    Font(R.font.lexend_extra_light, FontWeight.ExtraLight),
    Font(R.font.lexend_light, FontWeight.Light),
    Font(R.font.lexend_regular, FontWeight.Normal),
    Font(R.font.lexend_medium, FontWeight.Medium),
    Font(R.font.lexend_semi_bold, FontWeight.SemiBold),
    Font(R.font.lexend_bold, FontWeight.Bold),
    Font(R.font.lexend_extra_bold, FontWeight.ExtraBold),
)

private val BaselineM3 = Typography()

@Stable
val AppTypography = Typography(
    displayLarge  = BaselineM3.displayLarge.copy(fontFamily = lexendFontFamily),
    displayMedium = BaselineM3.displayMedium.copy(fontFamily = lexendFontFamily),
    displaySmall  = BaselineM3.displaySmall.copy(fontFamily = lexendFontFamily),

    headlineLarge  = BaselineM3.headlineLarge.copy(fontFamily = lexendFontFamily),
    headlineMedium = BaselineM3.headlineMedium.copy(fontFamily = lexendFontFamily),
    headlineSmall  = BaselineM3.headlineSmall.copy(fontFamily = lexendFontFamily),

    titleLarge  = BaselineM3.titleLarge.copy(fontFamily = lexendFontFamily),
    titleMedium = BaselineM3.titleMedium.copy(fontFamily = lexendFontFamily),
    titleSmall  = BaselineM3.titleSmall.copy(fontFamily = lexendFontFamily),

    bodyLarge  = BaselineM3.bodyLarge.copy(fontFamily = lexendFontFamily),
    bodyMedium = BaselineM3.bodyMedium.copy(fontFamily = lexendFontFamily),
    bodySmall  = BaselineM3.bodySmall.copy(fontFamily = lexendFontFamily),

    labelLarge  = BaselineM3.labelLarge.copy(fontFamily = lexendFontFamily),
    labelMedium = BaselineM3.labelMedium.copy(fontFamily = lexendFontFamily),
    labelSmall  = BaselineM3.labelSmall.copy(fontFamily = lexendFontFamily),
)