package com.renxo.user.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renxo.user.R
import com.renxo.user.ui.theme.AppColors
import com.renxo.user.utils.GetOneTimeBlock
import com.renxo.user.utils.LocalSnackBar
import com.renxo.user.utils.getTextFieldModifier
import com.renxo.user.utils.getTextFiledColors


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenameLpn(
    onDismissRequest: () -> Unit,
    onSubmit: (String, String) -> Unit
) {

    val renamerFocusRequester = remember { FocusRequester() }
    val newLpnFocusRequester = remember { FocusRequester() }
    var oldLpn by remember { mutableStateOf("") }
    var newLpn by remember { mutableStateOf("") }
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
                    text = stringResource(R.string.rename_lpn),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )

                TextField(
                    shape = RoundedCornerShape(15.dp),
                    value = oldLpn,
                    onValueChange = { oldLpn = it },
                    label = { Text(stringResource(R.string.enter_old_lpn)) },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.your_answer_here),
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    },
                    colors = getTextFiledColors(),
                    modifier = Modifier
                        .padding(10.dp)
                        .getTextFieldModifier()
                        .focusRequester(renamerFocusRequester)
                        .onKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown) {
                                if (event.key == Key.Tab) {
                                    newLpnFocusRequester.requestFocus()
                                    true
                                } else {
                                    false
                                }
                            } else {
                                false
                            }
                        }
                )
                GetOneTimeBlock {
                    renamerFocusRequester.requestFocus()
                }

                TextField(
                    shape = RoundedCornerShape(15.dp),
                    value = newLpn,
                    onValueChange = { newLpn = it },
                    label = { Text(stringResource(R.string.enter_new_lpn)) },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.your_answer_here),
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    },
                    colors = getTextFiledColors(),
                    modifier = Modifier
                        .padding(10.dp)
                        .getTextFieldModifier()
                        .focusRequester(newLpnFocusRequester)

                )

                Button(
                    onClick = {
                        if (newLpn.isEmpty()) {
                            snackBarState.showSnackBar(context.getString(R.string.enter_new_lpn))
                        } else if (oldLpn.isEmpty()) {
                            snackBarState.showSnackBar(context.getString(R.string.enter_old_lpn))
                        } else {
                            onSubmit(oldLpn.trim(), newLpn.trim())
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeOwnerShip(
    onDismissRequest: () -> Unit,
    onSubmit: (String, String, String) -> Unit
) {
    val lpnFocusRequester = remember { FocusRequester() }
    val ownerFocusRequester = remember { FocusRequester() }
    val newOwnerFocusRequester = remember { FocusRequester() }
    var lpn by remember { mutableStateOf("") }
    var oldOwner by remember { mutableStateOf("") }
    var newOwner by remember { mutableStateOf("") }
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
                    text = stringResource(R.string.change_ownership),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )

                TextField(
                    shape = RoundedCornerShape(15.dp),
                    value = lpn,
                    onValueChange = {
                        lpn = it
                    },
                    label = { Text(context.getString(R.string.enter_lpn)) },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.your_answer_here),
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    },
                    colors = getTextFiledColors(),
                    modifier = Modifier
                        .padding(10.dp)
                        .getTextFieldModifier()
                        .focusRequester(lpnFocusRequester)
                        .onKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown) {
                                if (event.key == Key.Tab) {
                                    ownerFocusRequester.requestFocus()
                                    true
                                } else {
                                    false
                                }
                            } else {
                                false
                            }
                        },
                )

                GetOneTimeBlock {
                    lpnFocusRequester.requestFocus()
                }


                TextField(
                    shape = RoundedCornerShape(15.dp),
                    value = oldOwner,
                    onValueChange = {
                        oldOwner = it
                    },
                    label = { Text(context.getString(R.string.enter_old_owner)) },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.your_answer_here),
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    },
                    colors = getTextFiledColors(),
                    modifier = Modifier
                        .padding(10.dp)
                        .getTextFieldModifier()
                        .focusRequester(ownerFocusRequester)
                        .onKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown) {
                                if (event.key == Key.Tab) {
                                    newOwnerFocusRequester.requestFocus()
                                    true
                                } else {
                                    false
                                }
                            } else {
                                false
                            }
                        },
                )


                TextField(
                    shape = RoundedCornerShape(15.dp),
                    value = newOwner,
                    onValueChange = { newOwner = it },
                    label = { Text(context.getString(R.string.enter_new_owner)) },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.your_answer_here),
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    },
                    colors = getTextFiledColors(),
                    modifier = Modifier
                        .padding(10.dp)
                        .getTextFieldModifier()
                        .focusRequester(newOwnerFocusRequester)

                )

                Button(
                    onClick = {
                        if (lpn.isEmpty()) {
                            snackBarState.showSnackBar(context.getString(R.string.enter_lpn))
                        } else if (newOwner.isEmpty()) {
                            snackBarState.showSnackBar(context.getString(R.string.enter_new_owner))
                        } else if (oldOwner.isEmpty()) {
                            snackBarState.showSnackBar(context.getString(R.string.enter_old_owner))
                        } else {
                            onSubmit(lpn.trim(), oldOwner.trim(), newOwner.trim())
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

