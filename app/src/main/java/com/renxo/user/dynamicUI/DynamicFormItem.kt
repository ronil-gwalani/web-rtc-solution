package com.renxo.user.dynamicUI

import androidx.compose.runtime.Composable
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.renxo.user.dynamicUI.components.CheckBoxComponent
import com.renxo.user.dynamicUI.components.DatePickerComponent
import com.renxo.user.dynamicUI.components.DateTimeComponent
import com.renxo.user.dynamicUI.components.DropdownComponent
import com.renxo.user.dynamicUI.components.EditTextComponent
import com.renxo.user.dynamicUI.components.EditTextNumberComponent
import com.renxo.user.dynamicUI.components.MultiSelectionCheckBox
import com.renxo.user.dynamicUI.components.MultipleScanningComponent
import com.renxo.user.dynamicUI.components.NormalButtonComponent
import com.renxo.user.dynamicUI.components.RadioSelectionsComponent
import com.renxo.user.dynamicUI.components.ToggleButtonComponent

@Composable
fun DynamicFormItem(
    inputData: InputData,
    onValueChange: (Any?) -> Unit,
    onFocusChanged: (Boolean) -> Unit = {},
    triggerAction: () -> Unit = {},
) {



    if (inputData.toShow) {
        when (inputData.type) {
            InputType.RADIO -> RadioSelectionsComponent(
                inputData = inputData,
                onValueChange = onValueChange,
                triggerAction = triggerAction,
            )

            InputType.MULTISELECT -> MultiSelectionCheckBox(
                inputData = inputData,
                onValueChange = onValueChange,
            )

            InputType.CHECKBOX -> CheckBoxComponent(
                inputData = inputData,
                onValueChange = onValueChange,
            )

            InputType.DROPDOWN -> DropdownComponent(
                inputData = inputData,
                onValueChange = onValueChange,
                triggerAction = triggerAction,
            )

            InputType.DATE_TIME -> DateTimeComponent(
                inputData = inputData,
                onValueChange = onValueChange,
                triggerAction = triggerAction,
                onFocusChanged = onFocusChanged
            )

            InputType.DATE -> DatePickerComponent(
                inputData = inputData,
                onValueChange = onValueChange,
                triggerAction = triggerAction,
                onFocusChanged = onFocusChanged
            )

            InputType.EDIT_TEXT_NUMBER -> EditTextNumberComponent(
                inputData = inputData,
                onValueChange = onValueChange,
                triggerAction = triggerAction,
                onFocusChanged = { onFocusChanged(it) },
            )

            InputType.EDIT_TEXT -> EditTextComponent(
                inputData = inputData,
                onValueChange = onValueChange,
                triggerAction = triggerAction,
                onFocusChanged = { onFocusChanged(it) },
            )

            InputType.MULTIPLE_SCANNING -> MultipleScanningComponent(
                inputData = inputData,
                onValueChange = onValueChange,
            )

            InputType.TOGGLE -> ToggleButtonComponent(
                inputData = inputData,
                onValueChange = onValueChange,
                triggerAction = triggerAction,
            )

            InputType.BUTTON -> NormalButtonComponent(
                inputData = inputData,
                triggerAction = triggerAction,
            )
        }
    }
}