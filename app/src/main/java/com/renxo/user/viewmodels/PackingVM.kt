package com.renxo.user.viewmodels

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renxo.user.R
import com.renxo.user.dynamicUI.InputData
import com.renxo.user.models.BoxModel
import com.renxo.user.models.InitialLpnPayload
import com.renxo.user.models.InitializePackingWorkFlow
import com.renxo.user.models.ParamModel
import com.renxo.user.models.SubInventory
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
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch


class PackingVM : ViewModel(), WebSocketInterface by WebSocketInterfaceImpl() {
    private val _uiEventsFlow = MutableSharedFlow<UiEvents>()
    val uiEventsFlow: Flow<UiEvents> = _uiEventsFlow.onSubscription {
        if (_uiEventsFlow.subscriptionCount.value == 1) {
            fetchInitialInventory()
            fetchBoxes()
        }
    }


    private var verifyIdTransactionId = ""
    private var scannedInventoryTransactionId = ""
    private var boxesTransactionId = ""
    private var initPackingTransactionId = ""
    private var modifyPackingTransactionId = ""
    private var createCartonTransactionID = ""
    private var packInventoryTransactionID = ""
    private var partialSubmitTransactionId = ""
    private var updateInventoryTransactionID = ""
    private var modifyPackingWorkFlow = mutableListOf<InitializePackingWorkFlow>()
    var partialSubmitAllowed = false
        private set
    private var unPackInventoryTransactionID = ""


    val boxFocusRequester = FocusRequester()
    val skuFocusRequester = FocusRequester()
    var focus by mutableStateOf(false)


    var packingLocation = ""
    var productId by mutableStateOf("")
    var inventoryId = ""
    private var sourceId = ""
    var scannedField by mutableStateOf("")
    var inventoryLpn by mutableStateOf("")
    val uiElements = mutableStateListOf<InputData>()
    val inventoryList = mutableStateListOf<SubInventory?>()
    val scannedInventoryList = mutableStateListOf<SubInventory?>()
    var leftSide by mutableFloatStateOf(0.25f)
    var rightSide by mutableFloatStateOf(0.0f)
    var middleSide by mutableFloatStateOf(0.75f)
    var isPinned by mutableStateOf(false)
    var showOverLay by mutableStateOf(false)
    var leftSideExpended by mutableStateOf(false)
    var staticQuantity by mutableStateOf("")
    var quantityEditable by mutableStateOf(false)

    var suggestedBoxes = mutableStateListOf<BoxModel>()
    var selectedBox by mutableStateOf<BoxModel?>(null)

    var toSearchQuery by mutableStateOf("")
    var boxes = mutableStateListOf<BoxModel>()
    var showAddCartonDialog by mutableStateOf(false)
    var newLpn by mutableStateOf("")
    var lastAddedBox by mutableStateOf<String?>(null)
    var selectedSubInventory: SubInventory? = null
//    var packingInitializedId by mutableStateOf("")


    var showUnpackButtons by mutableStateOf(false)
    private fun addBox(id: String, lpn: String) {
        BoxModel(id, lpn, true).let {
            boxes.add(it)
            suggestedBoxes.add(it)
        }
    }

    fun manageLeftSide() {

        if (leftSideExpended) {
            leftSideExpended = false
            leftSide = 0.25f
        } else {
            leftSideExpended = true
            leftSide = 0.45f
            if (isPinned) {
                manageRightSide()
            }
        }

        middleSide = 1.0f - (leftSide + rightSide)

    }

    fun manageRightSide() {
        if (isPinned) {
            rightSide = 0.0f
            isPinned = false
            showOverLay = false
        } else {
            if (showOverLay) {
                isPinned = true
                showOverLay = false
                rightSide = 0.25f
                leftSide = 0.25f
                leftSideExpended = false
            }
        }
        middleSide = 1f - (rightSide + leftSide)

    }

    fun adjustFocus() {

        if (uiElements.isNotEmpty()) {
            if (!showUnpackButtons) {
                boxFocusRequester.requestFocus()
            }
        } else {
            skuFocusRequester.requestFocus()
        }
    }


