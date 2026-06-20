package com.renxo.user.dynamicUI.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renxo.user.dynamicUI.InputData
import com.renxo.user.dynamicUI.Requirements
import com.renxo.user.ui.theme.AppColors
import com.renxo.user.utils.getTextFieldModifier
import com.renxo.user.utils.getTextFiledColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Date Picker Component
@Composable
fun DatePickerComponent(
    inputData: InputData,
    onValueChange: (Any?) -> Unit,
    triggerAction: () -> Unit,
    onFocusChanged: (Boolean) -> Unit,
) {
    val requirements = inputData.requirements as Requirements.DateRequirements
    var shouldTriggerAction by rememberSaveable { mutableStateOf<Boolean?>(null) }

    // Parse dates to milliseconds (simple implementation)
    val minDate = requirements.mindate?.toLongOrNull() ?: System.currentTimeMillis()
    val maxDate = requirements.maxdate?.toLongOrNull() ?: get100YearsBackDate()


    // Use remember with the key to preserve state during recomposition
    val selectedDate = remember(inputData.value) {

        val value =
            if (inputData.value != null && inputData.value.toString() == "null") {
                if (!inputData.showLiableOutside) {
                    inputData.value.toString().toLongOrNull() ?: 0
                } else {
                    ""
                }
            } else {
                if (inputData.value.toString() == "null") "" else
                    inputData.value.toString().toLongOrNull() ?: ""
            }

        mutableStateOf(value.toString().toLongOrNull())

    }

    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {


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
            ), shape = RoundedCornerShape(15.dp),

            singleLine = true,
            enabled = false,

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

            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),

            modifier = Modifier
                .padding(vertical = 4.dp)
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
                .clickable {
                    if (inputData.editable)
                        showDialog = true
                },
            value = selectedDate.value.toString(),
            onValueChange = {})


    }

    ShowDatePicker(
        value = showDialog,
        initialDate = selectedDate.value,
        minDate = minDate,
        maxDate = maxDate
    ) { newDate ->
        showDialog = false
        if (newDate != null) {
            selectedDate.value = newDate
            onValueChange(newDate)
            onFocusChanged(true)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Allows use of experimental API
@Composable
private fun ShowDatePicker(
    value: Boolean, initialDate: Long?,  // Default to current date
    minDate: Long,  // Minimum selectable date
    maxDate: Long,
    selectedDate: (Long?) -> Unit,
) {

    val customSelectableDates = object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            // Compare the milliseconds to check if the date is within range
            return utcTimeMillis in minDate..maxDate
        }
    }


    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate,
//        selectableDates = customSelectableDates,
    )
    if (value) {
        DatePickerDialog(
            onDismissRequest = {
                selectedDate(initialDate)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDate(datePickerState.selectedDateMillis)
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        selectedDate(initialDate)
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState
            )
        }
    }

}

private fun get100YearsBackDate(): Long {
    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR) - 100
    val currentMonth = calendar.get(Calendar.MONTH) + 1 // Add 1 as months are zero-based
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
    calendar.set(currentYear, currentMonth - 1, currentDay)
    return calendar.timeInMillis
}

private fun convertTimestampToDate(timestamp: Long): String {
    // Create a SimpleDateFormat instance with the desired format
    val dateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())

    // Create a Date object from the timestamp
    val date = Date(timestamp)

    // Format the date and return the string
    return dateFormat.format(date)
}
