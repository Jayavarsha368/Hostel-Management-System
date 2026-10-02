package com.example.freelancerconnect.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freelancerconnect.api.ApiClient
import com.example.freelancerconnect.api.ApiResult
import com.example.freelancerconnect.api.ApiUser
import com.example.freelancerconnect.api.safeApiCall
import kotlinx.coroutines.launch

class FreelancerViewModel : ViewModel() {

    val freelancers = MutableLiveData<List<ApiUser>>(emptyList())
    val isLoading = MutableLiveData(false)
    val error = MutableLiveData<String?>(null)

    fun fetchFreelancers(token: String) {
        isLoading.value = true
        error.value = null
        viewModelScope.launch {
            when (val result = safeApiCall { ApiClient.api.getFreelancers(token) }) {
                is ApiResult.Success -> freelancers.postValue(result.data)
                is ApiResult.Error -> error.postValue(result.message)
                else -> Unit
            }
            isLoading.postValue(false)
        }
    }
}
