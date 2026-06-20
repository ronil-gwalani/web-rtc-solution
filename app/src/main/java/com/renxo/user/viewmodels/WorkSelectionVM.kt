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
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString


data class ShowNextHopDialogue(
    val show: Boolean = false,
//    val action: String = "",
    val message: String = "",
    val transactionId: String = "",
    val params: HashMap<String, String?>? = hashMapOf()
)

class WorkSelectionVM : ViewModel(), WebSocketInterface by WebSocketInterfaceImpl() {
    var showConfirmationDialogue by mutableStateOf(
        ShowNextHopDialogue()
    )

    private var listWorkTransactionId = ""
    private var acceptTaskTransactionId = ""
    private val _uiEventsFlow = MutableSharedFlow<UiEvents>()
    val uiEventsFlow: Flow<UiEvents> = _uiEventsFlow
    private var selectedArea = ""
    private var selectedEquipment = ""
    var showWarningDialogue by mutableStateOf(false)


    private var compatibilityWarningTransactionId = ""

    var showProcessing by mutableStateOf(false)

    private var taskType = ""
    val workList = mutableStateListOf<WorkSelectionModel>()

    var isWorkAccepted = false

    fun updateList(list: List<WorkSelectionModel>) {
        workList.clear()
        workList.addAll(list)

    }


    fun onRejectTask() {
        if (workList.isNotEmpty()) {
            workList.removeAt(0)
        }
    }


    fun onStartTask() {
//        val acceptedTasksList = acceptedTasks.map { it.id }.toList()
//        currentTaskIndex = 0
//        acceptedTasks.clear()
    }

