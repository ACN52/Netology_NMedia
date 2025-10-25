package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.auth.AuthState

class AuthViewModel : ViewModel() {

    // Для наблюдения за состоянием авторизации в приложении
    val data: LiveData<AuthState> = AppAuth.getInstance()
        .authStateFlow
        .asLiveData(Dispatchers.Default)

    val authenticated: Boolean
        get() = AppAuth.getInstance().authStateFlow.value.id != 0L

    // Для процесса аутентификации (логин)
    private val _authProcessState = MutableLiveData<AuthProcessState>()
    val authProcessState: LiveData<AuthProcessState> = _authProcessState

    fun authenticate(login: String, password: String) {
        viewModelScope.launch {
            _authProcessState.value = AuthProcessState.Loading
            try {

                val response = PostsApi.auth.authenticate(login, password)

                if (response.isSuccessful && response.body() != null) {
                    val authData = response.body()!!
                    AppAuth.getInstance().setAuth(authData.id, authData.token)
                    _authProcessState.value = AuthProcessState.Success(authData)
                } else {
                    _authProcessState.value = AuthProcessState.Error("Authentication failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _authProcessState.value = AuthProcessState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // Состояния для процесса аутентификации (логина)
    sealed interface AuthProcessState {
        object Loading : AuthProcessState
        data class Success(val authData: AuthData) : AuthProcessState
        data class Error(val message: String) : AuthProcessState
    }
}

data class AuthData(
    val id: Long,
    val token: String
)