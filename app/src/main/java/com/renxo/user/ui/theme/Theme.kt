package com.renxo.user.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


private val LightColorScheme = lightColorScheme(
    primary = AppColors.accentColor,
    secondary = AppColors.secondaryColor,
    tertiary = AppColors.textColor

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = AppColors.whiteColor,
    onSecondary = AppColors.whiteColor,
    onTertiary = AppColors.whiteColor,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun RenxoUserAppTheme(

    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {

    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        window.statusBarColor = Color.Transparent.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = customTypography,
        content = content
    )
}