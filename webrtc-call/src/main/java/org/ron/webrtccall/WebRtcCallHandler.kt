/**
 * Created by Ronil Gwalani
 * WebRTC Solution - Call UI Handler (Composable)
 */
package org.ron.webrtccall

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.google.firebase.database.FirebaseDatabase
import org.koin.compose.koinInject
import org.ron.webrtccall.manager.CallManager
import org.ron.webrtccall.models.CallState

@Composable
fun WebRtcCallHandler(
    onAnswer: (() -> Unit)? = null,
    onDecline: (() -> Unit)? = null,
    onCallEnded: (() -> Unit)? = null
) {
    val callManager: CallManager = koinInject()
    val callState by callManager.currentCall.collectAsState()


    AnimatedContent(
        targetState = callState,
        transitionSpec = {
            slideInVertically { it } + fadeIn() togetherWith
            slideOutVertically { it } + fadeOut()
        },
        label = "call_ui_transition"
    ) { state ->
        when (state) {
            is CallState.Incoming -> {
                IncomingCallComposable(
                    callerName = state.callerName,
                    isAudioOnly = state.isAudioOnly,
                    onAnswer = {
                        callManager.answerCall()
                        onAnswer?.invoke()
                    },
                    onDecline = {
                        FirebaseDatabase.getInstance().getReference("rooms")
                            .child(state.roomId).child("rejected").setValue(true)
                        callManager.endCallUI()
                        onDecline?.invoke()
                    }
                )
            }

            is CallState.Active -> {
                CallScreen(
                    roomId = state.roomId,
                    isCaller = state.isCaller,
                    isAudioOnly = state.isAudioOnly,
                    onCallEnded = {
                        callManager.endCallUI()
                        onCallEnded?.invoke()
                    }
                )
            }

            CallState.Idle -> {
                // Do nothing
            }
        }
    }
}
