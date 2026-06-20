package com.renxo.user.viewmodels

import android.content.Context
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renxo.user.R
import com.renxo.user.dynamicUI.InputData
import com.renxo.user.dynamicUI.InputType
import com.renxo.user.dynamicUI.Requirements
import com.renxo.user.models.DataForPicking
import com.renxo.user.models.InvSrcId
import com.renxo.user.models.ParamModel
import com.renxo.user.models.PickingAttribute
import com.renxo.user.models.PickingPacks
import com.renxo.user.models.TaskInfo
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


data class PickingFixFields(
    val location: String = "",
    val lpn: String = "",
    val subLpn: String = "",
    val productId: String = "",
    val pack: PickingPacks? = null,
    val quantity: Int? = null,
)


class PickingVM : ViewModel(), WebSocketInterface by WebSocketInterfaceImpl() {

    private var initializePickingTransactionId = ""
    private var partialSubmitTransactionId = ""
    private var totalSubmitTransactionId = ""

    var staticFieldsEditable by mutableStateOf(true)
        private set

    val taskListIds = mutableListOf<TaskInfo?>()
    var dataForPicking: DataForPicking? by mutableStateOf(DataForPicking())
    var dataForPickingValues: PickingFixFields by mutableStateOf(PickingFixFields())
    var partialSubmitAllowed = false
    var submitLpnTxt by mutableStateOf("")
    var showSubmitButton by mutableStateOf(false)
        private set
    private val _uiEventsFlow = MutableSharedFlow<UiEvents>()
    val uiEventsFlow: Flow<UiEvents> = _uiEventsFlow.onSubscription {
        if (_uiEventsFlow.subscriptionCount.value == 1) {

        }
    }


    // LPN management
    var showCreateLpnDialog by mutableStateOf(false)
    private var selectedLpn by mutableStateOf("")

    private val lpnOptions = mutableStateListOf<String>()

    // For dynamic serial number counts based on quantity


    val uiElements = mutableStateListOf<InputData>()

    init {
        startResponseListening()
    }


