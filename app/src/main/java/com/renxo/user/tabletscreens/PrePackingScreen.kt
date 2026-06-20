package com.renxo.user.tabletscreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renxo.user.R
import com.renxo.user.ui.theme.AppColors
import com.renxo.user.utils.GetOneTimeBlock
import com.renxo.user.utils.LocalSnackBar
import com.renxo.user.utils.ShowSnackBar
import com.renxo.user.utils.StarVisualTransformation
import com.renxo.user.utils.TripleOrbitLoadingAnimation
import com.renxo.user.utils.basicButtonColors
import com.renxo.user.utils.getTextFieldModifier
import com.renxo.user.utils.getTextFiledColors
import com.renxo.user.viewmodels.PrePackingVM

@Composable
fun PrePackingScreen(
    viewModel: PrePackingVM,
    navigate: (String) -> Unit,

    ) {
    val context = LocalContext.current
    val snackBarState = LocalSnackBar.current
    GetOneTimeBlock {
        viewModel.uiEventsFlow.collect { event ->
            when (event) {
                is PrePackingVM.Navigate -> {
                    viewModel.onCleared()
                    navigate(event.lpn)
                }

                is ShowSnackBar -> {
                    snackBarState.showSnackBar(context.getString(event.message))
                }
            }

        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.backgroundColor)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),

        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            TextField(
                shape = RoundedCornerShape(15.dp),
                colors = getTextFiledColors(),
                singleLine = true,
                visualTransformation = if (viewModel.showStars) StarVisualTransformation() else VisualTransformation.None, // Masks text
                label = { Text(stringResource(id = R.string.enter_the_value)) },
                trailingIcon = {
                    if (viewModel.lpnInput.isNotEmpty() && viewModel.showStars)
                        TripleOrbitLoadingAnimation(modifier = Modifier.size(25.dp))
                },
                placeholder = {
                    Text(
                        text = stringResource(R.string.your_answer_here),
                        fontSize = 16.sp,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },

                value = viewModel.lpnInput,
                onValueChange = {
                    viewModel.lpnInput = it
                    if (it.isEmpty()) {
                        viewModel.showStars = true
                    }
                },
                modifier = Modifier
                    .padding(bottom = 16.dp)

                    .getTextFieldModifier()
                    .focusRequester(viewModel.preparingFocusRequester)
                    .onKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown) {
                            when (event.key) {
                                Key.Tab -> {
                                    viewModel.decodeLpn()
                                    true
                                }

                                else -> false
                            }
                        } else false
                    },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
                ),

                keyboardActions = KeyboardActions(
                    onDone = {
                        viewModel.sendData()
                    }
                ),
            )
            GetOneTimeBlock {
                viewModel.preparingFocusRequester.requestFocus()
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                colors = basicButtonColors(),
//                    enabled = viewModel.lpnInput.isNotEmpty(),
                shape = RoundedCornerShape(15.dp),
                onClick = { viewModel.sendData() }
            ) {
                Text(
                    text = stringResource(id = R.string.submit),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}


