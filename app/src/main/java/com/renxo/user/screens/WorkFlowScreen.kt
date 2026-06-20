package com.renxo.user.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.renxo.user.R
import com.renxo.user.dynamicUI.DynamicFormItem
import com.renxo.user.dynamicUI.Requirements
import com.renxo.user.ui.theme.AppColors
import com.renxo.user.utils.GetOneTimeBlock
import com.renxo.user.utils.LocalSnackBar
import com.renxo.user.utils.basicButtonColors
import com.renxo.user.viewmodels.WorkFlowVM


@Composable
fun WorkFlowQuestions(
    viewModel: WorkFlowVM,
    finish: () -> Unit,
) {

    val snackBarState = LocalSnackBar.current
    val context = LocalContext.current




    GetOneTimeBlock {
        viewModel.uiEventsFlow.collect { event ->
            when (event) {
                is WorkFlowVM.Finish -> {
                    finish()
                }

                is WorkFlowVM.GetDataForUi -> {
                    viewModel.getData(context).let {
                        viewModel.list.addAll(it)
                    }
                }

            }
        }
    }

    AnimatedFullScreenDialog {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                snackbarHost = {
                    SnackbarHost(
                        snackBarState.hostState,
                        snackbar = {
                            Snackbar(
                                it,
                                containerColor = Color.Red,
                                contentColor = AppColors.whiteColor
                            )
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxSize(),
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(20.dp)
                ) {
                    item {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            textAlign = TextAlign.Center,
                            text = stringResource(R.string.work_flow),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.sp
                            )
                        )
                    }

                    itemsIndexed(viewModel.list) { index, item ->
                        DynamicFormItem(item, onValueChange = {
                            viewModel.updateInputValue(index, it)
                        })
                    }
                    item {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 25.dp),
                            colors = basicButtonColors(),
                            onClick = {
                                viewModel.list.forEach {
                                    if (it.required && it.value == null) {
                                        viewModel.errorMessage = when (it.requirements) {
                                            is Requirements.DropDownRequirements -> it.requirements.error
                                            is Requirements.DateRequirements -> it.requirements.error
                                            is Requirements.EditTextRequirements -> it.requirements.error
                                            is Requirements.CheckBoxRequirements -> it.requirements.error
                                            is Requirements.MultiselectRequirements -> it.requirements.error
                                            is Requirements.RadioButtonRequirements -> it.requirements.error
                                            else -> context.getString(R.string.Fill_all_Fields)
                                        }
                                        viewModel.errorMessage?.let { error ->
                                            snackBarState.showSnackBar(error)
                                        }

                                        return@Button
                                    }
                                }

                                // Prepare data for the WebSocket payload

                                viewModel.submitData()


                            }
                        ) {
                            Text(
                                text = context.getString(R.string.submit),
                                modifier = Modifier.padding(5.dp),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                        Spacer(Modifier.height(100.dp))
                    }
                }
            }


        }
    }
}


@Composable
private fun AnimatedFullScreenDialog(
    content: @Composable () -> Unit,
) {
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
        content()

    }
}



