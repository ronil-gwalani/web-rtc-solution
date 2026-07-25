/**
 * Created by Ronil Gwalani
 * WebRTC Solution - User Registration ViewModel
 */
package org.ron.webRtcSolution

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.ron.webrtccall.repository.UserRepository

class RegistrationViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _registrationState = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val registrationState: StateFlow<RegistrationState> = _registrationState

    fun setStatus(state: RegistrationState) {
        _registrationState.value = state
    }

    fun register(userId: String, userName: String) {
        viewModelScope.launch {
            _registrationState.value = RegistrationState.Loading
            userRepository.registerUser(userId, userName)
                .onSuccess {
                    _registrationState.value = RegistrationState.Success
                }
                .onFailure { e ->
                    _registrationState.value =
                        RegistrationState.Error(e.message ?: "Registration failed")
                }
        }
    }

    sealed class RegistrationState {
        object Idle : RegistrationState()
        object Loading : RegistrationState()
        object Success : RegistrationState()
        data class Error(val message: String) : RegistrationState()
    }
}
