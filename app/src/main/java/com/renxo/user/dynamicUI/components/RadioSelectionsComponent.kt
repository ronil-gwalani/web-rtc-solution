package com.renxo.user.dynamicUI.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.RadioButton
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

// Radio Button Selection Component
@Composable
fun RadioSelectionsComponent(
    inputData: InputData,
    onValueChange: (Any?) -> Unit,
    triggerAction: () -> Unit,

    ) {
    val requirements = inputData.requirements as Requirements.RadioButtonRequirements
    val options = requirements.options

    // Use remember with the key to preserve state during recomposition
    val selectedItem = remember(inputData.placeholder, inputData.value) {
        mutableStateOf(inputData.value?.toString() ?: requirements.defaultValue)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = inputData.placeholder,
            fontSize = 16.sp,
            color = if (!inputData.editable) Color.Gray else Color.Black
        )
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxWidth()
                .height(((if (options.size % 2 == 0) (options.size / 2) else (options.size + 1) / 2) * 50).dp) // Set a proper height
                .padding(top = 4.dp),
            userScrollEnabled = true,
            columns = GridCells.Fixed(2),
        ) {
            itemsIndexed(options, key = { index, item ->
                item + index
            }) { _, item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .fillMaxWidth()
                ) {
                    RadioButton(
                        selected = (item == selectedItem.value),
                        onClick = {
                            if (inputData.editable) {
                                selectedItem.value = item
                                onValueChange(item)
                                triggerAction()
                            }
                        },
                        enabled = inputData.editable
                    )
                    Text(
                        text = item,
                        fontSize = 14.sp,
                        color = if (!inputData.editable) Color.Gray else Color.Black,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (inputData.editable) {
                                    selectedItem.value = item
                                    onValueChange(item)
                                    triggerAction()
                                }
                            }
                    )
                }
            }
        }
    }
}