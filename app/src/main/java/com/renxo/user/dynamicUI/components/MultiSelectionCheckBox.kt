package com.renxo.user.dynamicUI.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renxo.user.dynamicUI.InputData
import com.renxo.user.dynamicUI.Requirements

@Composable
fun MultiSelectionCheckBox(
    inputData: InputData,
    onValueChange: (Any?) -> Unit,
) {
    val requirements = inputData.requirements as Requirements.MultiselectRequirements
    val options = requirements.options ?: emptyList()

    // Use remember with the key to preserve state during recomposition
    val selectedItems = remember(inputData.placeholder, inputData.value) {
        mutableStateListOf<String>().apply {
            // Initialize with existing value or default
            when (val value = inputData.value) {
                is List<*> -> addAll(value.filterNotNull().map { it.toString() })
                null -> requirements.defaultValue?.let { addAll(it) }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = inputData.placeholder,
            fontSize = 16.sp,
            style = TextStyle(color = if (!inputData.editable) Color.Gray else Color.Black),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        options.filterNotNull().forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .let { mod ->
                        if (inputData.editable) {
                            mod.clickable {
                                if (selectedItems.contains(option)) {
                                    selectedItems.remove(option)
                                } else {
                                    selectedItems.add(option)
                                }
                                onValueChange(selectedItems.toList())
                            }
                        } else mod
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = selectedItems.contains(option),
                    onCheckedChange = {
                        if (inputData.editable) {
                            if (it) selectedItems.add(option) else selectedItems.remove(option)
                            onValueChange(selectedItems.toList())
                        }
                    },
                    modifier = Modifier.padding(end = 8.dp),
                    enabled = inputData.editable
                )
                Text(
                    text = option,
                    fontSize = 14.sp,
                    style = TextStyle(color = if (!inputData.editable) Color.Gray else Color.Black)
                )
            }
        }
    }
}