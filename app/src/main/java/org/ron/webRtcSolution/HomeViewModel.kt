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

class HomeViewModel(
//    private val callManager: CallManager
) : ViewModel() {
    var targetId by mutableStateOf("")
    private var _isStartingCall by mutableStateOf(false)
    val isStartingCall = _isStartingCall


//    val userId: StateFlow<String?>
//    = callManager.userId.stateIn(
//        viewModelScope,
//        SharingStarted.WhileSubscribed(5000),
//        null
//    )
//
//    val userName: StateFlow<String?> = callManager.userName.stateIn(
//        viewModelScope,
//        SharingStarted.WhileSubscribed(5000),
//        null
//    )

    fun startCall(
        targetUserId: String,
        isAudioOnly: Boolean,
    ) {
        _isStartingCall = true
//        viewModelScope.launch {
//            _isStartingCall = false
//            callManager.startCall(targetUserId, isAudioOnly)
//                .onSuccess { roomId ->
//                        callManager.startCallUI(roomId, isAudioOnly)
//                }
//        }
    }
}
