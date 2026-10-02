package com.example.freelancerconnect.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freelancerconnect.api.ApiClient
import com.example.freelancerconnect.api.ApiNotification
import com.example.freelancerconnect.api.ApiResult
import com.example.freelancerconnect.api.safeApiCall
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {

    val notifications = MutableLiveData<List<ApiNotification>>(emptyList())
    val isLoading = MutableLiveData(false)
    val error = MutableLiveData<String?>(null)

    fun fetchNotifications(token: String) {
        isLoading.value = true
        viewModelScope.launch {
            when (val result = safeApiCall { ApiClient.api.getNotifications(token) }) {
                is ApiResult.Success -> {
                    notifications.postValue(result.data)
                    error.postValue(null)
                }
                is ApiResult.Error -> error.postValue(result.message)
                else -> Unit
            }
            isLoading.postValue(false)
        }
    }

    fun markRead(token: String, notificationId: String) {
        viewModelScope.launch {
            when (val result = safeApiCall { ApiClient.api.markNotificationRead(token, notificationId) }) {
                is ApiResult.Success -> notifications.postValue(
                    notifications.value.orEmpty().map {
                        if (it.id == notificationId) it.copy(read = true) else it
                    }
                )
                is ApiResult.Error -> error.postValue(result.message)
                else -> Unit
            }
        }
    }

    fun markAllRead(token: String) {
        viewModelScope.launch {
            when (val result = safeApiCall { ApiClient.api.markAllNotificationsRead(token) }) {
                is ApiResult.Success -> notifications.postValue(
                    notifications.value.orEmpty().map { it.copy(read = true) }
                )
                is ApiResult.Error -> error.postValue(result.message)
                else -> Unit
            }
        }
    }
}