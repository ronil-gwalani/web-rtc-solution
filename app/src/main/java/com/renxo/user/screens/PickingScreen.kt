package com.renxo.user.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.renxo.user.R
import com.renxo.user.dynamicUI.DynamicFormItem
import com.renxo.user.models.PickingPacks
import com.renxo.user.ui.theme.AppColors
import com.renxo.user.utils.GetOneTimeBlock
import com.renxo.user.utils.LocalSnackBar
import com.renxo.user.utils.TripleOrbitLoadingAnimation
import com.renxo.user.utils.getTextFieldModifier
import com.renxo.user.utils.getTextFiledColors
import com.renxo.user.utils.showToast
import com.renxo.user.viewmodels.PickingVM


@Composable
fun PickingScreen(
    viewModel: PickingVM,
    onComplete: () -> Unit,
) {
    val context = LocalContext.current
    val snackBarState = LocalSnackBar.current
    val keyboardController = LocalSoftwareKeyboardController.current

    GetOneTimeBlock {
        viewModel.uiEventsFlow.collect { event ->
            when (event) {
                is PickingVM.GetDataForUI -> {
                    viewModel.getDataForUI(
                        context,
                        event.list,
                        event.showHints
                    )

                }

                is PickingVM.HideKeyBoard -> {
                    keyboardController?.hide()
                }

                is PickingVM.OpenPickingScreen -> {
                    context.showToast(context.getString(R.string.next_task_found))
                    viewModel.setUpScreenForPicking(context, event.data, event.workflowData)
                }

                is PickingVM.OnComplete -> {
                    onComplete()
                }


                is PickingVM.ErrorMessage -> {
                    snackBarState.showSnackBar(context.getString(event.messageResId))
                }

                is PickingVM.ShowSnackBar -> {
                    snackBarState.showSnackBar(context.getString(event.message, event.placeHolder))
                }

                is PickingVM.SuccessMessage -> {
                    context.showToast(event.messageResId)
                }
            }
        }
    }

    // Create LPN dialog
    if (viewModel.showCreateLpnDialog) {
        CreateLpnDialog(
            onDismissRequest = { viewModel.showCreateLpnDialog = false },
            onGenerateLpn = { lpnValue -> viewModel.createNewLpn(lpnValue, false) },
            onPrintLpn = { lpnValue -> viewModel.createNewLpn(lpnValue, true) })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.backgroundColor)
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Spacer(Modifier.height(5.dp))

        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = stringResource(R.string.picking_task),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 20.sp,
            ), color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(5.dp))
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            item {
                FixFieldsLayout(
                    placeHolder = stringResource(R.string.location),
                    hint = viewModel.dataForPicking?.inv_src_id?.location ?: "",
                    value = viewModel.dataForPickingValues.location,
                    editable = viewModel.staticFieldsEditable,
                    onFocusChanged = {}) {
                    viewModel.dataForPickingValues =
                        viewModel.dataForPickingValues.copy(location = it)
                }
            }
            item {
                FixFieldsLayout(
                    placeHolder = stringResource(R.string.lpn_number),
                    hint = viewModel.dataForPicking?.inv_src_id?.lpn ?: "",
                    value = viewModel.dataForPickingValues.lpn,
                    editable = viewModel.staticFieldsEditable,
                    onFocusChanged = {}) {
                    viewModel.dataForPickingValues =
                        viewModel.dataForPickingValues.copy(lpn = it)
                }
            }
            item {
                FixFieldsLayout(
                    placeHolder = stringResource(R.string.sub_lpn),
                    hint = viewModel.dataForPicking?.inv_src_id?.sub_lpn ?: "",
                    value = viewModel.dataForPickingValues.subLpn,
                    editable = viewModel.staticFieldsEditable,
                    onFocusChanged = {}) {
                    viewModel.dataForPickingValues =
                        viewModel.dataForPickingValues.copy(subLpn = it)
                }
            }
            item {

                FixFieldsLayout(
                    placeHolder = stringResource(R.string.product_id),
                    hint = viewModel.dataForPicking?.product_id ?: "",
                    value = viewModel.dataForPickingValues.productId,
                    editable = viewModel.staticFieldsEditable,
                    onFocusChanged = {}) {
                    viewModel.dataForPickingValues =
                        viewModel.dataForPickingValues.copy(productId = it)
                }
            }
            item {
                Row(Modifier.fillMaxWidth()) {

                    FixFieldsLayout(
                        placeHolder = stringResource(R.string.quantity),
                        hint = viewModel.dataForPicking?.quantity.toString(),
                        value = viewModel.dataForPickingValues.quantity?.toString() ?: "",
                        editable = true,
                        mod = Modifier.weight(1f),
                        onFocusChanged = { focused ->
                            if (!focused && viewModel.partialSubmitAllowed) {
                                viewModel.doPartialSubmit(context)
                            }
                        }, keyboardType = KeyboardType.Number
                    ) {
                        viewModel.dataForPickingValues =
                            viewModel.dataForPickingValues.copy(quantity = it.toIntOrNull())
                    }
                    PackLayout(
                        modifier = Modifier.weight(1f),
                        placeHolder = stringResource(R.string.select_pack),
                        selectedItem = viewModel.dataForPickingValues.pack,
                        editable = true, options = viewModel.dataForPicking?.pack, onValueChange = {
                            viewModel.dataForPickingValues =
                                viewModel.dataForPickingValues.copy(pack = it)
                        })
                    FixFieldsLayout(
                        placeHolder = stringResource(R.string.quantity),
                        hint = viewModel.dataForPicking?.quantity.toString(),
                        value = viewModel.dataForPickingValues.quantity?.toString() ?: "",
                        editable = true,
                        mod = Modifier.weight(1f),
                        onFocusChanged = { focused ->
                            if (!focused && viewModel.partialSubmitAllowed) {
                                viewModel.doPartialSubmit(context)
                            }
                        }, keyboardType = KeyboardType.Number
                    ) {
                        viewModel.dataForPickingValues =
                            viewModel.dataForPickingValues.copy(quantity = it.toIntOrNull())
                    }
                }
            }

            itemsIndexed(viewModel.uiElements, key = { index, item ->
                item.placeholder + index
            }) { index, item ->
                Box(modifier = Modifier.padding(vertical = 1.dp)) {
                    DynamicFormItem(item, onValueChange = { newValue ->
                        viewModel.uiElements[index] = item.copy(value = newValue)
                    }, onFocusChanged = { focused ->
                        if (!focused && viewModel.partialSubmitAllowed && item == viewModel.uiElements.last()) {
                            viewModel.doPartialSubmit(context)
                        }
                    })
                }


            }

            // Submit Button - Only show if elements are loaded
            if (viewModel.showSubmitButton) {
                item {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(10.dp),
                                spotColor = Color(0x10000000)
                            )
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .border(
                                width = 1.dp,
                                color = Color(0xFFE6E6E6),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        TextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onKeyEvent { event ->
                                    if (event.type == KeyEventType.KeyDown) {
                                        when (event.key) {
                                            Key.Tab -> {
                                                keyboardController?.hide()
                                                viewModel.completeSubmit(context)
                                                true
                                            }

                                            else -> false
                                        }
                                    } else false
                                },
                            shape = RoundedCornerShape(8.dp),
                            colors = getTextFiledColors().copy(
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White
                            ),
                            onValueChange = { viewModel.submitLpnTxt = it },
                            value = viewModel.submitLpnTxt,
                            placeholder = {
                                Text(
                                    text = stringResource(id = R.string.scan_lpn),
                                    color = Color(0xFF95A5A6)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
                            ), trailingIcon = {
                                if (viewModel.submitLpnTxt.isNotEmpty())
                                    TripleOrbitLoadingAnimation(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable {
                                                viewModel.completeSubmit(context)
                                            }
                                    )

                            }
                        )
                    }

                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }
}


