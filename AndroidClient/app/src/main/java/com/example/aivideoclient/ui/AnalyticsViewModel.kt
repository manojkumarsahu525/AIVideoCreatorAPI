package com.example.aivideoclient.ui

import androidx.lifecycle.*
import com.example.aivideoclient.model.AnalyticsResponse
import com.example.aivideoclient.repository.AnalyticsRepository
import kotlinx.coroutines.launch

class AnalyticsViewModel(private val repo: AnalyticsRepository) : ViewModel() {
    private val _analytics = MutableLiveData<AnalyticsResponse?>()
    val analytics: LiveData<AnalyticsResponse?> = _analytics

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    fun load() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            val result = repo.getUserAnalytics()
            if (result.isSuccess) {
                _analytics.value = result.getOrNull()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Unknown error"
            }
            _loading.value = false
        }
    }

    class Factory(private val repo: AnalyticsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AnalyticsViewModel(repo) as T
        }
    }
}
