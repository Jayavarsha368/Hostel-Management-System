package com.example.freelancerconnect.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freelancerconnect.api.ApiClient
import com.example.freelancerconnect.api.ApiResult
import com.example.freelancerconnect.api.ApiUser
import com.example.freelancerconnect.api.UpdateUserRequest
import com.example.freelancerconnect.api.safeApiCall
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    val isSaving  = MutableLiveData<Boolean>(false)
    val saveError = MutableLiveData<String?>(null)
    val profile   = MutableLiveData<ApiUser?>(null)

    fun fetchProfile(token: String) {
        viewModelScope.launch {
            val result = safeApiCall { ApiClient.api.getMe(token) }
            if (result is ApiResult.Success) {
                profile.postValue(result.data)
            }
        }
    }

    fun saveProfile(
        token: String,
        userId: String,
        request: UpdateUserRequest,
        onResult: (success: Boolean, error: String?) -> Unit
    ) {
        isSaving.value = true
        viewModelScope.launch {
            val result = safeApiCall { ApiClient.api.updateUser(token, userId, request) }
            isSaving.postValue(false)
            when (result) {
                is ApiResult.Success -> {
                    profile.postValue(result.data)
                    onResult(true, null)
                }
                is ApiResult.Error -> {
                    saveError.postValue(result.message)
                    onResult(false, result.message)
                }
                else -> Unit
            }
        }
    }
}
