package com.renxo.user.dynamicUI.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renxo.user.dynamicUI.InputData
import com.renxo.user.dynamicUI.Requirements

// CheckBox Component
@Composable
fun CheckBoxComponent(
    inputData: InputData,
    onValueChange: (Any?) -> Unit,
) {
    val requirements = inputData.requirements as Requirements.CheckBoxRequirements
    // Use remember with the key to preserve state during recomposition
    val isChecked = remember(inputData.placeholder, inputData.value) {
        mutableStateOf(
            when (inputData.value) {
                is Boolean -> inputData.value as Boolean
                null -> requirements.defaultValue
                else -> requirements.defaultValue
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked.value,
            onCheckedChange = { checked ->
                if (inputData.editable) {
                    isChecked.value = checked
                    onValueChange(checked)
                }
            },
            enabled = inputData.editable
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = inputData.placeholder,
            fontSize = 16.sp,
            style = TextStyle(color = if (!inputData.editable) Color.Gray else Color.Black),
            modifier = Modifier.weight(1f)
        )
    }
}
