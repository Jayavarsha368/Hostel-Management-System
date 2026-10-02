package com.example.freelancerconnect.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freelancerconnect.api.ApiApplication
import com.example.freelancerconnect.api.ApiClient
import com.example.freelancerconnect.api.ApiResult
import com.example.freelancerconnect.api.CreateApplicationRequest
import com.example.freelancerconnect.api.UpdateStatusRequest
import com.example.freelancerconnect.api.safeApiCall
import kotlinx.coroutines.launch

class ApplicationViewModel : ViewModel() {

    val applications = MutableLiveData<List<ApiApplication>>(emptyList())
    val isLoading    = MutableLiveData<Boolean>(false)
    val error        = MutableLiveData<String?>(null)

    fun fetchApplications(token: String) {
        isLoading.value = true
        viewModelScope.launch {
            val result = safeApiCall { ApiClient.api.getApplications(token) }
            isLoading.postValue(false)
            when (result) {
                is ApiResult.Success -> applications.postValue(result.data)
                is ApiResult.Error   -> error.postValue(result.message)
                else                 -> Unit
            }
        }
    }

    fun submitApplication(
        token: String,
        request: CreateApplicationRequest,
        onResult: (success: Boolean, error: String?) -> Unit
    ) {
        isLoading.value = true
        viewModelScope.launch {
            val result = safeApiCall { ApiClient.api.createApplication(token, request) }
            isLoading.postValue(false)
            when (result) {
                is ApiResult.Success -> onResult(true, null)
                is ApiResult.Error   -> onResult(false, result.message)
                else                 -> Unit
            }
        }
    }

    fun updateApplicationStatus(
        token: String,
        applicationId: String,
        newStatus: String,
        onResult: (success: Boolean, error: String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = safeApiCall {
                ApiClient.api.updateApplicationStatus(token, applicationId, UpdateStatusRequest(newStatus))
            }
            when (result) {
                is ApiResult.Success -> onResult(true, null)
                is ApiResult.Error   -> onResult(false, result.message)
                else                 -> Unit
            }
        }
    }

    fun withdrawApplication(
        token: String,
        applicationId: String,
        onResult: (success: Boolean, error: String?) -> Unit
    ) {
        viewModelScope.launch {
            when (val result = safeApiCall { ApiClient.api.withdrawApplication(token, applicationId) }) {
                is ApiResult.Success -> onResult(true, null)
                is ApiResult.Error -> onResult(false, result.message)
                else -> Unit
            }
        }
    }
}