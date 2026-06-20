package com.renxo.user.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renxo.user.R
import com.renxo.user.models.InitialTaskInfo
import com.renxo.user.models.MainMenu
import com.renxo.user.models.MenuOptionModel
import com.renxo.user.models.MessageHandling
import com.renxo.user.models.ParamModel
import com.renxo.user.models.Result
import com.renxo.user.models.TaskItem
import com.renxo.user.models.WorkSelectionModel
import com.renxo.user.navigation.NavRouts
import com.renxo.user.navigation.OptionMenuRouts
import com.renxo.user.navigation.UiEvents
import com.renxo.user.navigation.navigateTo
import com.renxo.user.navigation.showSnackBar
import com.renxo.user.screens.DefaultLangDialogue
import com.renxo.user.screens.ShowDialogue
import com.renxo.user.utils.AppConstants
import com.renxo.user.utils.ShackBarState
import com.renxo.user.utils.WindowInfo
import com.renxo.user.utils.getMap
import com.renxo.user.utils.getRequiredMessage
import com.renxo.user.utils.getTransactionId
import com.renxo.user.utils.json
import com.renxo.user.utils.preferenceManager
import com.renxo.user.utils.showToast
import com.renxo.user.webSocket.WebSocketInterface
import com.renxo.user.webSocket.WebSocketInterfaceImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch


class HomeVM : ViewModel(), WebSocketInterface by WebSocketInterfaceImpl() {
    private val _uiEventsFlow = MutableSharedFlow<UiEvents>()
    val uiEventsFlow: Flow<UiEvents> = _uiEventsFlow.onSubscription {
        if (_uiEventsFlow.subscriptionCount.value == 1) {
            setUpInit()
        }
    }

    //        Transaction IDs               //
    private var workAreaTransactionId = ""
    private var menuTransactionId = ""
    private var renameTransactionId = ""
    private var ownerChangeTransactionId = ""
    private var startPackingTransactionId = ""
    private var equipmentTransactionId = ""
    private var findWorkTransactionId = ""
    private var startAppTransactionId = ""

    //............................................

    var showCalculator by mutableStateOf(false)

    // Navigation
    val navigationItems = mutableStateListOf<DrawerNavItem>()
    var unableGesture by mutableStateOf(false)
    var showLanguageChangeDialogue by mutableStateOf(DefaultLangDialogue())

    // Message Dialogue
    var messageDialog by mutableStateOf(ShowDialogue())
        private set

    // Area Related
    var selectedArea by mutableStateOf("")
    val selectedAreaType = MutableStateFlow<String?>(null)
    val workAreaList = mutableStateListOf<String>()
    var areaDropDownExpanded by mutableStateOf(false)


    var showConfirmationDialogueForWork by mutableStateOf(
        ShowNextHopDialogue()
    )

    var findWorkWarning by mutableStateOf(ShowDialogue())
        private set
    private var initialTaskInfo = InitialTaskInfo()

    // Equipment
    var selectedEquipment by mutableStateOf("")
    val equipmentList = mutableStateListOf<String>()
    val tasksList = mutableStateListOf<TaskItem>()
    var equipmentExpanded by mutableStateOf(false)


    var showRenameLpnDialog by mutableStateOf(false)
    var showProfileMenu by mutableStateOf(false)
    var showOwnerShipChangeDialog by mutableStateOf(false)

    var onCloseFromCustomScreen = {}


    val workFlowsList = mutableStateListOf<WorkFlowVM>()


    val menuOptionModel =
        listOf(
            MenuOptionModel(R.string.calculator, OptionMenuRouts.Calculator),
            MenuOptionModel(R.string.rename_lpn, OptionMenuRouts.RenameLpn),
            MenuOptionModel(R.string.change_ownership, OptionMenuRouts.ChangeOwnerShip),
            MenuOptionModel(R.string.marked_damage, OptionMenuRouts.MarkDamage),
            MenuOptionModel(R.string.show_tasks, OptionMenuRouts.CancelWork),
            MenuOptionModel(R.string.settings, OptionMenuRouts.Settings),
        )


    init {
        startResponseListening()
    }


    private fun setUpInit() {
        fetchMenu()
        getEquipmentList()
        getStartWorkInfo()
         viewModelScope.launch {
            selectedAreaType.collectLatest {
                fetchWorkArea(it)
            }
        }
         viewModelScope.launch {
            preferenceManager.getString(AppConstants.Preferences.DEFAULT_EQUIPMENT)?.let {
                selectedEquipment = it
            }
        }
    }


