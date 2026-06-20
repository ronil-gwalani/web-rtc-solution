package com.renxo.user.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.renxo.user.R
import com.renxo.user.ui.theme.AppColors
import com.renxo.user.utils.GetOneTimeBlock
import com.renxo.user.utils.LocalSnackBar
import com.renxo.user.utils.ShowSnackBar
import com.renxo.user.utils.basicButtonColors
import com.renxo.user.utils.getTextFieldModifier
import com.renxo.user.utils.getTextFiledColors
import com.renxo.user.viewmodels.IBDVM

@Composable
fun IBDScreen(
    viewModel: IBDVM = viewModel(),
    onSubmit: (String) -> Unit,
) {
    // Get context and snackbar state
    val context = LocalContext.current
    val snackBarState = LocalSnackBar.current
    val scrollState = rememberScrollState()

    // Remember modifiers to prevent recreation
    val columnModifier = remember {
        Modifier
            .fillMaxSize()
            .background(AppColors.backgroundColor)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(scrollState)
    }

    val operationRowModifier = remember {
        Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    }

    val dropdownModifier = remember {
        Modifier.width(200.dp)
    }

    val buttonModifier = remember {
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    }

    // Handle UI events in a side effect
    GetOneTimeBlock {

        viewModel.uiEventsFlow.collect {
            when (it) {
                is IBDVM.VerificationDone -> {
                    viewModel.clear()
                    onSubmit(it.params)
                }

                is ShowSnackBar -> {
                    snackBarState.showSnackBar(context.getString(it.message))
                }
            }
        }
    }

    // Remember the send function to avoid recreation
    val sendData = remember {
        { viewModel.sendData() }
    }

    Column(
        modifier = columnModifier,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Operation selection row
            OperationSelectionRow(
                modifier = operationRowModifier,
                dropdownModifier = dropdownModifier,
                viewModel = viewModel,
            )

            // Input TextField
            InputTextField(
                viewModel = viewModel,
                onEnterPressed = sendData
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Submit button
        SubmitButton(
            modifier = buttonModifier,
            viewModel = viewModel,
            onClick = sendData
        )
    }
}

@Composable
private fun OperationSelectionRow(
    modifier: Modifier = Modifier,
    dropdownModifier: Modifier = Modifier,
    viewModel: IBDVM
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.operation),
            modifier = Modifier.padding(end = 16.dp),
            style = MaterialTheme.typography.titleLarge,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        OperationDropdown(
            modifier = dropdownModifier,
            viewModel = viewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OperationDropdown(
    modifier: Modifier = Modifier,
    viewModel: IBDVM
) {

    ExposedDropdownMenuBox(
        expanded = viewModel.isExpanded,
        onExpandedChange = { viewModel.isExpanded = !viewModel.isExpanded },
        modifier = modifier
    ) {
        TextField(
            value = viewModel.selectedOperation?.let { stringResource(it) } ?: "",
            onValueChange = { },
            readOnly = true,
            placeholder = { Text(stringResource(id = R.string.select)) },
            modifier = Modifier
                .getTextFieldModifier()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            trailingIcon = {
                Icon(
                    imageVector = if (viewModel.isExpanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(id = R.string.toggle)
                )
            },
            colors = getTextFiledColors(),
            shape = RoundedCornerShape(15.dp),
        )

        ExposedDropdownMenu(
            expanded = viewModel.isExpanded,
            onDismissRequest = { viewModel.isExpanded = false }
        ) {
            viewModel.operationsListState.forEach { operation ->
                DropdownMenuItem(
                    text = { Text(stringResource(operation)) },
                    onClick = {
                        viewModel.selectedOperation = operation
                        viewModel.isExpanded = false
                        viewModel.ibdFocusRequester.requestFocus()
                    }
                )
            }
        }
    }
}

@Composable
private fun InputTextField(
    viewModel: IBDVM,
    onEnterPressed: () -> Unit
) {


    TextField(
        shape = RoundedCornerShape(15.dp),
        colors = getTextFiledColors(),
        singleLine = true,
        label = { Text(stringResource(id = R.string.enter_the_value)) },
        placeholder = {
            Text(
                text = stringResource(R.string.your_answer_here),
                fontSize = 16.sp,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        },
        modifier = Modifier
            .getTextFieldModifier()
            .focusRequester(viewModel.ibdFocusRequester)
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    if (event.key == Key.Enter) {
                        onEnterPressed()
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
        ),
        value = viewModel.lpnInput,
        onValueChange = { viewModel.lpnInput = it }
    )
}

@Composable
private fun SubmitButton(
    modifier: Modifier = Modifier,
    viewModel: IBDVM,
    onClick: () -> Unit
) {
    val enabled by remember { derivedStateOf { viewModel.lpnInput.isNotEmpty() } }
    Button(
        modifier = modifier,
        colors = basicButtonColors(),
        enabled = enabled,
        shape = RoundedCornerShape(15.dp),
        onClick = onClick
    ) {
        Text(
            text = stringResource(id = R.string.submit),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}