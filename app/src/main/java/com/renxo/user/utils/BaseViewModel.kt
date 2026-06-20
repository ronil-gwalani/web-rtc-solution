package com.renxo.user.utils

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renxo.user.networking.ApiException
import com.renxo.user.networking.NetworkCallback
import com.renxo.user.utils.MyApplication.Companion.connectivityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseViewModel : ViewModel() {


    private val _exception: MutableStateFlow<String?> = MutableStateFlow(null)
    val exception: StateFlow<String?> = _exception


    private val _showToast = MutableStateFlow<String?>(null)
    val showToast: Flow<String?> get() = _showToast

    private val _showProgress = MutableStateFlow<Boolean>(false)
    val showProgress: Flow<Boolean> get() = _showProgress

    fun showToast(value: String?) {
        viewModelScope.launch {
            _showToast.emit(value)
            delay(200)
            _showToast.emit(null)
        }
    }

    fun showProgress(value: Boolean) {
        _showProgress.value = value
    }


    fun setException(value: String?) {
        value.let {
            viewModelScope.launch {
                _exception.value = it
                delay(100)
                _exception.value = null
            }
        }
    }


    inner class CallingHelper<T>(
        private val cancelCall: Boolean = true,
        private val connectCallAgain: Boolean = false,
    ) {
        private var job: Job? = null
        private fun cancel() {
            if (cancelCall) {
                job?.cancel()
            }
        }

        private var connectivityManagerJob: Job? = null
        fun launchCall(call: suspend () -> Result<T>, callback: NetworkCallback<T>) {
            callback.onProgressing(true)
            cancel()
            job = viewModelScope.launch {
                if (connectivityManager.isConnected.value) {
                    try {
                        val connect = call()
                        withContext(Dispatchers.Main) {
                            connect.onSuccess {
                                callback.onSuccess(it)
                            }
                            connect.onFailure {
                                val exception = it as ApiException?

                                if (exception?.code == 401) {
//                                        logoutAndClearPreference()
                                    callback.unKnownErrorFound(
                                        exception.errorMessage
                                    )
                                } else {
                                    callback.unKnownErrorFound(
                                        exception?.errorMessage ?: it.message.toString()
                                    )
                                }
                                if (!connectivityManager.isConnected.value) {
                                    callback.noInternetAvailable()
                                    callback.onProgressing(false)
                                    callback.unKnownErrorFound(
                                        "No Internet Available"
                                    )
                                    if (connectCallAgain) {
                                        connectivityManagerJob?.cancel()
                                        connectivityManagerJob = viewModelScope.launch {
                                            connectivityManager.isConnected.collectLatest {
                                                if (it) {
                                                    callback.onRequestAgainRestarted()
                                                    launchCall(call, callback)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            callback.onProgressing(false)
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            callback.unKnownErrorFound(e.message.toString())
                            callback.onProgressing(false)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Log.e("CallingHelper", "noInternetAvailable: noInternetAvailable")
                        callback.noInternetAvailable()
                        callback.onProgressing(false)

                    }
                    if (connectCallAgain) {
                        connectivityManagerJob?.cancel()
                        connectivityManagerJob = viewModelScope.launch {
                            connectivityManager.isConnected.collectLatest {
                                if (it) {
                                    callback.onRequestAgainRestarted()
                                    launchCall(call, callback)
                                }

                            }
                        }
                    }
                }
            }

        }


    }

}


