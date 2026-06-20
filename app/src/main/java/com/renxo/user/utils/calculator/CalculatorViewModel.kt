package com.renxo.user.utils.calculator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renxo.user.utils.AppConstants

import com.renxo.user.utils.preferenceManager
import kotlinx.coroutines.launch

class CalculatorViewModel(
    private val writer: ExpressionWriter = ExpressionWriter()
) : ViewModel() {
    var showFloating by mutableStateOf(true)

    fun getFloatingInfo() {
         viewModelScope.launch {  
            showFloating = preferenceManager.getBoolean(
                AppConstants.Preferences.CALCULATOR_TYPE_FLOATING,
                true
            )
        }

    }

    var expression by mutableStateOf("")
        private set

    fun onAction(action: CalculatorAction) {
        writer.processAction(action)
        this.expression = writer.expression
    }
}