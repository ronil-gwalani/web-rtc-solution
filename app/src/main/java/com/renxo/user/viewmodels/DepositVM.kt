package com.renxo.user.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renxo.user.R
import com.renxo.user.models.LpnItemsModel
import com.renxo.user.models.ParamModel
import com.renxo.user.navigation.UiEvents
import com.renxo.user.navigation.showSnackBar
import com.renxo.user.utils.AppConstants
import com.renxo.user.utils.ShackBarState
import com.renxo.user.utils.getMap
import com.renxo.user.utils.getTransactionId
import com.renxo.user.utils.json
import com.renxo.user.webSocket.WebSocketInterface
import com.renxo.user.webSocket.WebSocketInterfaceImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch

class DepositVM : ViewModel(), WebSocketInterface by WebSocketInterfaceImpl() {
    private val _uiEventsFlow = MutableSharedFlow<UiEvents>()
    val uiEventsFlow: Flow<UiEvents> = _uiEventsFlow.onSubscription {
        if (_uiEventsFlow.subscriptionCount.value == 1) {
            setupInit()
        }
    }

    var scannedItem by mutableStateOf("")
    var submitScan by mutableStateOf("")
    private val _selectArea = MutableStateFlow<String?>(null)
    val selectArea = _selectArea
    private var getAllDepositAreasTransactionId = ""
    private var depositTransactionId = ""
    private var submitDepositTransactionId = ""
    var selectAll by mutableStateOf(false)
    var suggestedLocation = ""
    var showSubmitDialogue by mutableStateOf(false)
    val list = mutableStateListOf<LpnItemsModel>()

    init {
        startResponseListening()
    }

    private fun setupInit() {
        listAllDepositAreas()
        viewModelScope.launch {
            _selectArea.collectLatest { area ->
                list.forEach { item ->
                    if (item.checked) {
                        area?.let {
                            updateItemCheckedState(item, it == item.work_area)
                        }
                    }
                }
                updateSelectAllState()
            }
        }
    }


