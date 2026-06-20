package com.renxo.user.dynamicUI.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.renxo.user.R
import com.renxo.user.dynamicUI.InputData
import com.renxo.user.dynamicUI.Requirements
import com.renxo.user.ui.theme.AppColors
import com.renxo.user.utils.getTextFieldModifier
import com.renxo.user.utils.getTextFiledColors

@Composable
fun MultipleScanningComponent(
    inputData: InputData,
    onValueChange: (Any?) -> Unit,
) {
    val requirements = inputData.requirements as Requirements.MultipleScanningRequirements

    val serialNumbers = remember { mutableStateListOf<String>() }
    var currentSerial by remember { mutableStateOf("") }
    var showSerialsList by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val serialFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    // Refresh the component when requiredCount changes
    val actualRequiredCount by remember(requirements.requiredCount) {
        mutableIntStateOf(if (requirements.requiredCount > 0) requirements.requiredCount else 1)
    }

    fun addSerialNumber(serial: String) {
        if (serialNumbers.size < actualRequiredCount) {
            if (serial.isNotEmpty() && !serialNumbers.contains(serial)) {
                serialNumbers.add(serial)
                onValueChange(serialNumbers.toList())
            }
        } else {
//            onError("You Cannot Enter More Than $actualRequiredCount Serial Numbers")
        }
    }

    // Initialize with any existing values
    LaunchedEffect(requirements.defaultValue) {
        if (serialNumbers.size != requirements.defaultValue.size) {
            serialNumbers.clear()
            serialNumbers.addAll(requirements.defaultValue)
        }
    }

    // Auto-complete if required count is met
    LaunchedEffect(serialNumbers.size, actualRequiredCount) {
        if (serialNumbers.size >= actualRequiredCount) {
            // Move focus to next field if we've reached the required count
            keyboardController?.hide()
            focusManager.moveFocus(FocusDirection.Down)

//            onComplete()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = inputData.placeholder,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Serial number counter
        if (requirements.requiredCount > 0) {
            Text(
                text = "${serialNumbers.size} out of $actualRequiredCount",
                style = MaterialTheme.typography.bodyMedium,
                color = if (serialNumbers.size >= actualRequiredCount) AppColors.accentColor else Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // Input field with list view button
        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                modifier = Modifier
                    .weight(1f)
                    .getTextFieldModifier()
                    .focusRequester(serialFocusRequester)
                    .onFocusChanged {
                        isFocused = it.isFocused
                    }
                    .onKeyEvent { event ->
                        // Handle tab key and other keys that might indicate a scan completion
                        if (event.type == KeyEventType.KeyDown) {
                            when (event.key) {
                                Key.Tab -> {
                                    if (currentSerial.isNotEmpty()) {
                                        addSerialNumber(currentSerial)
                                        currentSerial = ""
                                    }
                                    true
                                }

                                Key.Enter -> {
                                    if (currentSerial.isNotEmpty()) {
                                        addSerialNumber(currentSerial)
                                        currentSerial = ""
                                    }
                                    true
                                }

                                else -> false
                            }
                        } else false
                    },
                shape = RoundedCornerShape(15.dp),
                colors = getTextFiledColors(),
                singleLine = true,
                value = currentSerial,
                onValueChange = { newValue ->
                    // Check if the input contains a tab or newline (common in barcode scanners)
                    if (newValue.contains('\t') || newValue.contains('\n')) {
                        val cleanValue = newValue.replace("\t", "").replace("\n", "")
                        if (cleanValue.isNotEmpty()) {
                            addSerialNumber(cleanValue)
                            currentSerial = ""
                        }
                    } else {
                        currentSerial = newValue
                    }
                },
                placeholder = { Text(stringResource(id = R.string.scan_serial_number)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    if (currentSerial.isNotEmpty()) {
                        addSerialNumber(currentSerial)
                        currentSerial = ""
                        // Keep focus on this field until required count is met
                        if (serialNumbers.size < actualRequiredCount) {
                            serialFocusRequester.requestFocus()
                        }
                    }
                })
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Button to view list of scanned serial numbers
            IconButton(
                onClick = { showSerialsList = true }, modifier = Modifier
                    .background(
                        color = AppColors.lightBlue, shape = RoundedCornerShape(8.dp)
                    )
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = "View Serial Numbers",
                    tint = AppColors.accentColor
                )
            }
        }


    }

    // Dialog to show list of serial numbers
    if (showSerialsList) {
        Dialog(onDismissRequest = { showSerialsList = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Scanned Serial Numbers",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (serialNumbers.isEmpty()) {
                        Text(
                            text = "No serial numbers scanned yet",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f, false)
                                .heightIn(max = 300.dp)
                        ) {
                            itemsIndexed(serialNumbers, key = { index, item ->
                                item + index
                            }) { _, serial ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = serial, modifier = Modifier.weight(1f)
                                    )

                                    IconButton(
                                        onClick = {
                                            serialNumbers.remove(serial)
                                            onValueChange(serialNumbers.toList())
                                        }, modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                    }

                    Button(
                        onClick = { showSerialsList = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}