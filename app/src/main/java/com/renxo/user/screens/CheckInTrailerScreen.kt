package com.renxo.user.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.renxo.user.R
import com.renxo.user.ui.theme.AppColors
import com.renxo.user.utils.GetOneTimeBlock
import com.renxo.user.utils.LocalSnackBar
import com.renxo.user.utils.getTextFieldModifier
import com.renxo.user.utils.getTextFiledColors
import com.renxo.user.viewmodels.CheckInTrailerVM


@Composable
fun CheckInTrailerScreen(
    navigate: (questions: String, extraParams: String, transaction: String) -> Unit,
    onFinish: () -> Unit = {},
    viewModel: CheckInTrailerVM = viewModel(),
) {
    val snackBarState = LocalSnackBar.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val checkIn = {
        focusManager.clearFocus()
        viewModel.checkIn {
            snackBarState.showSnackBar(context.getString(it))
        }
    }


    GetOneTimeBlock {
        viewModel.uiEventsFlow.collect { event ->
            when (event) {
                is CheckInTrailerVM.Navigate -> {
                    navigate(event.questions, event.extraParams, event.transaction)
                }

                is CheckInTrailerVM.OnFinish -> {
                    onFinish()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 10.dp, horizontal = 25.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Trailer Number Field
        TrailerNumberField(
            viewModel
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Dock Number Field
        DockNumberField(
            viewModel,
            onEnter = checkIn,
        )

        Spacer(modifier = Modifier.height(15.dp))

        CheckInButton(onClick = checkIn)
    }
}

@Composable
private fun TrailerNumberField(
    viewModel: CheckInTrailerVM
) {
    val focusManager = LocalFocusManager.current

    TextField(
        value = viewModel.trailerNo,
        onValueChange = { viewModel.trailerNo = it },
        label = { Text(stringResource(id = R.string.trailer_no)) },
        placeholder = { Text(stringResource(id = R.string.enter_trailer_no)) },
        shape = RoundedCornerShape(15.dp),
        colors = getTextFiledColors(),
        modifier = Modifier
            .getTextFieldModifier()
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    if (event.key == Key.Tab) {
                        focusManager.moveFocus(FocusDirection.Down)
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
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Text
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }
        )
    )
}

@Composable
private fun DockNumberField(
    viewModel: CheckInTrailerVM,
    onEnter: () -> Unit
) {

    TextField(
        value = viewModel.dockNo,
        onValueChange = { viewModel.dockNo = it },
        label = { Text(stringResource(id = R.string.dock_no)) },
        placeholder = { Text(stringResource(id = R.string.enter_dock_no)) },
        shape = RoundedCornerShape(15.dp),
        colors = getTextFiledColors(),
        modifier = Modifier
            .getTextFieldModifier()
            .focusRequester(viewModel.dockFocusRequester)
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    if (event.key == Key.Enter) {
                        onEnter()
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            },
        singleLine = true,
    )
}

@Composable
private fun CheckInButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(15.dp)
    ) {
        Text(
            stringResource(id = R.string.check_in),
            style = MaterialTheme.typography.titleMedium
        )
    }
}


