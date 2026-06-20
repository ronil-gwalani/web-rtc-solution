package com.renxo.user.dynamicUI.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.renxo.user.dynamicUI.InputData

// Button Component
@Composable
fun NormalButtonComponent(
    inputData: InputData,
    triggerAction: () -> Unit
) {

    Button(
        onClick = { triggerAction() },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(15.dp)
    ) {
        Text(
            inputData.placeholder,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

