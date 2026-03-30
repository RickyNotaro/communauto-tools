package com.communauto.tools.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.communauto.tools.CommunautoApp
import com.communauto.tools.data.auth.AuthManager
import com.communauto.tools.data.auth.CookieEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authManager: AuthManager =
        (application as CommunautoApp).authManager

    val isAuthenticated = authManager.isAuthenticated
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _cookieStatus = MutableStateFlow<List<CookieEntry>>(emptyList())
    val cookieStatus = _cookieStatus.asStateFlow()

    private val _showWebView = MutableStateFlow(false)
    val showWebView = _showWebView.asStateFlow()

    init {
        viewModelScope.launch { authManager.init() }
    }

    fun startLogin() {
        _showWebView.value = true
    }

    fun onWebViewPageFinished(url: String) {
        // After the user navigates through login, check for cookies
        if (authManager.hasCookies()) {
            _cookieStatus.value = authManager.getCookieStatus()
            viewModelScope.launch {
                authManager.onLoginSuccess()
            }
            _showWebView.value = false
        }
    }

    fun dismissWebView() {
        _showWebView.value = false
        // Check if cookies were obtained while browsing
        if (authManager.hasCookies()) {
            _cookieStatus.value = authManager.getCookieStatus()
            viewModelScope.launch { authManager.onLoginSuccess() }
        }
    }

    fun refreshCookieStatus() {
        _cookieStatus.value = authManager.getCookieStatus()
    }

    fun getUid(): Int? = authManager.getUid()

    fun logout() {
        viewModelScope.launch { authManager.logout() }
    }
}
