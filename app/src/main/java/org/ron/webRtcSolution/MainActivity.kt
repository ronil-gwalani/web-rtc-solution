package org.ron.webRtcSolution

import android.Manifest
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseApp
import org.ron.webrtccall.CallScreen
import org.ron.webrtccall.CallViewModel

class MainActivity : ComponentActivity() {

    private val callViewModel: CallViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.CAMERA] == true && permissions[Manifest.permission.RECORD_AUDIO] == true) {
            // Permissions granted
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        
        setContent {
            val isCalling by callViewModel.isCalling
            val localTrack by callViewModel.localTrack
            val remoteTrack by callViewModel.remoteTrack
            val eglContext by callViewModel.eglContext
            val callDuration by callViewModel.callDuration
            val isAudioOnly by callViewModel.isAudioOnly
            val isMuted by callViewModel.isMuted
            val isSpeakerOn by callViewModel.isSpeakerOn
            val isVideoEnabled by callViewModel.isVideoEnabled
            
            var roomId by remember { mutableStateOf("") }

            // Keep screen on during call
            LaunchedEffect(isCalling) {
                if (isCalling) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            if (isCalling) {
                CallScreen(
                    localTrack = localTrack,
                    remoteTrack = remoteTrack,
                    eglContext = eglContext,
                    callDuration = callDuration,
                    isAudioOnly = isAudioOnly,
                    isMuted = isMuted,
                    isSpeakerOn = isSpeakerOn,
                    isVideoEnabled = isVideoEnabled,
                    onMuteToggle = { callViewModel.toggleMic() },
                    onSpeakerToggle = { callViewModel.toggleSpeaker() },
                    onVideoToggle = { callViewModel.toggleVideo() },
                    onSwitchCamera = { callViewModel.switchCamera() },
                    onEndCall = { callViewModel.endCall() }
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextField(
                        value = roomId,
                        onValueChange = { roomId = it },
                        label = { Text("Enter Room ID") }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row {
                        Button(onClick = {
                            if (roomId.isNotEmpty()) {
                                callViewModel.initCall(roomId, isCaller = true, isAudioOnly = false)
                            }
                        }) {
                            Text("Video Call")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            if (roomId.isNotEmpty()) {
                                callViewModel.initCall(roomId, isCaller = true, isAudioOnly = true)
                            }
                        }) {
                            Text("Voice Call")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        if (roomId.isNotEmpty()) {
                            callViewModel.initCall(roomId, isCaller = false, isAudioOnly = false)
                        }
                    }) {
                        Text("Join Call")
                    }
                }
            }
        }
    }
}
