package com.renxo.user.dynamicUI.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renxo.user.dynamicUI.InputData
import com.renxo.user.dynamicUI.Requirements
import com.renxo.user.ui.theme.AppColors
import com.renxo.user.utils.getTextFieldModifier
import com.renxo.user.utils.getTextFiledColors

@Composable
fun EditTextComponent(
    inputData: InputData,
    onValueChange: (Any?) -> Unit,
    triggerAction: () -> Unit,
    onFocusChanged: (Boolean) -> Unit = {},
) {

    val textValue = remember(inputData.value) {
        val value =
            if (inputData.value != null && inputData.value.toString()
                    .isNotEmpty() && inputData.value.toString() == "null"
            ) {
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

    var shouldTriggerAction by rememberSaveable { mutableStateOf<Boolean?>(null) }


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
            value = textValue.value,
            shape = RoundedCornerShape(15.dp),
            colors = getTextFiledColors().copy(
                unfocusedPlaceholderColor = Color.Gray.copy(alpha = 0.5f)
            ),
            onValueChange = { newValue ->
                if (inputData.editable) {
                    textValue.value = newValue
                    //                    if (newValue != "null") {
                    onValueChange(newValue)
                    //                    }
                    onFocusChanged(true)
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            enabled = inputData.editable,
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
                },
        )
    }
}