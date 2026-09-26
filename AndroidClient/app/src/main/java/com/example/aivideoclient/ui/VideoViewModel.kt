package com.example.aivideoclient.ui

import androidx.lifecycle.*
import com.example.aivideoclient.repository.VideoRepository
import kotlinx.coroutines.launch

class VideoViewModel(private val repo: VideoRepository) : ViewModel() {
    private val _videoUrl = MutableLiveData<String?>(null)
    val videoUrl: LiveData<String?> = _videoUrl

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    fun generate(prompt: String) {
        if (prompt.isBlank()) {
            _error.value = "Prompt required"
            return
        }
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            _videoUrl.value = null
            val result = repo.generateVideo(prompt)
            if (result.isSuccess) _videoUrl.value = result.getOrNull()
            else _error.value = result.exceptionOrNull()?.message ?: "Unknown error"
            _loading.value = false
        }
    }

    class Factory(private val repo: VideoRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VideoViewModel(repo) as T
        }
    }
}
