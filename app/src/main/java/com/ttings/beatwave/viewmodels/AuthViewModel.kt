package com.ttings.beatwave.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.ttings.beatwave.data.AuthState
import com.ttings.beatwave.data.User
import com.ttings.beatwave.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    val uiState: MutableStateFlow<AuthState> = MutableStateFlow(AuthState.Empty)

    fun isLoggedIn(): Flow<Boolean> {
        return authRepository.getAuthState()
    }

    fun signIn(email: String, password: String, navController: NavController) {
        viewModelScope.launch {
            try {
                uiState.value = AuthState.Loading("Signing in...")
                val user = authRepository.signInWithEmailAndPassword(email, password)
                if (user != null) {
                    // Сохраняем состояние авторизации
                    authRepository.saveAuthState(true)
                    uiState.value = AuthState.Success(user)
                    // Получаем документ пользователя.
                    authRepository.getUserDocument(user.uid) { userDocument ->
                        // Проверяем, прошел ли пользователь процесс настройки профиля.
                        if (userDocument?.profileSetupComplete == true) {
                            // Переходим на главный экран.
                            navController.navigate("HomeScreen")
                        } else {
                            // Переходим на экран настройки профиля.
                            navController.navigate("ProfileSetupScreen")
                        }
                    }
                }
            } catch (e: Exception) {
                uiState.value = AuthState.Error(e.message ?: "Sign in failed")
                e.printStackTrace()
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            try {
                uiState.value = AuthState.Loading("Signing up...")
                val result = authRepository.signUpWithEmailAndPassword(email, password)

                if (result.user != null) {
                    authRepository.saveAuthState(true)
                }

                uiState.value = AuthState.Success(authRepository.getCurrentUser()!!)

                val newUser = User(
                    userId = result.user!!.uid,
                    username = "",
                    avatar = null,
                    background = null,
                    profileSetupComplete = false
                )

                Timber.tag("AuthViewModel").d("Creating user document for %s", newUser.userId)
                authRepository.createUserDocument(newUser)
                Timber.tag("AuthViewModel").d("User document created for %s", newUser.userId)
            } catch (e: Exception) {
                uiState.value = AuthState.Error(e.message ?: "Sign up failed")
                e.printStackTrace()
            }
        }
    }
}