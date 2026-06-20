package com.renxo.user.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.renxo.user.R
import com.renxo.user.ui.theme.AppColors
import com.renxo.user.utils.AppConstants
import com.renxo.user.utils.GetOneTimeBlock
import com.renxo.user.utils.LocalSnackBar
import com.renxo.user.utils.ShowSnackBar
import com.renxo.user.utils.basicButtonColors
import com.renxo.user.utils.getRequiredMessage
import com.renxo.user.utils.getTextFieldModifier
import com.renxo.user.utils.getTextFiledColors
import com.renxo.user.utils.rememberPagingHelper
import com.renxo.user.viewmodels.PalletBuildingVM
import com.renxo.user.viewmodels.PalletBuildingVM.UpdateSelectedAreaType
import kotlinx.coroutines.delay


@Composable
fun PalletBuildingScreen(
    selectedArea: String,
    updateSelectedAreaType: (String?) -> Unit,
    clearSelectedArea: () -> Unit,
    viewModel: PalletBuildingVM = viewModel(),
    navigate: () -> Unit,
) {
    val context = LocalContext.current
    val snackBarState = LocalSnackBar.current
    val focusManager = LocalFocusManager.current
    LaunchedEffect(selectedArea) {
        viewModel.updatedSelectedArea.value = selectedArea
    }

    val locationPagingHelper = rememberPagingHelper()
    locationPagingHelper.onLoadMore {
        viewModel.fetchLocations()
    }

    BackHandler {
        navigate()
    }


    GetOneTimeBlock {

        viewModel.uiEventsFlow.collect { event ->
            when (event) {
                is PalletBuildingVM.ClearSelectedArea -> {
                    clearSelectedArea()
                }

                is UpdateSelectedAreaType -> {
                    updateSelectedAreaType(event.type)
                    locationPagingHelper.reset()
                }

                is PalletBuildingVM.GetRequiredMessage -> {
                    context.getRequiredMessage(event.result)?.let { message ->
                        if (event.type == PalletBuildingVM.GetRequiredMessage.START_PALLET) {
                            viewModel.processingMessage = message
                            viewModel.showProcessingDialog = true
                        } else if (event.type == PalletBuildingVM.GetRequiredMessage.PROCESS_PALLET) {
                            viewModel.warningMessage = message
                            viewModel.showWarningDialog = true
                        }
                    }
                }

                is PalletBuildingVM.SetPagingParams -> {
                    locationPagingHelper.setPagingParams(
                        event.map
                    )
                }

                is PalletBuildingVM.GetPagingParams -> {
                    locationPagingHelper.getPagingParams(event.map).let {
                        event.resultCallback(event.map)
                    }

                }

                is PalletBuildingVM.Navigate -> {
                    navigate()
                }

                is ShowSnackBar -> {
                    snackBarState.showSnackBar(context.getString(event.message))
                }

            }
        }
    }

    InitDialogues(viewModel, locationPagingHelper.listState, updateSelectedAreaType)

    Column(
        modifier = Modifier
            .fillMaxSize()  // Make sure Column takes full screen
            .background(AppColors.backgroundColor)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(20.dp))

        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = (stringResource(id = R.string.pallet_building)),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 20.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(20.dp))


        // lpn Number Input

        TextField(
            value = viewModel.lpnNumber,
            onValueChange = { viewModel.lpnNumber = it },
            label = { Text(stringResource(id = R.string.box_id)) },
            enabled = selectedArea.isNotEmpty(),
            shape = RoundedCornerShape(15.dp),
            colors = getTextFiledColors(),
            modifier = Modifier
                .clickable {
                    if (selectedArea.isEmpty()) {
                        viewModel.processingMessage =
                            context.getString(R.string.please_select_an_area)
                        viewModel.showProcessingDialog = true
                    }
                }
                .getTextFieldModifier()
                .focusRequester(viewModel.palletFocusRequester)
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown) {

                        if (event.key == Key.Enter) {
                            viewModel.fetchLPNs()
                            focusManager.clearFocus()
                            true
                        } else {
                            false
                        }
                    } else {
                        false
                    }
                },
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = {
                    viewModel.fetchLPNs()

                }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = (stringResource(id = R.string.search)),
                        tint = MaterialTheme.colorScheme.primary

                    )
                }
            })

        Spacer(Modifier.height(10.dp))

        // Show additional UI only after getting response
        if (viewModel.showAdditionalUI) {
            // Horizontal arrangement for translation_id and dropdown
            SuggestedLpnSection(
                suggestedLpns = viewModel.suggestedLpns,
                viewModel,
                navigate = navigate,
            )

            Spacer(Modifier.height(20.dp))

            // Preferred lpn Input

            if (viewModel.showAdditionalUI) {
                TextField(
                    value = viewModel.preferredLpn,
                    onValueChange = { viewModel.preferredLpn = it },
                    label = { Text(stringResource(id = R.string.pallet_id)) },
                    shape = RoundedCornerShape(15.dp),
                    colors = getTextFiledColors(),
                    modifier = Modifier
                        .getTextFieldModifier()
                        .focusRequester(viewModel.palletFocusRequester)
                        .onKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown) {
                                if (event.key == Key.Enter) {
                                    viewModel.submitPallet()
                                    true
                                } else {
                                    false
                                }
                            } else {
                                false
                            }
                        },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
                    ),

                    )


                GetOneTimeBlock {
                    delay(100) // Add slight delay to stabilize UI
                    viewModel.palletFocusRequester.requestFocus()
                }

                Spacer(Modifier.height(20.dp))

                // Submit Button
                Button(
                    onClick = {
                        viewModel.submitPallet()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = basicButtonColors(),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Text(stringResource(id = R.string.submit))
                }
            }
        }
    }
}

