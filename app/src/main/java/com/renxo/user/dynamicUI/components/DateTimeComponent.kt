package com.renxo.user.dynamicUI.components

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renxo.user.R
import com.renxo.user.dynamicUI.InputData
import com.renxo.user.ui.theme.AppColors
import com.renxo.user.utils.getTextFieldModifier
import com.renxo.user.utils.getTextFiledColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@Composable
fun DateTimeComponent(
    inputData: InputData,
    onValueChange: (Any?) -> Unit,
    triggerAction: () -> Unit,
    onFocusChanged: (Boolean) -> Unit = {},

    ) {
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var shouldTriggerAction by rememberSaveable { mutableStateOf<Boolean?>(null) }

    val dateTimeFormat = "EEE MMM dd yyyy HH:mm:ss 'GMT'XXX"
    val dateTimeRegex =
        Regex("""[A-Za-z]{3} [A-Za-z]{3} \d{2} \d{4} \d{2}:\d{2}:\d{2} GMT[+-]\d{2}:\d{2}""")


    var dateTime by remember(inputData.value) {
        val value =
            if (inputData.value != null && inputData.value.toString() == "null") {
                if (!inputData.showLiableOutside) {
                    inputData.value.toString()
                } else {
                    ""
                }
            } else {
                if (inputData.value.toString() == "null") "" else
                    inputData.value.toString()
            }

        mutableStateOf(value)
    }


    Column(Modifier.fillMaxWidth()) {
        if (inputData.showLiableOutside) {
            Text(
                inputData.placeholder.replaceFirstChar { it.uppercase() }, fontSize = 14.sp,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.textColor,
                modifier = Modifier.padding(start = 5.dp)
            )
        }

        TextField(
            colors = getTextFiledColors().copy(
                unfocusedPlaceholderColor = Color.Gray.copy(alpha = 0.5f)
            ),            shape = RoundedCornerShape(15.dp),
            value = dateTime,
            onValueChange = {
//                dateTime = it
                onValueChange(it)
                errorText = if (dateTimeRegex.matches(it)) null else context.getString(
                    R.string.invalid_format_use_this, dateTimeFormat
                )
            },
            label = if (!inputData.showLiableOutside) {
                {
                    Text(inputData.placeholder.replaceFirstChar { it.uppercase() })
                }
            } else {
                null
            },
            placeholder = if (inputData.showLiableOutside) {
                {
                    inputData.hint?.let { Text(it) }
                }
            } else {
                null
            },
            isError = errorText != null,
            trailingIcon = {
                IconButton(onClick = {
                    if (inputData.editable)
                        showDatePicker = true
                }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            enabled = inputData.editable,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth()
                .getTextFieldModifier()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        onFocusChanged(true)
                        shouldTriggerAction = true
                    }
                    if (!focusState.isFocused && shouldTriggerAction == true) {
                        triggerAction()
                        onFocusChanged(false)
                        shouldTriggerAction = false
                    }
                }
        )

        errorText?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (showDatePicker) {
            showDatePicker(context) { selectedDate ->
                dateTime = selectedDate
                showDatePicker = false
                showTimePicker = true
            }
        }

        if (showTimePicker) {
            showTimePicker(context, dateTime) { updatedDateTime ->
                dateTime = updatedDateTime
                showTimePicker = false
                errorText = if (dateTimeRegex.matches(updatedDateTime)) null else context.getString(
                    R.string.invalid_format_use_this, dateTimeFormat
                )
                onValueChange(dateTime)
                onFocusChanged(true)

            }
        }
    }
}


fun showDatePicker(context: Context, onDateSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }

            val sdf = SimpleDateFormat("EEE MMM dd yyyy", Locale.US)
            val formattedDate = sdf.format(selectedDate.time)
            onDateSelected("$formattedDate ")
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

fun showTimePicker(context: Context, date: String, onTimeSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    TimePickerDialog(
        context,
        { _, hour, minute ->
            val updatedDateTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }

            val sdf = SimpleDateFormat("HH:mm:ss z", Locale.US)
            val formattedTime = sdf.format(updatedDateTime.time)
            onTimeSelected("$date$formattedTime")
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    ).show()
}

