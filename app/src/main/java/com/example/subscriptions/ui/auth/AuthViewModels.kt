package com.example.subscriptions.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subscriptions.data.repository.AuthRepository
import com.example.subscriptions.data.repository.AuthResult
import com.example.subscriptions.util.Validators
import kotlinx.coroutines.launch

// ===================== Вход =====================

/** Состояние экрана входа */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false
)

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    var state by mutableStateOf(LoginUiState())
        private set

    fun onEmailChange(value: String) {
        state = state.copy(email = value, emailError = null, generalError = null)
    }

    fun onPasswordChange(value: String) {
        state = state.copy(password = value, passwordError = null, generalError = null)
    }

    fun login(onSuccess: () -> Unit) {
        val emailError = Validators.validateEmail(state.email)
        val passwordError = if (state.password.isEmpty()) "Введите пароль" else null
        if (emailError != null || passwordError != null) {
            state = state.copy(emailError = emailError, passwordError = passwordError)
            return
        }

        state = state.copy(isLoading = true)
        viewModelScope.launch {
            when (val result = repository.login(state.email, state.password)) {
                is AuthResult.Success -> {
                    state = state.copy(isLoading = false)
                    onSuccess()
                }
                is AuthResult.Error -> {
                    state = state.copy(isLoading = false, generalError = result.message)
                }
            }
        }
    }
}

// ===================== Регистрация =====================

/** Состояние экрана регистрации */
data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val passwordRepeat: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val passwordRepeatError: String? = null,
    val isLoading: Boolean = false
)

class RegisterViewModel(private val repository: AuthRepository) : ViewModel() {

    var state by mutableStateOf(RegisterUiState())
        private set

    fun onNameChange(value: String) {
        state = state.copy(name = value, nameError = null)
    }

    fun onEmailChange(value: String) {
        state = state.copy(email = value, emailError = null)
    }

    fun onPasswordChange(value: String) {
        state = state.copy(password = value, passwordError = null)
    }

    fun onPasswordRepeatChange(value: String) {
        state = state.copy(passwordRepeat = value, passwordRepeatError = null)
    }

    fun register(onSuccess: () -> Unit) {
        val nameError = Validators.validateName(state.name)
        val emailError = Validators.validateEmail(state.email)
        val passwordError = Validators.validatePassword(state.password)
        val repeatError = Validators.validatePasswordRepeat(state.password, state.passwordRepeat)

        if (nameError != null || emailError != null || passwordError != null || repeatError != null) {
            state = state.copy(
                nameError = nameError,
                emailError = emailError,
                passwordError = passwordError,
                passwordRepeatError = repeatError
            )
            return
        }

        state = state.copy(isLoading = true)
        viewModelScope.launch {
            when (val result = repository.register(state.name, state.email, state.password)) {
                is AuthResult.Success -> {
                    state = state.copy(isLoading = false)
                    onSuccess()
                }
                is AuthResult.Error -> {
                    // Единственная ошибка регистрации — занятый email
                    state = state.copy(isLoading = false, emailError = result.message)
                }
            }
        }
    }
}