@Composable
fun InitDialogues(
    viewModel: PalletBuildingVM,
    listState: LazyListState,
    updateSelectedAreaType: (String) -> Unit
) {
    if (viewModel.showWarningDialog) {
        WarningDialog(
            message = viewModel.warningMessage,
            onYes = {
                viewModel.showWarningDialog = false
                // Logic for Yes button
                viewModel.submitPallet(updateDuplicate = true)
            },
            onNo = {
                viewModel.showWarningDialog = false
                // Logic for No button
                viewModel.preferredLpn = ""
            }
        )
    }

    if (viewModel.showLocationDialog) {
        LocationSelectionDialog(
            locations = viewModel.locations,
            listState,
            onLocationSelected = { location ->
                viewModel.selectedLocation = location
                viewModel.showLocationDialog = false
            })
    }


    if (viewModel.showProcessingDialog == true) {
        CustomAlertDialog(
            title = stringResource(R.string.warning),
            message = viewModel.processingMessage,
            onDismiss = {
                viewModel.showProcessingDialog = false
            },
            onConfirm = {
                viewModel.showProcessingDialog = false
                updateSelectedAreaType(AppConstants.Type.PROCESSING)
            })
    }


}

@Composable
private fun SuggestedLpnSection(
    suggestedLpns: List<String>,
    viewModel: PalletBuildingVM,
    navigate: () -> Unit,
) {
    val gridState = rememberLazyGridState()

    when {
        suggestedLpns.isNotEmpty() ->
            Column {
                Text(
                    text = (stringResource(id = R.string.suggested_LPNs)),
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                )
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp, horizontal = 10.dp)
                        .background(AppColors.whiteColor)
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    LazyVerticalGrid(
                        userScrollEnabled = true,
                        columns = GridCells.Fixed(2),
                        state = gridState,

                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        itemsIndexed(suggestedLpns, key = { index, item ->
                            item + index
                        }) { _, item ->
                            Text(text = item, Modifier.padding(horizontal = 10.dp, vertical = 2.dp))
                        }
                    }
                    GridScrollBar(gridState)

                }
            }

        else -> CreateOwnLpnSection(viewModel, navigate)
    }
}

@Composable
private fun CreateOwnLpnSection(
    viewModel: PalletBuildingVM,
    navigate: () -> Unit,
) {
    GetOneTimeBlock {
        viewModel.showCreateLpnDialog = true
    }

    if (viewModel.showCreateLpnDialog) {
        CustomAlertDialog(
            title = (stringResource(id = R.string.warning)),
            message = (stringResource(id = R.string.do_you_want_to_create_this_LPN)),
            onDismiss = { viewModel.showCreateLpnDialog = false },
            onConfirm = {
                viewModel.showCreateLpnDialog = false
                viewModel.lpnCreateRequest()  // Send request on confirm
            })
    }
}

