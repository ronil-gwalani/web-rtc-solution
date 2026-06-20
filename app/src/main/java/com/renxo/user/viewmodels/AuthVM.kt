package com.renxo.user.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.viewModelScope
import com.renxo.user.R
import com.renxo.user.navigation.UiEvents
import com.renxo.user.networking.ApiRepository
import com.renxo.user.networking.AuthModel
import com.renxo.user.networking.AuthResponse
import com.renxo.user.networking.NetworkCallback
import com.renxo.user.screens.Language
import com.renxo.user.utils.AppConstants
import com.renxo.user.utils.BaseViewModel
import com.renxo.user.utils.json

import com.renxo.user.utils.preferenceManager
import com.renxo.user.webSocket.WebSocketInterface
import com.renxo.user.webSocket.WebSocketInterfaceImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AuthVM @Inject constructor(private val repository: ApiRepository) : BaseViewModel(),
    WebSocketInterface by WebSocketInterfaceImpl() {
    private val authCall by lazy { CallingHelper<AuthResponse>() }
    val passwordFocusRequester = FocusRequester()
    val languageList = mutableStateListOf<Language>()
    var selectedLanguage by mutableStateOf(Language("en-US", "English(US)"))
    var userId by mutableStateOf("")
    var password by mutableStateOf("")
    var showCircularProgress by mutableStateOf(false)
    var languageExpanded by mutableStateOf(false)
    private var initialLanguage: String = ""
    private val _uiEventsFlow = MutableSharedFlow<UiEvents>()
    val uiEventsFlow: Flow<UiEvents> = _uiEventsFlow
        .onSubscription {
            if (_uiEventsFlow.subscriptionCount.value == 1) {
                emitFetchLanguageList()
            }
        }

    private fun emitFetchLanguageList() {
         viewModelScope.launch {  
            _uiEventsFlow.emit(FetchLanguageList)
        }
    }

    // Add this to AuthVM class
    fun updateLanguage(language: Language) {
        languageExpanded = false
        selectedLanguage = language
    }

    suspend fun saveAllChanges(): Boolean {
        // Check if language changed
        val languageChanged = initialLanguage != selectedLanguage.code
        if (languageChanged) {
            preferenceManager.setValue(AppConstants.Preferences.LANGUAGE_KEY, selectedLanguage.code)
            initialLanguage = selectedLanguage.code
        }

        return languageChanged
    }


    private var authUrl = ""
    private var deviceId = ""

    init {
         viewModelScope.launch {  
            with(preferenceManager) {
                getAuthUrl()?.let {
                    authUrl = it
                }
                getDeviceId()?.let {
                    deviceId = it
                }

            }

        }
         viewModelScope.launch {
            webSocketErrorMessage.collectLatest {
                errorMessage.emit(it)
            }
        }
    }

    fun authenticateUser(success: (String?) -> Unit) {
        authCall.launchCall(
            {
                repository.authenticateUser(
                    AuthModel(
                        userId.trim(),
                        password.trim(),
                        deviceId,
                        "W123",
                        selectedLanguage.code
                    ), authUrl
                )
            },
            object : NetworkCallback<AuthResponse> {
                override fun noInternetAvailable() {
                     viewModelScope.launch {  
                        errorMessage.emit("Please Check your Internet Connection")
                    }
                }

                override fun unKnownErrorFound(error: String) {
                    showCircularProgress = false
                     viewModelScope.launch {  
                        errorMessage.emit(error)
                    }

                }

                override fun onProgressing(value: Boolean) {
                    if (value) {
                        showCircularProgress = true
                    }
                }

                override fun onRequestAgainRestarted() {
                }

                override fun onSuccess(result: AuthResponse) {
                    result.let {
                         viewModelScope.launch {  
                            it.token?.let { it1 -> preferenceManager.saveAuthToken(it1) }
                            it.url?.let { it1 -> preferenceManager.saveMainUrl(it1) }
                            preferenceManager.setUser(userId.trim())
                            success(  it.default_language)
                        }
                    }
                }

            }
        )

    }

    //    var userId by mutableStateOf("")
//    var password by mutableStateOf("")
    var errorMessage = MutableSharedFlow<String?>()

    init {
         viewModelScope.launch {  
            preferenceManager.getLanguage().let {
                initialLanguage = it
                setSelectedLanguage()

            }
            preferenceManager.getUserId()?.let {
                userId = it
            }
        }
    }

    private fun setSelectedLanguage() {
        selectedLanguage = languageList.firstOrNull { it.code == initialLanguage } ?: Language(
            "en-US",
            "English(US)"
        )
    }


    fun fetchLanguageList(context: Context) {
        val jsonString =
            context.resources.openRawResource(R.raw.languages).bufferedReader()
                .use { it.readText() }
        val list: List<Language> = json.decodeFromString<List<Language>>(jsonString)
        languageList.clear()
        languageList.addAll(list)
        setSelectedLanguage()
    }

    object FetchLanguageList : UiEvents


}

