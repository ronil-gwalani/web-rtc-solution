@file:OptIn(ExperimentalMaterial3Api::class)

package com.renxo.user.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.renxo.user.R
import com.renxo.user.ui.theme.AppColors
import com.renxo.user.utils.GetOneTimeBlock
import com.renxo.user.utils.LocalSnackBar
import com.renxo.user.utils.getTextFieldModifier
import com.renxo.user.utils.getTextFiledColors
import com.renxo.user.utils.setLanguageChanges
import com.renxo.user.viewmodels.AuthVM
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@Composable
fun AuthenticateScreen(
    openConfigurePage: () -> Unit,   // TODO we have to remove this this is just for the developer so that we do not have to clear data whenever we have to change the IP
    viewModel: AuthVM = hiltViewModel(),
    languageChanged: () -> Unit,
    navigate: () -> Unit,
) {
    val snackBarState = LocalSnackBar.current
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    GetOneTimeBlock {
        viewModel.uiEventsFlow.collect { event ->
            when (event) {
                is AuthVM.FetchLanguageList -> {
                    viewModel.fetchLanguageList(context)
                }
            }
        }
    }

    GetOneTimeBlock {
        viewModel.errorMessage.collectLatest {
            viewModel.showCircularProgress = false
            it?.let { it1 -> snackBarState.showSnackBar(it1) }
        }
    }
    val view = LocalView.current
    val authenticate = {
        focusManager.clearFocus() // Hide keyboard when button is clicked
        if (viewModel.userId.isEmpty()) {
            snackBarState.showSnackBar(context.getString(R.string.user_id_empty_error))
        } else if (viewModel.password.isEmpty()) {
            snackBarState.showSnackBar(context.getString(R.string.enter_password))
            viewModel.passwordFocusRequester.requestFocus()
        } else {
            viewModel.authenticateUser {
                it?.let { it1 ->
                    setLanguageChanges(context, it1) { orientation ->
                        val window = (view.context as? Activity)?.window
                        window?.decorView?.layoutDirection = orientation
                    }
                }
                navigate()
            }
        }
        Unit
    }

    Scaffold(
        topBar = {
            AuthTopBar(viewModel, openConfigurePage, languageChanged)
        },
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
            .fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.backgroundColor)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UserIdField(
                value = viewModel.userId,
                onValueChange = { viewModel.userId = it },
                authenticate = authenticate,
                focusManager = focusManager
            )

            Spacer(modifier = Modifier.height(10.dp))

            PasswordField(
                viewModel.password,
                onValueChange = { viewModel.password = it },
                authenticate,
                viewModel.passwordFocusRequester
            )

            Spacer(modifier = Modifier.height(10.dp))

            AuthButton(
                showProgress = viewModel.showCircularProgress,
                authenticate = authenticate
            )


        }
    }
}


@Composable
private fun AppNameHeader(
    openConfigurePage: () -> Unit
) {
    Text(
        text = stringResource(id = R.string.app_name),
        style = MaterialTheme.typography.headlineMedium.copy(
            fontSize = 20.sp,
        ), color = AppColors.textColor,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 30.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { openConfigurePage() },
                )
            }
    )
}

@Composable
private fun UserIdField(
    value: String,
    onValueChange: (String) -> Unit,
    authenticate: () -> Unit,
    focusManager: FocusManager
) {
    TextField(
        value = value,
        shape = RoundedCornerShape(15.dp),
        colors = getTextFiledColors(),
        onValueChange = onValueChange,
        label = { Text(stringResource(id = R.string.enter_user_id)) },
        placeholder = { Text(stringResource(id = R.string.user_id)) },
        modifier = Modifier
            .getTextFieldModifier()
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    if (event.key == Key.Tab) {
                        focusManager.moveFocus(FocusDirection.Down)
                    } else if (event.key == Key.Enter) {
                        authenticate()
                    }
                    true
                } else {
                    false
                }

            },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
        singleLine = true
    )
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    authenticate: () -> Unit,
    passwordFocusRequester: FocusRequester
) {
    TextField(
        value = value,
        shape = RoundedCornerShape(15.dp),
        colors = getTextFiledColors(),
        onValueChange = onValueChange,
        label = { Text(stringResource(id = R.string.enter_password)) },
        placeholder = { Text(stringResource(id = R.string.enter_password)) },
        modifier = Modifier
            .getTextFieldModifier()
            .focusRequester(passwordFocusRequester)
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    if (event.key == Key.Enter) {
                        authenticate()
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            },
//        visualTransformation = StarVisualTransformation(),
        keyboardOptions = KeyboardOptions(
//            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { authenticate() }),
        singleLine = true
    )
}

@Composable
private fun AuthButton(
    showProgress: Boolean,
    authenticate: () -> Unit
) {
    if (showProgress) {
        CircularProgressIndicator(
            modifier = Modifier.wrapContentSize(),
            color = MaterialTheme.colorScheme.primary
        )
    } else {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(15.dp),
            onClick = authenticate,
        ) {
            Text(
                stringResource(id = R.string.authenticate),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun AuthTopBar(
    viewModel: AuthVM,
    openConfigurePage: () -> Unit,
    changedLanguage: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AppNameHeader(openConfigurePage = openConfigurePage)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.whiteColor),
        actions = {
            Box {
                IconButton(
                    onClick = { viewModel.languageExpanded = true },
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (viewModel.languageExpanded) MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.12f
                            )
                            else Color.Transparent
                        )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.language),
                        tint = Color.Unspecified,
                        contentDescription = stringResource(R.string.select_language),
                        modifier = Modifier.size(24.dp),
                    )
                }

                DropdownMenu(
                    expanded = viewModel.languageExpanded,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.background(AppColors.whiteColor),
                    onDismissRequest = { viewModel.languageExpanded = false }) {
                    viewModel.languageList.forEach { language ->
                        DropdownMenuItem(text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = language.value)
                                if (language == viewModel.selectedLanguage) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }, onClick = {
//                            viewModel.languageExpanded = false
//                            viewModel.selectedLanguage = language
                            viewModel.updateLanguage(language)
                            viewModel.viewModelScope.launch {
                                val languageChanged = viewModel.saveAllChanges()
                                if (languageChanged) {
                                    val langCode = viewModel.selectedLanguage.code
                                    setLanguageChanges(context, langCode) { orientation ->
                                        viewModel.languageExpanded = false
                                        val window = (view.context as? Activity)?.window
                                        window?.decorView?.layoutDirection = orientation
                                        changedLanguage()
                                    }
                                }
                            }
                        })
                    }
                }
            }
        }
    )
}