package com.example.freelancerconnect.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freelancerconnect.api.ApiClient
import com.example.freelancerconnect.api.ApiMessage
import com.example.freelancerconnect.api.ApiResult
import com.example.freelancerconnect.api.SendMessageRequest
import com.example.freelancerconnect.api.safeApiCall
import kotlinx.coroutines.launch

class MessageViewModel : ViewModel() {

    val conversations = MutableLiveData<List<ApiMessage>>(emptyList())
    val messages = MutableLiveData<List<ApiMessage>>(emptyList())
    val isLoading = MutableLiveData(false)
    val error = MutableLiveData<String?>(null)

    fun fetchConversations(token: String) {
        isLoading.value = true
        viewModelScope.launch {
            when (val result = safeApiCall { ApiClient.api.getConversations(token) }) {
                is ApiResult.Success -> {
                    conversations.postValue(result.data)
                    error.postValue(null)
                }
                is ApiResult.Error -> error.postValue(result.message)
                else -> Unit
            }
            isLoading.postValue(false)
        }
    }

    fun fetchMessages(token: String, conversationId: String) {
        viewModelScope.launch {
            when (val result = safeApiCall { ApiClient.api.getMessages(token, conversationId) }) {
                is ApiResult.Success -> {
                    messages.postValue(result.data)
                    error.postValue(null)
                }
                is ApiResult.Error -> error.postValue(result.message)
                else -> Unit
            }
        }
    }

    fun sendMessage(
        token: String,
        request: SendMessageRequest,
        onResult: (success: Boolean, error: String?) -> Unit
    ) {
        viewModelScope.launch {
            when (val result = safeApiCall { ApiClient.api.sendMessage(token, request) }) {
                is ApiResult.Success -> onResult(true, null)
                is ApiResult.Error -> onResult(false, result.message)
                else -> Unit
            }
        }
    }
}