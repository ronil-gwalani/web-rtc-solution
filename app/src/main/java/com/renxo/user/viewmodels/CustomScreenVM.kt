package com.renxo.user.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renxo.user.R
import com.renxo.user.dynamicUI.InputData
import com.renxo.user.dynamicUI.InputType
import com.renxo.user.dynamicUI.Requirements
import com.renxo.user.models.CustomData
import com.renxo.user.models.CustomField
import com.renxo.user.models.CustomFieldResult
import com.renxo.user.models.CustomScreenAttributes
import com.renxo.user.models.ParamModel
import com.renxo.user.navigation.UiEvents
import com.renxo.user.utils.AppConstants
import com.renxo.user.utils.getMap
import com.renxo.user.utils.getTransactionId
import com.renxo.user.utils.json
import com.renxo.user.webSocket.WebSocketInterface
import com.renxo.user.webSocket.WebSocketInterfaceImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString


class CustomScreenVM : ViewModel(), WebSocketInterface by WebSocketInterfaceImpl() {
    val list = mutableStateListOf<InputData>()
    var errorMessage by mutableStateOf<String?>(null)
    private var onCloseAction: String? = null
    private var startCustomScreenTransactionId = ""


    private val _uiEventsFlow = MutableSharedFlow<UiEvents>()
    val uiEventsFlow: Flow<UiEvents> = _uiEventsFlow
        .onSubscription {
            if (_uiEventsFlow.subscriptionCount.value == 1) {
                 viewModelScope.launch {  
                    _uiEventsFlow.emit(ShowUi)
                }
            }
        }

    init {
        startResponseListening()
    }


    fun updateInputValue(index: Int, value: Any?) {
        list[index] = list[index].copy(value = value)
    }

    fun requestCloseScreen() {
        if (!onCloseAction.isNullOrBlank()) {
            // Collect the same full payload as for the other actions.
            val customFields = collectCustomFieldsWithValues()
            val paramsModel = ParamModel(
                action = onCloseAction!!,  // e.g. "CloseCustomScreen"
                type = AppConstants.Type.WMS_ACTION,
                transaction = onCloseAction,
                params = getMap {
                    put(AppConstants.Params.entityName, AppConstants.EntityNames.TASK)
                    put(AppConstants.Params.custom_fields, customFields)
                }
            )
            sendMessage(json.encodeToString(paramsModel))
        } else {
            // If no onClose action is defined, immediately allow exit.
             viewModelScope.launch {  
                _uiEventsFlow.emit(AllowExitScreen(true))
            }
        }
    }


    fun triggerAction(index: Int) {
        val action = list[index].action
        action?.let {
            val transaction = getTransactionId() + AppConstants.Defaults.CUSTOM_ACTION
            try {
                val customFields = collectCustomFieldsWithValues()

                val paramsModel = ParamModel(
                    action = it,
                    type = AppConstants.Type.WMS_ACTION,
                    transaction = transaction,
                    params = getMap {
                        put(AppConstants.Params.entityName, AppConstants.EntityNames.TASK)
                        put(AppConstants.Params.custom_fields, customFields)
                    }
                )
                sendMessage(json.encodeToString(paramsModel))
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
                actionCall(it)
            }
        }
    }


    private fun collectCustomFieldsWithValues(): List<CustomField> {
        return list.map { inputData ->
            CustomField(
                attribute_name = inputData.placeholder,
                editable = inputData.editable,
                mandatory = inputData.required,
                value = inputData.value?.toString() ?: ""
            )
        }
    }


    private fun actionCall(actionName: String) {
        val paramsModel = ParamModel(
            action = actionName,
            type = AppConstants.Type.WMS_ACTION,
            transaction = "Puneet",
            params = getMap {
                put(AppConstants.Params.entityName, AppConstants.EntityNames.TASK)
            })
        sendMessage(json.encodeToString(paramsModel))
    }


    fun getDataForUi(context: Context, data: CustomData): List<InputData> {

        onCloseAction = data.onClose
        val list = mutableStateListOf<InputData>()

        data.fields?.forEach { field ->
            when (field.type) {
                "text", "id" -> {
                    list.add(
                        InputData(
                            type = InputType.EDIT_TEXT,
                            action = field.action,
                            requirements = Requirements.EditTextRequirements(
                                error = context.getString(R.string.Please_provide_a_valid_input),
                            ),
                                value = field.value ?: "",
                            placeholder = field.attribute_name ?: "",
                            required = field.mandatory,
                            editable = field.editable
                        )
                    )
                }

                "toggle" -> {
                    list.add(
                        InputData(
                            action = field.action,
                            placeholder = field.attribute_name ?: "",
                            type = InputType.TOGGLE,
                            requirements = Requirements.ToggleButtonRequirements(
                                error = context.getString(R.string.Please_select_one_Options),
                                defaultValue = field.value?.toBoolean() ?: false
                            ),
                            required = field.mandatory,
                            editable = field.editable
                        )
                    )
                }

                "button" -> {
                    list.add(
                        InputData(
                            action = field.action,
                            placeholder = field.attribute_name ?: "",
                            type = InputType.BUTTON,
                            requirements = Requirements.ButtonRequirements,
                            required = field.mandatory,
                            editable = field.editable
                        )
                    )
                }

                "number" -> {
                    list.add(
                        InputData(
                            action = field.action,
                            type = InputType.EDIT_TEXT_NUMBER,
                            requirements = Requirements.EditTextNumberRequirements(
                                error = context.getString(R.string.Please_provide_a_valid_input),
                            ),
                            placeholder = field.attribute_name ?: "",
                            required = field.mandatory,
                                value = field.value,
                            editable = field.editable
                        )
                    )
                }

                else -> {
                    val defaults = field.list_of_values ?: emptyList()
                    list.add(
                        InputData(
                            action = field.action,
                            type = InputType.DROPDOWN,
                            requirements = Requirements.DropDownRequirements(
                                error = context.getString(R.string.Please_select_an_answer),
                                options = defaults,
                                defaultValue = field.value
                            ),
                            placeholder = field.attribute_name ?: "",
                            required = field.mandatory,
                            editable = field.editable
                        )
                    )
                }
            }
        }
        return list
    }


