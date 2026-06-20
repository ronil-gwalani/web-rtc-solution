package com.renxo.user.navigation

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.renxo.user.utils.GetOneTimeBlock
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun IdleDetectionApp(
    idleTimeoutMillis: Long,
    onIdle: () -> Unit,
    content: @Composable () -> Unit,
) {
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val coroutineScope = rememberCoroutineScope()

    // Function to reset idle timer
    fun resetIdleTimer() {
        lastInteractionTime = System.currentTimeMillis()
    }

    // Monitor idle state using a coroutine
        GetOneTimeBlock {
        coroutineScope.launch {
            while (true) {
                delay(10000) // Check every 10 second
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastInteractionTime > idleTimeoutMillis) {
                    resetIdleTimer()
                    onIdle() // Trigger the idle state callback
                }
            }
        }
    }

    // Idle detection wrapper
    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // Detect all touch events
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent()
                        resetIdleTimer()
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { resetIdleTimer() },
                    onTap = { resetIdleTimer() },
                    onDoubleTap = { resetIdleTimer() },
                    onLongPress = { resetIdleTimer() }
                )
            }
    ) {
        content()
    }
}
