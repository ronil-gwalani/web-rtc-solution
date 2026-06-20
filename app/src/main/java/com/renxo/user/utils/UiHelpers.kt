package com.renxo.user.utils

import android.app.Activity
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.renxo.user.ui.theme.AppColors
import com.renxo.user.viewmodels.HomeVM
import kotlinx.coroutines.CoroutineScope

@Composable
fun getTextFiledColors(): TextFieldColors {
    return TextFieldDefaults.colors(
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        focusedContainerColor = AppColors.lightBlue,
        unfocusedContainerColor = AppColors.whiteColor,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledContainerColor = AppColors.whiteColor,
        disabledTextColor = Color.Gray,
        disabledIndicatorColor = Color.Transparent,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = Color.Gray,
    )
}


val LocalHomeViewModelProvider = compositionLocalOf<HomeVM> {
    error("No HomeVM provided")
}


@Composable
fun Modifier.getTextFieldModifier(): Modifier {
    var focus by remember { mutableStateOf(false) }
    return this
        .fillMaxWidth()
        .border(
            width = if (focus) 1.dp else 0.dp,
            color = if (focus) AppColors.hintColor else Color.Transparent,
            shape = RoundedCornerShape(15.dp),
        )
        .onFocusChanged { focusState ->
            focus = focusState.isFocused
        }
}


@Composable
fun LockScreenOrientation(orientation: Int) {

    val context = LocalContext.current
    DisposableEffect(orientation) {
        val activity = context as Activity
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = orientation
        onDispose {
            // Restore original orientation when navigating away
//            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity.requestedOrientation = originalOrientation
        }
    }
}


val LocalSnackBar = compositionLocalOf<ShackBarState> {
    error("No ShackBarState provided")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> CustomDropdownMenu(
    hint: String,
    value: String?,
    showBottomSheet: Boolean,
    state: LazyListState,
    list: SnapshotStateList<T>,
    onItemSelected: (T) -> Unit,
    onDismissRequest: (Boolean) -> Unit,
    content: (@Composable () -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(
//        initialValue = ModalBottomSheetValue.Hidden,

        confirmValueChange = { true }
    )
    ExposedDropdownMenuBox(expanded = showBottomSheet, onExpandedChange = {
        onDismissRequest(!showBottomSheet)
    }) {
        Box(modifier = Modifier.clickable {
            onDismissRequest(true)
        }) {
            TextField(
                shape = RoundedCornerShape(15.dp),
                colors = getTextFiledColors(),
                value = value?.takeIf { it.isNotEmpty() } ?: hint,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = if (showBottomSheet) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null
                    )
                },
                modifier = Modifier
                    .getTextFieldModifier()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
        }
    }
    if (showBottomSheet) {
        ModalBottomSheet(
            sheetState = sheetState,
            content = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp)  // Half screen height
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    LazyColumn(Modifier.fillMaxWidth(), state = state) {
                        itemsIndexed(list, key = { index, item ->
                            item.toString() + index

                        }) { _, item ->
                            val selected = item == value
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (selected) AppColors.accentColor.copy(alpha = 0.1f) else AppColors.whiteColor)
                                    .clickable {
                                        onItemSelected(item)
                                    }
                            ) {
                                if (content == null) {
                                    Column {
                                        Text(
                                            item.toString(),
                                            modifier = Modifier.padding(
                                                horizontal = 18.dp,
                                                vertical = 10.dp
                                            ),
                                            fontSize = 15.sp
                                        )
                                        HorizontalDivider(
                                            thickness = 0.3.dp,
                                            color = AppColors.hintColor
                                        )
                                    }
                                } else {
                                    content()
                                }
                            }
                        }
                    }

                }
            },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            scrimColor = Color.Black.copy(alpha = 0.5f), onDismissRequest = {
                onDismissRequest(false)
            }
        )
    }
}


class StarVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val maskedText = "*".repeat(text.length) // Fully mask input
        return TransformedText(AnnotatedString(maskedText), OffsetMapping.Identity)
    }
}


private const val PADDING_PERCENTAGE_OUTER_CIRCLE = 0.15f
private const val PADDING_PERCENTAGE_INNER_CIRCLE = 0.3f
private const val POSITION_START_OFFSET_OUTER_CIRCLE = 90f
private const val POSITION_START_OFFSET_INNER_CIRCLE = 135f

@Composable
fun TripleOrbitLoadingAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
        ),
    )
    var width by remember {
        mutableIntStateOf(0)
    }
    Box(
        modifier = modifier
            .size(40.dp)
            .onSizeChanged {
                width = it.width
            },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            strokeWidth = 2.dp,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = rotation
                }
        )
        CircularProgressIndicator(
            strokeWidth = 2.dp,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    with(LocalDensity.current) {
                        (width * PADDING_PERCENTAGE_INNER_CIRCLE).toDp()
                    }
                )
                .graphicsLayer {
                    rotationZ = rotation + POSITION_START_OFFSET_INNER_CIRCLE
                }
        )
        CircularProgressIndicator(
            strokeWidth = 2.dp,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    with(LocalDensity.current) {
                        (width * PADDING_PERCENTAGE_OUTER_CIRCLE).toDp()
                    }
                )
                .graphicsLayer {
                    rotationZ = rotation + POSITION_START_OFFSET_OUTER_CIRCLE
                }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GetAlertDialogue(onDismissRequest: () -> Unit = {}, content: @Composable () -> Unit) {
    BasicAlertDialog(
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(20.dp))
            .background(AppColors.backgroundColor)
            .padding(vertical = 30.dp, horizontal = 15.dp)
    ) {
        content()
    }
}


@Composable
inline fun GetOneTimeBlock(crossinline block: suspend CoroutineScope.() -> Unit) =
    LaunchedEffect(Unit) {
        block()
    }