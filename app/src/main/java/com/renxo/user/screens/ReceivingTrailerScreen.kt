package com.renxo.user.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.renxo.user.R
import com.renxo.user.dynamicUI.DynamicFormItem
import com.renxo.user.dynamicUI.Requirements
import com.renxo.user.ui.theme.AppColors
import com.renxo.user.utils.GetOneTimeBlock
import com.renxo.user.utils.LocalSnackBar
import com.renxo.user.utils.StarVisualTransformation
import com.renxo.user.utils.basicButtonColors
import com.renxo.user.utils.getTextFieldModifier
import com.renxo.user.utils.getTextFiledColors
import com.renxo.user.viewmodels.ReceivingTrailerVM
import com.renxo.user.viewmodels.ReceivingTrailerVM.SelectionDialogueModel

@Composable
fun ReceivingTrailerScreen(
    initialParams: HashMap<String, String?>,
    viewModel: ReceivingTrailerVM = viewModel(),
    onRefresh: (HashMap<String, String?>) -> Unit,
    finish: () -> Unit,
) {

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val snackBarState = LocalSnackBar.current
    val keyboardController = LocalSoftwareKeyboardController.current

    BackHandler { finish() }

    GetOneTimeBlock {

        viewModel.uiEventsFlow.collect { event ->
            when (event) {
                is ReceivingTrailerVM.GetDataForUI -> {
                    viewModel.uiElements.addAll(viewModel.getDataForUI(context, event.list))
                }

                is ReceivingTrailerVM.HideKeyBoard -> {
                    keyboardController?.hide()
                }

                is ReceivingTrailerVM.OnRefresh -> {
                    onRefresh(event.params)
                }
            }
        }
    }


    GetOneTimeBlock {
        viewModel.updateInitialParams(initialParams)
        viewModel.trailerFocusRequester.requestFocus()
        keyboardController?.hide()
    }


    if (viewModel.selectionDialogue != null) {
        viewModel.selectionDialogue?.let {
            SelectionDialogue(it) { value ->
                viewModel.selectionDialogue?.let {
                    viewModel.initialParams[it.key] = value
                    viewModel.selectionDialogue = null
                    viewModel.fetchOtherFields()
                }
            }
        }

    }

    LazyColumn(
        modifier = Modifier
            .background(AppColors.backgroundColor)
            .padding(horizontal = 10.dp)
    ) {
        item {
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = stringResource(R.string.receiving_trailer),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 20.sp,
                ), color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(20.dp))
        }
        item {
            TextField(
                shape = RoundedCornerShape(15.dp),
                colors = getTextFiledColors(),
                singleLine = true,
                label = { Text(stringResource(id = R.string.enter_lpn_number)) },
                placeholder = {
                    Text(
                        text = stringResource(R.string.your_answer_here),
                        fontSize = 15.sp,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text, imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .getTextFieldModifier()
                    .focusRequester(viewModel.trailerFocusRequester)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            viewModel.fetchOtherFields()
                        }
                    }
                    .onKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown) {
                            when (event.key) {
                                Key.Tab -> {
                                    viewModel.decodeLpn()
                                    focusManager.moveFocus(FocusDirection.Down)
                                    true

                                }

                                else -> false
                            }
                        } else false
                    },
                value = viewModel.lpnNum,
                visualTransformation = if (viewModel.showLpnStar) StarVisualTransformation() else VisualTransformation.None, // Masks text

                onValueChange = {
                    viewModel.lpnNum = it
                    if (it.isEmpty()) {
                        viewModel.showLpnStar = true
                    }
                })
            Spacer(Modifier.height(10.dp))
        }
        item {
            TextField(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .getTextFieldModifier()
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            viewModel.fetchOtherFields()
                        }
                    }
                    .onKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown) {
                            when (event.key) {
                                Key.Tab -> {
                                    viewModel.decodeSku()
                                    focusManager.moveFocus(FocusDirection.Down)
                                    true
                                }

                                else -> false
                            }
                        } else false
                    },
                shape = RoundedCornerShape(15.dp),
                colors = getTextFiledColors(),
                singleLine = true,
                label = { Text(stringResource(id = R.string.enter_Item_Number)) },
                placeholder = {
                    Text(
                        text = stringResource(R.string.your_answer_here),
                        fontSize = 15.sp,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text, imeAction = ImeAction.Next
                ),
                visualTransformation = if (viewModel.showSkuStar) StarVisualTransformation() else VisualTransformation.None, // Masks text
                value = viewModel.skuNumber,
                onValueChange = {
                    viewModel.skuNumber = it
                    if (it.isEmpty()) {
                        viewModel.showSkuStar = true
                    }
                })
        }




        itemsIndexed(viewModel.uiElements, key = { index, item ->
            item.placeholder + index
        }) { index, item ->
            Box(modifier = Modifier.padding(horizontal = 10.dp)) {
                DynamicFormItem(item, onValueChange = {
                    viewModel.updateInputValue(index, it)
                })
            }
        }
        item {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 25.dp, horizontal = 10.dp),
                colors = basicButtonColors(),
                shape = RoundedCornerShape(15.dp),
                onClick = {

                    viewModel.uiElements.forEach {
                        if (it.required && (it.value == null || it.value.toString()
                                .isEmpty())
                        ) {
                            val message = when (it.requirements) {
                                is Requirements.DropDownRequirements -> it.requirements.error
                                is Requirements.DateRequirements -> it.requirements.error
                                is Requirements.EditTextRequirements -> it.requirements.error
                                is Requirements.CheckBoxRequirements -> it.requirements.error
                                else -> context.getString(R.string.Fill_all_Fields)
                            }
                            snackBarState.showSnackBar(message)
                            return@Button
                        }
                    }

                    viewModel.submitTraler()

                }) {
                Text(
                    text = context.getString(R.string.submit),
                    modifier = Modifier.padding(5.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(Modifier.height(100.dp))
        }

    }

}


@Composable
private fun SelectionDialogue(
    model: SelectionDialogueModel,
    onSubmit: (String) -> Unit

) {
    AlertDialog(
        onDismissRequest = { /* Do nothing to make it non-cancelable */ },
        title = {
            Text(
                text = stringResource(model.title),
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
                ) {
                    itemsIndexed(model.list, key = { index, item ->
                        item + index
                    }) { _, item ->
                        Text(
                            text = item,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSubmit(item) }
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            style = MaterialTheme.typography.bodyLarge)
                        if (item != model.list.last()) {
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