    private fun getList() {
        listWorkTransactionId = getTransactionId()
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.GET_PENDING_TASK_LIST,
            type = AppConstants.Type.WMS_ACTION,

            transaction = listWorkTransactionId,
            params = getMap {
                put(AppConstants.Params.entityName, AppConstants.EntityNames.TASK)
                put(AppConstants.Params.from_area, selectedArea)
                put(AppConstants.Params.equipment, selectedEquipment)
                put(AppConstants.Params.group_type, taskType)
                put(AppConstants.Params.status, AppConstants.Defaults.PENDING)

            }
        )
        sendMessage(json.encodeToString(paramsModel))
    }

    fun acceptTask() {
        val item = workList.first()
        if (acceptTaskTransactionId == item.id) {
            return
        }
        showProcessing = true
        acceptTaskTransactionId = item.id ?: ""
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.ACCEPT_TASK,
            type = AppConstants.Type.WMS_ACTION,
            transaction = acceptTaskTransactionId,
            params = getMap {
                put(AppConstants.Params.entityName, AppConstants.EntityNames.TASK)
                put(AppConstants.Params.task_group_id, item.id ?: "")
                put(AppConstants.Params.compatible_task, item.compatible_task)
                put(AppConstants.Params.next_hop, item.next_hop ?: "")
                put(AppConstants.Params.group_type, item.group_type ?: "")
            }
        )
        sendMessage(json.encodeToString(paramsModel))
    }

    fun confirmWithCompatibility(selectedEquipment: String) {
        val item = workList.first()
        acceptTaskTransactionId = item.id ?: ""
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.GET_PENDING_TASK_LIST,
            type = AppConstants.Type.WMS_ACTION,

            transaction = compatibilityWarningTransactionId,
            params = getMap {
                put(AppConstants.Params.entityName, AppConstants.EntityNames.TASK)
                put(AppConstants.Params.task_group_id, item.id ?: "")
                put(AppConstants.Params.compatible_task, item.compatible_task)
                put(AppConstants.Params.next_hop, item.next_hop ?: "")
                put(AppConstants.Params.from_area, item.from_area ?: "")
                put(AppConstants.Params.group_type, item.group_type ?: "")
                put(AppConstants.Params.equipment, selectedEquipment)
                put(AppConstants.Params.use_compatibility_type, true)

            }
        )
        sendMessage(json.encodeToString(paramsModel))
    }

    fun setTaskType(type: String) {
        if (taskType != type) {
            taskType = type
            getList()
        }
    }

    fun setAreaAndEquipment(area: String, equipment: String) {
        if (selectedEquipment != equipment) {
            selectedEquipment = equipment
        }
        if (selectedArea != area) {
            selectedArea = area
        }

    }


    private fun startResponseListening() {
         viewModelScope.launch {  
            response.collect { response ->
                launch {
                    try {
                        if (response.type == AppConstants.ReceivingType.UI) {
                            if (response.orig_action == AppConstants.SendingAction.ACCEPT_TASK) {
                                if (response.result?.code == "NeedConfirmation") {
//                                    val action = response.params?.get(AppConstants.Params.action)
                                    val message = response.params?.get(AppConstants.Params.message)
                                    showConfirmationDialogue =
                                        ShowNextHopDialogue(
                                            true,
//                                            action = action.toString(),
                                            message = message.toString(),
                                            transactionId = response.transaction.toString(),
                                            params = response.params
                                        )


                                }
                            }
                        } else if (response.type == AppConstants.ReceivingType.RESPONSE) {
                            if (response.orig_action == AppConstants.SendingAction.GET_PENDING_TASK_LIST) {
                                if (response.transaction == listWorkTransactionId) {
                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS327) {
                                        showProcessing = false
                                        response.params?.get(AppConstants.Params.result)
                                            ?.let { resultStr ->
                                                val tasks =
                                                    json.decodeFromString<List<WorkSelectionModel>>(
                                                        resultStr
                                                    )
                                                workList.clear()
                                                workList.addAll(tasks)

                                            }
                                    }
                                }
                            } else if (response.orig_action == AppConstants.SendingAction.ACCEPT_TASK) {
                                if (response.transaction == acceptTaskTransactionId) {
                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS1234) {
                                        showProcessing = false
                                        val task = workList.first()
                                        launch {
                                            _uiEventsFlow.emit(TaskAccepted(task))
                                        }
                                        isWorkAccepted = true
                                        workList.clear()

                                        response.params?.get(AppConstants.Params.result)
                                            ?.let { resultStr ->
                                                val tasks =
                                                    json.decodeFromString<List<WorkSelectionModel>>(
                                                        resultStr
                                                    )
                                                workList.addAll(tasks)

                                            }
                                    } else if (response.result?.code == AppConstants.WarningCodes.WARN007) {
                                        showWarningDialogue = true
                                    }
                                }
                            } else if (response.orig_action == AppConstants.SendingAction.WARNING_CONFIRMATION) {

                                if (response.transaction == listWorkTransactionId) {
                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS327) {
                                        showProcessing = false
                                        response.params?.get(AppConstants.Params.result)
                                            ?.let { resultStr ->
                                                val tasks =
                                                    json.decodeFromString<List<WorkSelectionModel>>(
                                                        resultStr
                                                    )
                                                workList.clear()
                                                workList.addAll(tasks)

                                            }
                                    }
                                } else if (response.result?.code == AppConstants.SuccessCodes.SUCCESS1234) {
                                    showProcessing = false

                                    val task = workList.first()
                                    launch {
                                        _uiEventsFlow.emit(TaskAccepted(task))
                                    }
                                    isWorkAccepted = true
                                    workList.clear()

                                    response.params?.get(AppConstants.Params.result)
                                        ?.let { resultStr ->
                                            val tasks =
                                                json.decodeFromString<List<WorkSelectionModel>>(
                                                    resultStr
                                                )
                                            workList.addAll(tasks)

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

    fun acceptWarning() {
        showConfirmationDialogue =
            showConfirmationDialogue.copy(show = false)
        listWorkTransactionId = showConfirmationDialogue.transactionId
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.WARNING_CONFIRMATION,
            type = AppConstants.Type.WMS_ACTION,
            transaction = listWorkTransactionId,
            params = getMap {
                showConfirmationDialogue.params
                    ?.forEach { (key, value) ->
                        put(key, value)
                    }

            }
        )
        sendMessage(json.encodeToString(paramsModel))

    }

    init {
        startResponseListening()
    }

    data class TaskAccepted(val task: WorkSelectionModel) : UiEvents

}



