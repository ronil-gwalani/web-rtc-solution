package com.renxo.user.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renxo.user.R
import com.renxo.user.models.LocationResult
import com.renxo.user.models.ParamModel
import com.renxo.user.models.Result
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString


class PalletBuildingVM : ViewModel(), WebSocketInterface by WebSocketInterfaceImpl() {

    private val _uiEventsFlow = MutableSharedFlow<UiEvents>()
    val uiEventsFlow: Flow<UiEvents> = _uiEventsFlow

    //               Transaction IDs               //
    private var locationsTransactionId = ""
    private var startPalletBuildingTransactionId = ""
    private var suggestedLpnsTransactionId = ""
    private var createLpnsTransactionId = ""
    private var submitPalletTransactionId = ""
    var showWarningDialog by mutableStateOf(false)
    var warningMessage = ""
    val palletFocusRequester = FocusRequester()


    //............................................//
    private var selectedArea = ""

    val updatedSelectedArea = MutableStateFlow("")
//    val userId = MutableStateFlow("")

    var lpnNumber by mutableStateOf("")
    var preferredLpn by mutableStateOf("")
    var processingMessage by mutableStateOf("")
    var suggestedLpns by mutableStateOf<List<String>>(emptyList())
    var showAdditionalUI by mutableStateOf(false)
    val locations = mutableStateListOf<String>()  // List of locations
    var selectedLocation by mutableStateOf("")  // Selected location
    var showLocationDialog by mutableStateOf(false)  // Control initial dialog visibility
    var showCreateLpnDialog by mutableStateOf(false)
    var showProcessingDialog: Boolean? by mutableStateOf(null)


    init {
        setupAreaSelection()
        startResponseListening()
    }

    private fun setupAreaSelection() {
         viewModelScope.launch {  
            updatedSelectedArea.collectLatest {
                if (it.isNotEmpty()) {
                    if (it != selectedArea) {
                        startPalletBuilding()
                    }
                } else {
                    _uiEventsFlow.emit(UpdateSelectedAreaType(AppConstants.Type.PROCESSING))
                }
            }

        }


    }

