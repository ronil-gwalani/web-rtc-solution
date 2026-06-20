package com.renxo.user.viewmodels

import android.content.Context
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
import com.renxo.user.dynamicUI.InputData
import com.renxo.user.dynamicUI.InputType
import com.renxo.user.dynamicUI.Requirements
import com.renxo.user.models.ParamModel
import com.renxo.user.models.ReceivingTrailerOutput
import com.renxo.user.models.ReceivingTrailerWorkFlow
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


class ReceivingTrailerVM : ViewModel(), WebSocketInterface by WebSocketInterfaceImpl() {

    //               Transaction IDs               //
    private var getOtherFieldsTransactionId = ""
    private var submitTrailerTransactionId = ""
    private var decodeLpnTransactionId = ""
    private var decodeSkuTransactionId = ""
    private val _uiEventsFlow = MutableSharedFlow<UiEvents>()
    val uiEventsFlow: Flow<UiEvents> = _uiEventsFlow
    //............................................//


    var lpnNum by mutableStateOf("")
    var showLpnStar by mutableStateOf(true)
    var skuNumber by mutableStateOf("")
    var showSkuStar by mutableStateOf(true)
    var selectionDialogue by mutableStateOf<SelectionDialogueModel?>(null)
    val trailerFocusRequester = FocusRequester()

    val uiElements = mutableStateListOf<InputData>()
    val initialParams = hashMapOf<String, String?>()


    fun updateInputValue(index: Int, value: Any?) {
        uiElements[index] = uiElements[index].copy(value = value)
    }

    fun updateInitialParams(params: HashMap<String, String?>) {
        initialParams.clear()
        initialParams.putAll(params)
    }


    init {
        startResponseListening()
    }


    fun fetchOtherFields() {
        if (lpnNum.isNotEmpty() || skuNumber.isNotEmpty()) {
            getOtherFieldsTransactionId = getTransactionId()
            val paramsModel = ParamModel(
                action = AppConstants.SendingAction.IDENTIFY_INVENTORY,
                type = AppConstants.Type.WMS_ACTION,

                transaction = getOtherFieldsTransactionId,
                params = getMap {
                    initialParams.forEach { (key, value) ->
                        put(key, value)
                    }

                    put(AppConstants.Params.entityName, AppConstants.EntityNames.INBOUND_DELIVERY)
                    lpnNum.takeIf { it.isNotEmpty() }?.let {
                        put(AppConstants.Params.lpn, it)
                    }
                    skuNumber.takeIf { it.isNotEmpty() }?.let {
                        put(AppConstants.Params.sku, it)
                    }

                }
            )
            sendMessage(json.encodeToString(paramsModel))
        }
    }

