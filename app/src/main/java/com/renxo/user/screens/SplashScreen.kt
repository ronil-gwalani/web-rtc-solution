package com.renxo.user.screens

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.renxo.user.R
import com.renxo.user.ui.theme.AppColors
import com.renxo.user.utils.GetOneTimeBlock
import com.renxo.user.utils.preferenceManager

@Composable
fun SplashScreen(
    navigate: suspend () -> Unit,
) {
    GetOneTimeBlock {
        preferenceManager.clearAuthToken()
    }
    val scale = remember {
        Animatable(0f)
    }
    GetOneTimeBlock {
        scale.animateTo(
            targetValue = 1.5f,
            animationSpec = tween(
                durationMillis = 500,
                easing = {
                    OvershootInterpolator(1.5f).getInterpolation(it)
                }
            )
        )
        navigate()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.whiteColor), contentAlignment = Alignment.Center
    ) {
        val value = scale.value
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier
                .scale(value)
                .background(Color.Transparent)
        )
    }


}
