package com.natradac.android.tokeninterceptor

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.TOKEN
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.defaultPrefs
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.get
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.isAccessTokenExpired
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.isRefreshTokenExpired
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class ExpiredTokenInterceptor(private val context: Context) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val pref = defaultPrefs(context)

        try {
            val token = pref.getString(TOKEN, null)
            if (token == null) {
                throw NotFoundTokenException()
            } else {
                return if (isAccessTokenExpired(context)) {
                    //Refresh Token process
                    if (isRefreshTokenExpired(context)) {
                        throw RefreshTokenExpired()
                    } else {
                        refreshToken(chain)
                        chain.proceed(getModifiedRequest(request, context))
                    }
                } else {
                    chain.proceed(getModifiedRequest(request, context))
                }
            }
        } catch (e: Exception) {
            throw IOException(e.message)
        }
    }

    private fun refreshToken(
        chain: Interceptor.Chain
    ) {

        val request = RefreshTokenManager.getRequest()
        Log.d(javaClass.name, request.method + " --> " + request.url)
        Log.d(javaClass.name, "Header " + request.headers)
        Log.d(javaClass.name, "Body " + request.body)
        val response = chain.proceed(request)
        Log.d(javaClass.name, " <-- " + response.code + " " + request.url)
        if (response.code == 200) {
            if (response.body != null) {
                val refreshTokenResponse: Map<String, Object> =
                    Gson().fromJson(
                        response.body!!.string(),
                        object : TypeToken<Map<String, Object>>() {}.type
                    )
                Log.d(javaClass.name, "body: $refreshTokenResponse")
                when {
                    refreshTokenResponse["data"] != null -> {
                        updateToken(refreshTokenResponse["data"] as Map<String, Object>)
                    }
                    refreshTokenResponse["datas"] != null -> {
                        updateToken(refreshTokenResponse["datas"] as Map<String, Object>)
                    }
                    refreshTokenResponse != null -> {
                        updateToken(refreshTokenResponse)
                    }
                    else -> {
                        throw CannotMapResponseToObject()
                    }
                }

            } else {
                throw RefreshTokenException("Response body: ${response.body}")
            }
        } else {
            throw RefreshTokenException("Response code: ${response.code}, message: ${response.message}")
        }

    }

    private fun updateToken(data: Map<String, Object>) {
        if (data[RefreshTokenManager.accessTokenKey] != null && data[RefreshTokenManager.refreshTokenKey] != null && data[RefreshTokenManager.accessValidKey] != null && data[RefreshTokenManager.refreshValidKey] != null) {
            RefreshTokenManager.updateToken(
                data[RefreshTokenManager.accessTokenKey].toString(),
                data[RefreshTokenManager.refreshTokenKey].toString(),
                (data[RefreshTokenManager.accessValidKey] as Double).toLong(),
                (data[RefreshTokenManager.refreshValidKey] as Double).toLong()
            )
        }
    }

    private fun getModifiedRequest(oldRequest: Request, context: Context): Request {

        val pref = defaultPrefs(context)

        when (ConfigInterceptor.getTokenParamType()) {
            TokenRequestParamType.Query -> {
                val url: HttpUrl = oldRequest.url
                    .newBuilder()
                    .addQueryParameter(
                        ConfigInterceptor.getTokenKey(),
                        pref[TOKEN, ""].toString()
                    )
                    .build()
                val newRequest = oldRequest.newBuilder().url(url).build()
                Log.d(javaClass.name, newRequest.method + " --> " + newRequest.url)
                Log.d(javaClass.name, "Header " + newRequest.headers.toString())
                Log.d(javaClass.name, "Body " + newRequest.body.toString())
                return newRequest
            }
            TokenRequestParamType.Header -> {
                val newRequest = oldRequest.newBuilder()
                    .addHeader(ConfigInterceptor.getTokenKey(), pref[TOKEN, ""].toString())
                    .build()
                Log.d(javaClass.name, newRequest.method + " --> " + newRequest.url)
                Log.d(javaClass.name, "Header " + newRequest.headers.toString())
                Log.d(javaClass.name, "Body " + newRequest.body.toString())
                return newRequest
            }
            else -> {
                Log.d(javaClass.name, oldRequest.method + " --> " + oldRequest.url)
                Log.d(javaClass.name, "Header " + oldRequest.headers.toString())
                Log.d(javaClass.name, "Body " + oldRequest.body.toString())
                return oldRequest
            }
        }
    }
}