package com.renxo.user.viewmodels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renxo.user.R
import com.renxo.user.dynamicUI.InputData
import com.renxo.user.dynamicUI.InputType
import com.renxo.user.dynamicUI.Requirements
import com.renxo.user.models.ParamModel
import com.renxo.user.models.WorkFlowQuestions
import com.renxo.user.navigation.UiEvents
import com.renxo.user.utils.AppConstants
import com.renxo.user.utils.getMap
import com.renxo.user.utils.json
import com.renxo.user.webSocket.WebSocketInterface
import com.renxo.user.webSocket.WebSocketInterfaceImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString


class WorkFlowVM(
    private var questions: WorkFlowQuestions,
    private var transactionId: String,
    private val extraParams: HashMap<String, String?>
) : ViewModel(), WebSocketInterface by WebSocketInterfaceImpl() {


    val list = mutableStateListOf<InputData>()
    var errorMessage by mutableStateOf<String?>(null)

    private val _uiEventsFlow = MutableSharedFlow<UiEvents>()
    val uiEventsFlow: Flow<UiEvents> = _uiEventsFlow
        .onSubscription {
            if (_uiEventsFlow.subscriptionCount.value == 1) {
                fetchDataForUi()
            }
        }

    fun updateInputValue(index: Int, value: Any?) {
        list[index] = list[index].copy(value = value)
    }


    init {
        startResponseListening()
    }


    private fun fetchDataForUi() {
        questions.let {
             viewModelScope.launch {  
                _uiEventsFlow.emit(
                    GetDataForUi
                )
            }
        }

    }


    private fun startResponseListening() {
         viewModelScope.launch {  
            response.collect { response ->
                launch {
                    try {
                        if (response.type == AppConstants.ReceivingType.RESPONSE) {
                            if (response.orig_action == AppConstants.SendingAction.SUBMIT_WORK_FLOW) {
                                // Need to handel this Case once sorted from backend
                                if (response.result?.code?.contains(
                                        AppConstants.SuccessCodes.SUCCESS,
                                        true
                                    ) == true
                                ) {
                                    _uiEventsFlow.emit(Finish)
                                }
                            }
                        }

                    } catch (e: Exception) {
                        Log.e("WorkFlowQuestions", "Error parsing response: ${e.message}")
                    }


                }
            }
        }
    }

    fun submitData() {
        val afterList = list.map {

            val answer = when (val value = it.value) {
                is List<*> -> value.joinToString(", ")
                else -> value.toString()
            }
            "${it.placeholder}: $answer"
        }
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.SUBMIT_WORK_FLOW,
//                                    type = "custom_action",
            type = AppConstants.Type.WMS_ACTION,
            transaction = transactionId,
            params = getMap {
                extraParams.mapValues { (key, value) ->
                    put(key, value)
                }
                put(
                    AppConstants.Params.entityName,
                    AppConstants.EntityNames.INBOUND_DELIVERY
                )
                put(
                    AppConstants.EntityNames.INBOUND_DELIVERY,
                    json.encodeToString(afterList)
                )

            }
        )
        sendMessage(json.encodeToString(paramsModel)) // Send message to WebSocket

    }

    fun getData(context: Context): ArrayList<InputData> {
        val list = ArrayList<InputData>()
        questions.questions?.forEach { question ->
            val placeholder = question.question ?: context.getString(R.string.No_Question_Provided)
            val options = question.possible_values ?: listOf()

            when (question.data_type?.lowercase() ?: "unknown") {
                WorkflowConstants.BOOLEAN -> {
                    list.add(
                        InputData(
                            placeholder = placeholder,

                            type = InputType.RADIO,
                            requirements = Requirements.RadioButtonRequirements(
                                error = context.getString(R.string.Please_select_one_Options),
                                options = options
                            ),
                            required = true, editable = true

                        )
                    )
                }

                WorkflowConstants.DROPDOWN -> {
                    list.add(
                        InputData(
                            placeholder = placeholder,

                            type = InputType.DROPDOWN,
                            requirements = Requirements.DropDownRequirements(
                                error = context.getString(R.string.Please_select_an_answer),
                                options = options
                            ),


                            required = true, editable = true

                        )
                    )
                }

                WorkflowConstants.STRING -> {
                    list.add(
                        InputData(
                            placeholder = placeholder,

                            type = InputType.EDIT_TEXT,
                            requirements = Requirements.EditTextRequirements(
                                error = context.getString(R.string.Please_provide_a_valid_input)
                            ),


                            required = true, editable = true

                        )
                    )
                }

                WorkflowConstants.INT -> {
                    list.add(
                        InputData(
                            type = InputType.EDIT_TEXT_NUMBER,
                            placeholder = placeholder,
                            requirements = Requirements.EditTextNumberRequirements(
                                error = context.getString(R.string.Please_provide_a_valid_input),
                                min = question.min.toString(),
                                max = question.max.toString()
                            ),


                            required = true, editable = true

                        )
                    )
                }

                WorkflowConstants.MULTI_SELECT -> {
                    list.add(
                        InputData(
                            placeholder = placeholder,

                            type = InputType.MULTISELECT,
                            requirements = Requirements.MultiselectRequirements(
                                error = context.getString(R.string.Please_select_atleast_one_Options),
                                options = options
                            ),


                            required = true, editable = true

                        )
                    )
                }

                else -> {
                    Log.e("getData", "Unknown or unsupported data type: ${question.data_type}")
                }
            }
        }
        return list
    }


    private interface WorkflowConstants {
        companion object {
            const val BOOLEAN = "boolean"
            const val DROPDOWN = "dropdown"
            const val STRING = "string"
            const val INT = "int"
            const val MULTI_SELECT = "multi-select"
        }


    }


    data object Finish : UiEvents
    data object GetDataForUi : UiEvents
}