@Composable
private fun CreateLpnDialog(
    onDismissRequest: () -> Unit, onGenerateLpn: (String) -> Unit, onPrintLpn: (String) -> Unit,
) {
    var lpnValue by remember { mutableStateOf("") }

    AlertDialog(onDismissRequest = onDismissRequest, title = { Text("Create New LPN") }, text = {
        Column {
            Text(
                "Enter the new LPN identifier", modifier = Modifier.padding(bottom = 16.dp)
            )
            TextField(
                value = lpnValue,
                onValueChange = { lpnValue = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                placeholder = { Text("Enter LPN") },
                singleLine = true
            )
        }
    }, confirmButton = {
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { onGenerateLpn(lpnValue) },
                enabled = lpnValue.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.accentColor
                ),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("Generate LPN")
            }

            Button(
                onClick = { onPrintLpn(lpnValue) },
                enabled = lpnValue.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Print LPN")
            }
        }
    }, dismissButton = {
        Button(
            onClick = onDismissRequest, colors = ButtonDefaults.buttonColors(
                containerColor = Color.Gray
            )
        ) {
            Text("Cancel")
        }
    })


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackLayout(
    modifier: Modifier = Modifier,
    placeHolder: String,
    selectedItem: PickingPacks?,
    editable: Boolean,
//    expanded: Boolean,
    options: List<PickingPacks?>?,
    onValueChange: (PickingPacks?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = modifier.fillMaxWidth(),
        expanded = expanded && editable,
        onExpandedChange = {
            expanded = it
        }
    ) {
        TextField(
            value = selectedItem?.packsize ?: "",
            shape = RoundedCornerShape(15.dp),
            colors = getTextFiledColors(),
            onValueChange = { },
            readOnly = true,
            enabled = editable,
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null
                )
            },
            label = { Text(placeHolder) },
            modifier = Modifier
                .getTextFieldModifier()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )

        ExposedDropdownMenu(
            expanded = expanded && editable,
            onDismissRequest = { expanded = false }
        ) {
            // Header
            DropdownMenuItem(
                onClick = { },
                enabled = false,
                text = {
                    Text(
                        text = placeHolder,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            )

            // Options
            options?.forEach { item ->
                DropdownMenuItem(
                    onClick = {
                        onValueChange(item)
                        expanded = false
                    },
                    text = {
                        Text(text = item?.packsize ?: "")
                    }
                )
            }
        }
    }
}

