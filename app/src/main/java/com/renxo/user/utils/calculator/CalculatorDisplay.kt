package com.renxo.user.utils.calculator

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun CalculatorDisplay(
    expression: String,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        val fontSize = ((if (maxWidth > maxHeight) maxWidth else maxHeight) * 0.2f).value.sp
        BasicTextField(
            value = expression,
            onValueChange = {},
            textStyle = TextStyle(
                fontSize = fontSize,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.End
            ),
            maxLines = Int.MAX_VALUE, // Allow multiple lines
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            readOnly = true,
            modifier = Modifier
                .wrapContentSize() // Ensures text field does not take full size
                .align(Alignment.BottomEnd) // Aligns text to bottom-end
        )
    }

}