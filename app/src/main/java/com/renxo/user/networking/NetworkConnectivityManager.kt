package com.renxo.user.networking

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class NetworkConnectivityManager(context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isConnected = MutableStateFlow(checkInitialConnectionState())
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // Use a single validation job with debounce
    private var validationJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Track last known state to avoid redundant updates
    private var lastKnownState = _isConnected.value

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onLost(network: Network) {
            super.onLost(network)
//            Log.d("NetworkConnectivity", "Network Lost")
            updateConnectivityState(false)
        }

        override fun onCapabilitiesChanged(
            network: Network, networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)

            val hasInternet = networkCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            )

            val isValidated = networkCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_VALIDATED
            )

            // Consider connected if we have internet capability
            // Even without validation in some cases
            if (hasInternet) {
                if (isValidated) {
                    // Definitely connected
                    updateConnectivityState(true)
                } else {
                    // Likely connected but not validated
                    // Use a shorter timeout for this case
                    checkWithTimeoutIfNeeded()
                }
            } else {
                updateConnectivityState(false)
            }
        }
    }

    init {
        val networkRequest =
            NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
        try {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        } catch (e: Exception) {
            Log.e("NetworkConnectivity", "Error registering network callback", e)
            _isConnected.value = false
        }
    }

    // Update state with debounce for faster state changes
    private fun updateConnectivityState(connected: Boolean) {
        if (connected != lastKnownState) {
            _isConnected.value = connected
            lastKnownState = connected
            // Cancel any pending checks since we have a definite state
            validationJob?.cancel()
            validationJob = null
        }
    }

    // Only perform timeout checks when necessary
    private fun checkWithTimeoutIfNeeded() {
        // Don't run timeout if we're already in that state
        val connected = true
        val timeoutMs = 500L
        if (connected == lastKnownState) return

        validationJob?.cancel()
        validationJob = coroutineScope.launch {
            delay(timeoutMs)
            // After timeout, use the suspected state
            updateConnectivityState(connected)
        }
    }

    private fun checkInitialConnectionState(): Boolean {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        val hasInternet =
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        val isValidated =
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true

        return hasInternet && isValidated
    }

    fun cleanup() {
        validationJob?.cancel()
        coroutineScope.cancel()
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            Log.e("NetworkConnectivity", "Error unregistering network callback", e)
        }
    }
}