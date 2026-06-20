package com.renxo.user.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renxo.user.R
import com.renxo.user.models.DataForPicking
import com.renxo.user.models.InvSrcId
import com.renxo.user.models.ParamModel
import com.renxo.user.models.PickingAttribute
import com.renxo.user.models.PickingPacks
import com.renxo.user.models.TaskInfo
import com.renxo.user.models.TaskItem
import com.renxo.user.navigation.UiEvents
import com.renxo.user.navigation.showSnackBar
import com.renxo.user.utils.AppConstants
import com.renxo.user.utils.getMap
import com.renxo.user.utils.getTransactionId
import com.renxo.user.utils.json
import com.renxo.user.webSocket.WebSocketInterface
import com.renxo.user.webSocket.WebSocketInterfaceImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch


class MainPageVM : ViewModel(), WebSocketInterface by WebSocketInterfaceImpl() {

    val focusRequester = FocusRequester()
    private var decodeLocationTransactionId = ""
    var scanForWork by mutableStateOf("")
    var showStartWorkDialogue by mutableStateOf(false)
    private val _uiEventsFlow = MutableSharedFlow<UiEvents>()
    val uiEventsFlow: Flow<UiEvents> = _uiEventsFlow
    private var startWorkTransactionId = ""


    init {
        startResponseListening()
    }


    fun startWork() {
        startWorkTransactionId = getTransactionId()
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.START_WORK,
            type = AppConstants.Type.WMS_ACTION,
            transaction = startWorkTransactionId,
            params = getMap {
                put(
                    AppConstants.Params.entityName,
                    AppConstants.EntityNames.INVENTORY,
                )
                put(
                    AppConstants.Params.location,
                    "A0101A04"//TODO this is hardCoded and needed to change
                )
            }
        )
        sendMessage(json.encodeToString(paramsModel))
    }

    fun decodeLocation() {
        if (scanForWork.isNotEmpty()) {
            decodeLocationTransactionId = getTransactionId()
            val paramsModel = ParamModel(
                action = AppConstants.SendingAction.DECODE_LOCATION,
                type = AppConstants.Type.WMS_ACTION,
                transaction = decodeLocationTransactionId,
                params = getMap {
                    put(
                        AppConstants.Params.entityName, AppConstants.EntityNames.INVENTORY
                    )
                    put(
                        AppConstants.Params.area, scanForWork
                    )
                },
            )
            sendMessage(json.encodeToString(paramsModel))
//            onSubmit()
        } else if (scanForWork.isEmpty()) {
            viewModelScope.launch {
                _uiEventsFlow.emit(showSnackBar(R.string.scan_code))
            }
        }
    }

    private fun startResponseListening() {
        viewModelScope.launch {
            response.collect { response ->
                launch {
                    try {
                        if (response.type == AppConstants.ReceivingType.RESPONSE) {
                            if (response.orig_action == AppConstants.SendingAction.DECODE_LOCATION) {
                                if (response.transaction == decodeLocationTransactionId) {
                                    // Pass the params to next screen
                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS2301) {
                                        response.params?.let { params ->
                                            params[AppConstants.Params.area]?.let {
                                                scanForWork = ""
                                                _uiEventsFlow.emit(UpdateLocation("HiBayEast"))// TODO need to update is with the value fetched from the response
                                            }
                                        }
                                    }
                                }
                            } else if (response.orig_action == AppConstants.SendingAction.START_WORK) {
                                if (response.transaction == startWorkTransactionId) {
                                    // Pass the params to next screen
                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS557) {

                                        response.params?.let { params ->
                                            val taskListId =
                                                response.params[AppConstants.Params.task_id_list]
                                                    ?.let { workflow ->
                                                        json.decodeFromString<List<TaskInfo>>(
                                                            workflow
                                                        )
                                                    }

                                            val workflowData =
                                                response.params[AppConstants.Params.id_fields]
                                                    ?.let { workflow ->
                                                        json.decodeFromString<List<PickingAttribute>>(
                                                            workflow
                                                        )
                                                    }
                                            val data =
                                                DataForPicking(
                                                    task_id = params[AppConstants.Params.task_id],
                                                    allocation_id = params[AppConstants.Params.allocation_id],
                                                    area = params[AppConstants.Params.area],
                                                    product_id = params[AppConstants.Params.product_id],
                                                    pack = params[AppConstants.Params.pack]?.let {
                                                        json.decodeFromString<List<PickingPacks>>(
                                                            it
                                                        )
                                                    },
                                                    inv_detail_id = params[AppConstants.Params.inv_detail_id],
                                                    inv_src_id = params[AppConstants.Params.inv_src_id]?.let {
                                                        json.decodeFromString<InvSrcId>(
                                                            it
                                                        )
                                                    },
                                                    quantity = params[AppConstants.Params.quantity]?.toInt(),
                                                    travel_sequence = params[AppConstants.Params.travel_sequence]?.toInt(),
                                                )
                                            _uiEventsFlow.emit(
                                                OpenPickingScreen(
                                                    data,
                                                    workflowData, taskListId
                                                )
                                            )

                                        }
                                    }
                                }
                            }
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                }
            }

        }


    }


    data class OpenPickingScreen(
        val data: DataForPicking,
        val workflowData: List<PickingAttribute>?,
        val taskListId: List<TaskInfo>?
    ) : UiEvents

    data class UpdateLocation(val location: String) : UiEvents
    data class UpdateTasksList(val list: List<TaskItem>) : UiEvents
}

