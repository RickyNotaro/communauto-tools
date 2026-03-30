package com.communauto.tools.data.auth

import android.content.Context
import android.webkit.CookieManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("auth")
private val KEY_AUTHENTICATED = booleanPreferencesKey("authenticated")

/**
 * Manages authentication state.
 *
 * Authentication works via the WebView cookie trick:
 * 1. User logs in via WebView → cookies stored in Android CookieManager
 * 2. OkHttp uses a CookieJar backed by the same CookieManager
 * 3. All API calls automatically carry the session cookies
 *
 * We only persist a boolean "has logged in before" flag — the actual
 * cookies live in CookieManager.
 */
class AuthManager(private val context: Context) {

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: Flow<Boolean> = _isAuthenticated.asStateFlow()

    /** Restore auth state from DataStore + check CookieManager. */
    suspend fun init() {
        val savedAuth = context.dataStore.data.first()[KEY_AUTHENTICATED] ?: false
        _isAuthenticated.value = savedAuth && hasCookies()
    }

    /** Mark as authenticated after successful WebView login. */
    suspend fun onLoginSuccess() {
        _isAuthenticated.value = true
        context.dataStore.edit { it[KEY_AUTHENTICATED] = true }
    }

    /** Log out: clear cookies and persisted flag. */
    suspend fun logout() {
        _isAuthenticated.value = false
        context.dataStore.edit { it[KEY_AUTHENTICATED] = false }
        CookieManager.getInstance().removeAllCookies(null)
    }

    /** Extract `uid` from the reservauto cookies (needed for booking calls). */
    fun getUid(): Int? {
        val cookies = CookieManager.getInstance()
            .getCookie("https://www.reservauto.net") ?: return null
        val match = Regex("uid=(\\d+)").find(cookies) ?: return null
        return match.groupValues[1].toIntOrNull()
    }

    /** Check if we have the required session cookies. */
    fun hasCookies(): Boolean {
        val cookies = CookieManager.getInstance()
            .getCookie("https://www.reservauto.net") ?: return false
        return cookies.contains("uid=") && cookies.contains("mySession=")
    }

    /** Get raw cookie string for display. */
    fun getCookieString(): String? =
        CookieManager.getInstance().getCookie("https://www.reservauto.net")

    /** Get status of required cookies. */
    fun getCookieStatus(): List<CookieEntry> {
        val raw = getCookieString() ?: ""
        val parsed = raw.split(";").associate { pair ->
            val parts = pair.trim().split("=", limit = 2)
            parts[0].trim() to (parts.getOrNull(1)?.trim() ?: "")
        }
        return listOf("uid", "bid", "mySession").map { name ->
            val value = parsed[name]
            CookieEntry(
                name = name,
                present = !value.isNullOrEmpty(),
                preview = value?.take(20),
            )
        }
    }
}

data class CookieEntry(
    val name: String,
    val present: Boolean,
    val preview: String?,
)
