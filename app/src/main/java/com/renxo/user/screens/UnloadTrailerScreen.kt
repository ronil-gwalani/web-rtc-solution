package com.renxo.user.screens

import android.content.Context
import androidx.annotation.StringRes
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.renxo.user.R
import com.renxo.user.models.UnloadDeliveryResult
import com.renxo.user.ui.theme.AppColors
import com.renxo.user.utils.CustomDropdownMenu
import com.renxo.user.utils.GetOneTimeBlock
import com.renxo.user.utils.LocalSnackBar
import com.renxo.user.utils.PagingHelper
import com.renxo.user.utils.ShackBarState
import com.renxo.user.utils.ShowSnackBar
import com.renxo.user.utils.getTextFieldModifier
import com.renxo.user.utils.getTextFiledColors
import com.renxo.user.utils.rememberPagingHelper
import com.renxo.user.viewmodels.UnloadTrailerVM

@Composable
fun UnloadTrailerScreen(
    viewmodel: UnloadTrailerVM = viewModel(),
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val snackBarState = LocalSnackBar.current
    val locationPagingHelper = rememberPagingHelper()
    locationPagingHelper.onLoadMore {
        viewmodel.isLocationCalled = false
        viewmodel.getLocations()
    }

    GetOneTimeBlock {
        viewmodel.uiEventsFlow.collect { event ->
            when (event) {
                is ShowSnackBar -> {
                    snackBarState.showSnackBar(context.getString(event.message))
                }

                is UnloadTrailerVM.GetPagingParams -> {
                    locationPagingHelper.getPagingParams(event.map).let {
                        event.resultCallback(event.map)
                    }
                }

                is UnloadTrailerVM.SetPagingParams -> {
                    locationPagingHelper.setPagingParams(event.map)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeaderSection()

        Spacer(modifier = Modifier.height(16.dp))

        LocationDropdown(
            locationPagingHelper = locationPagingHelper,
            viewmodel = viewmodel
        )

        Spacer(modifier = Modifier.height(16.dp))

        TrailerNumberInput(viewmodel = viewmodel)

        Spacer(modifier = Modifier.height(16.dp))

        ValidateButton(viewmodel = viewmodel)

        Spacer(modifier = Modifier.height(16.dp))

        ResultDisplay(viewmodel)

        Spacer(modifier = Modifier.height(16.dp))

        SubmitButton(
            viewmodel = viewmodel,
            snackBarState = snackBarState,
            context = context
        )
    }
}

@Composable
private fun HeaderSection() {
    Text(
        modifier = Modifier.padding(top = 16.dp),
        text = stringResource(R.string.unload_trailer),
        style = MaterialTheme.typography.headlineLarge.copy(
            fontSize = 20.sp

        ),
        color = AppColors.textColor
    )
    Spacer(Modifier.height(10.dp))
    Text(
        text = stringResource(R.string.receive_stage),
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = 16.sp

        ),
        modifier = Modifier.padding(bottom = 8.dp),
        color = AppColors.textColor
    )
}

@Composable
private fun LocationDropdown(
    locationPagingHelper: PagingHelper,
    viewmodel: UnloadTrailerVM
) {
    CustomDropdownMenu(
        hint = stringResource(R.string.staging_areas),
        value = viewmodel.selectedLocation ?: stringResource(R.string.staging_areas),
        showBottomSheet = viewmodel.expanded,
        state = locationPagingHelper.listState,
        list = viewmodel.locationNames,
        onItemSelected = {
            viewmodel.selectedLocation = it
            viewmodel.expanded = false
            viewmodel.unloadTrailerFocusRequester.requestFocus()
        },
        onDismissRequest = {
            viewmodel.expanded = it
        }
    )
}

@Composable
private fun TrailerNumberInput(viewmodel: UnloadTrailerVM) {
    TrailerTextField(
        value = viewmodel.trailerNo,
        onValueChange = { viewmodel.trailerNo = it },
        onValidate = { viewmodel.validate() },
        focusRequester = viewmodel.unloadTrailerFocusRequester
    )
}

@Composable
private fun ValidateButton(viewmodel: UnloadTrailerVM) {
    ActionButton(
        text = stringResource(R.string.validate),
        onClick = { viewmodel.validate() }
    )
}

@Composable
private fun ResultDisplay(viewmodel: UnloadTrailerVM) {
    when (viewmodel.uiState) {
        is UnloadTrailerVM.UiState.Idle -> {
            // No data to display
        }

        is UnloadTrailerVM.UiState.Loading -> {
            LoadingIndicator()
        }

        is UnloadTrailerVM.UiState.Success -> {
            (viewmodel.uiState as? UnloadTrailerVM.UiState.Success)?.data?.let { ResultsList(results = it) }
        }

        is UnloadTrailerVM.UiState.Error -> {
            (viewmodel.uiState as? UnloadTrailerVM.UiState.Error)?.message?.let {
                ErrorMessage(
                    messageResId = it
                )
            }
        }
    }
}

@Composable
private fun SubmitButton(
    viewmodel: UnloadTrailerVM,
    snackBarState: ShackBarState,
    context: Context
) {
    ActionButton(
        text = stringResource(R.string.submit),
        onClick = {
            if (viewmodel.selectedLocation != null && viewmodel.trailerNo.isNotEmpty()) {
                viewmodel.unloadTrailer()
            } else {
                snackBarState.showSnackBar(context.getString(R.string.please_fill_all_the_fields))
            }
        }
    )
}

// Extracted reusable composable with focused functionality

@Composable
private fun TrailerTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onValidate: () -> Unit,
    focusRequester: FocusRequester
) {
    val focusManager = LocalFocusManager.current

    TextField(
        shape = RoundedCornerShape(15.dp),
        colors = getTextFiledColors(),
        value = value,
        label = { Text(stringResource(id = R.string.enter_trailer_no)) },
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = stringResource(R.string.your_answer_here),
                fontSize = 15.sp,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        },
        modifier = Modifier
            .getTextFieldModifier()
            .focusRequester(focusRequester)
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    if (event.key == Key.Enter) {
                        onValidate()
                        focusManager.clearFocus()
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next
        ),
        singleLine = true
    )
}

@Composable
private fun ActionButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun LoadingIndicator() {
    CircularProgressIndicator(
        modifier = Modifier.padding(16.dp),
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun ResultsList(results: List<UnloadDeliveryResult>) {
    if (results.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(
                    AppColors.whiteColor,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            results.forEach { item ->
                ResultItem(item = item)

                if (results.last() != item) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultItem(item: UnloadDeliveryResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .padding(horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item.ibd?.let {
            Text(
                text = stringResource(R.string.ibd, it),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black
            )
        }
        item.transport_equipment?.let {
            Text(
                text = stringResource(R.string.transport_equipment, it),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
        }
        item.supplier?.let {
            Text(
                text = stringResource(R.string.supplier, it),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
        }
        item.location?.let {
            Text(
                text = stringResource(R.string.location_, it),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
        }
    }
}

@Composable
private fun ErrorMessage(@StringRes messageResId: Int) {
    Text(
        text = stringResource(id = messageResId),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
    )
}