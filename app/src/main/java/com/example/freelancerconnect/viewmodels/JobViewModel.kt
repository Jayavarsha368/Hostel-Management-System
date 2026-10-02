package com.example.freelancerconnect.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freelancerconnect.api.ApiClient
import com.example.freelancerconnect.api.ApiJob
import com.example.freelancerconnect.api.ApiResult
import com.example.freelancerconnect.api.CreateJobRequest
import com.example.freelancerconnect.api.safeApiCall
import kotlinx.coroutines.launch

class JobViewModel : ViewModel() {

    val jobs      = MutableLiveData<List<ApiJob>>(emptyList())
    val selectedJob = MutableLiveData<ApiJob?>(null)
    val isLoading = MutableLiveData<Boolean>(false)
    val error     = MutableLiveData<String?>(null)

    fun fetchJobs(token: String) {
        isLoading.value = true
        viewModelScope.launch {
            val result = safeApiCall { ApiClient.api.getJobs(token) }
            isLoading.postValue(false)
            when (result) {
                is ApiResult.Success -> jobs.postValue(result.data)
                is ApiResult.Error   -> error.postValue(result.message)
                else                 -> Unit
            }
        }
    }

    fun fetchJob(token: String, jobId: String) {
        isLoading.value = true
        viewModelScope.launch {
            when (val result = safeApiCall { ApiClient.api.getJobById(token, jobId) }) {
                is ApiResult.Success -> selectedJob.postValue(result.data)
                is ApiResult.Error -> error.postValue(result.message)
                else -> Unit
            }
            isLoading.postValue(false)
        }
    }

    fun createJob(
        token: String,
        request: CreateJobRequest,
        onResult: (success: Boolean, error: String?) -> Unit
    ) {
        isLoading.value = true
        viewModelScope.launch {
            val result = safeApiCall { ApiClient.api.createJob(token, request) }
            isLoading.postValue(false)
            when (result) {
                is ApiResult.Success -> onResult(true, null)
                is ApiResult.Error   -> onResult(false, result.message)
                else                 -> Unit
            }
        }
    }

    fun deleteJob(
        token: String,
        jobId: String,
        onResult: (success: Boolean, error: String?) -> Unit
    ) {
        viewModelScope.launch {
            when (val result = safeApiCall { ApiClient.api.deleteJob(token, jobId) }) {
                is ApiResult.Success -> onResult(true, null)
                is ApiResult.Error -> onResult(false, result.message)
                else -> Unit
            }
        }
    }
}