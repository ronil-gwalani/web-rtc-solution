package com.renxo.user.dynamicUI.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renxo.user.dynamicUI.InputData
import com.renxo.user.dynamicUI.Requirements
import com.renxo.user.utils.getTextFieldModifier
import com.renxo.user.utils.getTextFiledColors


// Dropdown Menu Component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownComponent(
    inputData: InputData, onValueChange: (Any?) -> Unit, triggerAction: () -> Unit,
) {
    val requirements = inputData.requirements as Requirements.DropDownRequirements
    val options = requirements.options ?: emptyList()


    var selectedItem by remember(inputData.value, requirements.defaultValue) {
        val value =
            if (inputData.value.toString() == "null" && !requirements.defaultValue.isNullOrEmpty() && requirements.defaultValue != "null") {
                onValueChange(requirements.defaultValue)
                requirements.defaultValue.toString()
            } else {
                if (inputData.value.toString() == "null") "" else
                    inputData.value.toString()
            }
        mutableStateOf(value)
    }

    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        ExposedDropdownMenuBox(
            modifier = Modifier.fillMaxWidth(),
            expanded = expanded && inputData.editable,
            onExpandedChange = {
                if (inputData.editable) expanded = !expanded
            }
        ) {
            TextField(
                value = selectedItem,
                shape = RoundedCornerShape(15.dp),
                colors = getTextFiledColors(),
                onValueChange = { },
                readOnly = true,
                enabled = inputData.editable,
                trailingIcon = {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null
                     )
                },
                label = { Text(inputData.placeholder) },
                modifier = Modifier
                    .getTextFieldModifier()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )

            ExposedDropdownMenu(
                expanded = expanded && inputData.editable,
                onDismissRequest = { expanded = false }
            ) {
                // Header
                DropdownMenuItem(
                    onClick = { },
                    enabled = false,
                    text = {
                        Text(
                            text = inputData.placeholder,
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                )

                // Options
                options.filterNotNull().forEach { item ->
                    DropdownMenuItem(
                        onClick = {
                            selectedItem = item
                            onValueChange(item)
                            expanded = false
                            triggerAction()
                        },
                        text = {
                            Text(text = item)
                        }
                    )
                }
            }
        }
    }
}