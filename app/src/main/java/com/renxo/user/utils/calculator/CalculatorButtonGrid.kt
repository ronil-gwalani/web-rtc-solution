package com.renxo.user.utils.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CalculatorButtonGrid(
    actions: List<CalculatorUiAction>,
    onAction: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val buttonCount = 20
        val columnCount = 4
        val rowCount = buttonCount / columnCount

        // Calculate button size dynamically
        val buttonSize = ((if (maxHeight < maxWidth) maxHeight else maxWidth) / columnCount) - 8.dp
        val buttonHeight = (maxHeight / rowCount) - 8.dp

        LazyVerticalGrid(
            columns = GridCells.Fixed(columnCount),
//            columns = GridCells.FixedSize(buttonHeight),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            itemsIndexed(actions, key = { index, item ->
                item.text + index
            }) { _, action ->
                CalculatorButton(
                    action = action,
                    modifier = Modifier
                        .size(buttonHeight), // Dynamic size
                    onClick = { onAction(action.action) }
                )
            }
        }
    }
}