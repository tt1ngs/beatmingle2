package com.ttings.beatwave.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttings.beatwave.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    fun becomeAuthor() {
        viewModelScope.launch {
            userRepository.becomeAuthor()
        }
    }

    suspend fun getAuthorStatus(): Boolean {
        return userRepository.getAuthorStatus()
    }
}