    init {
        startResponseListening()
    }

    fun updateInputValue(index: Int, value: Any?) {
        uiElements[index] = uiElements[index].copy(value = value)
    }

    private fun makeNonEditable() {
        uiElements.forEachIndexed { index, _ ->
            uiElements[index] = uiElements[index].copy(editable = false)
        }
    }


    private fun fetchInitialInventory() {
        inventoryList.clear()
        verifyIdTransactionId = getTransactionId() + "1"
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.GET_PACKING_INVENTORY,
            type = AppConstants.Type.WMS_ACTION,
            transaction = verifyIdTransactionId,
            params = getMap {
                put(AppConstants.Params.entityName, AppConstants.EntityNames.INVENTORY)
                put(AppConstants.Params.lpn, inventoryLpn)
                put(AppConstants.Params.all, true)
                put(AppConstants.Params.packing_Location, packingLocation)
            })
        sendMessage(json.encodeToString(paramsModel))
    }

    fun fetchScannedInventory() {
        scannedInventoryList.clear()
        scannedInventoryTransactionId = getTransactionId() + "2"
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.GET_PACKING_INVENTORY,
            type = AppConstants.Type.WMS_ACTION,
            transaction = scannedInventoryTransactionId,
            params = getMap {
                put(AppConstants.Params.entityName, AppConstants.EntityNames.INVENTORY)
                put(AppConstants.Params.box_id, selectedBox?.id)
            })
        sendMessage(json.encodeToString(paramsModel))
    }

    private fun fetchBoxes() {
        boxesTransactionId = getTransactionId() + "3"
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.GET_INVENTORY_BOXES,
            type = AppConstants.Type.WMS_ACTION,
            transaction = boxesTransactionId,
            params = getMap {
                put(AppConstants.Params.entityName, AppConstants.EntityNames.INVENTORY)
                put(AppConstants.Params.isBox, true)
                put(AppConstants.Params.all, true)
            })
        sendMessage(json.encodeToString(paramsModel))
    }

    fun modifyInventory(quantity: String, subInventoryId: String) {
//        uiElements.clear()
        showOverLay = false
        modifyPackingTransactionId = getTransactionId()
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.MODIFY_PACKING, type = AppConstants.Type.WMS_ACTION,

            transaction = modifyPackingTransactionId, params = getMap {
                put(AppConstants.Params.entityName, AppConstants.EntityNames.INVENTORY)
                put(AppConstants.Params.product_id, productId)
                put(AppConstants.Params.box_id, selectedBox?.id)
                put(AppConstants.Params.quantityForFrontend, quantity)
                put(AppConstants.Params.inv_detail_id, subInventoryId)
            })
        showUnpackButtons = true
        sendMessage(json.encodeToString(paramsModel))
    }

    fun initializePacking(
        productId: String?,
        quantity: String,
        subInventoryId: String,
        subLpn: String?
    ) {
        if (subInventoryId.isEmpty() && productId.isNullOrEmpty()) {
             viewModelScope.launch {  
                _uiEventsFlow.emit(CustomPackingSnackBar(R.string.scan_code))
            }
            return
        }
        if (showOverLay) {
            showOverLay = false
        }
//        uiElements.clear()
        initPackingTransactionId = getTransactionId()
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.INITIALIZE_PACKING,
            type = AppConstants.Type.WMS_ACTION,
            transaction = initPackingTransactionId,
            params = getMap {
                put(AppConstants.Params.entityName, AppConstants.EntityNames.INVENTORY)
                put(AppConstants.Params.source_Id, sourceId)
                put(AppConstants.Params.quantityForFrontend, quantity)
                put(AppConstants.Params.sub_lpn, subLpn)
                put(AppConstants.Params.product_id, productId)
                if (subInventoryId.isNotEmpty()) {
                    put(AppConstants.Params.inv_detail_id, subInventoryId)
                }
            })
        showUnpackButtons = false
        sendMessage(json.encodeToString(paramsModel))
    }

    fun createCarton() {
        createCartonTransactionID = getTransactionId()
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.CREATE_CARTON, type = AppConstants.Type.WMS_ACTION,

            transaction = createCartonTransactionID, params = getMap {
                put(AppConstants.Params.entityName, AppConstants.EntityNames.INVENTORY)
                put(AppConstants.Params.created_lpn, newLpn)
                put(AppConstants.Params.source_Id, sourceId)
            })
        sendMessage(json.encodeToString(paramsModel))
    }


    fun doPartialSubmit() {
        getRequiredWorkflow()?.let { workFlowData ->
            partialSubmitTransactionId = getTransactionId()
            val paramsModel = ParamModel(
                action = AppConstants.SendingAction.PARTIAL_SUBMIT,
                type = AppConstants.Type.WMS_ACTION,
                transaction = partialSubmitTransactionId,
                params = getMap {
                    put(AppConstants.Params.entityName, AppConstants.EntityNames.INVENTORY)
                    put(AppConstants.Params.id_fields, workFlowData)
                    put(AppConstants.Params.product_id, productId)
                    put(AppConstants.Params.quantity, staticQuantity.toInt())
                    put(AppConstants.Params.source_Id, sourceId)

                })
            sendMessage(json.encodeToString(paramsModel))
        }
    }

    fun packInventory() {
        getRequiredWorkflow()?.let { workFlowData ->
            packInventoryTransactionID = getTransactionId()
            val paramsModel = ParamModel(
                action = AppConstants.SendingAction.PACK_INVENTORY,
                type = AppConstants.Type.WMS_ACTION,
                transaction = packInventoryTransactionID,
                params = getMap {
                    put(AppConstants.Params.entityName, AppConstants.EntityNames.INVENTORY)
                    put(AppConstants.Params.box_id, selectedBox?.id)
//                    put(AppConstants.Params.source_lpn, inventoryLpn)
                    put(AppConstants.Params.workflow, workFlowData)
                    put(AppConstants.Params.quantity, staticQuantity.toInt())
                    put(AppConstants.Params.product_id, productId)
                    put(AppConstants.Params.inv_detail_id, inventoryId)
                    put(AppConstants.Params.source_Id, sourceId)
                })
            sendMessage(json.encodeToString(paramsModel))
        }
    }

    fun updateInventory() {
        getRequiredWorkflow()?.let { workFlowData ->

            updateInventoryTransactionID = getTransactionId()
            val paramsModel = ParamModel(
                action = AppConstants.SendingAction.UPDATE_INVENTORY,
                type = AppConstants.Type.WMS_ACTION,

                transaction = updateInventoryTransactionID,
                params = getMap {
                    put(AppConstants.Params.entityName, AppConstants.EntityNames.INVENTORY)
                    put(AppConstants.Params.box_id, selectedBox?.id)
                    put(AppConstants.Params.source_Id, sourceId)
                    put(AppConstants.Params.workflow, workFlowData)
                    put(AppConstants.Params.quantity, staticQuantity.toInt())
                    put(AppConstants.Params.product_id, productId)
                    put(AppConstants.Params.inv_detail_id, inventoryId)
                })
            sendMessage(json.encodeToString(paramsModel))
        }
    }

    fun unPackInventory() {
        unPackInventoryTransactionID = getTransactionId()
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.UNPACK_INVENTORY,
            type = AppConstants.Type.WMS_ACTION,

            transaction = unPackInventoryTransactionID,
            params = getMap {
                put(AppConstants.Params.entityName, AppConstants.EntityNames.INVENTORY)
                put(AppConstants.Params.box_id, selectedBox?.id)
                put(AppConstants.Params.source_Id, sourceId)
                put(AppConstants.Params.quantity, staticQuantity.toInt())
                put(AppConstants.Params.workflow, modifyPackingWorkFlow)
                put(AppConstants.Params.product_id, productId)
                put(AppConstants.Params.inv_detail_id, inventoryId)
            })
        sendMessage(json.encodeToString(paramsModel))
    }


    private fun getRequiredWorkflow(): List<InitializePackingWorkFlow>? {
        if (staticQuantity.isEmpty() || staticQuantity.toLongOrNull() == null) {
            CustomPackingSnackBar(
                R.string.please_provide, "Valid Quantity"
            )
            return null
        }
        uiElements.forEach {
            if (it.required && (it.value == null || it.value.toString()
                    .isEmpty() || it.value.toString() == "null")
            ) {

                 viewModelScope.launch {  
                    _uiEventsFlow.emit(
                        CustomPackingSnackBar(
                            R.string.please_provide, it.placeholder
                        )
                    )
                }

                return null
            }
        }

        val attributes = uiElements.map {
            InitializePackingWorkFlow(
                attribute_name = it.placeholder,
                value = if (it.value.toString() == "null") "" else
                    it.value.toString()
            )
        }
        return attributes

    }


    private fun startResponseListening() {
         viewModelScope.launch {  
            response.collect { response ->
                launch {
                    try {
                        if (response.type == AppConstants.ReceivingType.RESPONSE) {


                            if (response.orig_action == AppConstants.SendingAction.INITIALIZE_PACKING) {
                                if (response.transaction == initPackingTransactionId) {
                                    try {
                                        uiElements.clear()

                                        if (response.result?.code == AppConstants.SuccessCodes.SUCCESS547) {
                                            productId =
                                                response.params?.get(AppConstants.Params.product_id)
                                                    ?: ""
                                            response.params?.get(AppConstants.Params.inv_detail_id)
                                                ?.let {
                                                    inventoryId = it

                                                }
                                            partialSubmitAllowed = false
                                            quantityEditable = true
                                            response.params?.get(AppConstants.Params.id_fields)
                                                ?.let { inventory ->
                                                    val receivingOutput =
                                                        json.decodeFromString<List<InitializePackingWorkFlow>>(
                                                            inventory
                                                        )
                                                    partialSubmitAllowed = true
                                                    receivingOutput.let {
                                                        staticQuantity =
                                                            response.params[AppConstants.Params.quantityForFrontend]
                                                                ?: "1"
                                                        _uiEventsFlow.emit(GetDataForUI(it))
                                                    }
                                                }
                                            response.params?.get(AppConstants.Params.others)
                                                ?.let { inventory ->
                                                    val receivingOutput =
                                                        json.decodeFromString<List<InitializePackingWorkFlow>>(
                                                            inventory
                                                        )
                                                    makeNonEditable()
                                                    partialSubmitAllowed = false
                                                    receivingOutput.let {
                                                        staticQuantity =
                                                            response.params[AppConstants.Params.quantityForFrontend]
                                                                ?: "1"
                                                        _uiEventsFlow.emit(GetDataForUI(it))
                                                    }
                                                }
                                            if (response.params?.containsKey(AppConstants.Params.suggested_box) == true) {
                                                response.params[AppConstants.Params.suggested_box]?.let { suggestedBoxesData ->
                                                    val suggestions =
                                                        json.decodeFromString<List<BoxModel>>(
                                                            suggestedBoxesData
                                                        )
                                                    suggestedBoxes.clear()
                                                    suggestedBoxes.addAll(suggestions)

                                                    if (!suggestedBoxes.contains(selectedBox) && selectedBox != null) {
                                                        selectedBox = null
                                                        scannedInventoryList.clear()
                                                    }


                                                    if (suggestedBoxes.isEmpty()) {
                                                        showAddCartonDialog = true
                                                    } else {
                                                        boxFocusRequester.requestFocus()
                                                    }
                                                }

                                            } else {
                                                _uiEventsFlow.emit(HideKeyBoard)
                                            }
                                        } else {
                                            _uiEventsFlow.emit(CustomPackingSnackBar(R.string.no_fields_available))
                                        }

                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        _uiEventsFlow.emit(HideKeyBoard)
                                        _uiEventsFlow.emit(showSnackBar(R.string.error_occured_while_parsing))

                                    }
                                }

                            } else if (response.orig_action == AppConstants.SendingAction.PARTIAL_SUBMIT) {
                                if (response.transaction == partialSubmitTransactionId) {
                                    try {
                                        if (response.result?.code == AppConstants.SuccessCodes.SUCCESS552) {
                                            partialSubmitAllowed = false
//                                            quantityEditable = false
                                            response.params?.get(AppConstants.Params.inv_detail_id)
                                                ?.let {
                                                    inventoryId = it
                                                }
                                            response.params?.get(AppConstants.Params.others)
                                                ?.let { inventory ->
                                                    val receivingOutput =
                                                        json.decodeFromString<List<InitializePackingWorkFlow>>(
                                                            inventory
                                                        )
                                                    makeNonEditable()
                                                    partialSubmitAllowed = false
                                                    receivingOutput.let {
                                                        staticQuantity =
                                                            response.params[AppConstants.Params.quantityForFrontend]
                                                                ?: "1"
                                                        _uiEventsFlow.emit(GetDataForUI(it))
                                                    }
                                                }
                                            if (response.params?.containsKey(AppConstants.Params.suggested_box) == true) {
                                                response.params[AppConstants.Params.suggested_box]?.let { suggestedBoxesData ->
                                                    val suggestions =
                                                        json.decodeFromString<List<BoxModel>>(
                                                            suggestedBoxesData
                                                        )
                                                    suggestedBoxes.clear()
                                                    suggestedBoxes.addAll(suggestions)

                                                    if (!suggestedBoxes.contains(selectedBox) && selectedBox != null) {
                                                        selectedBox = null
                                                        scannedInventoryList.clear()
                                                    }


                                                    if (suggestedBoxes.isEmpty()) {
                                                        showAddCartonDialog = true
                                                    } else {
                                                        boxFocusRequester.requestFocus()
                                                    }
                                                }

                                            } else {
                                                _uiEventsFlow.emit(HideKeyBoard)
                                            }

                                        } else {
                                            _uiEventsFlow.emit(CustomPackingSnackBar(R.string.no_fields_available))
                                        }

                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        _uiEventsFlow.emit(HideKeyBoard)
                                        _uiEventsFlow.emit(showSnackBar(R.string.error_occured_while_parsing))

                                    }
                                }

                            } else if (response.orig_action == AppConstants.SendingAction.MODIFY_PACKING) {
                                if (response.transaction == modifyPackingTransactionId) {
                                    try {
                                        uiElements.clear()
                                        if (response.result?.code == AppConstants.SuccessCodes.SUCCESS547) {
                                            productId =
                                                response.params?.get(AppConstants.Params.product_id)
                                                    ?: ""
                                            response.params?.get(AppConstants.Params.id_fields)
                                                ?.let { inventory ->
                                                    val receivingOutput =
                                                        json.decodeFromString<List<InitializePackingWorkFlow>>(
                                                            inventory
                                                        )
                                                    partialSubmitAllowed = true
                                                    receivingOutput.let {
                                                        modifyPackingWorkFlow.apply {
                                                            clear()
                                                            addAll(it)
                                                        }
                                                        staticQuantity =
                                                            response.params[AppConstants.Params.quantityForFrontend]
                                                                ?: "1"
                                                        _uiEventsFlow.emit(GetDataForUI(it))
                                                    }
                                                }
                                            quantityEditable = true
                                            response.params?.get(AppConstants.Params.others)
                                                ?.let { inventory ->
                                                    val receivingOutput =
                                                        json.decodeFromString<List<InitializePackingWorkFlow>>(
                                                            inventory
                                                        )
                                                    receivingOutput.let {
                                                        modifyPackingWorkFlow.apply {
                                                            addAll(it)
                                                        }
                                                        staticQuantity =
                                                            response.params[AppConstants.Params.quantityForFrontend]
                                                                ?: "1"
                                                        _uiEventsFlow.emit(GetDataForUI(it.map { item ->
                                                            if (item.identification == true) {
                                                                item.copy(
                                                                    prompted = false,
                                                                    mandatory = false
                                                                )
                                                            } else {
                                                                item
                                                            }
                                                        }))
                                                    }
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
                            } else if (response.orig_action == AppConstants.SendingAction.GET_INVENTORY_BOXES) {
                                if (response.transaction == boxesTransactionId) {
                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS1595) {

                                        response.params?.get(AppConstants.Params.result)
                                            ?.let { lpn ->
                                                json.decodeFromString<List<BoxModel>>(lpn).let {
                                                    boxes.addAll(
                                                        it
                                                    )
                                                }
                                            }
                                    }
                                }
                            } else if (response.orig_action == AppConstants.SendingAction.GET_PACKING_INVENTORY) {
                                if (response.result?.code == AppConstants.SuccessCodes.SUCCESS1594) {
                                    if (response.transaction == verifyIdTransactionId || response.transaction == scannedInventoryTransactionId) {
                                        response.params?.get(AppConstants.Params.result)
                                            ?.let { data ->
                                                with(json.decodeFromString<InitialLpnPayload>(data)) {

                                                    sub_inventory?.let {
                                                        if (response.transaction == verifyIdTransactionId) {
                                                            id?.let {
                                                                sourceId = it
                                                            }

//                                                    inventoryList.clear()
                                                            inventoryList.addAll(it)
                                                        } else if (response.transaction == scannedInventoryTransactionId) {
//                                                    scannedInventoryList.clear()
                                                            scannedInventoryList.addAll(it)
                                                        } else {
                                                        }
                                                    }
                                                }
                                            }
                                    }
                                }
                            } else if (response.orig_action == AppConstants.SendingAction.CREATE_CARTON) {
                                if (response.transaction == createCartonTransactionID) {
                                    showAddCartonDialog = false
                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS002) {
                                        addBox(
                                            response.params?.get(AppConstants.Params.id).toString(),
                                            newLpn
                                        )
                                        lastAddedBox = newLpn
                                        newLpn = ""
                                    }
                                }
                            } else if (response.orig_action == AppConstants.SendingAction.PACK_INVENTORY) {
                                if (response.transaction == packInventoryTransactionID) {
                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS548) {
                                        toSearchQuery = ""
                                        staticQuantity = ""
                                        productId = ""
                                        inventoryId = ""
                                        quantityEditable = false
                                        uiElements.clear()
                                        suggestedBoxes.clear()
                                        skuFocusRequester.requestFocus()
                                        fetchScannedInventory()
                                        fetchInitialInventory()
                                    } else {
                                        selectedBox = null
                                    }
                                }
                            } else if (response.orig_action == AppConstants.SendingAction.UPDATE_INVENTORY) {
                                if (response.transaction == updateInventoryTransactionID) {

                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS551) {
                                        uiElements.clear()
                                        staticQuantity = ""
                                        productId = ""
                                        inventoryId = ""

                                        quantityEditable = false
                                        suggestedBoxes.clear()
                                        fetchScannedInventory()
                                        fetchInitialInventory()
                                    }
                                }
                            } else if (response.orig_action == AppConstants.SendingAction.UNPACK_INVENTORY) {
                                if (response.transaction == unPackInventoryTransactionID) {

                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS549) {
                                        uiElements.clear()
                                        staticQuantity = ""
                                        productId = ""
                                        inventoryId = ""
                                        quantityEditable = false
                                        suggestedBoxes.clear()
                                        fetchScannedInventory()
                                        fetchInitialInventory()
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


    data class CustomPackingSnackBar(
        @StringRes val message: Int,
        val placeHolder: String = "",
        val type: ShackBarState.ShackBarType = ShackBarState.ShackBarType.NEGATIVE
    ) : UiEvents

    data object HideKeyBoard : UiEvents

    data class GetDataForUI(val list: List<InitializePackingWorkFlow?>) : UiEvents

}