    fun findWork() {
        if (selectedArea.isEmpty()) {
             viewModelScope.launch {
                _uiEventsFlow.emit(showSnackBar(R.string.please_select_an_area))
            }
            return
        } else if (selectedEquipment.isEmpty() || !equipmentList.contains(selectedEquipment)) {
             viewModelScope.launch {
                _uiEventsFlow.emit(showSnackBar(R.string.please_select_the_equipment))
            }
            return
        }
        findWorkTransactionId = getTransactionId()
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.FIND_WORK,
            type = AppConstants.Type.WMS_ACTION,
            transaction = findWorkTransactionId,

            params = getMap {
                put(AppConstants.Params.from_area, selectedArea)
                put(AppConstants.Params.equipment, selectedEquipment)
                initialTaskInfo.also {
                    it.compatible_task?.let { item ->
                        put(AppConstants.Params.compatible_task, item)
                    }
                    it.next_hop?.let { item ->
                        put(AppConstants.Params.next_hop, item)
                    }
                    it.group_type?.let { item ->
                        put(AppConstants.Params.group_type, item)
                    }
                }
                put(AppConstants.Params.entityName, AppConstants.EntityNames.TASK)
            }
        )
        sendMessage(json.encodeToString(paramsModel))
    }


    private fun getStartWorkInfo() {
        startAppTransactionId = getTransactionId()
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.GET_START_APP_INFO,
            type = AppConstants.Type.WMS_ACTION,

            transaction = startAppTransactionId,
            params = getMap {
                put(AppConstants.Params.entityName, AppConstants.EntityNames.TASK)
            }
        )
        sendMessage(json.encodeToString(paramsModel))
    }


    private fun fetchWorkArea(type: String? = null) {
        workAreaTransactionId = getTransactionId()
        val hashMap = getMap {
            put(
                AppConstants.Params.entityName, AppConstants.EntityNames.AREA
            )
            put(
                AppConstants.Params.all, true
            )
            type?.let {
                put(
                    AppConstants.Params.type, it
                )
            }
        }
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.GET_WORK_AREAS,
            type = AppConstants.Type.WMS_ACTION,

            transaction = workAreaTransactionId,
            params = hashMap

        )
        sendMessage(json.encodeToString(paramsModel))
    }

    private fun fetchMenu() {
        navigationItems.clear()
        menuTransactionId = getTransactionId()
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.MAIN_MENU,
            type = AppConstants.Type.WMS_ACTION,
            transaction = menuTransactionId,

            params = getMap {
                put(
                    AppConstants.Params.entityName, AppConstants.EntityNames.MOBILE_APP_MENU
                )
            })
        sendMessage(json.encodeToString(paramsModel))

    }

    private fun getEquipmentList() {
        equipmentTransactionId = getTransactionId()
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.GET_EQUIPMENT,
            type = AppConstants.Type.WMS_ACTION,

            transaction = equipmentTransactionId,
            params = getMap {
                put(AppConstants.Params.entityName, AppConstants.EntityNames.LIST_OF_VALUE)
            })
        sendMessage(json.encodeToString(paramsModel))
    }


    fun renameLpn(oldLpn: String, newLpn: String) {
        showRenameLpnDialog = false

        renameTransactionId = getTransactionId()
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.RENAME_LPN,
            type = AppConstants.Type.WMS_ACTION,
            transaction = renameTransactionId,
            params = getMap {
                put(AppConstants.Params.entityName, AppConstants.EntityNames.INVENTORY)
                put(AppConstants.Params.lpn, oldLpn)
                put(AppConstants.Params.update, newLpn)
            })
        sendMessage(json.encodeToString(paramsModel))
    }

    fun changeOwnership(lpn: String, oldOwner: String, newOwner: String) {
        showOwnerShipChangeDialog = false

        ownerChangeTransactionId = getTransactionId()
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.CHANGE_OWNERSHIP,
            type = AppConstants.Type.WMS_ACTION,

            transaction = ownerChangeTransactionId,
            params = getMap {
                put(AppConstants.Params.entityName, AppConstants.EntityNames.INVENTORY)
                put(AppConstants.Params.lpn, lpn)
                put(AppConstants.Params.ownership, oldOwner)
                put(AppConstants.Params.update, newOwner)
            })
        sendMessage(json.encodeToString(paramsModel))
    }

    fun startPacking() {
        startPackingTransactionId = getTransactionId()
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.START_PACKING,
            type = AppConstants.Type.WMS_ACTION,

            transaction = startPackingTransactionId,
            params = getMap {
                put(AppConstants.Params.entityName, AppConstants.EntityNames.INVENTORY)
            })
        sendMessage(json.encodeToString(paramsModel))
    }


    private fun startResponseListening() {
         viewModelScope.launch {
            response.collect { response ->
                launch {
                    try {
                        if (response.type == AppConstants.ReceivingType.MESSAGE) {//TODO   need to change this
                            val message = json.decodeFromString<MessageHandling>(
                                response.params.toString()  // here its not correct for now once done from backend correct that also
                            )
                            showMessageDialogue(true, message.title, message.description)
                        } else if (response.type == AppConstants.ReceivingType.UI) {
                            if (response.result?.code == AppConstants.WarningCodes.LANGUAGE_MISMATCHED) {
                                showLanguageChangeDialogue = DefaultLangDialogue(
                                    true,
                                    currentLang = response.params?.get(AppConstants.Params.jwt_language),
                                    defaultLang = response.params?.get(AppConstants.Params.db_language)
                                )
                            } else if (response.transaction == findWorkTransactionId) {
                                if (response.orig_action == AppConstants.SendingAction.FIND_WORK || response.orig_action == AppConstants.SendingAction.WARNING_CONFIRMATION) {
                                    if (response.result?.code == "NeedConfirmation") {
                                        val message =
                                            response.params?.get(AppConstants.Params.message)
                                        showConfirmationDialogueForWork =
                                            ShowNextHopDialogue(
                                                true,
//                                                action = action.toString(),
                                                message = message.toString(),
                                                transactionId = response.transaction.toString(),
                                                params = response.params
                                            )
                                    } else if (response.result?.code == "WARN007") {
                                        val message =
                                            response.params?.get(AppConstants.Params.message)
                                        showFindWorkWarning(
                                            true,
                                            "Alert",
                                            message.toString()
//                                            "You have already assigned work in $selectedArea Either Cancel them or assign to another area or Accept Work in the same Area"
                                        )
                                    }
                                }
                            }
                        } else if (response.type == AppConstants.ReceivingType.RESPONSE) {
                            response.result?.let {
                                _uiEventsFlow.emit(ManageResponse(it))
                            }

                            if (response.orig_action == AppConstants.SendingAction.START_PACKING) {
                                if (response.transaction == startPackingTransactionId) {
                                    val location =
                                        response.params?.get(AppConstants.Params.PACKING_LOCATION)
                                            ?: ""

                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS005) {
                                        response.params?.let { parms ->

                                            parms[AppConstants.Params.lpn]?.let {

                                                _uiEventsFlow.emit(
                                                    navigateTo(
                                                        NavRouts.PackingScreen(
                                                            it, location
                                                        )
                                                    )
                                                )
                                            }
                                        }
                                            ?: _uiEventsFlow.emit(
                                                navigateTo(
                                                    NavRouts.PrePackingScreen(
                                                        location
                                                    )
                                                )
                                            )
                                    } else {
                                        _uiEventsFlow.emit(
                                            navigateTo(
                                                NavRouts.PrePackingScreen(
                                                    location
                                                )
                                            )
                                        )
                                    }
                                }
                            } else if (response.orig_action == AppConstants.SendingAction.MAIN_MENU) {
                                if (response.transaction == menuTransactionId) {
                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS201) {
                                        val mainPayload =
                                            response.params?.get(AppConstants.Params.result)
                                                ?.let { json.decodeFromString<List<MainMenu>>(it) }

                                        mainPayload?.let { setMenu(it) }
                                    }
                                }


                            } else if (response.orig_action == AppConstants.SendingAction.GET_WORK_AREAS) {
                                if (response.transaction == workAreaTransactionId) {
                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS1593) {
                                        response.params?.let { parms ->
                                            if (parms.containsKey(AppConstants.Params.result)) {
                                                val areaList =
                                                    parms[AppConstants.Params.result]?.let {
                                                        json.decodeFromString<List<String>>(
                                                            it
                                                        )
                                                    }
                                                if (!areaList.isNullOrEmpty()) {
                                                    workAreaList.clear()
                                                    workAreaList.addAll(areaList)
                                                    if (selectedAreaType.value != null) {
                                                        if (!workAreaList.contains(
                                                                selectedArea
                                                            )
                                                        ) {
                                                            areaDropDownExpanded = true
                                                        }
                                                    }
                                                }

                                            }

                                        }

                                    }
                                }

                            } else if (response.orig_action == AppConstants.SendingAction.GET_EQUIPMENT) {
                                if (response.transaction == equipmentTransactionId) {
                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS1592) {

                                        response.params?.let { params ->
                                            params[AppConstants.Params.value]?.let { resultStr ->
                                                try {
                                                    val equipments =
                                                        json.decodeFromString<List<String>>(
                                                            resultStr
                                                        )
                                                    equipmentList.clear()
                                                    equipmentList.addAll(equipments)

                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }

                                        }
                                    }
                                }

                            } else if (response.orig_action == AppConstants.SendingAction.RENAME_LPN) {
                                if (response.transaction == renameTransactionId) {
                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS324) {
                                        showRenameLpnDialog = false
                                    }
                                }
                            } else if (response.orig_action == AppConstants.SendingAction.FIND_WORK || response.orig_action == AppConstants.SendingAction.WARNING_CONFIRMATION) {
                                if (response.transaction == findWorkTransactionId) {
                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS326) {
                                        // Parse the task items from response
                                        response.params?.get(AppConstants.Params.result)
                                            ?.let { resultStr ->
                                                _uiEventsFlow.emit(
                                                    navigateTo(
                                                        NavRouts.DirectedWorkScreen(
                                                            resultStr
                                                        )
                                                    )
                                                )
                                            }
                                    } else if (response.result?.code == AppConstants.SuccessCodes.SUCCESS327) {
                                        response.params?.get(AppConstants.Params.result)
                                            ?.let { resultStr ->
                                                _uiEventsFlow.emit(
                                                    navigateTo(
                                                        NavRouts.WorkSelectionScreen(
                                                            null, workList = resultStr
                                                        )
                                                    )
                                                )
                                            }

                                    } else if (response.result?.code == AppConstants.WarningCodes.WARN004) {
                                        showFindWorkWarning(
                                            true,
                                            "Alert",
                                            "You have already assigned work in $selectedArea Either Cancel them or assign to another area or Accept Work in the same Area"
                                        )
                                    }


                                }
                            } else if (response.orig_action == AppConstants.SendingAction.GET_START_APP_INFO) {
                                if (response.transaction == startAppTransactionId) {
                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS329) {
                                        response.params?.let { params ->

                                            initialTaskInfo.next_hop =
                                                params[AppConstants.Params.next_hop]
                                            initialTaskInfo.compatible_task =
                                                params[AppConstants.Params.compatible_task]
                                            initialTaskInfo.default_area =
                                                params[AppConstants.Params.default_area]?.also {
                                                    selectedArea = it
                                                }
                                            initialTaskInfo.group_type =
                                                params[AppConstants.Params.group_type]
                                            params[AppConstants.Params.is_tasks_exists]
                                                ?.toBooleanStrictOrNull()?.let {
                                                    initialTaskInfo.is_tasks_exists = it
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

    private suspend fun setMenu(mainPayload: List<MainMenu>) {

        mainPayload.forEach { menuOption ->
            if (menuOption.type == AppConstants.Defaults.BUILT_IN) {

                when (menuOption.screen_id) {
                    MenuIds.CHECK_IN_TRAILER.id -> {
                        _uiEventsFlow.emit(
                            AddMenuItem(
                                menuOption.screen_type,
                                menuOption.screen_name.toString(),
                                NavRouts.CheckInTrailerScreen
                            )
                        )
                    }

                    MenuIds.UNLOAD_TRAILER.id -> {
                        _uiEventsFlow.emit(
                            AddMenuItem(
                                menuOption.screen_type,
                                menuOption.screen_name.toString(),
                                NavRouts.UnloadTrailerScreen

                            )
                        )
                    }

                    MenuIds.RECEIVING_AREA.id -> {
                        _uiEventsFlow.emit(
                            AddMenuItem(
                                menuOption.screen_type,
                                menuOption.screen_name.toString(),
                                NavRouts.IBDScreen
                            )
                        )
                    }

                    MenuIds.PALLET_BUILDING.id -> {
                        _uiEventsFlow.emit(
                            AddMenuItem(
                                menuOption.screen_type,
                                menuOption.screen_name.toString(),
                                NavRouts.PalletsScreen
                            )
                        )
                    }

                    MenuIds.PACKING_SCREEN.id -> {
                        _uiEventsFlow.emit(
                            AddMenuItem(
                                menuOption.screen_type,
                                menuOption.screen_name.toString(),
                                NavRouts.PrePackingScreen("")
                            )
                        )
                    }
                }
            } else {
                _uiEventsFlow.emit(
                    AddMenuItem(
                        menuOption.screen_type,
                        menuOption.screen_name.toString(),
                        NavRouts.CustomScreen(json.encodeToString(menuOption.custom_data))
                    )
                )
            }
        }
    }


    fun showMessageDialogue(
        show: Boolean,
        title: String? = null,
        description: String? = null
    ) {
        messageDialog = messageDialog.copy(
            show = show, title = title, description = description
        )
    }

    fun showFindWorkWarning(
        show: Boolean,
        title: String? = null,
        description: String? = null
    ) {
        findWorkWarning = findWorkWarning.copy(
            show = show, title = title, description = description
        )
    }

    fun handelResponse(
        context: Context,
        result: Result,
        snackBarState: ShackBarState,
    ) {
        context.getRequiredMessage(result)?.let {
            if (result.code?.contains(AppConstants.SuccessCodes.SUCCESS, true) == true) {
                context.showToast(it)
            } else if (result.code?.contains(AppConstants.ErrorCodes.ERROR, true) == true) {
                snackBarState.showSnackBar(it)

            }
        }
    }


    fun addMenuItem(
        windowInfo: WindowInfo, screenType: String?, navRoute: NavRouts, title: String
    ) {
//        navigationItems.add(
//            DrawerNavItem(
//                route = navRoute,
//                titleResId = title,
//            )
//        )
//        return
        if (screenType == "mobile" && !windowInfo.isTablet) {
            navigationItems.add(
                DrawerNavItem(
                    route = navRoute,
                    titleResId = title,
                )
            )
        } else if (windowInfo.isTablet) {
            navigationItems.add(
                DrawerNavItem(
                    route = navRoute,
                    titleResId = title,
                )
            )

        }

    }


    fun initialTaskInfoData(task: WorkSelectionModel) {
        initialTaskInfo.compatible_task = task.compatible_task
        initialTaskInfo.next_hop = task.next_hop
        initialTaskInfo.group_type = task.group_type
    }

    fun acceptWarning() {
        showConfirmationDialogueForWork =
            showConfirmationDialogueForWork.copy(show = false)
        findWorkTransactionId = showConfirmationDialogueForWork.transactionId
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.WARNING_CONFIRMATION,
            type = AppConstants.Type.WMS_ACTION,
            transaction = findWorkTransactionId,
            params = getMap {
                showConfirmationDialogueForWork.params
                    ?.forEach { (key, value) ->
                        put(key, value)
                    }
                /*              put(AppConstants.Params.from_area, selectedArea)
                                put(AppConstants.Params.equipment, selectedEquipment)
                                initialTaskInfo.also {
                                    it.compatible_task?.let { item ->
                                        put(AppConstants.Params.compatible_task, item)
                                    }
                                    it.next_hop?.let { item ->
                                        put(AppConstants.Params.next_hop, item)
                                    }
                                    it.group_type?.let { item ->
                                        put(AppConstants.Params.group_type, item)
                                    }
                }*/
            }
        )
        sendMessage(json.encodeToString(paramsModel))

    }

    data class DrawerNavItem(
        val route: NavRouts,
        val titleResId: String,
    )


    enum class MenuIds(val id: String) {
        CHECK_IN_TRAILER("mobile_chk_in_trlr"),
        UNLOAD_TRAILER("mobile_unld_trlr"),
        RECEIVING_AREA("mobile_recv_ar"),
        PALLET_BUILDING("mobile_palt_bld"),
        PACKING_SCREEN("mobile_pckng_scrn")
    }

    data class ManageResponse(val result: Result) : UiEvents
    data class AddMenuItem(
        val screenType: String?, val title: String, val routs: NavRouts
    ) : UiEvents


}
