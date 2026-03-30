package com.communauto.tools.data.api

import android.webkit.CookieManager
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * CookieJar that delegates to Android's [CookieManager].
 *
 * This is the key to the "cookie trick": the WebView login flow sets cookies
 * in CookieManager. Because OkHttp shares the same CookieManager, all API
 * calls automatically include the session cookies — no manual extraction needed.
 */
class WebViewCookieJar : CookieJar {

    private val cookieManager: CookieManager = CookieManager.getInstance()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val urlString = url.toString()
        for (cookie in cookies) {
            cookieManager.setCookie(urlString, cookie.toString())
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val urlString = url.toString()
        val cookieString = cookieManager.getCookie(urlString) ?: return emptyList()
        return cookieString.split(";").mapNotNull { raw ->
            Cookie.parse(url, raw.trim())
        }
    }
}

object NetworkModule {

    private const val BASE_URL = "https://www.reservauto.net/"

    private val cookieJar = WebViewCookieJar()

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
            .build()
    }

    val api: CommunautoApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CommunautoApi::class.java)
    }
}
