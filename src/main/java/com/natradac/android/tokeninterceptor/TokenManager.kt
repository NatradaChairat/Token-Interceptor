package com.natradac.android.tokeninterceptor

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.natradac.android.tokeninterceptor.db.PreferenceHelper
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.get
import com.natradac.android.tokeninterceptor.db.PreferenceHelper.isTokenUnAvailable
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException

class TokenManager {

    companion object {
        fun isUnAvailable(context: Context): Boolean{
            return isTokenUnAvailable(context)
        }

        fun getToken(context: Context, callBackOnSuccess: (String) -> Unit, callBackOnFailed: (Exception) -> Unit) {
            val token = PreferenceHelper.defaultPrefs(context).getString(PreferenceHelper.TOKEN, null)
            return  if (token == null) {
                callBackOnFailed.invoke(NotFoundTokenException())
            }else {
                if (PreferenceHelper.isAccessTokenExpired(context)) {
                    if (PreferenceHelper.isRefreshTokenExpired(context)) {
                        callBackOnFailed.invoke(RefreshTokenExpired())
                    } else {

                        val httpClient = OkHttpClient()
                        val request = RefreshTokenManager.getRequest()
                        Log.d(TokenManager::class.java.name, request.method + " --> " + request.url)
                        Log.d(TokenManager::class.java.name, "Header " + request.headers)
                        httpClient.newCall(request).enqueue(object : Callback{
                            override fun onFailure(call: Call, e: IOException) {
                                callBackOnFailed.invoke(RefreshTokenException(e.message ?: "Refresh token error"))
                            }

                            override fun onResponse(call: Call, response: Response) {
                                Log.d(TokenManager::class.java.name, " <-- " + response.code + " " + request.url)
                                if (response.code == 200) {
                                    if (response.body != null) {
                                        val refreshTokenResponse: Map<String, Object> =
                                            Gson().fromJson(
                                                response.body!!.string(),
                                                object : TypeToken<Map<String, Object>>() {}.type
                                            )
                                        Log.d(TokenManager::class.java.name, "body: $refreshTokenResponse")

                                        when {
                                            refreshTokenResponse["data"] != null -> {
                                                updateToken(refreshTokenResponse["data"] as Map<String, Object>, callBackOnSuccess)
                                            }
                                            refreshTokenResponse["datas"] != null -> {
                                                updateToken(refreshTokenResponse["datas"] as Map<String, Object>, callBackOnSuccess)
                                            }
                                            refreshTokenResponse != null -> {
                                                updateToken(refreshTokenResponse, callBackOnSuccess)
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
                        PreferenceHelper.defaultPrefs(context)[PreferenceHelper.TOKEN] ?: ""
                    )
                }
            }
        }

        private fun updateToken(data: Map<String, Object>, callBackOnSuccess: (String) -> Unit) {
            if (data[RefreshTokenManager.accessTokenKey] != null && data[RefreshTokenManager.refreshTokenKey] != null && data[RefreshTokenManager.accessValidKey] != null && data[RefreshTokenManager.refreshValidKey] != null) {
                RefreshTokenManager.updateToken(
                    data[RefreshTokenManager.accessTokenKey].toString(),
                    data[RefreshTokenManager.refreshTokenKey].toString(),
                    (data[RefreshTokenManager.accessValidKey] as Double).toLong(),
                    (data[RefreshTokenManager.refreshValidKey] as Double).toLong()
                )
                callBackOnSuccess.invoke(data[RefreshTokenManager.accessTokenKey].toString())
            }
        }

        fun clear(context: Context){
            PreferenceHelper.clear(context)
        }

    }

}
