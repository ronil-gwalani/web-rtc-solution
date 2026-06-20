package com.renxo.user.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.renxo.user.R
import com.renxo.user.ui.theme.AppColors
import com.renxo.user.utils.GetOneTimeBlock
import com.renxo.user.utils.calculator.CalculatorButtonGrid
import com.renxo.user.utils.calculator.CalculatorDisplay
import com.renxo.user.utils.calculator.CalculatorViewModel
import com.renxo.user.utils.calculator.calculatorActions
import kotlin.math.roundToInt


@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel = viewModel(), onDismissRequest: () -> Unit) {
    GetOneTimeBlock {
        viewModel.getFloatingInfo()
    }
    if (viewModel.showFloating) {
        DraggableFloatingView(
            initialPosition = Offset(110f, 310f), showFullScreen = {
                viewModel.showFloating = false
            }, onDismissRequest = onDismissRequest
        ) {
            Card(
                shape = RoundedCornerShape(bottomEnd = 16.dp, bottomStart = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)

            ) {
                CalculatorLayout(viewModel)
            }
        }
    } else {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnClickOutside = false,
                dismissOnBackPress = false,

                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            // Add animation to the dialog
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(AppColors.accentColor),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        viewModel.showFloating = true

                    }) {
                        Icon(
                            modifier = Modifier.rotate(90f),
                            painter = painterResource(R.drawable.colapes),
                            contentDescription = null, tint = AppColors.whiteColor
                        )
                    }
                    IconButton(onClick = {
                        onDismissRequest()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = null,
                            tint = AppColors.whiteColor
                        )
                    }

                }
                CalculatorLayout(viewModel)
            }

        }

    }
}


@Composable
private fun CalculatorLayout(
    viewModel: CalculatorViewModel
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.whiteColor)
            .padding(bottom = 20.dp)
            .padding(10.dp)
    ) {
        val isLandscape = maxWidth > maxHeight
        val spacing = 8.dp

        if (isLandscape) {
            // Landscape Layout: Display on the left, buttons on the right
            Row(modifier = Modifier.fillMaxSize()) {
                CalculatorDisplay(
                    expression = viewModel.expression,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(25.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(10.dp)
                )
                CalculatorButtonGrid(
                    actions = calculatorActions,
                    onAction = viewModel::onAction,
                    modifier = Modifier
                        .weight(1f)
                        .padding(spacing)
                )
            }
        } else {
            // Portrait Layout: Display on top, buttons below
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                CalculatorDisplay(
                    expression = viewModel.expression,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.5f)
                        .clip(RoundedCornerShape(25.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(10.dp)
                )
                CalculatorButtonGrid(
                    actions = calculatorActions,
                    onAction = viewModel::onAction,
                    modifier = Modifier
                        .padding(spacing)
                        .weight(2f)
                )
            }
        }
    }
}


@Composable
private fun DraggableFloatingView(
    modifier: Modifier = Modifier,
    initialPosition: Offset = Offset(0f, 0f),
    showFullScreen: () -> Unit,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    var position by remember { mutableStateOf(initialPosition) }
//    var isResizing by remember { mutableStateOf(false) }
    var size by remember { mutableStateOf(Size(250f, 450f)) } // Initial size

    Column(
        modifier = modifier
            .size(size.width.dp, size.height.dp)
            .offset { IntOffset(position.x.roundToInt(), position.y.roundToInt()) }

    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(AppColors.accentColor)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        position = position.plus(Offset(dragAmount.x, dragAmount.y))
                    }
                },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {
                showFullScreen()
            }) {
                Icon(
                    modifier = Modifier.rotate(90f),
                    painter = painterResource(R.drawable.expand),
                    contentDescription = null, tint = AppColors.whiteColor
                )
            }
            IconButton(onClick = {
                onDismissRequest()

            }) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = null,
                    tint = AppColors.whiteColor
                )
            }

        }

        Box(Modifier.fillMaxSize()) {

            content()

            Icon(
                painter = painterResource(R.drawable.resize),
                contentDescription = null,
                tint = AppColors.accentColor,
                modifier = Modifier
                    .padding(5.dp)
                    .size(34.dp)
//                .weight(1f)
                    .align(Alignment.BottomEnd)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
//                            isResizing = true
                            change.consume()

                            val newWidth = (size.width + dragAmount.x).coerceIn(150f, 500f)
                            val newHeight =
                                (size.height + dragAmount.y)
                                    .coerceIn(320f, 700f)

                            size = Size(newWidth, newHeight)
                        }
                    }
            )
        }
    }
}
    

