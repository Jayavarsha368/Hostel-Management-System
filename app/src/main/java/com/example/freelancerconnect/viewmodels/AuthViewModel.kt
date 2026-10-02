package com.example.freelancerconnect.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freelancerconnect.api.ApiClient
import com.example.freelancerconnect.api.ApiResult
import com.example.freelancerconnect.api.AuthResponse
import com.example.freelancerconnect.api.GoogleAuthRequest
import com.example.freelancerconnect.api.LoginRequest
import com.example.freelancerconnect.api.RegisterRequest
import com.example.freelancerconnect.api.ResetPasswordRequest
import com.example.freelancerconnect.api.safeApiCall
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    val isLoading = MutableLiveData<Boolean>(false)

    fun login(
        email: String,
        password: String,
        onResult: (success: Boolean, error: String?, response: AuthResponse?) -> Unit
    ) {
        isLoading.value = true
        viewModelScope.launch {
            val result = safeApiCall {
                ApiClient.api.login(LoginRequest(email, password))
            }
            isLoading.postValue(false)
            when (result) {
                is ApiResult.Success -> onResult(true, null, result.data)
                is ApiResult.Error   -> onResult(false, result.message, null)
                else                 -> Unit
            }
        }
    }

    fun register(
        email: String,
        password: String,
        name: String = "",
        onResult: (success: Boolean, error: String?, response: AuthResponse?) -> Unit
    ) {
        isLoading.value = true
        viewModelScope.launch {
            val result = safeApiCall {
                ApiClient.api.register(RegisterRequest(email, password, name))
            }
            isLoading.postValue(false)
            when (result) {
                is ApiResult.Success -> onResult(true, null, result.data)
                is ApiResult.Error   -> onResult(false, result.message, null)
                else                 -> Unit
            }
        }
    }

    fun googleSignIn(
        idToken: String,
        role: String,
        onResult: (success: Boolean, error: String?, response: AuthResponse?) -> Unit
    ) {
        isLoading.value = true
        viewModelScope.launch {
            val result = safeApiCall {
                ApiClient.api.googleSignIn(GoogleAuthRequest(idToken, role))
            }
            isLoading.postValue(false)
            when (result) {
                is ApiResult.Success -> onResult(true, null, result.data)
                is ApiResult.Error -> onResult(false, result.message, null)
                else -> Unit
            }
        }
    }

    fun resetPassword(
        email: String,
        onResult: (success: Boolean, error: String?) -> Unit
    ) {
        isLoading.value = true
        viewModelScope.launch {
            val result = safeApiCall {
                ApiClient.api.resetPassword(ResetPasswordRequest(email))
            }
            isLoading.postValue(false)
            when (result) {
                is ApiResult.Success -> onResult(true, null)
                is ApiResult.Error   -> onResult(false, result.message)
                else                 -> Unit
            }
        }
    }
}