    fun decodeLpn() {
        if (lpnNum.isNotEmpty()) {
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
                        AppConstants.Params.lpn, lpnNum
                    )
                },
            )
            sendMessage(json.encodeToString(paramsModel))
        }
    }

    fun decodeSku() {
        if (skuNumber.isNotEmpty()) {
            decodeSkuTransactionId = getTransactionId()
            val paramsModel = ParamModel(
                action = AppConstants.SendingAction.DECODE_SKU,
                type = AppConstants.Type.WMS_ACTION,

                transaction = decodeSkuTransactionId,
                params = getMap {
                    put(
                        AppConstants.Params.entityName, AppConstants.EntityNames.INVENTORY
                    )
                    put(
                        AppConstants.Params.sku, skuNumber
                    )
                },
            )
            sendMessage(json.encodeToString(paramsModel))

        }
    }


    fun submitTraler() {
        submitTrailerTransactionId = getTransactionId()

        val data = getMap {
            uiElements.forEach {
                it.value?.let { value -> put(it.placeholder, value) }
            }

        }


        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.SUBMIT_INVENTORY_FOR_RECEIVING,
            type = AppConstants.Type.WMS_ACTION,

            transaction = submitTrailerTransactionId,
            params = getMap {
                put(AppConstants.Params.entityName, AppConstants.EntityNames.INVENTORY)
            },
            payload = data
        )

        sendMessage(json.encodeToString(paramsModel))
    }

    private fun startResponseListening() {
        viewModelScope.launch {
            response.collect { response ->
                launch {
                    try {
                        if (response.type == AppConstants.ReceivingType.UI) {

                            if (response.orig_action == AppConstants.SendingAction.IDENTIFY_INVENTORY) {
                                if (response.transaction == getOtherFieldsTransactionId) {
                                    try {
                                        if (response.result?.code == AppConstants.SuccessCodes.SUCCESS545) {
                                            if (response.params?.containsKey(AppConstants.Params.workflow) == true) {
                                                response.params[AppConstants.Params.workflow]?.let { workflow ->
                                                    val receivingOutput =
                                                        json.decodeFromString<ReceivingTrailerOutput>(
                                                            workflow
                                                        )
                                                    val lpnObject =
                                                        receivingOutput.attributes?.find { it?.attribute_name == AppConstants.Params.lpn }
                                                    val skuObject =
                                                        receivingOutput.attributes?.find { it?.attribute_name == AppConstants.Params.sku }
                                                    skuObject?.default_value?.takeIf { it.isNotEmpty() }
                                                        .let {
                                                            skuNumber
                                                        }
                                                    lpnObject?.default_value?.takeIf { it.isNotEmpty() }
                                                        .let {
                                                            lpnNum
                                                        }
                                                    val filteredAttributes =
                                                        receivingOutput.attributes?.filter {
                                                            it?.attribute_name !in listOf(
                                                                AppConstants.Params.lpn,
                                                                AppConstants.Params.sku
                                                            )
                                                        }
                                                    receivingOutput.let {
                                                        uiElements.clear()
                                                        filteredAttributes?.let { it1 ->
                                                            _uiEventsFlow.emit(GetDataForUI(it1))
                                                        }


                                                    }
                                                }
                                            } else {
                                                _uiEventsFlow.emit(
                                                    showSnackBar(
                                                        R.string.no_fields_available
                                                    )
                                                )

                                            }
                                        } else if (response.result?.code == AppConstants.SuccessCodes.MULTIPLE_IBD_FOUND) {
                                            val list =
                                                response.params?.get(AppConstants.Params.multiple_ibds)
                                                    ?.let {
                                                        json.decodeFromString<List<String>>(it)
                                                    }
                                            if (!list.isNullOrEmpty()) {

                                                selectionDialogue = SelectionDialogueModel(
                                                    title = R.string.select_ibd,
                                                    list = list,
                                                    key = AppConstants.Params.ibd
                                                )


                                            }
                                        } else if (response.result?.code == AppConstants.SuccessCodes.MULTIPLE_ORDERS_FOUND) {
                                            val list =
                                                response.params?.get(AppConstants.Params.multiple_orders)
                                                    ?.let {
                                                        json.decodeFromString<List<String>>(it)
                                                    }
                                            if (!list.isNullOrEmpty()) {
                                                selectionDialogue = SelectionDialogueModel(
                                                    title = R.string.select_order,
                                                    list = list,
                                                    key = AppConstants.Params.order
                                                )


                                            }
                                        } else {
                                            _uiEventsFlow.emit(HideKeyBoard)

                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        _uiEventsFlow.emit(HideKeyBoard)
                                        _uiEventsFlow.emit(showSnackBar(R.string.error_occured_while_parsing))


                                    }
                                }
                            }
                        } else if (response.type == AppConstants.ReceivingType.RESPONSE) {
                            if (response.orig_action == AppConstants.SendingAction.SUBMIT_INVENTORY_FOR_RECEIVING) {
                                if (response.transaction == submitTrailerTransactionId) {
                                    try {
                                        if (response.result?.code == AppConstants.SuccessCodes.SUCCESS122) {
                                            _uiEventsFlow.emit(OnRefresh(initialParams))
                                        } else {
                                            _uiEventsFlow.emit(HideKeyBoard)

                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        _uiEventsFlow.emit(HideKeyBoard)
                                        _uiEventsFlow.emit(showSnackBar(R.string.error_occured_while_parsing))

                                    }
                                }
                            } else if (response.orig_action == AppConstants.SendingAction.DECODE_LPN) {
                                if (response.transaction == decodeLpnTransactionId) {
                                    // Pass the params to next screen
                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS1201) {
                                        response.params?.let { params ->
                                            params[AppConstants.Params.lpn]?.let {
                                                showLpnStar = false
                                                lpnNum = it
                                            }
                                        }
                                    }
                                }

                            } else if (response.orig_action == AppConstants.SendingAction.DECODE_SKU) {
                                if (response.transaction == decodeSkuTransactionId) {
                                    // Pass the params to next screen
                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS1901) {
                                        response.params?.let { params ->
                                            params[AppConstants.Params.sku]?.let {
                                                showSkuStar = false
                                                skuNumber = it
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

    fun getDataForUI(
        context: Context,
        questions: List<ReceivingTrailerWorkFlow?>
    ): ArrayList<InputData> {
        val list = ArrayList<InputData>()
        questions.forEach { question ->
            when (question?.data_type?.lowercase() ?: "unknown") {

                DataTypes.money, DataTypes.weekday, DataTypes.month, DataTypes.statuses, DataTypes.velocity -> {
                    val default =
                        question?.list_of_values
                    list.add(
                        InputData(
                            placeholder = question?.attribute_name
                                ?: "",
                            type = InputType.DROPDOWN,
                            requirements = Requirements.DropDownRequirements(

                                error = context.getString(R.string.Please_select_an_answer),
                                options = default
                            ),
                            required = question?.mandatory ?: true,
                            editable = true
                        )
                    )
                }

                DataTypes.id -> {
                    list.add(
                        InputData(
                            placeholder = question?.attribute_name
                                ?: "",
                            type = InputType.EDIT_TEXT,
                            requirements = Requirements.EditTextRequirements(

                                error = context.getString(R.string.Please_provide_a_valid_input),
                            ),
                            value = question?.default_value, required = question?.mandatory ?: true,
                            editable = true

                        )
                    )
                }

                DataTypes.text -> {
                    list.add(
                        InputData(
                            placeholder = question?.attribute_name
                                ?: "",
                            type = InputType.EDIT_TEXT,
                            requirements = Requirements.EditTextRequirements(

                                error = context.getString(R.string.Please_provide_a_valid_input),
                            ),
                            value = question?.default_value,
                            required = question?.mandatory ?: true,
                            editable = true

                        )
                    )
                }

                DataTypes.number -> {
                    list.add(
                        InputData(
                            placeholder = question?.attribute_name
                                ?: "",
                            type = InputType.EDIT_TEXT_NUMBER,
                            requirements = Requirements.EditTextNumberRequirements(

                                error = context.getString(R.string.Please_provide_a_valid_input),
                            ),
                            required = question?.mandatory ?: true,
                            value = question?.default_value,
                            editable = true

                        )
                    )
                }


                DataTypes.date -> {
                    list.add(
                        InputData(
                            placeholder = question?.attribute_name
                                ?: "",
                            type = InputType.DATE, requirements = Requirements.DateRequirements(

                                error = "Please provide a valid input",
                            ),

                                hint = question?.default_value,
                            required = question?.mandatory ?: false, editable = true

                        )
                    )
                }

                else -> {
                    Log.e("getData", "Unknown or unsupported data type: ${question?.data_type}")
                }
            }
        }
        return list
    }

    private object DataTypes {

        const val id = "id"               // String AlphaNumeric
        const val text = "text"           //String
        const val number = "number"       // Number
        const val money = "money"         // DropDown
        const val velocity = "velocity"         // DropDown
        const val address = "address"     // TODO
        const val date = "date"           // Calendars
        const val dateTime = "datetime"    // Calendar And Timing
        const val time = "time"            // Time Picker
        const val weekday = "weekday"      // DropDown
        const val month = "month"          // DropDown
        const val toggle = "toggle"        // Radio
        const val range = "range"          // TODO
        const val statuses = "statuses"          // DropDown


    }


    data class SelectionDialogueModel(
        @StringRes val title: Int,
        val key: String,
        val list: List<String>,
//        val value: String? = null,
    )

    data class GetDataForUI(val list: List<ReceivingTrailerWorkFlow?>) : UiEvents
    data object HideKeyBoard : UiEvents
    data class OnRefresh(val params: HashMap<String, String?>) : UiEvents

}