    private fun getDepositInventory() {
        depositTransactionId = getTransactionId()
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.DEPOSIT_INVENTORY,
            type = AppConstants.Type.WMS_ACTION,
            transaction = depositTransactionId,
            params = getMap {
                put(
                    AppConstants.Params.entityName, AppConstants.EntityNames.INVENTORY
                )

            },
        )
        sendMessage(json.encodeToString(paramsModel))

    }

    fun confirmSubmit(selectedArea: String,list: List<String?>) {
        submitDepositTransactionId = getTransactionId()
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.SUBMIT_DEPOSIT,
            type = AppConstants.Type.WMS_ACTION,
            transaction = submitDepositTransactionId,
            params = getMap {
                put(
                    AppConstants.Params.entityName, AppConstants.EntityNames.INVENTORY
                )
                put(
                    AppConstants.Params.location, submitScan
                )
                put(
                    AppConstants.Params.lpn_ids, list
                )
                put(
                    AppConstants.Params.area, selectedArea
                )

            },
        )
        sendMessage(json.encodeToString(paramsModel))

    }

    private fun listAllDepositAreas() {
        getAllDepositAreasTransactionId = getTransactionId()
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.LIST_ALL_DEPOSIT_AREAS,
            type = AppConstants.Type.WMS_ACTION,
            transaction = getAllDepositAreasTransactionId,
            params = getMap {
                put(
                    AppConstants.Params.entityName, AppConstants.EntityNames.INVENTORY
                )

            },
        )
        sendMessage(json.encodeToString(paramsModel))

    }


    private fun startResponseListening() {
        viewModelScope.launch {
            response.collect { response ->
                launch {
                    try {
                        if (response.type == AppConstants.ReceivingType.RESPONSE) {
                            if (response.transaction == depositTransactionId) {
                                if (response.orig_action == AppConstants.SendingAction.DEPOSIT_INVENTORY) {
                                    // Pass the params to next screen
                                    if (response.result?.code?.contains(AppConstants.SuccessCodes.SUCCESS) == true) {
                                        response.params?.let { params ->
                                            suggestedLocation = params["location"].toString()

                                            showSubmitDialogue = true
                                        }
                                    }
                                }
                            } else if (response.transaction == getAllDepositAreasTransactionId) {
                                if (response.orig_action == AppConstants.SendingAction.LIST_ALL_DEPOSIT_AREAS) {
                                    // Pass the params to next screen
                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS555) {
                                        response.params?.let { params ->
                                            list.clear()
                                            params[AppConstants.Params.result]?.let {
                                                list.addAll(
                                                    json.decodeFromString<List<LpnItemsModel>>(
                                                        it
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            } else if (response.transaction == submitDepositTransactionId) {
                                if (response.orig_action == AppConstants.SendingAction.SUBMIT_DEPOSIT) {
                                    // Pass the params to next screen
                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS556) {

                                        showSubmitDialogue = false
                                        viewModelScope.launch {
                                            _uiEventsFlow.emit(
                                                showSnackBar(
                                                    R.string.deposit_submit_successfully,
                                                    ShackBarState.ShackBarType.POSITIVE
                                                )
                                            )
                                            _uiEventsFlow.emit(Finish)
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

//    private fun getList(): List<LpnItemsModel> {
//        return listOf(
//            LpnItemsModel(
//                lpn = "LPN1", work_area = "Area1", location = "Location1"
//            ),
//            LpnItemsModel(lpn = "LPN2", work_area = "HiBayEast"),
//            LpnItemsModel(lpn = "LPN3", work_area = "Area2"),
//            LpnItemsModel(lpn = "LPN4", work_area = "Area1"),
//            LpnItemsModel(lpn = "LPN5", work_area = "HiBayEast"),
//            LpnItemsModel(lpn = "LPN6", work_area = "Area2"),
//            LpnItemsModel(lpn = "LPN7", work_area = "Area1"),
//            LpnItemsModel(lpn = "LPN8", work_area = "HiBayEast"),
//            LpnItemsModel(lpn = "LPN9", work_area = "Area2"),
//            LpnItemsModel(lpn = "LPN10", work_area = "HiBayEast"),
//        ).also {
//
//            viewModelScope.launch {
//                delay(5000)
//                it.forEachIndexed { index, lpnItemsModel ->
//                    updateItemLocationState(lpnItemsModel, "COPD $index")
//                }
//            }
//        }
//    }


    fun updateItemCheckedState(item: LpnItemsModel, checked: Boolean) {
        val index = list.indexOf(item)
        if (index != -1) {
            list[index] = item.copy(checked = checked)
            updateSelectAllState()
        }
    }

    private fun updateItemLocationState(item: LpnItemsModel, value: String) {
        val index = list.indexOf(item)
        if (index != -1) {
            list[index] = item.copy(location = value)
        }
    }

    fun setSelectedArea(area: String?) {
        viewModelScope.launch {
            _selectArea.emit(area)
        }
    }

    fun handleSelectAll(checked: Boolean) {
        selectAll = checked
        updateAllCheckedStates()
    }

    private fun updateAllCheckedStates() {
        val currentArea = _selectArea.value
        list.forEachIndexed { index, item ->
            if (item.lpn?.contains(scannedItem) == true) {
                val shouldBeChecked = if (currentArea != null) {
                    // If area is selected, only check items in that area if selectAll is true
                    selectAll && item.work_area == currentArea
                } else {
                    // If no area selected, check all items if selectAll is true
                    selectAll
                }
                list[index] = item.copy(checked = shouldBeChecked)
            }
        }
    }

    fun updateSelectAllState() {
        val currentArea = _selectArea.value
        val relevantItems = if (currentArea != null) {
            list.filter { it.work_area == currentArea }
        } else {
            list
        }.filter { it.lpn?.contains(scannedItem) == true }

        selectAll = relevantItems.isNotEmpty() && relevantItems.all { it.checked }
    }

    fun itemScanned() {
        val item = list.find { it.lpn.equals(scannedItem, true) }
        item?.let {
            updateItemCheckedState(it, true)
        }

    }

    fun deposit() {
        val selectedList = list.filter { it.checked }
        if (selectedList.isNotEmpty()) {
            getDepositInventory()
        } else {
            viewModelScope.launch {
                _uiEventsFlow.emit(showSnackBar(R.string.Please_select_atleast_one_item))
            }
        }

    }


    fun handelAreaSelection(area: String?, selectedArea: String) {
        if (area == null) {
            setSelectedArea(selectedArea)
        } else {
            setSelectedArea(null)
        }
    }

    data object Finish : UiEvents

}