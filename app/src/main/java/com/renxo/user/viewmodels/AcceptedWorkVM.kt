package com.renxo.user.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renxo.user.models.ParamModel
import com.renxo.user.models.WorkSelectionModel
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

class AcceptedWorkVM : ViewModel(), WebSocketInterface by WebSocketInterfaceImpl() {
    private val _uiEventsFlow = MutableSharedFlow<UiEvents>()

    private var acceptedWorkTransactionId = ""
    private var acknowledgedWorkTransactionId = ""
    private var cancelTaskTransactionId = ""
    val acknowledgedWorkList = mutableStateListOf<WorkSelectionModel>()

    val uiEventsFlow: Flow<UiEvents> = _uiEventsFlow
        .onSubscription {
            if (_uiEventsFlow.subscriptionCount.value == 1) {
                getAcknowledgedWork()

            }
        }


    var needToRefreshPreviousScreen = false

    var showWarningDialogue by mutableStateOf(false)

    fun cancelTask() {
        cancelTaskTransactionId = getTransactionId()
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.CANCEL_TASK,
            type = AppConstants.Type.WMS_ACTION,
            transaction = cancelTaskTransactionId,
            params = getMap {
                put(AppConstants.Params.entityName, AppConstants.EntityNames.TASK)
                put(AppConstants.Params.task_id, "69570058-27c1-4876-a0f2-6f23aecfeccc")
                put(AppConstants.Params.id, "3563e185-af5e-4620-842f-674efdf78447")

            }
        )
        sendMessage(json.encodeToString(paramsModel))
    }

    fun cancelWork(taskGroupId: String) {
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.CANCEL_WORK,
            type = AppConstants.Type.WMS_ACTION,
            transaction = "K$taskGroupId",
            params = getMap {
                put(AppConstants.Params.entityName, AppConstants.EntityNames.TASK)
                put(AppConstants.Params.id, taskGroupId)

            }
        )
        sendMessage(json.encodeToString(paramsModel))
    }


    private fun startResponseListening() {
         viewModelScope.launch {  
            response.collect { response ->
                launch {
                    try {
                        if (response.orig_action == AppConstants.SendingAction.CANCEL_WORK) {
                            if (acknowledgedWorkList.map { "K${it.id}" }
                                    .contains(response.transaction)) {
                                if (response.result?.code == AppConstants.SuccessCodes.SUCCESS1237) {
                                    needToRefreshPreviousScreen = true
                                    response.params?.get(AppConstants.Params.original_id)
                                        ?.let { removedTaskId ->
                                            acknowledgedWorkList.removeIf { it.id == removedTaskId }
                                            if (acknowledgedWorkList.isEmpty()) {
                                                _uiEventsFlow.emit(RemoveNextHop)
                                            }
                                        }
                                }
                            }
                        } else if (response.orig_action == AppConstants.SendingAction.CANCEL_TASK) {
                            if (response.transaction == cancelTaskTransactionId) {
                                if (response.result?.code == AppConstants.SuccessCodes.SUCCESS1235) {
                                    needToRefreshPreviousScreen = true
                                    response.params?.get(AppConstants.Params.task_group_id)
                                        ?.let { removedTaskId ->
                                            acknowledgedWorkList.removeIf { it.id == removedTaskId }
                                            if (acknowledgedWorkList.isEmpty()) {
                                                _uiEventsFlow.emit(RemoveNextHop)
                                            }
                                        }
                                } else if (response.result?.code == AppConstants.ErrorCodes.ERR427) {


                                }
                            }
                        } else if (response.orig_action == AppConstants.SendingAction.GET_ALL_WORK_LIST) {
                            if (response.transaction == acknowledgedWorkTransactionId) {
                                if (response.result?.code == AppConstants.SuccessCodes.SUCCESS328) {
                                    response.params?.get(AppConstants.Params.result)
                                        ?.let { resultStr ->
                                            val tasks =
                                                json.decodeFromString<List<WorkSelectionModel>>(
                                                    resultStr
                                                )
                                            acknowledgedWorkList.clear()
                                            acknowledgedWorkList.addAll(tasks)
                                        } ?: run {
                                        showWarningDialogue = true

                                    }
                                } else {
                                    showWarningDialogue = true

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

    private fun getAcknowledgedWork() {
        acknowledgedWorkTransactionId = getTransactionId()
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.GET_ALL_WORK_LIST,
            type = AppConstants.Type.WMS_ACTION,
            transaction = acknowledgedWorkTransactionId,
            params = getMap {
                put(AppConstants.Params.entityName, AppConstants.EntityNames.TASK)
            }
        )
        sendMessage(json.encodeToString(paramsModel))
    }


    init {
        startResponseListening()
    }


    data object RemoveNextHop : UiEvents

}