    private fun initializePicking() {
        initializePickingTransactionId = getTransactionId()
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.INITIALIZE_PICKING,
            type = AppConstants.Type.WMS_ACTION,
            transaction = initializePickingTransactionId,
            params = getMap {
                put(AppConstants.Params.entityName, AppConstants.Params.inventory)
                put(AppConstants.Params.product_id, dataForPicking?.product_id)
                put(AppConstants.Params.allocation_id, dataForPicking?.allocation_id)
                put(AppConstants.Params.inv_detail_id, dataForPicking?.inv_detail_id)
                put(AppConstants.Params.travel_sequence, dataForPicking?.travel_sequence)
                put(AppConstants.Params.task_id, dataForPicking?.task_id)
                put(AppConstants.Params.area, dataForPicking?.area)
            }
        )
        sendMessage(json.encodeToString(paramsModel))
    }

    fun createNewLpn(lpnValue: String, isPrint: Boolean = false) {
        if (lpnValue.isNotEmpty()) {
            if (!lpnOptions.contains(lpnValue)) {
                lpnOptions.add(lpnValue)
            }
            selectedLpn = lpnValue
            showCreateLpnDialog = false

            uiElements.forEachIndexed { index, element ->
                if (element.placeholder.equals("LPN", ignoreCase = true)) {
                    uiElements[index] = element.copy(value = lpnValue)
                }
            }

            if (isPrint) {
                viewModelScope.launch {
                    _uiEventsFlow.emit(SuccessMessage(R.string.lpn_print_request_sent))
                }
            }
        }
    }

    private fun startResponseListening() {
        viewModelScope.launch {
            response.collect { response ->
                if (response.type == AppConstants.ReceivingType.RESPONSE) {
                    if (response.transaction == initializePickingTransactionId) {
                        if (response.orig_action == AppConstants.SendingAction.INITIALIZE_PICKING) {
                            if (response.result?.code == AppConstants.SuccessCodes.SUCCESS552) {
                                response.params?.get(AppConstants.Params.id_fields)
                                    ?.let { workflow ->
                                        try {
                                            val workflowData =
                                                json.decodeFromString<List<PickingAttribute>>(
                                                    workflow
                                                )
                                            _uiEventsFlow.emit(GetDataForUI(workflowData))
                                        } catch (e: Exception) {

                                            _uiEventsFlow.emit(ErrorMessage(R.string.error_occured_while_parsing))
                                        }
                                    }
                            }
                        }
                    } else if (response.transaction == partialSubmitTransactionId) {
                        if (response.orig_action == AppConstants.SendingAction.PARTIAL_PICKING_SUBMIT) {
                            if (response.result?.code == AppConstants.SuccessCodes.SUCCESS553) {
                                partialSubmitAllowed = false
                                showSubmitButton = true
                                response.params?.get(AppConstants.Params.others)
                                    ?.let { workflow ->
                                        val workflowData =
                                            json.decodeFromString<List<PickingAttribute>>(
                                                workflow
                                            )
                                        staticFieldsEditable = false


                                        _uiEventsFlow.emit(
                                            GetDataForUI(
                                                workflowData,
                                                false
                                            )
                                        )
                                    }
                            }
                        }
                    } else if (response.transaction == totalSubmitTransactionId) {
                        if (response.orig_action == AppConstants.SendingAction.SUBMIT_PICKING) {
                            if (response.result?.code == AppConstants.SuccessCodes.SUCCESS558) {
                                submitLpnTxt = ""
                                showSubmitButton = false
                                response.params?.let { params ->
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
                                    taskListIds.removeAt(0)
                                    _uiEventsFlow.emit(
                                        OpenPickingScreen(
                                            data,
                                            workflowData
                                        )
                                    )


                                }
                            } else if (response.result?.code == AppConstants.SuccessCodes.SUCCESS554) {
                                _uiEventsFlow.emit(
                                    OnComplete
                                )
                            }
                        }
                    }
                }
            }
        }
    }


    fun doPartialSubmit(context: Context) {
        getRequiredWorkflow(context)?.let { params ->
            partialSubmitTransactionId = getTransactionId()
            val paramsModel = ParamModel(
                action = AppConstants.SendingAction.PARTIAL_PICKING_SUBMIT,
                type = AppConstants.Type.WMS_ACTION,
                transaction = partialSubmitTransactionId,
                params = getMap {
                    put(AppConstants.Params.entityName, AppConstants.EntityNames.INVENTORY)
                    params.forEach {
                        put(it.key, it.value)
                    }
//                    put(AppConstants.Params.product_id, productId)
//                    put(AppConstants.Params.quantity, staticQuantity.toInt())
//                    put(AppConstants.Params.source_Id, sourceId)

                })
            Log.e("doPartialSubmit", ": $paramsModel")
//            sendMessage(json.encodeToString(paramsModel))
        }
    }

    fun completeSubmit(context: Context) {

        getRequiredWorkflow(context)?.let { params ->
            totalSubmitTransactionId = getTransactionId()
            val paramsModel = ParamModel(
                action = AppConstants.SendingAction.SUBMIT_PICKING,
                type = AppConstants.Type.WMS_ACTION,
                transaction = totalSubmitTransactionId,
                params = getMap {
                    put(AppConstants.Params.entityName, AppConstants.EntityNames.INVENTORY)
//                    put(AppConstants.Params.task_id, taskListIds.firstOrNull()?.task_id)
//                    put(AppConstants.Params.task_group_id, taskListIds.firstOrNull()?.task_group_id)
                    put(AppConstants.Params.task_id_list, getTwoItems(taskListIds))
                    put(AppConstants.Params.allocation_id, dataForPicking?.allocation_id)
                    put(AppConstants.Params.inv_detail_id, dataForPicking?.inv_detail_id)
                    put(AppConstants.Params.travel_sequence, dataForPicking?.travel_sequence)
                    put(AppConstants.Params.area, dataForPicking?.area)
                    put(AppConstants.Params.to_lpn, submitLpnTxt)

                    params.forEach {
                        put(it.key, it.value)
                    }
                })
            sendMessage(json.encodeToString(paramsModel))
        }
    }

    private fun getTwoItems(list: List<TaskInfo?>): List<TaskInfo?> {
        return if (list.size >= 2) {
            list.subList(0, 2)
        } else {
            list
        }
    }

    private fun getRequiredWorkflow(context: Context): HashMap<String, Any?>? {
        when {
            dataForPickingValues.location.isEmpty() -> {
                viewModelScope.launch {
                    _uiEventsFlow.emit(
                        ShowSnackBar(
                            R.string.please_provide, context.getString(R.string.location)
                        )
                    )

                }
                return null

            }

            dataForPickingValues.lpn.isEmpty() -> {
                viewModelScope.launch {
                    _uiEventsFlow.emit(
                        ShowSnackBar(
                            R.string.please_provide, context.getString(R.string.lpn_number)
                        )
                    )
                }
                return null

            }

            dataForPickingValues.subLpn.isEmpty() -> {
                viewModelScope.launch {
                    _uiEventsFlow.emit(
                        ShowSnackBar(
                            R.string.please_provide, context.getString(R.string.sub_lpn)
                        )
                    )
                }
            }

            dataForPickingValues.productId.isEmpty() -> {
                viewModelScope.launch {
                    _uiEventsFlow.emit(
                        ShowSnackBar(
                            R.string.please_provide, context.getString(R.string.product_id)
                        )
                    )
                }
                return null

            }

            dataForPickingValues.quantity.toString().toIntOrNull() == null -> {
                viewModelScope.launch {
                    _uiEventsFlow.emit(
                        ShowSnackBar(
                            R.string.please_provide, context.getString(R.string.quantity)
                        )
                    )
                }
                return null

            }

            else -> {
                uiElements.forEach {
                    if (it.required && (it.value == null || it.value.toString()
                            .isEmpty() || it.value.toString() == "null")
                    ) {
                        viewModelScope.launch {
                            _uiEventsFlow.emit(
                                ShowSnackBar(
                                    R.string.please_provide, it.placeholder
                                )
                            )
                        }

                        return null
                    }
                }
            }


        }

        val attributes = uiElements.filter { it.identification != null }.map {
            PickingAttribute(
                identification = it.identification,
                attribute_name = it.placeholder,
                value = if (it.value.toString() == "null") "" else
                    it.value.toString()
            )
        }

        return getMap {
            put(AppConstants.Params.id_fields, attributes)
            put(
                AppConstants.Params.location,
                dataForPickingValues.location
            )
            put(
                AppConstants.Params.lpn,
                dataForPickingValues.lpn
            )
            put(
                AppConstants.Params.sub_lpn,
                dataForPickingValues.subLpn
            )
            put(
                AppConstants.Params.product_id,
                dataForPickingValues.productId
            )
            put(
                AppConstants.Params.quantity,
                dataForPickingValues.quantity
            )

        }

    }

    fun getDataForUI(
        context: Context, questions: List<PickingAttribute?>?, showHints: Boolean,
    ) {
        val list = ArrayList<InputData>()
        questions?.forEach { question ->
            val toShow =
                !(question?.identification == false && question.mandatory == true && question.prompted == false && question.value?.isNotEmpty() == true)
            val value = if (!showHints) question?.value else null
            val editable = true
//            if (question?.identification == true && !question.value.isNullOrEmpty()) false else
//                (question?.prompted == true) || (question?.prompted == false && question.mandatory == true && question.value.isNullOrEmpty())
            when (question?.data_type?.lowercase() ?: "unknown") {

                DataTypes.money, DataTypes.weekday, DataTypes.month, DataTypes.statuses, DataTypes.family, DataTypes.velocity -> {
                    val default = question?.list_of_values
                    list.add(
                        InputData(
                            type = InputType.DROPDOWN,
                            requirements = Requirements.DropDownRequirements(
                                error = context.getString(
                                    R.string.please_provide,
                                    question?.attribute_name
                                ),
                                options = default,
                            ),
                            hint = question?.value,
                            toShow = toShow,
                            placeholder = question?.attribute_name ?: "",
                            required = question?.mandatory ?: true,
                            editable = editable,
                            identification = question?.identification, value = value
                        )
                    )
                }

                DataTypes.id, DataTypes.string, DataTypes.text -> {

                    list.add(
                        InputData(
                            type = InputType.EDIT_TEXT,
                            requirements = Requirements.EditTextRequirements(
                                error = context.getString(
                                    R.string.please_provide,
                                    question?.attribute_name
                                ),
                            ),
                            hint = question?.value,
                            toShow = toShow,
                            placeholder = question?.attribute_name ?: "",
                            required = question?.mandatory ?: true,
                            editable = editable,
                            identification = question?.identification,
                            showLiableOutside = showHints,
                            value = value
                        )
                    )
                }


                DataTypes.number -> {
                    list.add(
                        InputData(
                            type = InputType.EDIT_TEXT_NUMBER,
                            requirements = Requirements.EditTextNumberRequirements(
                                error = context.getString(
                                    R.string.please_provide,
                                    question?.attribute_name
                                ),
                            ),
                            hint = question?.value,
                            toShow = toShow,
                            placeholder = question?.attribute_name ?: "",
                            required = question?.mandatory ?: true,
                            editable = editable,
                            identification = question?.identification,
                            showLiableOutside = showHints,
                            value = value
                        )
                    )
                }

                DataTypes.date -> {
                    list.add(
                        InputData(
                            type = InputType.DATE,
                            requirements = Requirements.DateRequirements(
                                error = context.getString(
                                    R.string.please_provide,
                                    question?.attribute_name
                                ),
                            ),
                            hint = question?.value,
                            showLiableOutside = showHints,
                            toShow = toShow,
                            placeholder = question?.attribute_name ?: "",
                            required = question?.mandatory ?: false,
                            editable = editable,
                            identification = question?.identification,
                            value = value
                        )
                    )
                }

                DataTypes.datetime -> {
                    list.add(
                        InputData(
                            type = InputType.DATE_TIME,
                            requirements = Requirements.DateTimeRequirements(
                                error = context.getString(
                                    R.string.please_provide,
                                    question?.attribute_name
                                ),
                            ),
                            hint = question?.value,
                            showLiableOutside = showHints,
                            toShow = toShow,
                            placeholder = question?.attribute_name ?: "",
                            required = question?.mandatory ?: false,
                            editable = editable,
                            identification = question?.identification,
                            value = value
                        )
                    )
                }

                else -> {
                    Log.e("getData", "Unknown or unsupported data type: ${question?.data_type}")
                }
            }
        }
        uiElements.addAll(
            list
        )
    }

    fun setUpScreenForPicking(
        context: Context,
        data: DataForPicking,
        workflowData: List<PickingAttribute>?,
    ) {
        partialSubmitAllowed
        uiElements.clear()
        dataForPicking = data
        dataForPickingValues = PickingFixFields()
        partialSubmitAllowed = true
        getDataForUI(context, workflowData, true)
    }


    data class GetDataForUI(val list: List<PickingAttribute?>, val showHints: Boolean = true) :
        UiEvents

    data class ShowSnackBar(
        @StringRes val message: Int,
        val placeHolder: String = "",
    ) : UiEvents

    data class OpenPickingScreen(
        val data: DataForPicking,
        val workflowData: List<PickingAttribute>?,
    ) : UiEvents

    object HideKeyBoard : UiEvents
    object OnComplete : UiEvents
    data class ErrorMessage(val messageResId: Int) : UiEvents
    data class SuccessMessage(val messageResId: Int) : UiEvents
    private object DataTypes {
        const val id = "id"               // String AlphaNumeric
        const val text = "text"           //String
        const val string = "string"           //String
        const val number = "number"       // Number
        const val money = "money"         // DropDown
        const val velocity = "velocity"         // DropDown
        const val address = "address"     // TODO
        const val date = "date"           // Calendars
        const val datetime = "datetime"    // Calendar And Timing
        const val time = "time"            // Time Picker
        const val weekday = "weekday"      // DropDown
        const val month = "month"          // DropDown
        const val family = "family"          // DropDown
        const val toggle = "toggle"        // Radio
        const val range = "range"          // TODO
        const val statuses = "statuses"          // DropDown
    }
}




