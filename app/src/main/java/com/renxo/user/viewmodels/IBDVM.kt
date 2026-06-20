package com.renxo.user.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renxo.user.R
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
import kotlinx.serialization.encodeToString


class IBDVM : ViewModel(), WebSocketInterface by WebSocketInterfaceImpl() {

    private val _uiEventsFlow = MutableSharedFlow<UiEvents>()
    val uiEventsFlow: Flow<UiEvents> = _uiEventsFlow

    val operationsListState = mutableStateListOf(
        R.string.dock,
        R.string.receive_stage,
        R.string.trailer_no,
        R.string.ibd_no,
        R.string.order_no
    )

    private var verifyIdTransactionId = ""
    var lpnInput by mutableStateOf("")
    var selectedOperation by mutableStateOf<Int?>(null)
    private var responseJob: Job? = null
    val ibdFocusRequester =  FocusRequester()
    var isExpanded by  mutableStateOf(false)


    private val operationsMap = mapOf(
        R.string.dock to "location",
        R.string.receive_stage to "location",
        R.string.trailer_no to "transport_equipment",
        R.string.ibd_no to "ibd",
        R.string.order_no to "order",
    )

    init {
        startResponseListening()
    }

    override fun onCleared() {
        responseJob?.cancel()
        super.onCleared()

    }


    fun sendData() {
        if (lpnInput.isNotEmpty() && selectedOperation != null) {
            verifyIdTransactionId = getTransactionId()
            val paramsModel = ParamModel(
                action = AppConstants.SendingAction.VERIFY_ID_FOR_RECEIVING,
                type = AppConstants.Type.WMS_ACTION,

                transaction = verifyIdTransactionId,
                params = getMap {
                    put(
                        AppConstants.Params.entityName, AppConstants.EntityNames.INBOUND_DELIVERY
                    )
                    put(
                        operationsMap[selectedOperation].toString(),
                        lpnInput
                    )
                },
            )
            sendMessage(json.encodeToString(paramsModel))
        } else if (lpnInput.isEmpty()) {
             viewModelScope.launch {  
                _uiEventsFlow.emit(showSnackBar(R.string.please_fill_lpn))
            }
        } else {
             viewModelScope.launch {  
                _uiEventsFlow.emit(showSnackBar(R.string.please_select_operation))
            }
        }
    }


    private fun startResponseListening() {
        responseJob =  viewModelScope.launch {  
            response.collect { response ->
                launch {
                    try {
                        if (response.type == AppConstants.ReceivingType.RESPONSE) {
                            if (response.orig_action == AppConstants.SendingAction.VERIFY_ID_FOR_RECEIVING) {
                                if (response.transaction == verifyIdTransactionId) {
                                    // Pass the params to next screen
                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS500) {
                                        response.params?.let { params ->
                                            val filteredParams =
                                                params.filterValues { it is String }
                                                    .mapValues { it.value as String }
                                            _uiEventsFlow.emit(
                                                VerificationDone(
                                                    json.encodeToString(
                                                        filteredParams
                                                    )
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

    fun clear() {
        onCleared()
    }

    data class VerificationDone(val params: String) : UiEvents


}