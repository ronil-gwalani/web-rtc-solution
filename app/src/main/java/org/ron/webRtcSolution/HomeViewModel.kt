/**
 * Created by Ronil Gwalani
 * WebRTC Solution - Home Screen ViewModel
 */
package org.ron.webRtcSolution

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ron.webrtccall.data.PreferenceProvider
import org.ron.webrtccall.manager.CallManager

class HomeViewModel(
    private val preferenceProvider: PreferenceProvider,
    private val callManager: CallManager
) : ViewModel() {
    var targetId by mutableStateOf("")

    val userId: StateFlow<String?> = preferenceProvider.userId.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    val userName: StateFlow<String?> = preferenceProvider.userName.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    fun startCall(
        targetUserId: String,
        isAudioOnly: Boolean,
        onCallReady: (String, Boolean) -> Unit
    ) {
        viewModelScope.launch {
            callManager.startCall(targetUserId, isAudioOnly)
                .onSuccess { roomId ->
                    onCallReady(roomId, isAudioOnly)
                }
        }
    }
}