@Composable
private fun LocationSelectionDialog(
    locations: List<String>,
    state: LazyListState,
    onLocationSelected: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { /* Do nothing to make it non-cancelable */ },
        title = {
            Text(
                text = stringResource(R.string.select_location),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }, text = {
            Box(
                modifier = Modifier
                    .height(300.dp)  // Fixed height for the dialog content
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = state
                ) {
                    itemsIndexed(locations, key = { index, item ->
                        item + index
                    }) { _, location ->
                        Text(
                            text = location,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLocationSelected(location) }
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            style = MaterialTheme.typography.bodyLarge)
                        if (location != locations.last()) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }, confirmButton = { }, properties = DialogProperties(
            dismissOnBackPress = false, dismissOnClickOutside = false
        )
    )
}

@Composable
fun GridScrollBar(
    lazyGridState: LazyGridState,
    color: Color = Color.LightGray,
    width: Int = 10,
) {
    val height by remember(lazyGridState) {
        derivedStateOf {
            val gridHeight = lazyGridState.layoutInfo.viewportSize.height
            val totalItems = lazyGridState.layoutInfo.totalItemsCount.takeIf { it > 0 } ?: 1
            val visibleItemCount =
                lazyGridState.layoutInfo.visibleItemsInfo.size.takeIf { it > 0 } ?: 1

            if (visibleItemCount == totalItems) 0f // No scrollbar if no scrolling is needed
            else visibleItemCount * (gridHeight.toFloat() / totalItems)
        }
    }

    val topOffset by remember(lazyGridState) {
        derivedStateOf {
            val gridHeight = lazyGridState.layoutInfo.viewportSize.height
            val totalItems = lazyGridState.layoutInfo.totalItemsCount.takeIf { it > 0 } ?: 1
            val firstVisibleIndex = lazyGridState.firstVisibleItemIndex

            val scrollItemHeight = (gridHeight.toFloat() / totalItems)
            val offset = (firstVisibleIndex) * scrollItemHeight
            offset
        }
    }

    val gridSize by remember(lazyGridState) {
        derivedStateOf {
            lazyGridState.layoutInfo.viewportSize
        }
    }
    // Draw scrollbar only if height is greater than 0
    if (height > 0) {
        Canvas(
            modifier = Modifier.size(width = gridSize.width.dp, height = gridSize.height.dp),
            onDraw = {
                drawRect(
                    color = color,
                    topLeft = Offset(this.size.width - width, topOffset),
                    size = Size(width.toFloat(), height),
                )
            }
        )
    }
}


@Composable
private fun CustomAlertDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon based on dialog type
                Icon(
                    imageVector = when (title) {
                        stringResource(id = R.string.warning) -> Icons.Rounded.Warning
                        stringResource(id = R.string.success) -> Icons.Rounded.CheckCircle
                        stringResource(id = R.string.error) -> Icons.Rounded.Close
                        else -> Icons.Rounded.Info
                    },
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(bottom = 8.dp),
                    tint = when (title) {
                        stringResource(id = R.string.warning) -> MaterialTheme.colorScheme.error
                        stringResource(id = R.string.success) -> MaterialTheme.colorScheme.tertiary
                        stringResource(id = R.string.error) -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    }
                )

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = when (title) {
                    stringResource(id = R.string.warning), stringResource(id = R.string.attention) -> Arrangement.SpaceEvenly
                    else -> Arrangement.Center
                }
            ) {
                when (title) {
                    stringResource(id = R.string.warning), stringResource(id = R.string.attention) -> {
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                stringResource(id = R.string.okay_text),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                    stringResource(id = R.string.success) -> {
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = basicButtonColors(),
                            shape = RoundedCornerShape(15.dp)
                        ) {
                            Text(
                                stringResource(id = R.string.okay_text),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                    stringResource(id = R.string.error) -> {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(0.8f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEF5350)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                stringResource(id = R.string.cancel),
//                                modifier = Modifier.padding(vertical = 4.dp)

                            )
                        }
                    }
                }
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )


    )
}

@Composable
private fun WarningDialog(
    title: String = stringResource(R.string.warning),
    message: String,
    onYes: () -> Unit,
    onNo: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Warning Icon
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(bottom = 8.dp),
                    tint = MaterialTheme.colorScheme.error
                )

                // Dialog Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Yes Button
                Button(
                    onClick = onYes,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        stringResource(id = R.string.yes),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // No Button
                Button(
                    onClick = onNo,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        stringResource(id = R.string.no),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    )
}

