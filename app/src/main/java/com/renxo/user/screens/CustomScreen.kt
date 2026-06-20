package com.renxo.user.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.renxo.user.R
import com.renxo.user.dynamicUI.DynamicFormItem
import com.renxo.user.dynamicUI.InputType
import com.renxo.user.models.CustomData
import com.renxo.user.utils.GetOneTimeBlock
import com.renxo.user.utils.LocalHomeViewModelProvider
import com.renxo.user.utils.LocalSnackBar
import com.renxo.user.viewmodels.CustomScreenVM


@Composable
fun CustomScreen(data: CustomData, viewModel: CustomScreenVM = viewModel(), onExit: () -> Unit) {
    val context = LocalContext.current
    val snackBarState = LocalSnackBar.current
    val scope = rememberCoroutineScope()
    val closeFunction = { viewModel.requestCloseScreen() }
    BackHandler {
        closeFunction()
    }
    val homeVM = LocalHomeViewModelProvider.current
    DisposableEffect(Unit) {
        homeVM.onCloseFromCustomScreen = closeFunction
        onDispose {
            homeVM.onCloseFromCustomScreen = {}
        }
    }

    GetOneTimeBlock {
        viewModel.uiEventsFlow.collect { event ->
            when (event) {
//                is CustomScreenVM.GetDataForUi -> {
//                    viewModel.list.clear()
//                    viewModel.list.addAll(viewModel.getDataForUi(context, event.data))
//                }

                is CustomScreenVM.ShowSnackBarWithStatus -> {
                    snackBarState.showSnackBar(event.message)
                }

                is CustomScreenVM.AllowExitScreen -> {
                    if (event.allow) {
                        onExit()
                    }
                }

                is CustomScreenVM.ShowUi -> {
                    viewModel.initScreen(data.init_function)
                    viewModel.list.clear()
                    viewModel.list.addAll(viewModel.getDataForUi(context, data))
                }
            }

        }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        item {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center,
                text = stringResource(R.string.custom_screen1),
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 20.sp
                )
            )
        }
        itemsIndexed(viewModel.list) { index, item ->
            DynamicFormItem(item, onValueChange = {
                viewModel.updateInputValue(index, it)
            }, triggerAction = {
                viewModel.triggerAction(index)
            })
            if (item.type == InputType.BUTTON && index < viewModel.list.size - 1) {
                val nextItem = viewModel.list[index + 1]
                if (nextItem.type == InputType.BUTTON) {
                    Spacer(modifier = Modifier.height(16.dp))  // Adjust the height as needed.
                }
            }
        }
//        item {
//            Button(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 25.dp),
//                colors = basicButtonColors(),
//                onClick = {
//                    viewModel.list.forEach {
//                        Log.e("WorkFlow", ": ${it.value}")
//                        if (it.required && it.value == null) {
//                            viewModel.errorMessage = when (it.requirements) {
//                                is Requirements.DropDownRequirements -> it.requirements.error
//                                is Requirements.DateRequirements -> it.requirements.error
//                                is Requirements.EditTextRequirements -> it.requirements.error
//                                is Requirements.CheckBoxRequirements -> it.requirements.error
//                                is Requirements.MultiselectRequirements -> it.requirements.error
//                                is Requirements.RadioButtonRequirements -> it.requirements.error
//                                else -> context.getString(R.string.Fill_all_Fields)
//                            }
//                            viewModel.errorMessage?.let {
//                                snackBarState.showSnackBar(it)
//                            }
//
//                            return@Button
//                        }
//                    }
//
//                    // Prepare data for the WebSocket payload
//
//                    viewModel.submitData()
//
//
//                }
//            ) {
//                Text(
//                    text = context.getString(R.string.submit),
//                    modifier = Modifier.padding(5.dp),
//                    style = MaterialTheme.typography.bodyLarge.copy(
//                        color = MaterialTheme.colorScheme.onPrimary
//                    )
//                )
//            }
//            Spacer(Modifier.height(100.dp))
//        }
    }
}