package com.renxo.user.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renxo.user.models.ParamModel
import com.renxo.user.navigation.UiEvents
import com.renxo.user.screens.Language
import com.renxo.user.utils.AppConstants
import com.renxo.user.utils.getMap
import com.renxo.user.utils.getTransactionId
import com.renxo.user.utils.json
import com.renxo.user.utils.preferenceManager
import com.renxo.user.webSocket.WebSocketInterface
import com.renxo.user.webSocket.WebSocketInterfaceImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString


class SettingsVM : ViewModel(), WebSocketInterface by WebSocketInterfaceImpl() {

    var selectedLanguage by mutableStateOf(Language("en-US", "English"))
        private set
    var selectedEquipment by mutableStateOf("")
        private set
    var calculatorFloating by mutableStateOf(false)
        private set
    private var languageTransactionId = ""

    private val _uiEventsFlow = MutableSharedFlow<UiEvents>()
    val uiEventsFlow: Flow<UiEvents> = _uiEventsFlow
    var userId by mutableStateOf("")
    var deviceId by mutableStateOf("")
    var languageExpended by mutableStateOf(false)
    var calculatorIsExpanded by mutableStateOf(false)
    var defaultEquipmentIsExpanded by mutableStateOf(false)

    // Store initial language to check if it changed
    private var initialLanguage: String = "en-US"

    val languageList = ArrayList<Language>()
    val calculatorTypes = ArrayList<String>()
    val equipmentList = ArrayList<String>()

    fun setLanguageList(list: List<Language>) {
         viewModelScope.launch {  
            preferenceManager.getLanguage().let { language ->
                initialLanguage = language
                list.firstOrNull { it.code == language }?.let {
                    selectedLanguage = it
                }
            }

        }
        languageList.clear()
        languageList.addAll(list)

    }

    // Update methods that only update the UI but don't save to system
    fun updateCalculatorType(type: Boolean) {
        calculatorFloating = type
        calculatorIsExpanded = false
    }

    fun updateDefaultEquipment(equipment: String) {
        selectedEquipment = equipment
        defaultEquipmentIsExpanded = false

    }

    fun updateLanguage(language: Language) {
        selectedLanguage = language
        languageExpended = false
    }

    // Save all changes to the system
    suspend fun saveAllChanges() {
        // Check if language changed
        val languageChanged = initialLanguage != selectedLanguage.code

        // Save calculator type
        preferenceManager.setValue(
            AppConstants.Preferences.CALCULATOR_TYPE_FLOATING,
            calculatorFloating
        )

        // Save default equipment
        preferenceManager.setValue(AppConstants.Preferences.DEFAULT_EQUIPMENT, selectedEquipment)

        // Save language
        if (languageChanged) {
            changeLanguage(selectedLanguage.code)
        } else {
            _uiEventsFlow.emit(Finish(false))
        }

    }

    fun updateEquipmentList(list: List<String>) {
        equipmentList.clear()
        equipmentList.add("None")
        equipmentList.addAll(list)

    }

    init {
        startResponseListening()
        initSetup()
    }


    private fun initSetup() {
         viewModelScope.launch {  
            calculatorFloating = preferenceManager.getBoolean(
                AppConstants.Preferences.CALCULATOR_TYPE_FLOATING,
                true
            )
            selectedEquipment =
                preferenceManager.getString(AppConstants.Preferences.DEFAULT_EQUIPMENT)?.let {
                    if (equipmentList.contains(it)) it else "None"

                } ?: "None"
            with(preferenceManager) {
                getUserId()?.let {
                    userId = it
                }
                getDeviceId()?.let {
                    deviceId = it
                }
            }

        }

    }

    private fun changeLanguage(language: String) {
        languageTransactionId = getTransactionId()
        val paramsModel = ParamModel(
            action = AppConstants.SendingAction.CHANGE_LANGUAGE,
            type = AppConstants.Type.WMS_ACTION,

            transaction = languageTransactionId,
            params = getMap {
                put(AppConstants.Params.entityName, AppConstants.EntityNames.TASK)
                put(AppConstants.Params.language_id, language)
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
                            if (response.orig_action == AppConstants.SendingAction.CHANGE_LANGUAGE) {
                                if (response.transaction == languageTransactionId) {
                                    if (response.result?.code == AppConstants.SuccessCodes.SUCCESS100) {
                                        preferenceManager.setValue(
                                            AppConstants.Preferences.LANGUAGE_KEY,
                                            selectedLanguage.code
                                        )
                                        initialLanguage = selectedLanguage.code
                                        _uiEventsFlow.emit(Finish(true))
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

    data class Finish(val value: Boolean) : UiEvents

}

