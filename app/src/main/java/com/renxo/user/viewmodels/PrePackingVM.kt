package com.renxo.user.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renxo.user.R
import com.renxo.user.models.DataForPicking
import com.renxo.user.models.ParamModel
import com.renxo.user.navigation.UiEvents
import com.renxo.user.navigation.showSnackBar
import com.renxo.user.utils.AppConstants
import com.renxo.user.utils.getMap
import com.renxo.user.utils.getTransactionId
import com.renxo.user.utils.json
import com.renxo.user.webSocket.WebSocketInterface
import com.renxo.user.webSocket.WebSocketInterfaceImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch


class PrePackingVM : ViewModel(), WebSocketInterface by WebSocketInterfaceImpl() {
    //               Transaction IDs               //
    private var verifyIdTransactionId = ""
    private var decodeLpnTransactionId = ""

    //............................................//
    private val _uiEventsFlow = MutableSharedFlow<UiEvents>()
    val uiEventsFlow: Flow<UiEvents> = _uiEventsFlow
    var lpnInput by mutableStateOf("")
    var showStars by mutableStateOf(true)
    val preparingFocusRequester = FocusRequester()
    private var responseJob: Job? = null
    var packingLocation = ""

    init {
        startResponseListening()
    }

    fun decodeLpn() {
        if (lpnInput.isNotEmpty()) {
            decodeLpnTransactionId = getTransactionId()
            val paramsModel = ParamModel(
                action = AppConstants.SendingAction.DECODE_LPN,
                type = AppConstants.Type.WMS_ACTION,

                transaction = decodeLpnTransactionId,
                params = getMap {
                    put(
                        AppConstants.Params.entityName, AppConstants.EntityNames.INVENTORY
                    )
                    put(
                        AppConstants.Params.lpn, lpnInput
                    )
                },
            )
            sendMessage(json.encodeToString(paramsModel))
//            onSubmit()
        } else if (lpnInput.isEmpty()) {
             viewModelScope.launch {  
                _uiEventsFlow.emit(showSnackBar(R.string.please_fill_lpn))
            }
        }
    }

    fun sendData() {
        if (lpnInput.isNotEmpty()) {
            verifyIdTransactionId = getTransactionId()
            val paramsModel = ParamModel(
                action = AppConstants.SendingAction.VERIFY_LPN_FOR_PACKING,
                type = AppConstants.Type.WMS_ACTION,

                transaction = verifyIdTransactionId,
                params = getMap {
                    put(
                        AppConstants.Params.entityName, AppConstants.EntityNames.INVENTORY
                    )
                    put(
                        AppConstants.Params.lpn, lpnInput
                    )
                    put(
                        AppConstants.Params.packing_Location, packingLocation
                    )
                },
            )
            sendMessage(json.encodeToString(paramsModel))
//            onSubmit()
        } else if (lpnInput.isEmpty()) {
             viewModelScope.launch {  
                _uiEventsFlow.emit(showSnackBar(R.string.please_fill_lpn))
            }
        }
    }

    private fun startResponseListening() {
        responseJob =  viewModelScope.launch {  
            response.collect { response ->
                launch {
                    try {
                        if (response.type == AppConstants.ReceivingType.RESPONSE) {
                            if (response.orig_action == AppConstants.SendingAction.VERIFY_LPN_FOR_PACKING) {
                                if (response.transaction == verifyIdTransactionId) {
                                    // Pass the params to next screen
                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS201) {
                                        response.params?.let { params ->
                                            if (params[AppConstants.Params.operation] == AppConstants.Defaults.PACKING) {
                                                if (params.containsKey(AppConstants.Params.lpn)) {
                                                    params.getValue(AppConstants.Params.lpn)?.let {
                                                        _uiEventsFlow.emit(Navigate(it))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else if (response.orig_action == AppConstants.SendingAction.DECODE_LPN) {
                                if (response.transaction == decodeLpnTransactionId) {
                                    // Pass the params to next screen
                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS1201) {
                                        response.params?.let { params ->
                                            params[AppConstants.Params.result]?.let {
                                                showStars = false
                                                lpnInput = it
                                            }
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


    data class Navigate(val lpn: String) : UiEvents

    public override fun onCleared() {
        responseJob?.cancel()
        super.onCleared()
    }


}