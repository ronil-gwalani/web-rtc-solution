package com.renxo.user.dynamicUI.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renxo.user.dynamicUI.InputData
import com.renxo.user.dynamicUI.Requirements

// Toggle Selection Component
@Composable
fun ToggleButtonComponent(
    inputData: InputData,
    onValueChange: (Any?) -> Unit,
    triggerAction: () -> Unit

) {
    val requirements = inputData.requirements as Requirements.ToggleButtonRequirements

    // Use remember with the key to preserve state during recomposition
    val selectedItem = remember(inputData.placeholder, inputData.value) {
        mutableStateOf(inputData.value?.toString()?.toBoolean() ?: requirements.defaultValue)
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = inputData.placeholder,
            fontSize = 18.sp,
            color = if (!inputData.editable) Color.Gray else Color.Black
        )
        Switch(
            checked = selectedItem.value,
            onCheckedChange = {
                onValueChange(it)
                triggerAction()
            }
        )

    }
}