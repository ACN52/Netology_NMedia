package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.AuthApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.repository.PostRepository

@HiltViewModel
class AuthViewModel @Inject constructor(

    private val appAuth: AppAuth,
    private val authApiService: AuthApiService,
    private val postRepository: PostRepository
) : ViewModel() {

    // Для наблюдения за состоянием авторизации в приложении
    val data: LiveData<AuthState> = appAuth
        .authStateFlow
        .asLiveData(Dispatchers.Default)

    val authenticated: Boolean
        get() = appAuth.authStateFlow.value.id != 0L

    // Для процесса аутентификации (логин)
    private val _authProcessState = MutableLiveData<AuthProcessState>()
    val authProcessState: LiveData<AuthProcessState> = _authProcessState

    fun authenticate(login: String, password: String) {
        viewModelScope.launch {
            _authProcessState.value = AuthProcessState.Loading
            try {

                val response = authApiService.authenticate(login, password)

                if (response.isSuccessful && response.body() != null) {
                    val authData = response.body()!!
                    appAuth.setAuth(authData.id, authData.token)

                    // Триггерим обновление данных постов
                    postRepository.refresh()

                    _authProcessState.value = AuthProcessState.Success(authData)
                } else {
                    _authProcessState.value = AuthProcessState.Error("Authentication failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _authProcessState.value = AuthProcessState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            appAuth.removeAuth()
            // Триггерим обновление данных постов
            postRepository.refresh()
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