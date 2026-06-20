package com.renxo.user.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renxo.user.utils.preferenceManager
import kotlinx.coroutines.launch


class ConnectToServerVM : ViewModel() {
    var authServerUrl by mutableStateOf("")
    var deviceId by mutableStateOf("")

    fun saveIP(done: () -> Unit) {
         viewModelScope.launch {  
            preferenceManager.saveAuthUrl(authServerUrl)
            preferenceManager.saveDeviceId(deviceId)
            done()
        }

    }

}