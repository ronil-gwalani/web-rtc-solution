package com.renxo.user.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renxo.user.R
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
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString


class CheckInTrailerVM : ViewModel(), WebSocketInterface by WebSocketInterfaceImpl() {
    private val _uiEventsFlow = MutableSharedFlow<UiEvents>()
    val uiEventsFlow: Flow<UiEvents> = _uiEventsFlow

    //               Transaction IDs               //
    private var checkInTrailerTransactionId = ""

    //............................................//
    var trailerNo by mutableStateOf("")
    var dockNo by mutableStateOf("")
    val dockFocusRequester = FocusRequester()


    init {
        startResponseListening()
    }


    private fun startResponseListening() {
         viewModelScope.launch {  
            response.collect { response ->
                launch {
                    try {
                        if (response.type == AppConstants.ReceivingType.RESPONSE) {
                            if (response.orig_action == AppConstants.SendingAction.SUBMIT_WORK_FLOW) {
                                if (response.transaction == checkInTrailerTransactionId) {
                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS522) {
                                        _uiEventsFlow.emit(OnFinish)
                                    }
                                }
                            }
                        } else if (response.type == AppConstants.ReceivingType.UI) {
                            if (response.action == AppConstants.ReceivingActions.WORKFLOW) {
                                if (response.orig_action == AppConstants.SendingAction.CHECK_IN_TRAILER) {
                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS2001) {
                                        response.params?.let { params ->
                                            if (params.containsKey(AppConstants.Params.workflow)) {
                                                params[AppConstants.Params.workflow]?.let {
                                                    val extraParams = HashMap<String, String?>()
                                                    if (params.containsKey(AppConstants.Params.ibd)) {
                                                        extraParams[AppConstants.Params.ibd] =
                                                            params[AppConstants.Params.ibd]
                                                    }
                                                    _uiEventsFlow.emit(
                                                        Navigate(
                                                            it,
                                                            json.encodeToString(extraParams),
                                                            response.transaction.toString()
                                                        )
                                                    )
                                                }
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

    fun checkIn(showError: (Int) -> Unit) {
        if (trailerNo.isEmpty()) {
            showError(R.string.enter_trailer_no)
        } else if (dockNo.isEmpty()) {
            showError(R.string.enter_dock_no)
            dockFocusRequester.requestFocus()
        } else {
            checkInTrailerTransactionId = getTransactionId()
            val paramsModel = ParamModel(
                action = AppConstants.SendingAction.CHECK_IN_TRAILER,
                type = AppConstants.Type.WMS_ACTION,
//                type = "custom_action",
                transaction = checkInTrailerTransactionId,
                params = getMap {
                    put(
                        AppConstants.Params.entityName,
                        AppConstants.EntityNames.INBOUND_DELIVERY
                    )
                    put(AppConstants.Params.transport_equipment, trailerNo)
                    put(AppConstants.Params.dock, dockNo)
                }
            )

            sendMessage(json.encodeToString(paramsModel))
        }
    }

    data object OnFinish : UiEvents
    data class Navigate(val questions: String, val extraParams: String, val transaction: String) :
        UiEvents

}