    private fun startResponseListening() {
         viewModelScope.launch {  
            response.collect { response ->
                launch {
                    try {
                        if (response.type == AppConstants.ReceivingType.RESPONSE) {
                            if (response.transaction == initFunction) {
                                if (response.result?.code == (AppConstants.SuccessCodes.SUCCESS)) {
                                    val resultArray =
                                        response.params?.get(AppConstants.Params.result)
                                    resultArray?.let { resultString ->
                                        try {
                                            val fieldResults =
                                                json.decodeFromString<List<CustomFieldResult>>(
                                                    resultString
                                                )
                                            fieldResults.forEach { fieldResult ->
                                                if (fieldResult.attribute_name != null && fieldResult.value != null) {
                                                    val matchingIndex = list.indexOfFirst {
                                                        it.placeholder.equals(
                                                            fieldResult.attribute_name,
                                                            ignoreCase = true
                                                        )
                                                    }
                                                    if (matchingIndex != -1) {
                                                        updateInputValue(
                                                            matchingIndex,
                                                            fieldResult.value
                                                        )
                                                    }
                                                }
                                                // Handle messages if message and status are present
                                                if (!fieldResult.message.isNullOrBlank() && fieldResult.status != null) {
                                                    _uiEventsFlow.emit(
                                                        ShowSnackBarWithStatus(
                                                            fieldResult.message
                                                        )
                                                    )
                                                }
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            } else if (response.transaction == onCloseAction) {
                                if (response.result?.code == (AppConstants.SuccessCodes.SUCCESS)) {
                                    _uiEventsFlow.emit(AllowExitScreen(true))
                                } else if (response.result?.code == (AppConstants.ErrorCodes.ERROR)) {

                                }


                                val resultArray = response.params?.get("result")
                                resultArray?.let { resultString ->
                                    try {
                                        val fieldResults =
                                            json.decodeFromString<List<CustomFieldResult>>(
                                                resultString
                                            )
                                        var allowExit = false
                                        fieldResults.forEach { fieldResult ->
                                            if (!fieldResult.message.isNullOrBlank() && fieldResult.status != null) {
                                                if (fieldResult.status.equals(
                                                        "Success",
                                                        ignoreCase = true
                                                    )
                                                ) {
                                                    allowExit = true
                                                } else {
                                                    _uiEventsFlow.emit(
                                                        ShowSnackBarWithStatus(
                                                            fieldResult.message
                                                        )
                                                    )
                                                    allowExit = false
                                                }
                                            }
                                        }
                                        _uiEventsFlow.emit(AllowExitScreen(allowExit))
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            } else if (response.transaction?.contains(AppConstants.Defaults.CUSTOM_ACTION) == true) {
                                if (response.result?.code == (AppConstants.SuccessCodes.SUCCESS)) {
                                    val resultArray = response.params?.get(AppConstants.Params.result)
                                    resultArray?.let { resultString ->
                                        try {
                                            val fieldResults =
                                                json.decodeFromString<List<CustomFieldResult>>(
                                                    resultString
                                                )
                                            fieldResults.forEach { fieldResult ->
                                                if (fieldResult.attribute_name != null && fieldResult.value != null) {
                                                    val matchingIndex = list.indexOfFirst {
                                                        it.placeholder.equals(
                                                            fieldResult.attribute_name,
                                                            ignoreCase = true
                                                        )
                                                    }
                                                    if (matchingIndex != -1) {
                                                        updateInputValue(
                                                            matchingIndex,
                                                            fieldResult.value
                                                        )
                                                    }
                                                }
                                                // Handle messages if message and status are present
                                                if (!fieldResult.message.isNullOrBlank() && fieldResult.status != null) {
                                                    _uiEventsFlow.emit(
                                                        ShowSnackBarWithStatus(
                                                            fieldResult.message
                                                        )
                                                    )
                                                }
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            }
//                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private var initFunction = ""
    fun initScreen(action: String?) {
        initFunction = action ?: ""
        val paramsModel = ParamModel(
            action = initFunction,  // e.g. "CloseCustomScreen"
            type = AppConstants.Type.WMS_ACTION,
            transaction = initFunction,
            params = getMap {
                put(AppConstants.Params.entityName, AppConstants.EntityNames.TASK)
//                put(AppConstants.Params.custom_fields, customFields)
            }
        )
        sendMessage(json.encodeToString(paramsModel))
    }

    data class GetDataForUi(val data: CustomScreenAttributes) : UiEvents
    data class ShowSnackBarWithStatus(val message: String) : UiEvents
    data class AllowExitScreen(val allow: Boolean) : UiEvents
    data object ShowUi : UiEvents

}