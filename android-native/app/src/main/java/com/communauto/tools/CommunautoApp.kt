package com.communauto.tools

import android.app.Application
import android.webkit.CookieManager
import com.communauto.tools.data.auth.AuthManager
import com.communauto.tools.data.repository.VehicleRepository

class CommunautoApp : Application() {

    lateinit var authManager: AuthManager
        private set

    lateinit var vehicleRepository: VehicleRepository
        private set

    override fun onCreate() {
        super.onCreate()

        // Enable cookies for WebView (shared with OkHttp via WebViewCookieJar)
        CookieManager.getInstance().setAcceptCookie(true)

        authManager = AuthManager(this)
        vehicleRepository = VehicleRepository()
    }
}
