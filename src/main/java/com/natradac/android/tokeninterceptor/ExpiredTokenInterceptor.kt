package com.natradac.android.tokeninterceptor

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.natradac.android.tokeninterceptor.db.PreferenceHelper
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

class ExpiredTokenInterceptor(private val context: Context, private val prefName: String) :
    Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val pref = defaultPrefs(context, prefName)

        try {
            val token = pref.getString(TOKEN, null)
            if (token == null) {
                throw NotFoundTokenException()
            } else {
                return if (isAccessTokenExpired(context, prefName)) {
                    //Refresh Token process
                    if (isRefreshTokenExpired(context, prefName)) {
                        throw RefreshTokenExpired()
                    } else {
                        refreshToken(chain)
                        chain.proceed(getModifiedRequest(request))
                    }
                } else {
                    chain.proceed(getModifiedRequest(request))
                }
            }
        } catch (e: Exception) {
            throw IOException(e.message)
        }
    }

    private fun refreshToken(
        chain: Interceptor.Chain
    ) {
        val request = RefreshTokenManager.getRequest(context, prefName)
        Log.d(javaClass.name, request.method + " --> " + request.url)
        Log.d(javaClass.name, "Header " + request.headers)
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
        val pref = defaultPrefs(
            context,
            prefName
        )
        if (data[pref[PreferenceHelper.REFRESH_TOKEN_KEY, "refresh_token"]] != null &&
            data[pref[PreferenceHelper.TOKEN_KEY, "access_token"]] != null &&
            data[pref[PreferenceHelper.ACCESS_VALID_KEY, "access_valid"]] != null &&
            data[pref[PreferenceHelper.REFRESH_VALID_KEY, "refresh_valid"]] != null
        ) {
            RefreshTokenManager.updateToken(
                context,
                prefName,
                data[pref[PreferenceHelper.TOKEN_KEY, "access_token"]].toString(),
                data[pref[PreferenceHelper.REFRESH_TOKEN_KEY, "refresh_token"]].toString(),
                (data[pref[PreferenceHelper.ACCESS_VALID_KEY, "access_valid"]] as Double).toLong(),
                (data[pref[PreferenceHelper.REFRESH_VALID_KEY, "refresh_valid"]] as Double).toLong()
            )
        }
    }

    private fun getModifiedRequest(oldRequest: Request): Request {

        val pref = defaultPrefs(context, prefName)

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
                return newRequest
            }
            TokenRequestParamType.Header -> {
                val newRequest = oldRequest.newBuilder()
                    .addHeader(ConfigInterceptor.getTokenKey(), pref[TOKEN, ""].toString())
                    .build()
                Log.d(javaClass.name, newRequest.method + " --> " + newRequest.url)
                Log.d(javaClass.name, "Header " + newRequest.headers.toString())
                return newRequest
            }
            else -> {
                Log.d(javaClass.name, oldRequest.method + " --> " + oldRequest.url)
                Log.d(javaClass.name, "Header " + oldRequest.headers.toString())
                return oldRequest
            }
        }
    }
}