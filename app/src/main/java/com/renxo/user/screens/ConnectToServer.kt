package com.renxo.user.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.renxo.user.R
import com.renxo.user.ui.theme.AppColors
import com.renxo.user.utils.LocalSnackBar
import com.renxo.user.utils.getTextFieldModifier
import com.renxo.user.utils.getTextFiledColors
import com.renxo.user.viewmodels.ConnectToServerVM

@Composable
fun ConnectToServer(
    viewModel: ConnectToServerVM = hiltViewModel(),
    navigate: () -> Unit,
) {
    val context = LocalContext.current
    val snackBarState = LocalSnackBar.current
    val focusManager = LocalFocusManager.current
    val connect = {
        if (viewModel.authServerUrl.isEmpty()) {
            snackBarState.showSnackBar(context.getString(R.string.ip_empty_error))
        } else if (viewModel.deviceId.isEmpty()) {
            snackBarState.showSnackBar(context.getString(R.string.device_id_empty_error))
        } else {
            viewModel.saveIP {
                navigate()
            }
        }
    }

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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
//                .align(Alignment.TopCenter)
                .padding(vertical = 32.dp, horizontal = 15.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = viewModel.authServerUrl,
                shape = RoundedCornerShape(15.dp),
                colors = getTextFiledColors(),
                onValueChange = { viewModel.authServerUrl = it.trim() },
                label = { Text(stringResource(id = R.string.enter_ip_address)) },
                placeholder = { Text("e.g., http://192.168.21.82:8082/") },
                modifier = Modifier
                    .getTextFieldModifier(),
//                    .onKeyEvent { event ->
//                        if (event.type == KeyEventType.KeyDown) {
//                            if (event.key == Key.Tab) {
//                                focusManager.moveFocus(FocusDirection.Down)
//                                true
//                            } else {
//                                false
//                            }
//                        } else {
//                            false
//                        }
//                    },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions =
                    KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                singleLine = true

            )
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = viewModel.deviceId,
                shape = RoundedCornerShape(15.dp),
                colors = getTextFiledColors(),
                onValueChange = { viewModel.deviceId = it.trim() },
                label = { Text(stringResource(id = R.string.enter_device_Id)) },
                placeholder = { Text(stringResource(R.string.e_g_deviceid)) },
                modifier = Modifier
                    .getTextFieldModifier(),
//                    .onKeyEvent { event ->
//                        if (event.type == KeyEventType.KeyDown) {
//                            if (event.key == Key.Enter) {
//                                connect()
//                                true
//                            } else {
//                                false
//                            }
//                        } else {
//                            false
//                        }
//                    },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { connect() }  // Also handle IME Done action
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    connect()
                }, modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.connect))
            }

            Spacer(modifier = Modifier.weight(1f))


        }


    }


}


