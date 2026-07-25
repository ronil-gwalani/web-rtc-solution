/**
 * Created by Ronil Gwalani
 * WebRTC Solution - Main Activity
 */
package org.ron.webRtcSolution

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel
import org.ron.webrtccall.WebRtcCallHandler
import org.ron.webrtccall.manager.CallManager
import org.ron.webrtccall.data.PreferenceProvider

class MainActivity : ComponentActivity() {

    private val callManager: CallManager by inject()
    private val preferenceProvider: PreferenceProvider by inject()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIntent(intent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val isRegistered by preferenceProvider.isRegistered.collectAsState(initial = null)

            Box(modifier = Modifier.fillMaxSize()) {
                if (isRegistered == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (isRegistered == false) {
                    RegistrationScreen(onRegistrationSuccess = {})
                } else {
                    val viewModel: HomeViewModel = koinViewModel()

                    HomeScreen(viewModel)
                    // The library handles all call UI (Incoming and Active)
                    WebRtcCallHandler(onDecline = {
                        viewModel.targetId = ""
                    }, onCallEnded = {
                        viewModel.targetId = ""
                    }
                    , onAnswer = {
                            Log.d("onCreate", ":Call started  ")
                        })

                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val action = intent.getStringExtra("action")
        if (intent.getBooleanExtra("isIncomingCall", false)) {
            val rid = intent.getStringExtra("roomId") ?: ""
            if (rid.isNotEmpty()) {
                val isAudioOnly = intent.getBooleanExtra("isAudioOnly", false)
                val callerName = intent.getStringExtra("callerName") ?: "Unknown"

                if (action == "answer") {
                    callManager.notifyIncomingCall(rid, callerName, isAudioOnly)
                    callManager.answerCall()
                } else {
                    callManager.notifyIncomingCall(rid, callerName, isAudioOnly)
                }
            }
        }
    }
}
