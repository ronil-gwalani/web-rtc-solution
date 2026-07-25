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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel
import org.ron.webRtcSolution.RegistrationViewModel.RegistrationState
import org.ron.webrtccall.WebRtcCallHandler
import org.ron.webrtccall.manager.CallManager

class MainActivity : ComponentActivity() {

    private val callManager: CallManager by inject()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        callManager.handleIntent(intent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {

            val isRegistered by callManager.isRegistered.collectAsState(initial = null)
            Box(modifier = Modifier.fillMaxSize()) {
                if (isRegistered == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (isRegistered == false) {
                    val viewModel: RegistrationViewModel = koinViewModel()
                    RegistrationScreen(viewModel, registerUser = { userID, userName ->
                        viewModel.viewModelScope.launch {
                            callManager.registerUser(userID, userName).onSuccess {
                                viewModel.setStatus(RegistrationState.Success)
                            }.onFailure { e ->
                                viewModel.setStatus(
                                    RegistrationState.Error(e.message ?: "Registration failed"))
                            }
                        }
                    }, onRegistrationSuccess = {})
                } else {
                    val viewModel: HomeViewModel = koinViewModel()
                    HomeScreen(viewModel)
                    // The library handles all call UI (Incoming and Active)
                    WebRtcCallHandler(onDecline = {
                        viewModel.targetId = ""
                    }, onCallEnded = {
                        viewModel.targetId = ""
                    }, onAnswer = {
                        Log.d("onCreate", ":Call started  ")
                    })

                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        callManager.handleIntent(intent)
    }


}