    fun lpnCreateRequest() {
        createLpnsTransactionId = getTransactionId()
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.GENERATE_LPN,
            type = AppConstants.Type.WMS_ACTION,

            transaction = createLpnsTransactionId,
            params = getMap {
                put(AppConstants.Params.entityName, AppConstants.EntityNames.SEQUENCE)
            },
        )
        sendMessage(json.encodeToString(paramsModel))
    }


    private fun startPalletBuilding() {
        selectedArea = updatedSelectedArea.value
        startPalletBuildingTransactionId = getTransactionId()
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.START_PALLET_BUILD,
            type = AppConstants.Type.WMS_ACTION,

            transaction = startPalletBuildingTransactionId,
            params = getMap {
                put(AppConstants.Params.entityName, AppConstants.EntityNames.AREA)
                put(AppConstants.Params.workarea, selectedArea)
            },
        )
        sendMessage(json.encodeToString(paramsModel))
    }

    private fun fetchSuggestedLpns() {
        if (lpnNumber.isNotEmpty()) {
            suggestedLpnsTransactionId = getTransactionId()

            val paramsModel = ParamModel(
                action = AppConstants.SendingAction.LPN_FOR_PALLETS,
                type = AppConstants.Type.WMS_ACTION,

                transaction = suggestedLpnsTransactionId,
                params = getMap {
                    put(AppConstants.Params.entityName, AppConstants.EntityNames.INVENTORY)
                    put(AppConstants.Params.lpn, lpnNumber)
                })
            sendMessage(json.encodeToString(paramsModel))
        }
    }

    fun fetchLPNs() {
        if (lpnNumber.isEmpty()) {
             viewModelScope.launch {  
                _uiEventsFlow.emit(
                    showSnackBar(R.string.enter_box_id)
                )
            }
        } else {
            fetchSuggestedLpns()
        }
    }

    fun submitPallet(updateDuplicate: Boolean = false) {
        if (lpnNumber.isNotEmpty() && preferredLpn.isNotEmpty()) {
            submitPalletTransactionId = getTransactionId()
            val paramsModel = ParamModel(
                action = AppConstants.SendingAction.PROCESS_LPN_SUBMISSION,
                type = AppConstants.Type.WMS_ACTION,

                transaction = submitPalletTransactionId,
                params = getMap {
                    put(AppConstants.Params.entityName, AppConstants.EntityNames.INVENTORY)
                    put(
                        AppConstants.Params.source_lpn,
                        lpnNumber
                    ) // Box ID entered by user
                    put(
                        AppConstants.Params.target_lpn,
                        preferredLpn
                    ) // Pallet ID entered by user
                    if (updateDuplicate) {
                        put(AppConstants.Params.update_duplicate, true)
                    }
                }
            )
            // Send the JSON payload to the backend
            sendMessage(json.encodeToString(paramsModel))
        }
    }

    fun fetchLocations() {
        locationsTransactionId = getTransactionId()
         viewModelScope.launch {  
            _uiEventsFlow.emit(GetPagingParams(getMap {
                this[AppConstants.Params.entityName] = AppConstants.EntityNames.LOCATION
                this[AppConstants.Params.areas] = updatedSelectedArea.value
            }, resultCallback = { resultParams ->
                val paramsModel = ParamModel(
                    action = AppConstants.SendingAction.GET_LOCATIONS,
                    type = AppConstants.Type.WMS_ACTION,

                    transaction = locationsTransactionId,
                    params = resultParams
                )
                sendMessage(json.encodeToString(paramsModel))
            }))
        }


    }


    private fun startResponseListening() {
         viewModelScope.launch {  
            response.collect { response ->
                launch {
                    try {
                        if (response.type == AppConstants.ReceivingType.RESPONSE) {
                            if (response.orig_action == AppConstants.SendingAction.START_PALLET_BUILD) {
                                if (response.transaction == startPalletBuildingTransactionId) {
                                    if (response.result?.code?.contains(AppConstants.WarningCodes.WARNING) == true) {
                                        _uiEventsFlow.emit(ClearSelectedArea)
                                        response.result.let {
                                            _uiEventsFlow.emit(
                                                GetRequiredMessage(
                                                    response.result,
                                                    GetRequiredMessage.START_PALLET
                                                )
                                            )
                                        }


                                    } else if (response.result?.code?.contains(AppConstants.SuccessCodes.SUCCESS) == true) {

                                        _uiEventsFlow.emit(UpdateSelectedAreaType(AppConstants.Type.PROCESSING))
                                        locations.clear()
                                        fetchLocations()
                                    } else if (response.result?.code?.contains(AppConstants.ErrorCodes.ERROR) == true) {
                                        _uiEventsFlow.emit(ClearSelectedArea)
                                    }
                                }
                            } else if (response.orig_action == AppConstants.SendingAction.GET_LOCATIONS) {
                                if (response.transaction == locationsTransactionId) {
                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS1590) {
                                        response.params?.let { params ->
                                            _uiEventsFlow.emit(SetPagingParams(params))
                                        }
                                        response.params?.let { payload ->
                                            val result = payload[AppConstants.Params.result]?.let {
                                                json.decodeFromString<List<LocationResult>?>(it)
                                            }

                                            result?.let { results ->
                                                results.mapNotNull { it.location }.let {
                                                    if (it.isNotEmpty()) {
                                                        locations.addAll(it)
                                                        showLocationDialog =
                                                            locations.size > 1 // Show dialog if locations exist
                                                    }
                                                } // Extract locations

                                            }
                                        }
                                    }
                                }
                            } else if (response.orig_action == AppConstants.SendingAction.LPN_FOR_PALLETS) {
                                if (response.transaction == suggestedLpnsTransactionId) {
                                    showAdditionalUI = false  // First set to false
                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS511) {
                                        response.params?.get(AppConstants.Params.available_pallets)
                                            ?.let {
                                                val list =
                                                    json.decodeFromString<List<String>?>(it.toString())
                                                if (list != null) {
                                                    suggestedLpns = list
                                                    showAdditionalUI = true
                                                }
                                            }
                                    }
                                }
                            } else if (response.orig_action == AppConstants.SendingAction.GENERATE_LPN) {
                                if (response.transaction == createLpnsTransactionId) {
                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS002) {

                                        response.params?.get(AppConstants.Params.created_lpn)
                                            ?.let { newLpn ->
                                                suggestedLpns = listOf(newLpn.toString())
                                                showAdditionalUI =
                                                    true   // Then set to true again
                                            }
                                    }
                                }
                            } else if (response.orig_action == AppConstants.SendingAction.PROCESS_LPN_SUBMISSION) {
                                if (response.transaction == submitPalletTransactionId) {
                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS199) {
                                        _uiEventsFlow.emit(Navigate)
                                    } else if (response.result?.code == AppConstants.WarningCodes.WARN001) {
                                        _uiEventsFlow.emit(
                                            GetRequiredMessage(
                                                response.result,
                                                GetRequiredMessage.PROCESS_PALLET
                                            )
                                        )

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


    data class UpdateSelectedAreaType(val type: String?) : UiEvents
    data class GetRequiredMessage(val result: Result, val type: String) : UiEvents {
        companion object {
            const val START_PALLET = "StartPallet"
            const val PROCESS_PALLET = "ProcessLpn"

        }
    }

    data class SetPagingParams(val map: HashMap<String, String?>) : UiEvents
    data class GetPagingParams(
        val map: HashMap<String, Any?>,
        val resultCallback: (HashMap<String, Any?>) -> Unit
    ) : UiEvents

    data object ClearSelectedArea : UiEvents
    data object Navigate : UiEvents

}