@Composable
fun FixFieldsLayout(
    placeHolder: String,
    hint: String,
    value: String,
    editable: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    mod: Modifier = Modifier,
    onFocusChanged: (Boolean) -> Unit,
    onValueChange: (String) -> Unit,
) {
    var shouldTriggerAction by rememberSaveable { mutableStateOf<Boolean?>(null) }

    Column(mod.fillMaxWidth()) {
        Text(
            placeHolder, fontSize = 14.sp,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.textColor,
            modifier = Modifier.padding(start = 5.dp)
        )

        TextField(
            value = value,
            shape = RoundedCornerShape(15.dp),
            colors = getTextFiledColors().copy(
                unfocusedPlaceholderColor = Color.Gray.copy(alpha = 0.5f)
            ),
            onValueChange = { newValue ->
                if (editable) {
                    onValueChange(newValue)
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            enabled = editable,
            placeholder =
                {
                    Text(hint.replaceFirstChar { it.uppercase() })
                },
            modifier = Modifier
                .padding(vertical = 4.dp)
                .getTextFieldModifier()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        onFocusChanged(true)
                        shouldTriggerAction = true
                    }
                    if (!focusState.isFocused && shouldTriggerAction == true) {
                        onFocusChanged(false)
                        shouldTriggerAction = false
                    }
                },
        )
    }

}


@Composable
private fun LpnDropdownField(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    onCreateNewLpn: () -> Unit,
) {
    var showDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {


        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .getTextFieldModifier()
                .clickable(
                    onClick = { showDropdown = true }, indication = null, // No ripple effect
                    interactionSource = remember { MutableInteractionSource() }),
            shape = RoundedCornerShape(15.dp),
            colors = getTextFiledColors().copy(disabledTextColor = AppColors.textColor),
            enabled = false,
            readOnly = true,
            singleLine = true,
            value = value,
            onValueChange = { },
            label = { Text(label) },
            placeholder = { Text("Select or create new $label") },

            )

        if (showDropdown) {
            Dialog(onDismissRequest = { showDropdown = false }) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Select $label",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Add a "New" option
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showDropdown = false
                                    onCreateNewLpn()
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                                .background(
                                    color = AppColors.lightBlue, shape = RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)) {
                            Text(
                                text = "Create New $label",
                                color = AppColors.accentColor,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // List of existing LPNs
                        if (options.isEmpty()) {
                            Text(
                                text = "No existing LPNs available",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                textAlign = TextAlign.Center,
                                color = Color.Gray
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f, false)
                                    .heightIn(max = 300.dp)
                            ) {

                                itemsIndexed(options, key = { index, item ->
                                    item + index
                                }) { _, option ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onValueChange(option)
                                                showDropdown = false
                                            }
                                            .padding(vertical = 12.dp, horizontal = 8.dp)) {
                                        Text(
                                            text = option,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                    HorizontalDivider()
                                }
                            }
                        }

                        Button(
                            onClick = { showDropdown = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}