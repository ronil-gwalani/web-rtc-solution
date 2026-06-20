package com.renxo.user.viewmodels

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renxo.user.R

import com.renxo.user.models.ParamModel
import com.renxo.user.models.UnloadDeliveryOutput
import com.renxo.user.models.UnloadDeliveryPayloadResult
import com.renxo.user.models.UnloadDeliveryResult
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
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString


class UnloadTrailerVM : ViewModel(), WebSocketInterface by WebSocketInterfaceImpl() {
    //               Transaction IDs               //
    private var locationsTransactionId = ""
    private var unloadTrailerTransactionId = ""
    private var inBoundDeliveryTransactionId = ""

    //............................................//

    private val _uiEventsFlow = MutableSharedFlow<UiEvents>()
    val uiEventsFlow: Flow<UiEvents> = _uiEventsFlow
        .onSubscription {
            if (_uiEventsFlow.subscriptionCount.value == 1) {
                getLocations()
            }
        }

    val unloadTrailerFocusRequester = FocusRequester()

    var selectedLocation by mutableStateOf<String?>(null)
    var trailerNo by mutableStateOf("")
    var uiState by mutableStateOf<UiState>(UiState.Idle)
    val locationNames = mutableStateListOf<String>()
    var isLocationCalled = false
    var expanded by mutableStateOf(false)



    init {
        startResponseListening()
    }


    fun getLocations() {
        if (isLocationCalled) {
            return
        }
        isLocationCalled = true
        locationsTransactionId = getTransactionId()
         viewModelScope.launch {  
            _uiEventsFlow.emit(GetPagingParams(getMap {
                this[AppConstants.Params.entityName] =
               AppConstants.EntityNames.LOCATION
                this[
                    AppConstants.Params.type] =
                    AppConstants.Type.STAGING

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

    fun unloadTrailer() {
        unloadTrailerTransactionId = getTransactionId()
        val params = ParamModel(
            action = AppConstants.SendingAction.UNLOAD_TRAILER,
            type = AppConstants.Type.WMS_ACTION,

            transaction = unloadTrailerTransactionId,
            params = getMap {
                put(
                    AppConstants.Params.entityName,
                    AppConstants.EntityNames.INBOUND_DELIVERY
                )
                put(AppConstants.Params.transport_equipment, trailerNo)
                put(AppConstants.Params.location, selectedLocation ?: "")
            })
        sendMessage(json.encodeToString(params))
        uiState = UiState.Loading // Show loading state
    }

    fun validate() {
        if (trailerNo.isNotEmpty()) {
            inBoundDeliveryTransactionId = getTransactionId()
            val params = ParamModel(
                action = AppConstants.SendingAction.VALIDATE_TRAILER,
                type = AppConstants.Type.WMS_ACTION,
                transaction = inBoundDeliveryTransactionId,
                params = getMap {
                    put(
                        AppConstants.Params.entityName,
                        AppConstants.EntityNames.INBOUND_DELIVERY
                    )
                    put(
                        AppConstants.Params.all, true
                    )
                    put(AppConstants.Params.transport_equipment, trailerNo)
                })


            sendMessage(json.encodeToString(params))

            uiState = UiState.Loading

        } else {
             viewModelScope.launch {  
                _uiEventsFlow.emit(showSnackBar(R.string.please_fill_all_the_fields))
            }
        }
    }

    private fun startResponseListening() {
         viewModelScope.launch {  
            response.collect { response ->
                launch {
                    try {

                        if (response.type == AppConstants.ReceivingType.RESPONSE) {
                            if (response.orig_action == AppConstants.SendingAction.GET_LOCATIONS) {
                                response.params?.let { payload ->
                                    if (response.transaction == locationsTransactionId) {
                                        if (response.result?.code == AppConstants.SuccessCodes.SUCCESS1590) {
                                            response.params?.let { it1 ->
                                                _uiEventsFlow.emit(SetPagingParams(it1))
                                            }
                                            if (payload.containsKey(AppConstants.Params.result)) {
                                                payload[AppConstants.Params.result]?.let { value ->
                                                    val result =
                                                        json.decodeFromString<List<UnloadDeliveryPayloadResult>>(
                                                            value
                                                        )
                                                    result.mapNotNull { it.location }
                                                        .let { items ->
                                                            if (items.isNotEmpty()) {
                                                                locationNames.addAll(items)
                                                            }

                                                        }

                                                }
                                            }
                                        }

//                                            }
                                    }

                                }

                            } else if (response.orig_action == AppConstants.SendingAction.VALIDATE_TRAILER) {
                                if (response.transaction == inBoundDeliveryTransactionId) {
                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS1591) {
                                        response.params?.let { params ->

                                            if (params.containsKey(AppConstants.Params.result)) {
                                                params[AppConstants.Params.result]?.let {
                                                    val results =
                                                        json.decodeFromString<List<UnloadDeliveryResult>>(
                                                            it.toString()
                                                        )
                                                    val validResults =
                                                        results.filter { result ->
                                                            result.location != null || result.transport_equipment != null || result.supplier != null || result.ibd != null
                                                        }
                                                    if (validResults.isNotEmpty()) {
                                                        uiState =
                                                            UiState.Success(validResults)
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Log.e("startRes", ": ", )
                                        uiState = UiState.Idle // Show loading state
                                    }
                                }


                            } else if (response.orig_action == AppConstants.SendingAction.UNLOAD_TRAILER) {
                                if (response.transaction == unloadTrailerTransactionId) {


                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS323) {
                                        response.params?.let { params ->
//                                            if (params.containsKey(AppConstants.Params.entityName) && params[AppConstants.Params.entityName] == AppConstants.EntityNames.INBOUND_DELIVERY) {
                                            if (params.containsKey(AppConstants.Params.result)) {
                                                params[AppConstants.Params.result]?.let {
                                                    val result =
                                                        json.decodeFromString<UnloadDeliveryOutput>(
                                                            it.toString()
                                                        )
                                                    result.appointments_update?.status?.let {
                                                        uiState = UiState.Idle
//                                                        snackBarState.showPositiveSnackBar(
//                                                            successMessage
//                                                        )
                                                        trailerNo = ""
                                                        selectedLocation = null
                                                    }
                                                }
                                            }
//                                            }

                                        }
                                    }
                                }
                            }
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        if (uiState is UiState.Loading) {
                            uiState = UiState.Error(R.string.error_parsing_response)
                        }
                    }
                }
            }
        }
    }

    sealed class UiState {
        data object Idle : UiState() // Initial state
        data object Loading : UiState() // Loading state
        data class Success(val data: List<UnloadDeliveryResult>) : UiState() // Success state
        data class Error(@StringRes val message: Int) : UiState() // Error state
    }

    data class GetPagingParams(
        val map: HashMap<String, Any?>,
        val resultCallback: (HashMap<String, Any?>) -> Unit
    ) : UiEvents

    data class SetPagingParams(val map: HashMap<String, String?>) : UiEvents

}