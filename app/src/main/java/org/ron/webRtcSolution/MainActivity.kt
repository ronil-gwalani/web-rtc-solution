package org.ron.webRtcSolution

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ron.webrtccall.CallScreen

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.CAMERA] == true && permissions[Manifest.permission.RECORD_AUDIO] == true) {
            // Permissions granted
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        try {
//            FirebaseApp.initializeApp(this)
//        } catch (e: Exception) {}
        permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        
        setContent {
            var roomId by remember { mutableStateOf("") }
            var isCalling by remember { mutableStateOf(false) }
            var isCaller by remember { mutableStateOf(false) }
            var isAudioOnly by remember { mutableStateOf(false) }

            if (isCalling) {
                CallScreen(
                    roomId = roomId,
                    isCaller = isCaller,
                    isAudioOnly = isAudioOnly,
                    onCallEnded = { isCalling = false }
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
                                isCaller = true
                                isAudioOnly = false
                                isCalling = true
                            }
                        }) {
                            Text("Video Call")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            if (roomId.isNotEmpty()) {
                                isCaller = true
                                isAudioOnly = true
                                isCalling = true
                            }
                        }) {
                            Text("Voice Call")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        if (roomId.isNotEmpty()) {
                            isCaller = false
                            isAudioOnly = false
                            isCalling = true
                        }
                    }) {
                        Text("Join Call")
                    }
                }
            }
        }
    }
}
