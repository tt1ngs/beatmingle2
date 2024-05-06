package com.ttings.beatwave.viewmodels

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ttings.beatwave.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {
    // LiveData для отслеживания ошибок
    private val error: MutableLiveData<String> = MutableLiveData()

    // Функция для загрузки изображения на Firebase Storage
    suspend fun uploadImage(uri: Uri) {
        val downloadUrl = repository.uploadImage(uri)
        repository.updateUserImage(downloadUrl)
    }
    // Функция для загрузки изображения на Firebase Storage
    suspend fun uploadBackground(uri: Uri) {
        val downloadUrl = repository.uploadBackground(uri)
        repository.updateUserBackground(downloadUrl)
    }

    // Функция для обновления имени пользователя
    suspend fun updateUserName(name: String) {
        if (name.isBlank()) {
            error.value = "Name cannot be blank"
            return
        }
        repository.updateUserName(name)
    }
}