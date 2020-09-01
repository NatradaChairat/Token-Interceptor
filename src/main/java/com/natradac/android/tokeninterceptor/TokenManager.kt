package com.natradac.android.tokeninterceptor

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.natradac.android.tokeninterceptor.db.PreferenceHelper
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.get
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.isTokenUnAvailable
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.set
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import java.time.Instant

class TokenManager {

    companion object {
        fun isUnAvailable(context: Context, prefName: String): Boolean {
            return isTokenUnAvailable(context, prefName)
        }

        fun getToken(
            context: Context,
            prefName: String,
            callBackOnSuccess: (String) -> Unit,
            callBackOnFailed: (Exception) -> Unit
        ) {
            val token = PreferenceHelper.defaultPrefs(context, prefName)
                .getString(PreferenceHelper.TOKEN, null)
            return if (token == null) {
                callBackOnFailed.invoke(NotFoundTokenException())
            } else {
                if (PreferenceHelper.isAccessTokenExpired(context, prefName)) {
                    if (PreferenceHelper.isRefreshTokenExpired(context, prefName)) {
                        callBackOnFailed.invoke(RefreshTokenExpired())
                    } else {

                        val httpClient = OkHttpClient()
                        val request = RefreshTokenManager.getRequest(context, prefName)
                        Log.d(TokenManager::class.java.name, request.method + " --> " + request.url)
                        Log.d(TokenManager::class.java.name, "Header " + request.headers)
                        httpClient.newCall(request).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                callBackOnFailed.invoke(
                                    RefreshTokenException(
                                        e.message ?: "Refresh token error"
                                    )
                                )
                            }

                            override fun onResponse(call: Call, response: Response) {
                                Log.d(
                                    TokenManager::class.java.name,
                                    " <-- " + response.code + " " + request.url
                                )
                                if (response.code == 200) {
                                    if (response.body != null) {
                                        val refreshTokenResponse: Map<String, Object> =
                                            Gson().fromJson(
                                                response.body!!.string(),
                                                object : TypeToken<Map<String, Object>>() {}.type
                                            )
                                        Log.d(
                                            TokenManager::class.java.name,
                                            "body: $refreshTokenResponse"
                                        )

                                        when {
                                            refreshTokenResponse["data"] != null -> {
                                                updateToken(
                                                    context,
                                                    prefName,
                                                    refreshTokenResponse["data"] as Map<String, Object>,
                                                    callBackOnSuccess
                                                )
                                            }
                                            refreshTokenResponse["datas"] != null -> {
                                                updateToken(
                                                    context,
                                                    prefName,
                                                    refreshTokenResponse["datas"] as Map<String, Object>,
                                                    callBackOnSuccess
                                                )
                                            }
                                            refreshTokenResponse != null -> {
                                                updateToken(
                                                    context,
                                                    prefName,
                                                    refreshTokenResponse,
                                                    callBackOnSuccess
                                                )
                                            }
                                            else -> {
                                                throw CannotMapResponseToObject()
                                            }
                                        }
                                    } else {
                                        callBackOnFailed.invoke(RefreshTokenException("Response body: ${response.body}"))
                                    }
                                } else {
                                    callBackOnFailed.invoke(RefreshTokenException("Response code: ${response.code}, message: ${response.message}"))
                                }
                            }

                        })

                    }
                } else {
                    callBackOnSuccess.invoke(
                        PreferenceHelper.defaultPrefs(context, prefName)[PreferenceHelper.TOKEN]
                            ?: ""
                    )
                }
            }
        }

        private fun updateToken(
            context: Context,
            prefName: String,
            data: Map<String, Object>,
            callBackOnSuccess: (String) -> Unit
        ) {
            val pref = PreferenceHelper.defaultPrefs(
                context,
                prefName
            )
            if (data[pref[PreferenceHelper.REFRESH_TOKEN_KEY, "refresh_token"]] != null &&
                data[pref[PreferenceHelper.TOKEN_KEY, "access_token"]] != null &&
                data[pref[PreferenceHelper.ACCESS_VALID_KEY, "access_valid"]] != null &&
                data[pref[PreferenceHelper.REFRESH_VALID_KEY, "refresh_valid"]] != null
            ) {

                pref[PreferenceHelper.TOKEN] =
                    data[pref[PreferenceHelper.TOKEN_KEY, "access_token"]].toString()
                pref[PreferenceHelper.REFRESH_TOKEN] =
                    data[pref[PreferenceHelper.REFRESH_TOKEN_KEY, "refresh_token"]].toString()
                pref[PreferenceHelper.ACCESS_VALID] =
                    (data[pref[PreferenceHelper.ACCESS_VALID_KEY, "access_valid"]] as Double).toLong()
                pref[PreferenceHelper.REFRESH_VALID] =
                    (data[pref[PreferenceHelper.REFRESH_VALID_KEY, "refresh_valid"]] as Double).toLong()
                pref[PreferenceHelper.TIME_STAMP] =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Instant.now().epochSecond
                    } else {
                        System.currentTimeMillis() / 1000L
                    }
                callBackOnSuccess.invoke(data[pref[PreferenceHelper.TOKEN_KEY, "access_token"]].toString())
            }
        }

        fun clear(context: Context, prefName: String) {
            PreferenceHelper.clear(context, prefName)
        }

    }

}
