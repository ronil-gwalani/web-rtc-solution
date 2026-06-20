package com.renxo.user.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.renxo.user.R
import com.renxo.user.models.DataForPicking
import com.renxo.user.models.PickingAttribute
import com.renxo.user.models.TaskInfo
import com.renxo.user.ui.theme.AppColors
import com.renxo.user.utils.GetOneTimeBlock
import com.renxo.user.utils.LocalHomeViewModelProvider
import com.renxo.user.utils.LocalSnackBar
import com.renxo.user.utils.TripleOrbitLoadingAnimation
import com.renxo.user.utils.getTextFieldModifier
import com.renxo.user.utils.getTextFiledColors
import com.renxo.user.viewmodels.MainPageVM

@Composable
fun MainScreen(
    viewModel: MainPageVM = viewModel(),
    openMenu: () -> Unit,
    startDeposit: () -> Unit,
    startPicking: (DataForPicking, List<PickingAttribute>?, List<TaskInfo>?) -> Unit,
) {
    val homeVM = LocalHomeViewModelProvider.current
    val snackBarState = LocalSnackBar.current
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    GetOneTimeBlock {
        keyboardController?.hide()
    }
    GetOneTimeBlock {
        viewModel.uiEventsFlow.collect { event ->
            when (event) {
                is MainPageVM.UpdateLocation -> {
                    homeVM.selectedArea = event.location
                }

                is MainPageVM.UpdateTasksList -> {
                    homeVM.tasksList.clear()
                    homeVM.tasksList.addAll(event.list)
                }

                is MainPageVM.OpenPickingScreen -> {
                    startPicking(event.data, event.workflowData, event.taskListId)
                }
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Scan field with subtle shadow

        ScanSection(viewModel = viewModel)


        Spacer(modifier = Modifier.height(32.dp))

        // Main buttons grid with 2x2 layout
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // First row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Menu Button
                AppButton(
                    text = stringResource(R.string.show_menu),
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color(0xFFE9EFF7),
                    borderColor = Color(0xFFD8E2ED),
                    contentColor = Color(0xFF2C3E50),
                    iconContent = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .width(20.dp)
                                    .height(2.dp)
                                    .background(Color(0xFF2C3E50))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .width(20.dp)
                                    .height(2.dp)
                                    .background(Color(0xFF2C3E50))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .width(20.dp)
                                    .height(2.dp)
                                    .background(Color(0xFF2C3E50))
                            )
                        }
                    },
                    onClick = { openMenu() }
                )

                // Find Work Button
                AppButton(
                    text = stringResource(id = R.string.find_work),
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color(0xFFE6F5F3),
                    borderColor = Color(0xFFD7EBE9),
                    contentColor = Color(0xFF16A085),
                    iconContent = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = Color(0xFF16A085),
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    onClick = { homeVM.findWork() }
                )
            }

            // Second row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Deposit Button
                AppButton(
                    text = stringResource(R.string.deposit_screen),
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color(0xFFF2E6F5),
                    borderColor = Color(0xFFE8D7EB),
                    contentColor = Color(0xFF8E44AD),
                    iconContent = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = null,
                            tint = Color(0xFF8E44AD),
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    onClick = {
                        if (homeVM.selectedArea.isEmpty()) {
                            snackBarState.showSnackBar(context.getString(R.string.please_select_an_area))
                            return@AppButton
                        }
                        startDeposit()
                    }
                )

                // Start Work Button
                AppButton(
                    text = stringResource(id = R.string.start_work),
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color(0xFFE6F0F7),
                    borderColor = Color(0xFFD7E5F0),
                    contentColor = Color(0xFF2980B9),
                    iconContent = {
                        Icon(
                            painter = painterResource(R.drawable.startwork),
                            tint = Color.Unspecified,
                            contentDescription = stringResource(R.string.select_equipment),
                            modifier = Modifier.size(24.dp),
                        )
                    },
                    onClick = {
                        viewModel.startWork()
                    }
                )
            }
        }
    }

    if (viewModel.showStartWorkDialogue) {
        ScanLocationDialog(onDismissRequest = {
            viewModel.showStartWorkDialogue = false
        }, onSubmit = {
            viewModel.showStartWorkDialogue = false
            viewModel.startWork()
        })
    }
}

@Composable
private fun AppButton(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    borderColor: Color,
    contentColor: Color,
    iconContent: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(110.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(10.dp),
                spotColor = Color(0x08000000)
            )
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                iconContent()
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = text,
                color = contentColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ScanSection(
    viewModel: MainPageVM,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(viewModel.focusRequester)
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown) {
                        when (event.key) {
                            Key.Tab -> {
                                keyboardController?.hide()
                                viewModel.decodeLocation()
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
            onValueChange = { viewModel.scanForWork = it },
            value = viewModel.scanForWork,
            placeholder = {
                Text(
                    text = stringResource(id = R.string.scan_here_for_work),
                    color = Color(0xFF95A5A6)
                )
            },
            trailingIcon = {
                if (viewModel.scanForWork.isNotEmpty())
                    TripleOrbitLoadingAnimation(modifier = Modifier.size(25.dp))
            },
//        placeholder = {
//            Text(
//                text = stringResource(R.string.scan_here),
//                fontSize = 16.sp,
//                maxLines = 1,
//                color = Color(0xFF2C3E50)
//            )
//        },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
            )
        )
    }
    GetOneTimeBlock {
        viewModel.focusRequester.requestFocus()
        keyboardController?.hide()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanLocationDialog(
    onDismissRequest: () -> Unit,
    onSubmit: (String) -> Unit
) {
    val renamerFocusRequester = remember { FocusRequester() }
    var location by remember { mutableStateOf("") }
    val snackBarState = LocalSnackBar.current
    val context = LocalContext.current
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        content = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(20.dp))
                    .background(AppColors.backgroundColor)
                    .padding(vertical = 30.dp, horizontal = 15.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.alert),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )

                TextField(
                    shape = RoundedCornerShape(15.dp),
                    value = location,
                    onValueChange = { location = it },
                    label = { Text(stringResource(R.string.scan_location)) },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.scan_location_here),
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    },
                    singleLine = true,
                    colors = getTextFiledColors(),
                    modifier = Modifier
                        .padding(10.dp)
                        .getTextFieldModifier()
                        .focusRequester(renamerFocusRequester)
                        .onKeyEvent { keyEvent ->
                            if (keyEvent.type == KeyEventType.KeyDown) {
                                keyEvent.key == Key.Tab
                            } else {
                                false
                            }
                        }
                )
                GetOneTimeBlock {
                    renamerFocusRequester.requestFocus()
                }

                Button(
                    onClick = {
                        if (location.isEmpty()) {
                            snackBarState.showSnackBar(context.getString(R.string.scan_location))
                        } else {
                            onSubmit(location)
                        }
                    }, Modifier.padding(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.submit)
                    )
                }
            }
        }
    )
}