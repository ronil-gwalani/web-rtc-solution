package com.renxo.user.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.renxo.user.R

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)



val customTypography = Typography(

    bodyLarge = TextStyle(
        fontFamily = FontFamily(Font(R.font.medium)),
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight =18.sp,
        letterSpacing = 0.5.sp
    )
    ,
    displayMedium = TextStyle(
        fontFamily = FontFamily(Font(R.font.medium)),
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.2.sp
    )
    ,
    displaySmall = TextStyle(
        fontFamily = FontFamily(Font(R.font.regular)),
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.2.sp
    )
    ,
    headlineLarge = TextStyle(
        fontFamily = FontFamily(Font(R.font.medium)),
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight =18.sp,
        letterSpacing = 0.5.sp
    )
    ,
    headlineMedium = TextStyle(
        fontFamily = FontFamily(Font(R.font.medium)),
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.2.sp

    )
    ,
    headlineSmall = TextStyle(
        fontFamily = FontFamily(Font(R.font.regular)),
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.2.sp
    )
    ,
    titleLarge = TextStyle(
        fontFamily = FontFamily(Font(R.font.medium)),
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight =18.sp,
        letterSpacing = 0.5.sp
    )
    ,
    titleMedium = TextStyle(
        fontFamily = FontFamily(Font(R.font.medium)),
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.2.sp
    )
    ,
    titleSmall = TextStyle(
        fontFamily = FontFamily(Font(R.font.regular)),
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.2.sp
    )
    ,
    bodyMedium = TextStyle(
        fontFamily = FontFamily(Font(R.font.medium)),
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.2.sp
    )
    ,
    bodySmall = TextStyle(
        fontFamily = FontFamily(Font(R.font.regular)),
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.2.sp
    )
    ,
    labelLarge = TextStyle(
        fontFamily = FontFamily(Font(R.font.medium)),
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight =18.sp,
        letterSpacing = 0.5.sp
    )
    ,
    labelMedium = TextStyle(
        fontFamily = FontFamily(Font(R.font.medium)),
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.2.sp
    )
    ,
    labelSmall = TextStyle(
        fontFamily = FontFamily(Font(R.font.regular)),
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.2.sp
    )
    ,

    displayLarge = TextStyle(
        fontFamily = FontFamily(Font(R.font.medium)),
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight =18.sp,
        letterSpacing = 0.5.sp
        // Other text style properties if needed
    ),

    // And so on for all typography levels
)